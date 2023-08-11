package com.xh.system.controller;

import com.xh.common.core.dto.SysLoginUserInfoDto;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.common.core.web.RestResponse;
import com.xh.system.client.dto.SysUserJobDTO;
import com.xh.system.client.entity.SysUser;
import com.xh.system.client.entity.SysUserGroup;
import com.xh.system.client.entity.SysUserJob;
import com.xh.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
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

    @Operation(description = "系统用户组查询")
    @PostMapping("/queryUserGroupList")
    public RestResponse<PageResult<SysUserGroup>> queryUserGroupList(@RequestBody PageQuery<Map<String, Object>> pageQuery) {
        PageResult<SysUserGroup> data = sysUserService.queryUserGroupList(pageQuery);
        return RestResponse.success(data);
    }

    @Operation(description = "用户组保存")
    @PostMapping("/saveUserGroup")
    public RestResponse<SysUserGroup> saveUserGroup(@RequestBody SysUserGroup sysUserGroup) {
        return RestResponse.success(sysUserService.saveUserGroup(sysUserGroup));
    }

    @Operation(description = "id获取用户组详情")
    @GetMapping("/getUserGroup/{id}")
    public RestResponse<SysUserGroup> getUserGroupById(@PathVariable Serializable id) {
        return RestResponse.success(sysUserService.getUserGroupById(id));
    }

    @Operation(description = "ids批量删除用户组")
    @DeleteMapping("/delUserGroup/{ids}")
    public RestResponse<?> delUserGroup(@PathVariable String ids) {
        sysUserService.delUserGroup(ids);
        return RestResponse.success();
    }

    @Operation(description = "获取用户或者用户组的岗位信息")
    @GetMapping("/getUserJobs")
    public RestResponse<List<SysUserJob>> getUserJobs(@RequestParam Map<String, Object> param) {
        return RestResponse.success(sysUserService.getUserJobs(param));
    }

    @Operation(description = "用户岗位保存")
    @PostMapping("/saveUserJobs")
    public RestResponse<?> saveUserJobs(@RequestBody SysUserJobDTO sysUserJobDTO) {
        sysUserService.saveUserJobs(sysUserJobDTO);
        return RestResponse.success();
    }

    @Operation(description = "id获取用户所在的所有用户组信息")
    @GetMapping("/getUserGroups/{id}")
    public RestResponse<List<SysUserGroup>> getUserGroups(@PathVariable Serializable id) {
        return RestResponse.success(sysUserService.getUserGroups(id));
    }
}
