package com.xh.system.service;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.http.Header;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.xh.common.core.dto.*;
import com.xh.common.core.service.BaseServiceImpl;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.utils.LoginUtil;
import com.xh.common.core.utils.WebLogs;
import com.xh.common.core.web.MyException;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.common.core.web.RestResponse;
import com.xh.system.client.dto.ImageCaptchaDTO;
import com.xh.system.client.entity.SysUser;
import com.xh.system.client.vo.LoginUserInfoVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SysLoginService extends BaseServiceImpl {
    @Resource
    private DataSourceTransactionManager dstManager;

    public static final String captchaKeyPrefix = "captcha:";

    /**
     * 生成图形验证码
     */
    public ImageCaptchaDTO getImageCaptcha(String captchaKey) {
        //定义图形验证码的长、宽、验证码字符数、干扰元素个数
        AbstractCaptcha captcha = CaptchaUtil.createLineCaptcha(100, 30, 4, 10);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        ImageCaptchaDTO imageCaptcha = new ImageCaptchaDTO();
        imageCaptcha.setCaptchaKey(captchaKey);
        imageCaptcha.setImageBase64(captcha.getImageBase64Data());
        valueOperations.set(captchaKeyPrefix + captchaKey, captcha, 2, TimeUnit.MINUTES);
        return imageCaptcha;
    }

    /**
     * 管理端用户登录
     */
    @Transactional
    public LoginUserInfoVO login(HttpServletRequest request, Map<String, Object> params) {
        String username = CommonUtil.getString(params.get("username"));
        String password = CommonUtil.getString(params.get("password"));
        String captchaKey = CommonUtil.getString(params.get("captchaKey"));
        String captchaCode = CommonUtil.getString(params.get("captchaCode"));
        String locale = CommonUtil.getString(params.get("locale"));
        String localeLabel = CommonUtil.getString(params.get("localeLabel"));

        SaSession session = null;
        try {
            session = StpUtil.getSession();
        } catch (NotLoginException e) {
            log.info("用户未登录需要重新登录~");
        }
        //尝试登录
        if (session == null && CommonUtil.isNotEmpty(username)) {

            if (CommonUtil.isEmpty(captchaKey)) throw new MyException("非法登录");
            if (CommonUtil.isEmpty(captchaCode)) throw new MyException("请输入图形验证码");

            //验证图形验证码
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            String key = captchaKeyPrefix + captchaKey;
            AbstractCaptcha captcha = (AbstractCaptcha) valueOperations.get(key);
            if (captcha == null) throw new MyException("验证码已失效");
            boolean verify = captcha.verify(captchaCode);
            if (!verify) throw new MyException("验证码错误");

            String sql = "select * from sys_user where code = ? and enabled = 1";
            SysUser sysUser = baseJdbcDao.findBySql(SysUser.class, sql, username);

            if (sysUser == null) throw new MyException("账号不存在");
            if (CommonUtil.getString(sysUser.getStatus()).equals("2"))
                throw new MyException(sysUser.getLockMsg());
            boolean matches = BCrypt.checkpw(password, sysUser.getPassword());
            if (!matches) {
                try {
                    //最大尝试次数
                    int maxTryNum = 3;
                    Integer failuresNum = sysUser.getFailuresNum();
                    if (failuresNum == null) failuresNum = 0;
                    failuresNum++;
                    //记录登录失败次数
                    sysUser.setFailuresNum(failuresNum);
                    //失败次数大于最大尝试次数账号锁定，保存
                    if (failuresNum >= maxTryNum) {
                        sysUser.setStatus(2);
                        sysUser.setLockMsg("用户登录失败次数超过%s次，账号已锁定，请联系管理员处理。".formatted(maxTryNum));
                        throw new MyException(sysUser.getLockMsg());
                    }
                    throw new MyException("密码错误！您还可以尝试%s次。".formatted(maxTryNum - failuresNum));
                } finally {
                    // 开启新事务，保存用户登录的失败信息
                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    // 获得事务状态
                    TransactionStatus transaction = dstManager.getTransaction(def);
                    try {
                        baseJdbcDao.update(sysUser);
                        dstManager.commit(transaction);
                    } catch (Exception e) {
                        dstManager.rollback(transaction);
                        WebLogs.error(e.getMessage());
                    }
                }
            } else {
                //失败次数置零
                sysUser.setFailuresNum(0);
                sysUser.setLockMsg(null);
                baseJdbcDao.update(sysUser);
            }

            StpUtil.login(sysUser.getId());
            //刷新用户信息和权限缓存
            refreshUserPermission(sysUser.getId());
            session = StpUtil.getSession();

            SysLoginUserInfoDTO loginUserInfoDTO = session.getModel(LoginUtil.SYS_USER_KEY, SysLoginUserInfoDTO.class);
            List<SysOrgRoleDTO> roles = loginUserInfoDTO.getRoles();
            if (roles == null || roles.isEmpty()) throw new MyException("当前账号未分配角色，无法登录！");

            UserAgent ua = UserAgentUtil.parse(request.getHeader(Header.USER_AGENT.toString()));
            String ip = request.getHeader("X-Real-IP");
            if (CommonUtil.isEmpty(ip)) {
                ip = request.getRemoteAddr();
            }
            String ipRegion = getIpRegion2(ip);

            OnlineUserDTO onlineUserDTO = new OnlineUserDTO();
            onlineUserDTO.setToken(StpUtil.getTokenValue());
            onlineUserDTO.setUserCode(sysUser.getCode());
            onlineUserDTO.setUserName(sysUser.getName());
            onlineUserDTO.setLoginBrowser(ua.getBrowser().getName());
            onlineUserDTO.setBrowserVersion(ua.getVersion());
            onlineUserDTO.setLoginIp(ip);
            onlineUserDTO.setLoginBrowser(ua.getBrowser().getName());
            onlineUserDTO.setLoginOs(ua.getOs().getName());
            onlineUserDTO.setIsMobile(ua.isMobile());
            onlineUserDTO.setLoginAddress(ipRegion);
            onlineUserDTO.setLocale(locale);
            onlineUserDTO.setLocaleLabel(localeLabel);

            SysOrgRoleDTO orgRole = roles.getFirst(); //默认当前使用角色为第一个角色
            onlineUserDTO.setOrgId(orgRole.getSysOrgId());
            onlineUserDTO.setRoleId(orgRole.getSysRoleId());
            onlineUserDTO.setOrgName(orgRole.getOrgName());
            onlineUserDTO.setRoleName(orgRole.getRoleName());
            onlineUserDTO.setLoginTime(LocalDateTime.now());
            StpUtil.getTokenSession().set(LoginUtil.SYS_USER_KEY, onlineUserDTO);
            //登录成功后删除验证码
            redisTemplate.delete(key);
        }
        return getCurrentLoginUserVO();
    }

    /**
     * 管理端用户角色切换
     */
    public LoginUserInfoVO switchUserRole(Map<String, Object> params) {
        String orgId = CommonUtil.getString(params.get("sysOrgId"));
        String roleId = CommonUtil.getString(params.get("sysRoleId"));
        SysLoginUserInfoDTO loginUserInfoDTO = StpUtil.getSession().getModel(LoginUtil.SYS_USER_KEY, SysLoginUserInfoDTO.class);
        List<SysOrgRoleDTO> roles = loginUserInfoDTO.getRoles();
        for (SysOrgRoleDTO orgRole : roles) {
            //从当前登录用户session中寻找匹配的角色，并设置当前角色，机构，及名称
            if (Objects.equals(orgRole.getSysOrgId().toString(), orgId) && Objects.equals(orgRole.getSysRoleId().toString(), roleId)) {
                SaSession tokenSession = StpUtil.getTokenSession();
                OnlineUserDTO onlineUserDTO = tokenSession.getModel(LoginUtil.SYS_USER_KEY, OnlineUserDTO.class);
                onlineUserDTO.setOrgId(orgRole.getSysOrgId());
                onlineUserDTO.setOrgName(orgRole.getOrgName());
                onlineUserDTO.setRoleId(orgRole.getSysRoleId());
                onlineUserDTO.setRoleName(orgRole.getRoleName());
                tokenSession.set(LoginUtil.SYS_USER_KEY, onlineUserDTO);
                return getCurrentLoginUserVO();
            }
        }
        throw new MyException("角色切换异常，请重新登录后操作！");
    }

    /**
     * 语言切换
     */
    public void switchLocale(Map<String, Object> params) {
        String locale = CommonUtil.getString(params.get("locale"));
        String localeLabel = CommonUtil.getString(params.get("localeLabel"));
        SaSession tokenSession = StpUtil.getTokenSession();
        OnlineUserDTO onlineUserDTO = tokenSession.getModel(LoginUtil.SYS_USER_KEY, OnlineUserDTO.class);
        onlineUserDTO.setLocale(locale);
        onlineUserDTO.setLocaleLabel(localeLabel);
        tokenSession.set(LoginUtil.SYS_USER_KEY, onlineUserDTO);
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
        // 查询所有已登录的 Token
        List<String> tokens = StpUtil.searchTokenValue("", 0, -1, false);
        List<OnlineUserDTO> onlineUserList = tokens.stream()
                //截取实际的token值，去掉前缀
                .map(i -> i.split(":")[3])
                //过滤掉未登录的token
                .filter(i -> StpUtil.getLoginIdByToken(i) != null)
                .map(StpUtil::getTokenSessionByToken)
                .map(i -> i.getModel(LoginUtil.SYS_USER_KEY, OnlineUserDTO.class))
                .filter(Objects::nonNull)
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
        if (roleIds.isEmpty()) {
            throw new MyException("该用户未分配角色，无法登录!");
        }
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
        session.set(LoginUtil.SYS_USER_KEY, loginUserInfoDTO);
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
                SysLoginUserInfoDTO loginUserInfoDTO = session.getModel(LoginUtil.SYS_USER_KEY, SysLoginUserInfoDTO.class);
                loginUserInfo = new LoginUserInfoVO();
                loginUserInfo.setTokenName(StpUtil.getTokenName());
                loginUserInfo.setTokenValue(StpUtil.getTokenValue());
                loginUserInfo.setUser(loginUserInfoDTO.getUser());
                OnlineUserDTO onlineUser = tokenSession.getModel(LoginUtil.SYS_USER_KEY, OnlineUserDTO.class);
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


    public String getIpRegion2(String ip) {
        // 1、创建 searcher 对象
        String dbPath = "/ip2region.xdb";
        try (
                InputStream inputStream = new ClassPathResource(dbPath).getInputStream();
                MySearcher searcher = MySearcher.newWithBuffer(FileCopyUtils.copyToByteArray(inputStream))
        ) {
            return searcher.search(ip).replaceAll("\\|", "").replaceAll("0", "");
        } catch (Exception e) {
            log.error("解析ip属地异常", e);
            return "";
        }
    }


    static class MySearcher extends Searcher implements AutoCloseable {
        public MySearcher(String dbFile, byte[] vectorIndex, byte[] cBuff) throws IOException {
            super(dbFile, vectorIndex, cBuff);
        }

        public static MySearcher newWithBuffer(byte[] cBuff) throws IOException {
            return new MySearcher((String) null, (byte[]) null, cBuff);
        }
    }
}
