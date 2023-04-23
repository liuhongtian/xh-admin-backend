package com.xh.system.controller;

import com.xh.common.core.web.PageQuery;
import com.xh.system.client.entity.SysMenu;
import com.xh.common.core.web.PageResult;
import com.xh.common.core.web.RestResponse;
import com.xh.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Map;

@Tag(name = "系统菜单")
@RestController
@RequestMapping("/api/system/menu")
public class SysMenuController {

    @Resource
    private SysMenuService sysMenuService;

    @Operation(description = "菜单列表查询")
    @PostMapping("/query")
    public RestResponse<PageResult<SysMenu>> query(@RequestBody PageQuery<Map<String, Object>> pageQuery) {
        PageResult<SysMenu> data = sysMenuService.query(pageQuery);
        return RestResponse.success(data);
    }

    @Operation(description = "切换菜单字段值")
    @PostMapping("/switch-menu-prop")
    public RestResponse<PageResult<SysMenu>> switchMenuProp(@RequestBody Map<String, Object> param) {
        sysMenuService.switchMenuProp(param);
        return RestResponse.success();
    }

    @Operation(description = "菜单保存")
    @PostMapping("/save")
    public RestResponse<SysMenu> save(@RequestBody SysMenu sysMenu) {
        return RestResponse.success(sysMenuService.save(sysMenu));
    }

    @Operation(description = "获取菜单详情")
    @GetMapping("/get/{id}")
    public RestResponse<SysMenu> getById(@PathVariable Serializable id) {
        return RestResponse.success(sysMenuService.getById(id));
    }
}
