package com.xh.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.common.core.web.RestResponse;
import com.xh.system.client.dto.DownloadFileDTO;
import com.xh.system.client.entity.SysFile;
import com.xh.system.service.SysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "文件操作")
@RestController
@RequestMapping("/api/file/operation")
@Slf4j
public class SysFileController {

    @Resource
    private SysFileService sysFileService;

    @Operation(description = "文件上传")
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RestResponse<SysFile> uploadFile(@RequestParam("file") MultipartFile file) {
        SysFile sysFile = sysFileService.uploadFile(file);
        return RestResponse.success(sysFile);
    }

    @Operation(description = "文件下载（图片预览）")
    @GetMapping("/download")
    public void downloadFile(DownloadFileDTO downloadFileDTO, @RequestHeader(value = "Range", defaultValue = "") String range, HttpServletRequest request, HttpServletResponse response) {
        sysFileService.downloadFile(downloadFileDTO, range, request, response);
    }

    @SaCheckPermission("system:file")
    @Operation(description = "文件列表查询")
    @PostMapping("/query")
    public RestResponse<PageResult<SysFile>> query(@RequestBody PageQuery<Map<String, Object>> pageQuery) {
        PageResult<SysFile> data = sysFileService.query(pageQuery);
        return RestResponse.success(data);
    }

    @SaCheckPermission("system:file:edit")
    @Operation(description = "文件保存")
    @PostMapping("/save")
    public RestResponse<SysFile> save(@RequestBody SysFile sysMenu) {
        return RestResponse.success(sysFileService.save(sysMenu));
    }

    @SaCheckPermission(value = {"system:file:edit", "system:file:detail"}, mode = SaMode.OR)
    @Operation(description = "获取文件详情")
    @GetMapping("/get/{id}")
    public RestResponse<SysFile> getById(@PathVariable Integer id) {
        return RestResponse.success(sysFileService.getById(id));
    }

    @Operation(description = "删除文件")
    @DeleteMapping("/del")
    public RestResponse<?> del(@RequestParam List<Integer> ids) {
        sysFileService.del(ids);
        return RestResponse.success();
    }
}
