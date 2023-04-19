package com.xh.system.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xh.common.core.dto.SysUserDto;
import com.xh.common.core.service.BaseServiceImpl;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.utils.WebLogs;
import com.xh.common.core.web.MyException;
import com.xh.common.core.web.RestResponse;
import com.xh.system.client.dto.SysLoginUserInfoDto;
import com.xh.system.client.entity.SysMenu;
import com.xh.system.client.entity.SysUser;
import com.xh.common.core.web.SysContextHolder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 系统用户登录service
 * sunxh 2023/2/26
 */
@Service
public class SysUserLoginService extends BaseServiceImpl {

    @Value("${sys.auth.authTokenRedisPrefix}")
    private String authTokenRedisPrefix;
    @Resource
    private ObjectMapper jsonMapper;

    /**
     * 管理端用户登录
     */
    public RestResponse<SysLoginUserInfoDto> login(Map<String, Object> params) {
        String authToken = SysContextHolder.getAuthToken();
        SysLoginUserInfoDto loginUserInfo = null;
        //查看token是否已登录
        if (CommonUtil.isNotEmpty(authToken)) {
            loginUserInfo = getLoginInfoByAuthToken(authToken);
        }
        String username = CommonUtil.getString(params.get("username"));
        String password = CommonUtil.getString(params.get("password"));
        //尝试登录
        if (loginUserInfo == null && CommonUtil.isNotEmpty(username) && CommonUtil.isNotEmpty(password)) {
            String sql = "select * from sys_user where code = ? and enabled = 1";
            SysUser sysUser = baseJdbcDao.findBySql(SysUser.class, sql, username);
            if (sysUser == null) throw new MyException("账号不存在");
            if (!password.equals(sysUser.getPassword())) throw new MyException("密码错误！");

            //生成token，(用户id:uuid)
            authToken = sysUser.getId() + ":" + "sdr28skdf8rsdr287fsdkf0eqd87eisd";

            //查询权限菜单
            String menuSql = "select * from sys_menu where enabled = 1 order by `order` asc";
            List<SysMenu> menus = baseJdbcDao.findList(SysMenu.class, menuSql);

            SysUserDto sysUserDto = new SysUserDto();
            BeanUtils.copyProperties(sysUser, sysUserDto);

            loginUserInfo = new SysLoginUserInfoDto();
            loginUserInfo.setUser(sysUserDto);
            loginUserInfo.setMenus(menus);
            loginUserInfo.setToken(authToken);
        }
        //登录成功，将登录的信息缓存
        if (loginUserInfo != null) setLoginInfo(authToken, loginUserInfo);
        return RestResponse.success(loginUserInfo);
    }

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
        this.writeMessage(response, restResponse);
        return false;
    }

    /**
     * 管理端用户注销
     */
    public RestResponse<?> logout() {
        String authToken = SysContextHolder.getAuthToken();
        deleteLoginInfoByAuthToken(authToken);
        return RestResponse.success();
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
