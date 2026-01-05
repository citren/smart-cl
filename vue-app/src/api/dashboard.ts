import request from '@/utils/request'

/**
 * 获取工作台统计数据
 */
export function getStatistics() {
  return request({
    url: '/dashboard/statistics',
    method: 'get'
  })
}

/**
 * 获取待办事项
 */
export function getTodos() {
  return request({
    url: '/dashboard/todos',
    method: 'get'
  })
}

/**
 * 获取快捷菜单
 */
export function getQuickMenus() {
  return request({
    url: '/dashboard/quickMenus',
    method: 'get'
  })
}

/**
 * 获取图表数据
 */
export function getChartData() {
  return request({
    url: '/dashboard/chartData',
    method: 'get'
  })
}