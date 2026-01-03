import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as sysApi from '../api/sys'

// 模块名称到 API 对象的映射
const apiMap = {
  factory: sysApi.factoryApi,
  workshop: sysApi.workshopApi,
  line: sysApi.lineApi,
  machine: sysApi.machineApi,
  'abnormal-category': sysApi.abnormalCategoryApi,
  'abnormal-type': sysApi.abnormalTypeApi,
  team: sysApi.teamApi,
  people: sysApi.personApi
}

export const useMasterDataStore = defineStore('masterData', () => {
  const factories = ref([])
  const workshops = ref([])
  const lines = ref([])
  const machines = ref([])
  const abnormalCategories = ref([])
  const abnormalTypes = ref([])
  const teams = ref([])
  const people = ref([])
  
  const loaded = ref(false)

  const peopleMap = computed(() => {
    const map = new Map()
    for (const person of people.value) {
      map.set(person.id, person.name)
    }
    return map
  })

  const loadAll = async () => {
    console.log('Loading master data...')
    const [
      resFactories,
      resWorkshops,
      resLines,
      resMachines,
      resCategories,
      resTypes,
      resTeams,
      resPeople
    ] = await Promise.all([
      sysApi.getFactories(),
      sysApi.getWorkshops(),
      sysApi.getLines(),
      sysApi.getMachines(),
      sysApi.getAbnormalCategories(),
      sysApi.getAbnormalTypes(),
      sysApi.getTeams(),
      sysApi.getPeople()
    ])

    factories.value = resFactories || []
    workshops.value = resWorkshops || []
    lines.value = resLines || []
    machines.value = resMachines || []
    abnormalCategories.value = resCategories || []
    abnormalTypes.value = resTypes || []
    teams.value = resTeams || []
    people.value = resPeople || []
    
    loaded.value = true
  }

  // 通用 CRUD Action
  async function createItem(moduleKey, data) {
    const api = apiMap[moduleKey]
    if (!api) throw new Error(`Unknown module: ${moduleKey}`)
    await api.create(data)
    await loadAll() // 刷新数据
  }

  async function updateItem(moduleKey, id, data) {
    const api = apiMap[moduleKey]
    if (!api) throw new Error(`Unknown module: ${moduleKey}`)
    await api.update(id, data)
    await loadAll()
  }

  async function deleteItem(moduleKey, id) {
    const api = apiMap[moduleKey]
    if (!api) throw new Error(`Unknown module: ${moduleKey}`)
    await api.delete(id)
    await loadAll()
  }

  function reset() {
    factories.value = []
    workshops.value = []
    lines.value = []
    machines.value = []
    abnormalCategories.value = []
    abnormalTypes.value = []
    teams.value = []
    people.value = []
    loaded.value = false
  }

  return {
    factories,
    workshops,
    lines,
    machines,
    abnormalCategories,
    abnormalTypes,
    teams,
    people,
    peopleMap,
    loadAll,
    createItem,
    updateItem,
    deleteItem,
    reset
  }
})
