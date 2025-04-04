package com.xh.system.service;

import com.xh.common.core.service.BaseServiceImpl;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.web.MyException;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.system.client.entity.SysDictDetail;
import com.xh.system.client.entity.SysDictType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统数据字典service
 * sunxh 2023/4/16
 */
@Slf4j
@Service
public class SysDictService extends BaseServiceImpl {

    /**
     * 数据字典类型查询
     */
    @Transactional(readOnly = true)
    public PageResult<SysDictType> queryTypes(PageQuery<Map<String, Object>> pageQuery) {
        Map<String, Object> param = pageQuery.getParam();
        if (param == null) param = new HashMap<>();
        String sql = "select * from sys_dict_type where deleted is false ";
        if (CommonUtil.isNotEmpty(param.get("name"))) {
            sql += " and name like '%' ? '%'";
            pageQuery.addArg(param.get("name"));
        }
        if (CommonUtil.isNotEmpty(param.get("enabled"))) {
            sql += " and enabled = ?";
            pageQuery.addArg(param.get("enabled"));
        }
        sql += " order by id asc";
        pageQuery.setBaseSql(sql);
        return baseJdbcDao.query(SysDictType.class, pageQuery);
    }

    /**
     * 数据字典明细查询
     */
    @Transactional(readOnly = true)
    public PageResult<SysDictDetail> queryDetails(PageQuery<Map<String, Object>> pageQuery) {
        Map<String, Object> param = pageQuery.getParam();
        String sql = """
                    select a.*, b.id dict_type_id,b.name dict_type_name
                    from sys_dict_detail a
                    left join sys_dict_type b  on a.sys_dict_type_id = b.id
                    where a.deleted is false
                """;
        if (CommonUtil.isNotEmpty(param.get("dictTypeId"))) {
            sql += " and sys_dict_type_id = ?";
            pageQuery.addArg(param.get("dictTypeId"));
        }
        if (CommonUtil.isNotEmpty(param.get("value"))) {
            sql += " and value like '%' ? '%'";
            pageQuery.addArg(param.get("value"));
        }
        if (CommonUtil.isNotEmpty(param.get("label"))) {
            sql += " and label like '%' ? '%'";
            pageQuery.addArg(param.get("label"));
        }
        if (CommonUtil.isNotEmpty(param.get("enabled"))) {
            sql += " and enabled = ?";
            pageQuery.addArg(param.get("enabled"));
        }
        sql += " order by sys_dict_type_id asc,`order` asc";
        pageQuery.setBaseSql(sql);
        return baseJdbcDao.query(SysDictDetail.class, pageQuery);
    }


    /**
     * id获取数据字典明细详情
     */
    @Transactional(readOnly = true)
    public SysDictDetail getDictDetailById(Serializable id) {
        String sql = """
                select a.*,b.id dict_type_id,b.name dict_type_name from sys_dict_detail a
                left join sys_dict_type b on a.sys_dict_type_id = b.id
                where a.id = ?
        """;
        return baseJdbcDao.findBySql(SysDictDetail.class, sql, id);
    }

    /**
     * 数据字典明细保存
     */
    @Transactional
    public SysDictDetail saveDictDetail(SysDictDetail sysDictDetail) {
        String sql = """
                select count(1) from sys_dict_detail where deleted is false
                and sys_dict_type_id = '%s' and value = '%s'
                """.formatted(sysDictDetail.getSysDictTypeId(), sysDictDetail.getValue());
        if (sysDictDetail.getId() != null) {
            sql += " and id <> %s ".formatted(sysDictDetail.getId());
        }
        Integer count = primaryJdbcTemplate.queryForObject(sql, Integer.class);
        if (count > 0) throw new MyException("字典key:%s重复！".formatted(sysDictDetail.getValue()));
        if (sysDictDetail.getId() == null) baseJdbcDao.insert(sysDictDetail);
        else baseJdbcDao.update(sysDictDetail);
        return sysDictDetail;
    }

    /**
     * ids批量删除数据字典明细
     */
    @Transactional
    public void delDetail(List<Integer> ids) {
        log.info("批量删除数据字典明细--");
        String sql = "update sys_dict_detail set deleted = 1 where id in (:ids)";
        Map<String, Object> paramMap = new HashMap<>() {{
            put("ids", ids);
        }};
        primaryNPJdbcTemplate.update(sql, paramMap);
    }
}
