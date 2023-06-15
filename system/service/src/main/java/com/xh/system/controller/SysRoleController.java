package com.xh.system.controller;

import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.common.core.web.RestResponse;
import com.xh.system.client.entity.SysOrg;
import com.xh.system.service.SysOrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Map;

@Tag(name = "系统角色")
@RestController
@RequestMapping("/api/system/role")
public class SysRoleController {

    @Resource
    private SysOrgService sysOrgService;

    @Operation(description = "角色列表查询")
    @PostMapping("/query")
    public RestResponse<PageResult<SysOrg>> query(@RequestBody PageQuery<Map<String, Object>> pageQuery) {
        PageResult<SysOrg> data = sysOrgService.query(pageQuery);
        return RestResponse.success(data);
    }

    @Operation(description = "角色保存")
    @PostMapping("/save")
    public RestResponse<SysOrg> save(@RequestBody SysOrg sysOrg) {
        return RestResponse.success(sysOrgService.save(sysOrg));
    }

    @Operation(description = "获取角色详情")
    @GetMapping("/get/{id}")
    public RestResponse<SysOrg> getById(@PathVariable Serializable id) {
        return RestResponse.success(sysOrgService.getById(id));
    }


    @Operation(description = "角色批量删除")
    @DeleteMapping("/del/{ids}")
    public RestResponse<?> del(@PathVariable String ids) {
        sysOrgService.del(ids);
        return RestResponse.success();
    }
}
