package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.entity.SysUser;
import org.example.mapper.SysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("开始查询用户：{}", username);

        // 根据用户名精确查询用户
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));

        if (user == null) {
            logger.error("用户不存在：{}", username);
            throw new UsernameNotFoundException("用户名或密码错误"); // 模糊错误信息，避免信息泄露
        }

        logger.info("用户{}查询成功，角色：{}", username, user.getRole());

        // 关键优化：显式指定用户状态（是否启用、账号是否过期、凭证是否过期、是否锁定）
        // 若你的SysUser表有enabled/locked等字段，可替换为user.getEnabled()等，此处默认启用
        boolean enabled = true; // 账号是否启用
        boolean accountNonExpired = true; // 账号是否未过期
        boolean credentialsNonExpired = true; // 凭证（密码）是否未过期
        boolean accountNonLocked = true; // 账号是否未锁定

        // 封装用户信息（包含角色权限和状态）
        return new User(
                user.getUsername(),
                user.getPassword(), // 数据库中BCrypt加密后的密码
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                AuthorityUtils.createAuthorityList("ROLE_" + user.getRole()) // 角色带ROLE_前缀，匹配Security配置
        );
    }
}