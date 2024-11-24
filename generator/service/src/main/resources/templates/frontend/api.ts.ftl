import createAxios from '@/utils/request'

const baseUrl = import.meta.env.VITE_${service?upper_case}_BASE_URL

// ${name}列表查询
export function ${queryFun}(params: PageQuery, option?: RequestOption) {
    return createAxios(option).post(`${r'${baseUrl}'}${mappingPath}/query`, params)
}

// 保存${name}
export function ${saveFun}(params = {}, option?: RequestOption) {
    return createAxios(option).post(`${r'${baseUrl}'}${mappingPath}/save`, params)
}

// 获取${name}详情
export function ${getFun}(id: number) {
    return createAxios().get(`${r'${baseUrl}'}${mappingPath}/get/${r'${id}'}`)
}

// 批量删除${name}
export function ${delFun}(ids: string, option?: RequestOption) {
    return createAxios(option).delete(`${r'${baseUrl}'}${mappingPath}/del`, { params: { ids } })
}
<#if hasImport>

// ${name}导入
export function ${importFun}(params: object[], option?: RequestOption) {
    return createAxios(option).post(`${r'${baseUrl}'}${mappingPath}/imports`, params)
}
</#if>
