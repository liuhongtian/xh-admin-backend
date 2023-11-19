package com.xh.system.service;

import com.xh.common.core.service.BaseServiceImpl;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.system.client.entity.SysOrg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统机构service
 * sunxh 2023/6/3
 */
@Service
@Slf4j
public class SysOrgService extends BaseServiceImpl {

    /**
     * 系统机构查询
     */
    @Transactional(readOnly = true)
    public PageResult<SysOrg> queryOrgTree(String name) {
        if (CommonUtil.isEmpty(name)) {
            String sql = """
                    SELECT * from sys_org where deleted = 0
                    """;
            List<SysOrg> list = baseJdbcDao.findList(SysOrg.class, sql);
            return new PageResult<>(list, list.size());
        }
        //递归查询树
        String sql = """
                WITH recursive tb as (
                	SELECT * from sys_org where deleted = 0 and name like '%' ? '%'
                	UNION ALL
                	SELECT b.* from tb inner join sys_org b on b.id = tb.parent_id
                )
                select  DISTINCT  * from tb
                """;
        List<SysOrg> list = baseJdbcDao.findList(SysOrg.class, sql, name);
        return new PageResult<>(list, list.size());
    }

    /**
     * 系统机构查询
     */
    @Transactional(readOnly = true)
    public PageResult<SysOrg> query(PageQuery<Map<String, Object>> pageQuery) {
        Map<String, Object> param = pageQuery.getParam();
        String sql = """
                select a.*, b.name parent_name from sys_org a
                left join sys_org b on a.parent_id = b.id where a.deleted = 0
                """;
        if (CommonUtil.isNotEmpty(param.get("code"))) {
            sql += " and a.code like '%' ? '%'";
            pageQuery.addArg(param.get("code"));
        }
        if (CommonUtil.isNotEmpty(param.get("name"))) {
            sql += " and a.name like '%' ? '%'";
            pageQuery.addArg(param.get("name"));
        }
        if (CommonUtil.isNotEmpty(param.get("parentId"))) {
            sql += " and a.parent_id = ?";
            pageQuery.addArg(param.get("parentId"));
        }
        if (CommonUtil.isNotEmpty(param.get("enabled"))) {
            sql += " and a.enabled = ?";
            pageQuery.addArg(param.get("enabled"));
        }
        pageQuery.setBaseSql(sql);
        return baseJdbcDao.query(SysOrg.class, pageQuery);
    }


    @Transactional
    public SysOrg save(SysOrg sysOrg) {
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
    public void del(List<Serializable> ids) {
        log.info("批量删除机构--");
        String sql = "update sys_org set deleted = 1 where id in (:ids)";
        Map<String, Object> paramMap = new HashMap<>() {{
            put("ids", ids);
        }};
        primaryNPJdbcTemplate.update(sql, paramMap);
    }
}
