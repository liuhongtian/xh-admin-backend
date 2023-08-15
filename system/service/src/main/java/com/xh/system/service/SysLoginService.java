package com.xh.system.service;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.xh.common.core.dto.SysLoginUserInfoDto;
import com.xh.common.core.dto.SysMenuDTO;
import com.xh.common.core.dto.SysOrgRoleDTO;
import com.xh.common.core.dto.SysUserDTO;
import com.xh.common.core.service.BaseServiceImpl;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.web.MyException;
import com.xh.common.core.web.RestResponse;
import com.xh.system.client.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SysLoginService extends BaseServiceImpl {

    /**
     * 管理端用户登录
     */
    public SysLoginUserInfoDto login(Map<String, Object> params) {
        String username = CommonUtil.getString(params.get("username"));
        String password = CommonUtil.getString(params.get("password"));

        SaSession tokenSession = null;
        try {
            tokenSession = StpUtil.getSession();
        } catch (NotLoginException e) {
            log.info("用户未登录需要重新登录~");
        }
        //尝试登录
        if (tokenSession == null && CommonUtil.isNotEmpty(username)) {
            String sql = "select * from sys_user where code = ? and enabled = 1";
            SysUser sysUser = baseJdbcDao.findBySql(SysUser.class, sql, username);

            if (sysUser == null) throw new MyException("账号不存在");

            boolean matches = BCrypt.checkpw(password, sysUser.getPassword());
            if (!matches) throw new MyException("密码错误！");

            StpUtil.login(sysUser.getId());
            //刷新用户信息和权限缓存
            refreshUserPermission(sysUser.getId());
            tokenSession = StpUtil.getSession();
        }
        SysLoginUserInfoDto loginUserInfo = null;
        if (tokenSession != null) {
            loginUserInfo = new SysLoginUserInfoDto();
            loginUserInfo.setTokenInfo(StpUtil.getTokenInfo());
            loginUserInfo.setUser((SysUserDTO) tokenSession.get("sysUserInfo"));
            loginUserInfo.setMenus((List<SysMenuDTO>) tokenSession.get("menus"));
            loginUserInfo.setRoles((List<SysOrgRoleDTO>) tokenSession.get("roles"));
        }
        return loginUserInfo;
    }

    /**
     * 管理端用户注销
     */
    public RestResponse<?> logout() {
        StpUtil.logout();
        return RestResponse.success();
    }

    /**
     * 刷新用户信息和权限进缓存
     */
    public void refreshUserPermission(Object userId) {
        SaSession session = StpUtil.getSessionByLoginId(userId);
        SysUser sysUser = baseJdbcDao.findById(SysUser.class, (Serializable) userId);
        SysUserDTO sysUserDTO = new SysUserDTO();
        BeanUtils.copyProperties(sysUser, sysUserDTO);
        session.set("sysUserInfo", sysUserDTO);

        //用户拥有的机构角色岗位，包括所在用户组的机构角色岗位
        String sql = """
                    select
                        tem.*,
                        o.code org_code,
                        o.name org_name,
                        r.name role_name
                    from (
                        SELECT
                            sys_org_id, sys_role_id
                        FROM
                            sys_user_job
                        WHERE
                           type = 1 AND enabled = 1 AND user_id = ?
                        UNION
                        SELECT
                            c.sys_org_id, c.sys_role_id
                        FROM
                            sys_user_group_member a
                            LEFT JOIN sys_user_group b ON b.id = a.sys_user_group_id
                            LEFT JOIN sys_user_job c ON b.id = c.user_id AND c.type = 2 AND c.enabled = 1
                        WHERE
                            a.sys_user_id = ?
                    ) tem
                    left join sys_org o on o.id = tem.sys_org_id
                    left join sys_role r on r.id = tem.sys_role_id
                """;
        List<SysOrgRoleDTO> roles = baseJdbcDao.findList(SysOrgRoleDTO.class, sql, userId, userId);
        session.set("roles", roles);

        String roleIds = roles.stream().map(i -> i.getSysRoleId().toString()).collect(Collectors.joining(","));
        String sql2 = """
                    select
                         distinct
                         b.*
                    from sys_role_menu a
                    left join sys_menu b on a.sys_menu_id = b.id
                    where a.sys_role_id in (%s)
                    order by b.`order` asc
                """.formatted(roleIds);
        //查询用户拥有的所有菜单权限
        List<SysMenuDTO> menus = baseJdbcDao.findList(SysMenuDTO.class, sql2);
        session.set("menus", menus);
    }
}
