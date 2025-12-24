package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.SysUser;

public interface SysUserMapper extends BaseMapper<SysUser> {
    // 新增：根据 username 查询用户（用于获取 userId）
    SysUser selectByUsername(@Param("username") String username);
}