package com.xh.common.core.configuration;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.xh.common.core.dto.OnlineUserDTO;
import com.xh.common.core.dto.SysLoginUserInfoDTO;
import com.xh.common.core.dto.SysMenuDTO;
import com.xh.common.core.dto.SysOrgRoleDTO;
import com.xh.common.core.service.CommonService;
import com.xh.common.core.utils.LoginUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义权限加载接口实现类
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private CommonService commonService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        SaSession tokenSession = StpUtil.getTokenSession();
        OnlineUserDTO onlineUser = tokenSession.getModel(LoginUtil.SYS_USER_KEY, OnlineUserDTO.class);
        List<SysMenuDTO> rolePermissions = commonService.getRolePermissions(onlineUser.getRoleId(), false);
        return rolePermissions.stream().map(SysMenuDTO::getName).toList();
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SaSession session = StpUtil.getSession();
        SysLoginUserInfoDTO loginUserInfoDTO = session.getModel(LoginUtil.SYS_USER_KEY, SysLoginUserInfoDTO.class);
        if (loginUserInfoDTO == null) return null;
        List<SysOrgRoleDTO> roles = loginUserInfoDTO.getRoles();
        return roles.stream().map(i -> i.getSysOrgId() + i.getRoleName()).toList();
    }
}
