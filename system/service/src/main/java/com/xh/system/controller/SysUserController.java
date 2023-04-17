package com.xh.system.controller;

import com.xh.common.core.web.RestResponse;
import com.xh.system.client.entity.SysUser;
import com.xh.system.service.SysUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/system/user")
public class SysUserController {

    @Resource
    private SysUserService sysUserService;

    @PostMapping("/login")
    public RestResponse<SysUser> login(@RequestBody Map<String, Object> params) {
        SysUser sysUser = sysUserService.login(params);
        RestResponse<SysUser> response = RestResponse.success(sysUser);
        SysUser data = response.getData();
        return response;
    }
}
