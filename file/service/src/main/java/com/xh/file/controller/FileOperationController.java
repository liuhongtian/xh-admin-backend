package com.xh.file.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.annotation.SaMode;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.common.core.web.RestResponse;
import com.xh.file.client.dto.DownloadFileDTO;
import com.xh.file.client.entity.SysFile;
import com.xh.file.service.FileOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.Map;

@Tag(name = "文件操作控制器")
@RestController
@RequestMapping("/api/file/operation")
@Slf4j
public class FileOperationController {

    @Resource
    private FileOperationService fileOperationService;

    @Operation(description = "文件上传")
    @PostMapping("/upload")
    public RestResponse<SysFile> uploadFile(@Schema(type = "blob") @RequestParam("file") MultipartFile file) {
        SysFile sysFile = fileOperationService.uploadFile(file);
        return RestResponse.success(sysFile);
    }

    @SaIgnore
    @Operation(description = "文件下载（图片预览）")
    @GetMapping("/download")
    public void downloadFile(DownloadFileDTO downloadFileDTO, @RequestHeader(value = "Range", defaultValue = "") String range, HttpServletResponse response) {
        fileOperationService.downloadFile(downloadFileDTO, range, response);
    }

    @SaCheckPermission("system:file")
    @Operation(description = "文件列表查询")
    @PostMapping("/query")
    public RestResponse<PageResult<SysFile>> query(@RequestBody PageQuery<Map<String, Object>> pageQuery) {
        PageResult<SysFile> data = fileOperationService.query(pageQuery);
        return RestResponse.success(data);
    }

    @SaCheckPermission("system:file:edit")
    @Operation(description = "文件保存")
    @PostMapping("/save")
    public RestResponse<SysFile> save(@RequestBody SysFile sysMenu) {
        return RestResponse.success(fileOperationService.save(sysMenu));
    }

    @SaCheckPermission(value = {"system:file:edit", "system:file:detail"}, mode = SaMode.OR)
    @Operation(description = "获取系统文件详情")
    @GetMapping("/get/{id}")
    public RestResponse<SysFile> getById(@PathVariable Serializable id) {
        return RestResponse.success(fileOperationService.getById(id));
    }

    @SaCheckPermission("system:file:del")
    @Operation(description = "批量删除文件")
    @DeleteMapping("/del/{ids}")
    public RestResponse<?> del(@PathVariable String ids) {
        fileOperationService.del(ids);
        return RestResponse.success();
    }
}
