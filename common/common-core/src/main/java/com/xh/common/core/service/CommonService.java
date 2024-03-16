package com.xh.common.core.service;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.xh.common.core.dto.OnlineUserDTO;
import com.xh.common.core.entity.SysLog;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.utils.LoginUtil;
import com.xh.common.core.web.RestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class CommonService extends BaseServiceImpl {

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
                sysLog.setIpAddress(onlineUserDTO.getLoginAddress());
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
        } catch (Exception ignored) {
        }
        baseJdbcDao.insert(sysLog);
        return sysLog;
    }
}
