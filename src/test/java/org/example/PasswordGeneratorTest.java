package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

// @SpringBootTest：加载Spring上下文，用于注入PasswordEncoder
@SpringBootTest
public class PasswordGeneratorTest {

    // 修正：将@Autowiredmvn改为@Autowired
    @Autowired
    private PasswordEncoder passwordEncoder;

    // 测试方法：生成"123456"的加密密码
    @Test
    public void generateEncodedPassword() {
        String rawPassword = "123456"; // 明文密码
        String encodedPassword = passwordEncoder.encode(rawPassword); // 加密

        // 打印加密结果（复制此结果更新到数据库）
        System.out.println("明文'123456'对应的BCrypt加密密码：");
        System.out.println(encodedPassword);
    }
}