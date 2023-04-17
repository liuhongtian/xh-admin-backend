package com.xh.system.service;

import com.xh.common.core.service.BaseServiceImpl;
import com.xh.system.client.entity.SysUser;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SysUserService extends BaseServiceImpl {

    public SysUser login(Map<String, Object> params) {
        SysUser sysUser = new SysUser();
        sysUser.setId(1);
        sysUser.setCode("admin");
        sysUser.setName("超级管理员");
        sysUser.setPassword("skdfjklksjdflksjdflksjfdlgoufosiudf");
        sysUser.setEnabled(true);
        baseJdbcDao.insert(sysUser);

        SysUser sysUser1 = baseJdbcDao.findById(SysUser.class, 11);

        SysUser u = new SysUser();

        u.setId(2);
        SysUser user = baseJdbcDao.findById(u);
        return sysUser;
    }
}
