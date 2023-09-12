package com.xh.system.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author sunxh 2023/9/12
 */
@Schema(title = "存放图形验证码数据")
@Data
public class ImageCaptchaDTO implements Serializable {

    @Schema(title = "key")
    private String captchaKey;
    @Schema(title = "base64图形数据")
    private String imageBase64;
}
