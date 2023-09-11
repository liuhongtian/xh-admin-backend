package com.xh.common.core.dto;

import cn.dev33.satoken.stp.SaTokenInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Table
@Data
@Schema(title = "微信用户信息")
@EqualsAndHashCode(callSuper = true)
public class WxUserInfoDTO extends BaseDTO<Integer> {

    @Schema(title = "sessionKey")
    private String sessionKey;
    @Schema(title = "appid")
    private String appid;
    @Schema(title = "openid")
    private String openid;
    @Schema(title = "unionid")
    private String unionid;
    @Schema(title = "微信昵称")
    private String nickName;
    @Schema(title = "性别")
    private String gender;
    @Schema(title = "语言")
    private String language;
    @Schema(title = "城市")
    private String city;
    @Schema(title = "省")
    private String province;
    @Schema(title = "国家")
    private String country;
    @Schema(title = "头像")
    private String avatarUrl;
    @Schema(title = "绑定手机号")
    private String phoneNumber;
    @Schema(title = "token信息")
    private SaTokenInfo tokenInfo;

}
