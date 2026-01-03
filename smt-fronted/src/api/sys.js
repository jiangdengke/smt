import { request } from '../utils/request'

// 通用 CRUD 生成器
const createCrudApi = (resource) => ({
  list: (params) => request(`/sys/${resource}`, { params }),
  create: (data) => request(`/sys/${resource}`, { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/sys/${resource}/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => request(`/sys/${resource}/${id}`, { method: 'DELETE' })
})

export const factoryApi = createCrudApi('factories')
export const workshopApi = createCrudApi('workshops')
export const lineApi = createCrudApi('lines')
export const machineApi = createCrudApi('machines')
export const abnormalCategoryApi = createCrudApi('abnormal-categories')
export const abnormalTypeApi = createCrudApi('abnormal-types')
export const teamApi = createCrudApi('teams')
export const personApi = createCrudApi('people')

// 保持兼容旧的导出（只读），逐步迁移
export const getFactories = () => factoryApi.list()
export const getWorkshops = () => workshopApi.list()
export const getLines = () => lineApi.list()
export const getMachines = () => machineApi.list()
export const getAbnormalCategories = () => abnormalCategoryApi.list()
export const getAbnormalTypes = () => abnormalTypeApi.list()
export const getTeams = () => teamApi.list()
export const getPeople = () => personApi.list()
