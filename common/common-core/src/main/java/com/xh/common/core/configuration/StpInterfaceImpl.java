package com.xh.common.core.configuration;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.xh.common.core.dto.SysMenuDTO;
import com.xh.common.core.dto.SysOrgRoleDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义权限加载接口实现类
 */
@Component
public class StpInterfaceImpl implements StpInterface {
    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        SaSession session = StpUtil.getSession();
        List<SysMenuDTO> menus = (List<SysMenuDTO>) session.get("menus");
        return menus.stream().map(SysMenuDTO::getName).toList();
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SaSession session = StpUtil.getSession();
        List<SysOrgRoleDTO> roles = (List<SysOrgRoleDTO>) session.get("roles");
        return roles.stream().map(i -> i.getSysOrgId() + i.getRoleName()).toList();
    }
}
