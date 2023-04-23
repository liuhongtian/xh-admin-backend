package com.xh.file.controller;

import com.xh.common.core.web.RestResponse;
import com.xh.file.service.FileOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件操作控制器")
@RestController
@RequestMapping("/api/file/operation")
@Log4j2
public class FileOperationController {

    @Resource
    private FileOperationService fileOperationService;


    @Operation(description = "菜单列表查询")
    @PutMapping("/upload")
    public RestResponse<?> uploadFile(@RequestParam("file") MultipartFile file) {
        fileOperationService.uploadFile(file);
        return RestResponse.success();
    }

    @Operation(description = "菜单列表查询")
    @PostMapping("/upload2")
    public RestResponse<?> query(@RequestBody String name) {
        log.info(name);
        return RestResponse.success();
    }
//
//    @Operation(description = "切换菜单字段值")
//    @PostMapping("/switch-menu-prop")
//    public RestResponse<PageResult<SysMenu>> switchMenuProp(@RequestBody Map<String, Object> param) {
//        sysMenuService.switchMenuProp(param);
//        return RestResponse.success();
//    }
//
//    @Operation(description = "菜单保存")
//    @PostMapping("/save")
//    public RestResponse<SysMenu> save(@RequestBody SysMenu sysMenu) {
//        return RestResponse.success(sysMenuService.save(sysMenu));
//    }
}
