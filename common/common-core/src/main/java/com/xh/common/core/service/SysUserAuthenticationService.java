package com.xh.common.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xh.common.core.dto.SysLoginUserInfoDto;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.utils.WebLogs;
import com.xh.common.core.web.RestResponse;
import com.xh.common.core.web.SysContextHolder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * 系统用户鉴权service
 * sunxh 2023/2/26
 */
@Slf4j
@Service
public class SysUserAuthenticationService extends BaseServiceImpl {

    @Value("${sys.auth.authTokenRedisPrefix}")
    private String authTokenRedisPrefix;
    @Resource
    private ObjectMapper jsonMapper;

    /**
     * 管理端请求鉴权
     */
    public Boolean authentication(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authToken = SysContextHolder.getAuthToken();
        RestResponse<?> restResponse;
        //验证登录信息
        if (CommonUtil.isNotEmpty(authToken)) {
            SysLoginUserInfoDto loginInfo = getLoginInfoByAuthToken(authToken);
            if (loginInfo == null) {
                restResponse = RestResponse.error("token非法，或已失效，请重新登录");
            } else {
                setLoginInfo(authToken, loginInfo);
                SysContextHolder.SYS_USER.set(loginInfo.getUser());
                return true;
            }
        } else {
            restResponse = RestResponse.error("非法请求，用户未登录！");
        }

        restResponse.setHttpCode(HttpStatus.FORBIDDEN.value());
        log.error(restResponse.getMessage());
        this.writeMessage(response, restResponse);
        return false;
    }

    public void setLoginInfo(String authToken, SysLoginUserInfoDto loginUserInfoDto) {
        redisTemplate.opsForValue().set(authTokenRedisPrefix + authToken, loginUserInfoDto, 60, TimeUnit.MINUTES);
    }

    public SysLoginUserInfoDto getLoginInfoByAuthToken(String authToken) {
        return (SysLoginUserInfoDto) redisTemplate.opsForValue().get(authTokenRedisPrefix + authToken);
    }

    public void deleteLoginInfoByAuthToken(String authToken) {
        redisTemplate.delete(authTokenRedisPrefix + authToken);
    }

    /**
     * 转为json数据
     */
    private void writeMessage(HttpServletResponse response, Object object) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try (PrintWriter writer = response.getWriter()) {
            String jsonStr = jsonMapper.writeValueAsString(object);
            writer.print(jsonStr);
        } catch (IOException e) {
            WebLogs.error("拦截器输出流异常" + e);
        }
    }
}
