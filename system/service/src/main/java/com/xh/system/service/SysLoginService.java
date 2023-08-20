package com.xh.system.service;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.Header;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.xh.common.core.dto.*;
import com.xh.common.core.service.BaseServiceImpl;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.utils.WebLogs;
import com.xh.common.core.web.MyException;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.common.core.web.RestResponse;
import com.xh.system.client.entity.SysUser;
import com.xh.system.client.vo.LoginUserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SysLoginService extends BaseServiceImpl {

    /**
     * 管理端用户登录
     */
    @Transactional
    public LoginUserInfoVO login(HttpServletRequest request, Map<String, Object> params) {
        String username = CommonUtil.getString(params.get("username"));
        String password = CommonUtil.getString(params.get("password"));

        SaSession session = null;
        try {
            session = StpUtil.getSession();
        } catch (NotLoginException e) {
            log.info("用户未登录需要重新登录~");
        }
        //尝试登录
        if (session == null && CommonUtil.isNotEmpty(username)) {
            String sql = "select * from sys_user where code = ? and enabled = 1";
            SysUser sysUser = baseJdbcDao.findBySql(SysUser.class, sql, username);

            if (sysUser == null) throw new MyException("账号不存在");

            boolean matches = BCrypt.checkpw(password, sysUser.getPassword());
            if (!matches) throw new MyException("密码错误！");

            StpUtil.login(sysUser.getId());
            //刷新用户信息和权限缓存
            refreshUserPermission(sysUser.getId());
            session = StpUtil.getSession();

            SysLoginUserInfoDTO loginUserInfoDTO = session.getModel("userInfo", SysLoginUserInfoDTO.class);
            List<SysOrgRoleDTO> roles = loginUserInfoDTO.getRoles();
            if (roles == null || roles.size() == 0) throw new MyException("当前账号未分配角色，无法登录！");

            UserAgent ua = UserAgentUtil.parse(request.getHeader(Header.USER_AGENT.toString()));
            String ip = request.getHeader("X-Real-IP");
            if (CommonUtil.isEmpty(ip)) {
                ip = request.getRemoteAddr();
            }
            OnlineUserDTO onlineUserDTO = new OnlineUserDTO();
            onlineUserDTO.setToken(StpUtil.getTokenValue());
            onlineUserDTO.setUserCode(sysUser.getCode());
            onlineUserDTO.setUserName(sysUser.getName());
            onlineUserDTO.setLoginBrowser(ua.getBrowser().getName());
            onlineUserDTO.setLoginIp(ip);
            onlineUserDTO.setLoginBrowser(ua.getBrowser().getName());
            onlineUserDTO.setLoginOs(ua.getOs().getName());
            onlineUserDTO.setIsMobile(ua.isMobile());
            SysOrgRoleDTO orgRole = roles.get(0); //默认当前使用角色为第一个角色
            onlineUserDTO.setOrgId(orgRole.getSysOrgId());
            onlineUserDTO.setRoleId(orgRole.getSysRoleId());
            onlineUserDTO.setOrgName(orgRole.getOrgName());
            onlineUserDTO.setRoleName(orgRole.getRoleName());
            onlineUserDTO.setLoginTime(LocalDateTime.now());
            StpUtil.getTokenSession().set("userInfo", onlineUserDTO);
        }
        return getCurrentLoginUserVO();
    }

    /**
     * 管理端用户角色切换
     */
    public LoginUserInfoVO switchUserRole(Map<String, Object> params) {
        String orgId = CommonUtil.getString(params.get("sysOrgId"));
        String roleId = CommonUtil.getString(params.get("sysRoleId"));
        SysLoginUserInfoDTO loginUserInfoDTO = StpUtil.getSession().getModel("userInfo", SysLoginUserInfoDTO.class);
        List<SysOrgRoleDTO> roles = loginUserInfoDTO.getRoles();
        for (SysOrgRoleDTO orgRole : roles) {
            if (Objects.equals(orgRole.getSysOrgId().toString(), orgId) && Objects.equals(orgRole.getSysRoleId().toString(), roleId)) {
                SaSession tokenSession = StpUtil.getTokenSession();
                OnlineUserDTO onlineUserDTO = tokenSession.getModel("userInfo", OnlineUserDTO.class);
                onlineUserDTO.setOrgId(orgRole.getSysOrgId());
                onlineUserDTO.setOrgName(orgRole.getOrgName());
                onlineUserDTO.setRoleId(orgRole.getSysRoleId());
                onlineUserDTO.setRoleName(orgRole.getRoleName());
                tokenSession.set("userInfo", onlineUserDTO);
                return getCurrentLoginUserVO();
            }
        }
        throw new MyException("角色切换异常，请重新登录后操作！");
    }

    /**
     * 管理端用户注销
     */
    public RestResponse<?> logout() {
        StpUtil.logout();
        return RestResponse.success();
    }

    /**
     * 在线用户查询
     */
    public PageResult<OnlineUserDTO> queryOnlineUser(PageQuery<Map<String, Object>> pageQuery) {
        final Map<String, Object> param = pageQuery.getParam();
        WebLogs.debug("在线用户查询---");
        // 查询所有已登录的 Token
        List<String> tokens = StpUtil.searchTokenValue("", 0, -1, false);
        List<OnlineUserDTO> onlineUserList = tokens.stream()
                //截取实际的token值，去掉前缀
                .map(i -> i.split(":")[3])
                //过滤掉未登录的token
                .filter(i -> StpUtil.getLoginIdByToken(i) != null)
                .map(StpUtil::getTokenSessionByToken)
                .map(i -> i.getModel("userInfo", OnlineUserDTO.class))
                //模糊查询
                .filter(i -> {
                    boolean r = true;
                    if (CommonUtil.isNotEmpty(param.get("userCode"))) {
                        r = i.getUserCode().contains(param.get("userCode").toString());
                    }
                    if (r && CommonUtil.isNotEmpty(param.get("userName"))) {
                        r = i.getUserName().contains(param.get("userName").toString());
                    }
                    if (r && CommonUtil.isNotEmpty(param.get("ip"))) {
                        r = i.getLoginIp().contains(param.get("ip").toString());
                    }
                    return r;
                })
                .toList();
        PageResult<OnlineUserDTO> pageResult = new PageResult<>();
        pageResult.setIsPage(pageQuery.getIsPage());
        pageResult.setCurrentPage(pageQuery.getCurrentPage());
        pageResult.setPageSize(pageQuery.getPageSize());
        pageResult.setTotal(onlineUserList.size());
        if (pageQuery.getIsPage()) {
            onlineUserList = onlineUserList.stream()
                    .skip((long) (pageQuery.getCurrentPage() - 1) * pageQuery.getPageSize())
                    .limit(pageQuery.getPageSize())
                    .toList();
        }
        pageResult.setList(onlineUserList);
        return pageResult;
    }

    /**
     * 踢用户下线
     */
    public void kickOut(String token) {
        StpUtil.kickoutByTokenValue(token);
    }

    /**
     * 刷新用户信息和权限进session缓存
     */
    public void refreshUserPermission(Object userId) {
        SaSession session = StpUtil.getSessionByLoginId(userId);
        SysUser sysUser = baseJdbcDao.findById(SysUser.class, (Serializable) userId);
        SysUserDTO sysUserDTO = new SysUserDTO();
        BeanUtils.copyProperties(sysUser, sysUserDTO);
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
        String roleIds = roles.stream().map(i -> i.getSysRoleId().toString()).collect(Collectors.joining(","));
        String sql2 = """
                    select
                         a.sys_role_id role_id,b.*
                    from sys_role_menu a
                    left join sys_menu b on a.sys_menu_id = b.id
                    where a.sys_role_id in (%s)
                    order by b.`order` asc
                """.formatted(roleIds);
        //查询角色拥有的所有菜单权限
        List<SysMenuDTO> menus = baseJdbcDao.findList(SysMenuDTO.class, sql2);
        Map<Integer, List<SysMenuDTO>> roleMenus = menus.stream().collect(Collectors.groupingBy(SysMenuDTO::getRoleId));
        SysLoginUserInfoDTO loginUserInfoDTO = new SysLoginUserInfoDTO();
        loginUserInfoDTO.setUser(sysUserDTO);
        loginUserInfoDTO.setRoles(roles);
        loginUserInfoDTO.setRoleMenuMap(roleMenus);
        session.set("userInfo", loginUserInfoDTO);
    }

    /**
     * 获取当前token的用户角色信息
     */
    private LoginUserInfoVO getCurrentLoginUserVO() {
        try {
            SaSession session = StpUtil.getSession();
            SaSession tokenSession = StpUtil.getTokenSession();
            LoginUserInfoVO loginUserInfo = null;
            if (session != null && tokenSession != null) {
                SysLoginUserInfoDTO loginUserInfoDTO = session.getModel("userInfo", SysLoginUserInfoDTO.class);
                loginUserInfo = new LoginUserInfoVO();
                loginUserInfo.setTokenName(StpUtil.getTokenName());
                loginUserInfo.setTokenValue(StpUtil.getTokenValue());
                loginUserInfo.setUser(loginUserInfoDTO.getUser());
                OnlineUserDTO onlineUser = (OnlineUserDTO) tokenSession.get("userInfo");
                List<SysOrgRoleDTO> roles = loginUserInfoDTO.getRoles();
                for (SysOrgRoleDTO role : roles) {
                    role.setActive(Objects.equals(onlineUser.getRoleId(), role.getSysRoleId()) && Objects.equals(onlineUser.getOrgId(), role.getSysOrgId()));
                }
                loginUserInfo.setRoles(roles);
                loginUserInfo.setMenus(loginUserInfoDTO.getRoleMenuMap().get(onlineUser.getRoleId()));

            }
            return loginUserInfo;
        } catch (NotLoginException e) {
            return null;
        }
    }
}
