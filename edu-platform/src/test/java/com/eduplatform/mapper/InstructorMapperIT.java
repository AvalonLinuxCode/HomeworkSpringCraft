package com.eduplatform.mapper;

import com.eduplatform.entity.Instructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
 * <p>连接本地 PostgreSQL 18 实例（默认 {@code 127.0.0.1:5433/edu_platform_test}），
 * 每个用例前整段重放 {@code schema.sql}（见 {@link AbstractMapperIT}），用例之间互不干扰。
 */
@SpringBootTest
class InstructorMapperIT extends AbstractMapperIT {

    @Autowired
    private InstructorMapper instructorMapper;

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
