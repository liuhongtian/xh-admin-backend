package com.xh.system.controller;

import com.xh.common.core.dto.SysLoginUserInfoDto;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.common.core.web.RestResponse;
import com.xh.system.client.entity.SysUser;
import com.xh.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Map;

@Tag(name = "系统用户管理")
@RestController
@RequestMapping("/api/system/user")
public class SysUserController {

    @Resource
    private SysUserService sysUserService;

    @Operation(description = "web管理系统登录")
    @PostMapping("/login")
    public RestResponse<SysLoginUserInfoDto> login(@RequestBody Map<String, Object> params) {
        return sysUserService.login(params);
    }

    @Operation(description = "web管理系统注销")
    @PostMapping("/logout")
    public RestResponse<?> login() {
        return sysUserService.logout();
    }

    @Operation(description = "用户列表查询")
    @PostMapping("/query")
    public RestResponse<PageResult<SysUser>> query(@RequestBody PageQuery<Map<String, Object>> pageQuery) {
        PageResult<SysUser> data = sysUserService.query(pageQuery);
        return RestResponse.success(data);
    }

    @Operation(description = "切换用户字段值")
    @PostMapping("/switch_prop")
    public RestResponse<PageResult<SysUser>> switchMenuProp(@RequestBody Map<String, Object> param) {
        sysUserService.switchMenuProp(param);
        return RestResponse.success();
    }

    @Operation(description = "用户保存")
    @PostMapping("/save")
    public RestResponse<SysUser> save(@RequestBody SysUser sysUser) {
        return RestResponse.success(sysUserService.save(sysUser));
    }

    @Operation(description = "获取用户详情")
    @GetMapping("/get/{id}")
    public RestResponse<SysUser> getById(@PathVariable Serializable id) {
        return RestResponse.success(sysUserService.getById(id));
    }

    @Operation(description = "用户批量删除")
    @DeleteMapping("/del/{ids}")
    public RestResponse<?> del(@PathVariable String ids) {
        sysUserService.del(ids);
        return RestResponse.success();
    }
}
