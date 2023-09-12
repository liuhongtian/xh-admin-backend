package com.xh.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
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
import java.util.List;
import java.util.Map;

@Tag(name = "系统机构")
@RestController
@RequestMapping("/api/system/org")
@SaCheckPermission("system:org")
public class SysOrgController {

    @Resource
    private SysOrgService sysOrgService;

    @Operation(description = "机构树查询")
    @GetMapping("/tree")
    public RestResponse<PageResult<SysOrg>> query(String name) {
        PageResult<SysOrg> data = sysOrgService.queryOrgTree(name);
        return RestResponse.success(data);
    }

    @Operation(description = "机构列表查询")
    @PostMapping("/query")
    public RestResponse<PageResult<SysOrg>> query(@RequestBody PageQuery<Map<String, Object>> pageQuery) {
        PageResult<SysOrg> data = sysOrgService.query(pageQuery);
        return RestResponse.success(data);
    }

    @SaCheckPermission(value = {"system:org:add", "system:org:edit"}, mode = SaMode.OR)
    @Operation(description = "机构保存")
    @PostMapping("/save")
    public RestResponse<SysOrg> save(@RequestBody SysOrg sysOrg) {
        return RestResponse.success(sysOrgService.save(sysOrg));
    }

    @SaCheckPermission(value = {"system:org:edit", "system:org:detail"}, mode = SaMode.OR)
    @Operation(description = "获取机构详情")
    @GetMapping("/get/{id}")
    public RestResponse<SysOrg> getById(@PathVariable Serializable id) {
        return RestResponse.success(sysOrgService.getById(id));
    }

    @SaCheckPermission("system:org:del")
    @Operation(description = "机构批量删除")
    @DeleteMapping("/del")
    public RestResponse<?> del(@RequestParam List<Serializable> ids) {
        sysOrgService.del(ids);
        return RestResponse.success();
    }
}
