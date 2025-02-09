package com.xh.system.client.exchange;

import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.common.core.web.RestResponse;
import com.xh.system.client.dto.DownloadFileDTO;
import com.xh.system.client.entity.SysFile;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

/**
 * 文件操作跨服务调用客户端
 * sunxh 2023/9/22
 */
@HttpExchange(url = "/api/file/operation")
public interface SysFileClient {

    @Operation(description = "文件下载（图片预览）")
    @GetExchange("/download")
    ResponseEntity<byte[]> downloadFile(DownloadFileDTO downloadFileDTO, @RequestHeader(value = "Range", defaultValue = "") String range);

    @Operation(description = "文件列表查询")
    @PostExchange("/query")
    RestResponse<PageResult<SysFile>> query(@RequestBody PageQuery<Map<String, Object>> pageQuery);

    @Operation(description = "文件保存")
    @PostExchange("/save")
    RestResponse<SysFile> save(@RequestBody SysFile sysMenu);

    @Operation(description = "获取系统文件详情")
    @GetExchange("/get/{id}")
    RestResponse<SysFile> getById(@PathVariable String id);

    @Operation(description = "批量删除文件")
    @DeleteExchange("/del")
    RestResponse<?> del(@RequestParam List<String> ids);
}
