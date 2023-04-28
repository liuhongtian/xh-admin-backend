package com.xh.system.service;

import com.xh.common.core.dto.SysLoginUserInfoDto;
import com.xh.common.core.dto.SysMenuDTO;
import com.xh.common.core.dto.SysUserDTO;
import com.xh.common.core.service.BaseServiceImpl;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.web.MyException;
import com.xh.common.core.web.RestResponse;
import com.xh.common.core.web.SysContextHolder;
import com.xh.system.client.entity.SysUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SysUserService extends BaseServiceImpl {


    @Value("${sys.auth.authTokenRedisPrefix}")
    private String authTokenRedisPrefix;

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
            List<SysMenuDTO> menus = baseJdbcDao.findList(SysMenuDTO.class, menuSql);

            SysUserDTO sysUserDto = new SysUserDTO();
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
}
