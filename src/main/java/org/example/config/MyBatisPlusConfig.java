package org.example.config; // 包名必须和文件夹路径一致

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类（核心：注册分页插件）
 */
@Configuration // 标记为Spring配置类，启动时会自动加载
@MapperScan("org.example.mapper") // 【关键】替换为你实际的Mapper接口包路径
public class MyBatisPlusConfig {

    /**
     * 注册分页插件（适配PostgreSQL）
     * 该Bean会被MyBatis-Plus自动识别，实现分页功能
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件，指定数据库类型为PostgreSQL（适配你的数据库）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }
}