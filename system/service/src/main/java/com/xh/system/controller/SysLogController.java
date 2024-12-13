package com.xh.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xh.common.core.entity.SysLog;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.common.core.web.RestResponse;
import com.xh.system.service.SysLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "系统日志")
@RestController
@RequestMapping("/api/system/log")
public class SysLogController {

    @Resource
    private SysLogService sysLogService;

    @Operation(description = "日志列表查询")
    @PostMapping("/query")
    @SaCheckPermission("system:log")
    public RestResponse<PageResult<SysLog>> query(@RequestBody PageQuery<Map<String, Object>> pageQuery) {
        PageResult<SysLog> data = sysLogService.query(pageQuery);
        return RestResponse.success(data);
    }

    @SaCheckPermission("system:log:detail")
    @Operation(description = "获取日志详情")
    @GetMapping("/get/{id}")
    public RestResponse<SysLog> getById(@PathVariable Integer id) {
        return RestResponse.success(sysLogService.getById(id));
    }

    @SaCheckPermission("system:log:del")
    @Operation(description = "日志批量删除")
    @DeleteMapping("/del")
    public RestResponse<?> del(@RequestParam List<Integer> ids) {
        sysLogService.del(ids);
        return RestResponse.success();
    }
}
