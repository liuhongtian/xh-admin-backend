package com.xh.generator.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.xh.common.core.web.MyException;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.common.core.web.RestResponse;
import com.xh.generator.GeneratorApplication;
import com.xh.generator.client.entity.GenTable;
import com.xh.generator.client.vo.TableMateDataVO;
import com.xh.generator.service.CodeGenService;
import freemarker.template.TemplateException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Tag(name = "代码生成")
@RestController
@RequestMapping("/api/generator")
@Slf4j
public class CodeGenController {

    @Resource
    private CodeGenService codeGenService;

    @Value("${env}")
    private String env;

    @SaCheckPermission("generator")
    @Operation(description = "代码生成列表查询")
    @PostMapping("/query")
    public RestResponse<PageResult<GenTable>> query(@RequestBody PageQuery<Map<String, Object>> pageQuery) {
        PageResult<GenTable> data = codeGenService.query(pageQuery);
        return RestResponse.success(data);
    }

    @SaCheckPermission("generator:edit")
    @Operation(description = "代码生成保存")
    @PostMapping("/save")
    public RestResponse<GenTable> save(@RequestBody GenTable genTable) {
        return RestResponse.success(codeGenService.save(genTable));
    }

    @SaCheckPermission(value = {"generator:edit", "generator:detail"}, mode = SaMode.OR)
    @Operation(description = "获取代码生成详情")
    @GetMapping("/get/{id}")
    public RestResponse<GenTable> getById(@PathVariable Integer id) {
        return RestResponse.success(codeGenService.getById(id));
    }

    @Operation(description = "删除代码生成")
    @DeleteMapping("/del")
    public RestResponse<?> del(@RequestParam List<Integer> ids) {
        codeGenService.del(ids);
        return RestResponse.success();
    }

    @Operation(description = "生成代码")
    @PostMapping("/generate/{id}")
    public void generate(@PathVariable Integer id) throws IOException, TemplateException {
        if (!"development".equals(env)) {
            throw new MyException("仅开发环境可操作！");
        }
        codeGenService.generate(id);
    }

    @Operation(description = "代码zip文件下载")
    @GetMapping("/getCodeZipFile/{id}")
    public void getCodeZipFile(@PathVariable Integer id, HttpServletResponse response) throws IOException, TemplateException {
        codeGenService.getCodeZipFile(id, response.getOutputStream());
    }

    @Operation(description = "开发环境获取后端java项目的绝对路径")
    @GetMapping("/getBackendPath")
    public RestResponse<String> getBackendPath() {
        var path = "";
        if ("development".equals(env)) {
            ApplicationHome home = new ApplicationHome(GeneratorApplication.class);
            var absolutePath = home.getDir().getAbsolutePath().replaceAll("\\\\", "/");
            path = absolutePath.split("/generator/service")[0];
        }
        return RestResponse.success(path);
    }

    @SaCheckPermission(value = {"generator:edit", "generator:add"}, mode = SaMode.OR)
    @Operation(description = "获取数据库表列表")
    @GetMapping("/getTableList")
    public RestResponse<List<TableMateDataVO>> getTableList() {
        return RestResponse.success(codeGenService.getTableList(null));
    }

    @SaCheckPermission(value = {"generator:edit", "generator:add"}, mode = SaMode.OR)
    @Operation(description = "获取表详情")
    @GetMapping("/getTableDetail")
    public RestResponse<TableMateDataVO> getTableDetail(@RequestParam Map<String, Object> param) throws SQLException {
        return RestResponse.success(codeGenService.getTableDetail(param));
    }

}
