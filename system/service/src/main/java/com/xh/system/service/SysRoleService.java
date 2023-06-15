package com.xh.system.service;

import com.xh.common.core.service.BaseServiceImpl;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.utils.WebLogs;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.system.client.entity.SysOrg;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 系统机构service
 * sunxh 2023/6/3
 */
@Service
public class SysRoleService extends BaseServiceImpl {

    /**
     * 系统机构查询
     */
    @Transactional(readOnly = true)
    public PageResult<SysOrg> query(PageQuery<Map<String, Object>> pageQuery) {
        WebLogs.info("机构列表查询---");
        Map<String, Object> param = pageQuery.getParam();
        String sql = "select * from sys_org where deleted = 0 ";
        if (CommonUtil.isNotEmpty(param.get("name"))) {
            sql += " and name like '%' ? '%'";
            pageQuery.addArg(param.get("name"));
        }
        if (CommonUtil.isNotEmpty(param.get("enabled"))) {
            sql += " and enabled = ?";
            pageQuery.addArg(param.get("enabled"));
        }
        sql += " order by `order` asc";
        pageQuery.setBaseSql(sql);
        return baseJdbcDao.query(SysOrg.class, pageQuery);
    }


    @Transactional
    public SysOrg save(SysOrg sysOrg) {
        WebLogs.getLogger().info("机构保存---");
        if (sysOrg.getId() == null) baseJdbcDao.insert(sysOrg);
        else baseJdbcDao.update(sysOrg);
        return sysOrg;
    }

    /**
     * id获取机构详情
     */
    @Transactional(readOnly = true)
    public SysOrg getById(Serializable id) {
        return baseJdbcDao.findById(SysOrg.class, id);
    }

    /**
     * ids批量删除机构
     */
    @Transactional
    public void del(String ids) {
        String sql = "select * from sys_org where id in (%s)".formatted(ids);
        List<SysOrg> list = baseJdbcDao.findList(SysOrg.class, sql);
        for (SysOrg sysOrg : list) {
            sysOrg.setDeleted(true);//已删除
            baseJdbcDao.update(sysOrg);
        }
    }
}
