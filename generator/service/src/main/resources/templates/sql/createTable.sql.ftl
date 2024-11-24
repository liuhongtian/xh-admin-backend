CREATE TABLE `${tableName}`
(
<#list columns as field>
<#if !(field.isVirtual!false)>
    ${field.sqlColStr}<#if field_has_next>,</#if>
</#if>
</#list>
) COMMENT = '${tableComment}';
