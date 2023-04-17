package com.xh.system.client.entity;

import com.xh.common.core.entity.BaseEntity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Table
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity<Integer> {

    private String code;
    private String name;
    private String password;
    private String avatar;
    private Boolean enabled;
}
