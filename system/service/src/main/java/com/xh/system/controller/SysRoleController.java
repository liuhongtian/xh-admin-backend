package com.xh.system.controller;

import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.common.core.web.RestResponse;
import com.xh.system.client.entity.SysMenu;
import com.xh.system.client.entity.SysRole;
import com.xh.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Tag(name = "系统角色")
@RestController
@RequestMapping("/api/system/role")
public class SysRoleController {

    @Resource
    private SysRoleService sysRoleService;

    @Operation(description = "角色列表查询")
    @PostMapping("/query")
    public RestResponse<PageResult<SysRole>> query(@RequestBody PageQuery<Map<String, Object>> pageQuery) {
        PageResult<SysRole> data = sysRoleService.query(pageQuery);
        return RestResponse.success(data);
    }

    @Operation(description = "角色保存")
    @PostMapping("/save")
    public RestResponse<SysRole> save(@RequestBody SysRole sysRole) {
        return RestResponse.success(sysRoleService.save(sysRole));
    }

    @Operation(description = "获取角色详情")
    @GetMapping("/get/{id}")
    public RestResponse<SysRole> getById(@PathVariable Serializable id) {
        return RestResponse.success(sysRoleService.getById(id));
    }

    @Operation(description = "角色批量删除")
    @DeleteMapping("/del/{ids}")
    public RestResponse<?> del(@PathVariable String ids) {
        sysRoleService.del(ids);
        return RestResponse.success();
    }

    @Operation(description = "查询角色可配置的所有菜单权限")
    @GetMapping("/queryRoleMenu")
    public RestResponse<List<SysMenu>> queryRoleMenu(@RequestParam Map<String, Object> param) {
        List<SysMenu> roleMenus = sysRoleService.queryRoleMenu(param);
        return RestResponse.success(roleMenus);
    }
}
