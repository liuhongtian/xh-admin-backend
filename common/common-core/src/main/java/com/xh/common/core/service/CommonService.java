package com.xh.common.core.service;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.xh.common.core.Constant;
import com.xh.common.core.dto.OnlineUserDTO;
import com.xh.common.core.dto.RolePermissionsDTO;
import com.xh.common.core.dto.SysMenuDTO;
import com.xh.common.core.entity.SysLog;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.utils.LoginUtil;
import com.xh.common.core.web.RestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
public class CommonService extends BaseServiceImpl {

    /**
     * 获取角色拥有的权限集合
     */
    public List<SysMenuDTO> getRolePermissions(Integer roleId, Boolean refresh) {
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();

        if (!refresh) {
            RolePermissionsDTO rolePermissions = (RolePermissionsDTO) operations.get(Constant.ROLE_PERMISSIONS_PREFIX + roleId);
            if (rolePermissions != null) {
                return rolePermissions.getPermissions();
            }
        }

        String sql2 = """
                    select
                         a.sys_role_id role_id,b.*
                    from sys_role_menu a
                    left join sys_menu b on a.sys_menu_id = b.id
                    where a.deleted = 0 and b.deleted = 0 and a.sys_role_id = ?
                    order by b.`order` asc
                """;
        //查询角色拥有的所有菜单权限
        List<SysMenuDTO> menus = baseJdbcDao.findList(SysMenuDTO.class, sql2, roleId);

        RolePermissionsDTO rolePermissions = new RolePermissionsDTO();
        rolePermissions.setPermissions(menus);
        rolePermissions.setRoleId(roleId);
        rolePermissions.setCreateTime(LocalDateTime.now());
        operations.set(Constant.ROLE_PERMISSIONS_PREFIX + roleId, rolePermissions);

        return menus;
    }

    /**
     * 保存日志
     */
    @Transactional
    public SysLog save(SysLog sysLog) {
        sysLog.setToken(StpUtil.getTokenValue());
        sysLog.setEndTime(LocalDateTime.now());
        sysLog.setTime(ChronoUnit.MILLIS.between(sysLog.getStartTime(), sysLog.getEndTime()));
        try {
            if (StpUtil.isLogin()) {
                OnlineUserDTO onlineUserDTO = StpUtil.getTokenSession().getModel(LoginUtil.SYS_USER_KEY, OnlineUserDTO.class);
                sysLog.setLocale(onlineUserDTO.getLocale());
                sysLog.setLocaleLabel(onlineUserDTO.getLocaleLabel());
                sysLog.setSysOrgId(onlineUserDTO.getOrgId());
                sysLog.setSysRoleId(onlineUserDTO.getRoleId());
                sysLog.setOrgName(onlineUserDTO.getOrgName());
                sysLog.setRoleName(onlineUserDTO.getRoleName());
                BeanUtils.copyProperties(onlineUserDTO, sysLog);
            }

            String responseBody = sysLog.getResponseBody();
            if (CommonUtil.isNotEmpty(responseBody)) {
                RestResponse<?> restResponse = JSON.parseObject(responseBody, RestResponse.class);
                sysLog.setStatus(restResponse.getStatus());
            }
        } catch (Exception e) {
            log.error("存储日志异常", e);
        }
        baseJdbcDao.insert(sysLog);
        return sysLog;
    }
}
