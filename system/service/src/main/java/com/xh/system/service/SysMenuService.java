package com.xh.system.service;

import com.xh.common.core.service.BaseServiceImpl;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.utils.WebLogs;
import com.xh.common.core.web.MyException;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.system.client.entity.SysMenu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统菜单service
 * sunxh 2023/4/16
 */
@Service
@Slf4j
public class SysMenuService extends BaseServiceImpl {

    /**
     * 系统菜单查询
     */
    @Transactional(readOnly = true)
    public PageResult<SysMenu> query(PageQuery<Map<String, Object>> pageQuery) {
        WebLogs.info("菜单列表查询---");
        Map<String, Object> param = pageQuery.getParam();
        if (param == null) param = new HashMap<>();
        String flag = CommonUtil.getString(param.get("flag"));
        String sql = "select * from sys_menu where deleted = 0 ";
        if (CommonUtil.isNotEmpty(param.get("title"))) {
            sql += " and title like '%' ? '%'";
            pageQuery.addArg(param.get("title"));
        }
        if (CommonUtil.isNotEmpty(param.get("enabled"))) {
            sql += " and enabled = ?";
            pageQuery.addArg(param.get("enabled"));
        }
        if (flag.equals("selectParentMenu")) {
            sql += " and type in ('dir', 'menu')";
        }
        sql += " order by `order` asc";
        pageQuery.setBaseSql(sql);
        return baseJdbcDao.query(SysMenu.class, pageQuery);
    }

    /**
     * 切换菜单字段值
     */
    @Transactional
    public void switchMenuProp(Map<String, Object> param) {
        WebLogs.info("切换菜单字段值---", param);
        Object id = param.get("id");
        Object prop = param.get("prop");
        Object value = param.get("value");
        SysMenu menu = baseJdbcDao.findById(SysMenu.class, (Serializable) id);
        if ("cache".equals(prop)) menu.setCache((Boolean) value);
        else if ("enabled".equals(prop)) menu.setEnabled((Boolean) value);
        else throw new MyException("参数异常，检查后重试！");
        baseJdbcDao.update(menu);
    }


    @Transactional
    public SysMenu save(SysMenu sysMenu) {
        WebLogs.getLogger().info("菜单保存---");

        String sql = "select count(1) from sys_menu where deleted = 0 and name = ?";
        if (sysMenu.getId() != null) {
            sql += " and id <> %s ".formatted(sysMenu.getId());
        }
        Integer count = primaryJdbcTemplate.queryForObject(sql, Integer.class, sysMenu.getName());
        assert  count != null;
        if (count > 0) throw new MyException("菜单name：%s重复！".formatted(sysMenu.getName()));
        if (sysMenu.getId() == null) baseJdbcDao.insert(sysMenu);
        else baseJdbcDao.update(sysMenu);
        return sysMenu;
    }

    /**
     * id获取菜单详情
     */
    @Transactional(readOnly = true)
    public SysMenu getById(Serializable id) {
        return baseJdbcDao.findById(SysMenu.class, id);
    }


    /**
     * ids批量删除菜单
     */
    @Transactional
    public void del(List<Serializable> ids) {
        log.info("批量删除菜单--");
        String sql = "update sys_menu set deleted = 1 where id in (:ids)";
        Map<String, Object> paramMap = new HashMap<>(){{
            put("ids", ids);
        }};
        primaryNPJdbcTemplate.update(sql, paramMap);
    }
}
