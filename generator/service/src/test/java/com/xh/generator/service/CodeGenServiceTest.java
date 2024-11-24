package com.xh.generator.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * @author sunxh 2024/9/10
 */
@SpringBootTest
@Rollback
class CodeGenServiceTest {
    @Resource
    CodeGenService codeGenService;

    @Test
    void getTableDetail() throws SQLException {
        codeGenService.getTableDetail(new HashMap<>(){{
            put("tableName", "sys_user");
        }});
    }

    @Test
    void testGetTableList() {
        codeGenService.getTableList(null);
    }
}
