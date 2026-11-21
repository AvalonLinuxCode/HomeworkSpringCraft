package com.eduplatform.mapper;

import com.eduplatform.entity.Instructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
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
 *
 * <p>连接本地 PostgreSQL 18 实例（默认 {@code 127.0.0.1:5433/edu_platform_test}，
 * 账号 {@code edu_user/edu_password}）。连接参数见 {@code src/test/resources/application-it.yml}，
 * 可用 {@code TEST_DB_URL} / {@code TEST_DB_USER} / {@code TEST_DB_PASSWORD} 覆盖。
 * 每个用例前整段重放 {@code schema.sql}，用例之间互不干扰。
 */
@ActiveProfiles("it")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class InstructorMapperIT {

    @Autowired
    private InstructorMapper instructorMapper;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void applySchema() throws Exception {
        String ddl = StreamUtils.copyToString(
                new ClassPathResource("schema.sql").getInputStream(), StandardCharsets.UTF_8);
        // 整段执行：DDL 中的 $$ 函数体不能被分号切分
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(ddl);
        }
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
        entity.setDescription("十年后端经验");

        assertThat(instructorMapper.insert(entity)).isEqualTo(1);

        Instructor loaded = instructorMapper.selectById(entity.getId());
        assertThat(loaded.getName()).isEqualTo("王老师");
        assertThat(loaded.getAvatar()).isEqualTo("/avatar/wang.png");
        assertThat(loaded.getTitle()).isEqualTo("架构师");
        assertThat(loaded.getDescription()).isEqualTo("十年后端经验");
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getUpdatedAt()).isNotNull();
    }

    // 注意：本类不能加 @Transactional —— 每步必须在各自事务里，CURRENT_TIMESTAMP 才会前进
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
