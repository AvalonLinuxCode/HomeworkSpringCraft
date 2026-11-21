package com.eduplatform.mapper;

import com.eduplatform.entity.Instructor;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ring B：真实 PostgreSQL 上的 Mapper + DDL 集成测试。
 * 不依赖 Docker：zonky 启动本地 PostgreSQL 16 实例。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class InstructorMapperIT {

    private static final EmbeddedPostgres POSTGRES = startPostgres();

    @Autowired
    private InstructorMapper instructorMapper;

    @Autowired
    private DataSource dataSource;

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
    }

    @BeforeEach
    void applySchema() throws Exception {
        String ddl = StreamUtils.copyToString(
                new ClassPathResource("schema.sql").getInputStream(), StandardCharsets.UTF_8);
        // 整段执行：DDL 中的 $$ 函数体不能被分号切分
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(ddl);
        }
    }

    @AfterAll
    static void stopPostgres() throws IOException {
        POSTGRES.close();
    }

    @Test
    void schemaCreatesAllTablesAndEnumTypes() throws SQLException {
        assertThat(queryStrings("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'"))
                .containsExactlyInAnyOrder("users", "instructor", "category", "course", "orders");
        assertThat(queryStrings("SELECT typname FROM pg_type WHERE typtype = 'e'"))
                .containsExactlyInAnyOrder("user_role", "course_status", "order_status");
    }

    @Test
    void insertThenSelectByIdRoundTripsAllColumns() {
        Instructor entity = new Instructor();
        entity.setName("王老师");
        entity.setAvatar("/avatar/wang.png");
        entity.setTitle("架构师");
        entity.setDescription("多行\n描述");

        assertThat(instructorMapper.insert(entity)).isEqualTo(1);
        assertThat(entity.getId()).isNotNull();

        Instructor loaded = instructorMapper.selectById(entity.getId());
        assertThat(loaded.getName()).isEqualTo("王老师");
        assertThat(loaded.getAvatar()).isEqualTo("/avatar/wang.png");
        assertThat(loaded.getTitle()).isEqualTo("架构师");
        assertThat(loaded.getDescription()).isEqualTo("多行\n描述");
        assertThat(loaded.getCreatedAt()).isNotNull();
    }

    @Test
    void updateBumpsUpdatedAtViaTrigger() throws InterruptedException {
        Instructor entity = new Instructor();
        entity.setName("赵老师");
        instructorMapper.insert(entity);
        OffsetDateTime createdAt = instructorMapper.selectById(entity.getId()).getCreatedAt();

        Thread.sleep(10);
        entity.setName("赵老师（改）");
        instructorMapper.updateById(entity);

        Instructor updated = instructorMapper.selectById(entity.getId());
        assertThat(updated.getName()).isEqualTo("赵老师（改）");
        assertThat(updated.getUpdatedAt()).isAfter(createdAt);
    }

    @Test
    void deleteRemovesRow() {
        Instructor entity = new Instructor();
        entity.setName("钱老师");
        instructorMapper.insert(entity);

        assertThat(instructorMapper.deleteById(entity.getId())).isEqualTo(1);
        assertThat(instructorMapper.selectById(entity.getId())).isNull();
    }

    @Test
    void countCoursesByInstructorIdCountsOnlyThatInstructor() {
        // 依赖 schema.sql 种子数据：张老师(id=1)有1门课程，李老师(id=2)有1门课程
        assertThat(instructorMapper.countCoursesByInstructorId(1L)).isEqualTo(1L);
        assertThat(instructorMapper.countCoursesByInstructorId(2L)).isEqualTo(1L);
        assertThat(instructorMapper.countCoursesByInstructorId(999L)).isZero();
    }

    @Test
    void listFiltersByKeywordAndPaginates() {
        // 依赖 schema.sql 种子数据（张老师、李老师）
        assertThat(instructorMapper.count(null)).isEqualTo(2L);
        assertThat(instructorMapper.count("张")).isEqualTo(1L);

        assertThat(instructorMapper.list(null, 10, 0))
                .extracting(Instructor::getName)
                .containsExactly("李老师", "张老师");
        assertThat(instructorMapper.list("张", 10, 0))
                .extracting(Instructor::getName)
                .containsExactly("张老师");
        assertThat(instructorMapper.list(null, 1, 1))
                .extracting(Instructor::getName)
                .containsExactly("张老师");
    }

    private static EmbeddedPostgres startPostgres() {
        try {
            return EmbeddedPostgres.builder()
                    .setLocaleConfig("locale", "C")
                    .setLocaleConfig("encoding", "UTF8")
                    .start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> queryStrings(String sql) throws SQLException {
        List<String> values = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                values.add(resultSet.getString(1));
            }
        }
        return values;
    }
}
