package com.xh.file.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件下载DTO
 *
 * @author sunxh 2023/5/13
 */
@Schema(description = "文件下载")
@Data
public class DownloadFileDTO {
    @Schema(title = "文件表ID")
    private Integer id;
    @Schema(title = "对象存储key")
    private String object;
    @Schema(title = "文件MIME类型")
    private String contentType;
    @Schema(title = "Content-Disposition属性", allowableValues = {"attachment", "inline"}, requiredMode = Schema.RequiredMode.REQUIRED,
            description = "告诉浏览器如何处理文件流，attachment为下载，inline浏览器在线预览，默认下载")
    private String disposition = "attachment";
    @Schema(title = "文件名")
    private String fileName;
    @Schema(title = "缩略图显示", description = "是否压缩显示缩略图，仅图片文件有效")
    private Boolean isScale = false;
    @Schema(title = "缩略图长边大小", description = "仅图片文件有效，以最长边缩小到此大小等比例缩放,默认60像素")
    private Double scaleWidth = 60.0;
    @Schema(title = "视频帧图片预览", description = "仅视频文件有效，是可以传入需要预览视频第几帧图片，响应一个视频对应帧的图片流")
    private Integer videoFrameNum;
}
