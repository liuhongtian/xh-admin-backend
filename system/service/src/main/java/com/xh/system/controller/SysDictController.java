package com.xh.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.common.core.web.RestResponse;
import com.xh.system.client.entity.SysDictDetail;
import com.xh.system.client.entity.SysDictType;
import com.xh.system.service.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "数据字典")
@RestController
@RequestMapping("/api/system/dict")
public class SysDictController {

    @Resource
    private SysDictService sysDictService;

    @Operation(description = "数据字典类型查询")
    @PostMapping("/type/query")
    @SaCheckPermission("system:dict")
    public RestResponse<PageResult<SysDictType>> queryTypes(@RequestBody PageQuery<Map<String, Object>> pageQuery) {
        PageResult<SysDictType> data = sysDictService.queryTypes(pageQuery);
        return RestResponse.success(data);
    }

    @Operation(description = "数据字典明细查询")
    @PostMapping("/detail/query")
    public RestResponse<PageResult<SysDictDetail>> queryDetails(@RequestBody PageQuery<Map<String, Object>> pageQuery) {
        PageResult<SysDictDetail> data = sysDictService.queryDetails(pageQuery);
        return RestResponse.success(data);
    }

    @SaCheckPermission(value = {"system:dict:edit", "system:dict:detail"}, mode = SaMode.OR)
    @Operation(description = "获取字典明细详情")
    @GetMapping("/detail/get/{id}")
    public RestResponse<SysDictDetail> getById(@PathVariable Integer id) {
        return RestResponse.success(sysDictService.getDictDetailById(id));
    }

    @SaCheckPermission(value = {"system:dict:add", "system:dict:edit"}, mode = SaMode.OR)
    @Operation(description = "数据字典明细保存")
    @PostMapping("/detail/save")
    public RestResponse<SysDictDetail> saveDictDetail(@RequestBody SysDictDetail sysDictDetail) {
        return RestResponse.success(sysDictService.saveDictDetail(sysDictDetail));
    }

    @SaCheckPermission("system:dict:del")
    @Operation(description = "批量数据字典删除")
    @DeleteMapping("/detail/del")
    public RestResponse<?> del(@RequestParam List<Integer> ids) {
        sysDictService.delDetail(ids);
        return RestResponse.success();
    }
}
