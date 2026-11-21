package com.eduplatform.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Ring B 集成测试基类：连真实 PostgreSQL 18（独立库 {@code edu_platform_test}）。
 *
 * <p>连接参数见 {@code src/test/resources/application-it.yml}，可用
 * {@code TEST_DB_URL} / {@code TEST_DB_USER} / {@code TEST_DB_PASSWORD} 覆盖。
 * 每个用例前整段重放 {@code schema.sql}（其中 {@code $$} 函数体不能按分号切分，
 * 必须整段交给服务端解析），确保用例之间互不干扰；DDL 自带 DROP + 种子数据，
 * 断言若涉及全局计数需显式考虑种子行。
 */
@ActiveProfiles("it")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
abstract class AbstractMapperIT {

    @Autowired
    protected DataSource dataSource;

    @BeforeEach
    void applySchema() throws Exception {
        String ddl = StreamUtils.copyToString(
                new ClassPathResource("schema.sql").getInputStream(), StandardCharsets.UTF_8);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(ddl);
        }
    }
}