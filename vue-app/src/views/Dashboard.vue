<template>
  <div class="dashboard-container">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="statistics-row">
      <el-col :xs="12" :sm="6" :lg="6">
        <el-card class="statistics-card">
          <div class="statistics-item">
            <div class="statistics-content">
              <div class="statistics-title">总用户数</div>
              <div class="statistics-value">{{ statistics.totalUsers }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6" :lg="6">
        <el-card class="statistics-card">
          <div class="statistics-item">
            <div class="statistics-content">
              <div class="statistics-title">总订单数</div>
              <div class="statistics-value">{{ statistics.totalOrders }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6" :lg="6">
        <el-card class="statistics-card">
          <div class="statistics-item">
            <div class="statistics-content">
              <div class="statistics-title">总收入</div>
              <div class="statistics-value">¥{{ formatMoney(statistics.totalRevenue) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6" :lg="6">
        <el-card class="statistics-card">
          <div class="statistics-item">
            <div class="statistics-content">
              <div class="statistics-title">今日订单</div>
              <div class="statistics-value">{{ statistics.todayOrders }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 中间区域 -->
    <el-row :gutter="20" class="middle-row">
      <!-- 图表 -->
      <el-col :xs="24" :lg="16">
        <el-card title="数据趋势" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>数据趋势</span>
            </div>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <!-- 待办事项 -->
      <el-col :xs="24" :lg="8">
        <el-card title="待办事项" class="todo-card">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-button type="primary" link>查看全部</el-button>
            </div>
          </template>
          <div class="todo-list">
            <div 
              v-for="item in todos" 
              :key="item.id"
              class="todo-item"
              :class="{ completed: item.status === 'completed' }"
            >
              <div class="todo-content">
                <div class="todo-title">{{ item.title }}</div>
                <div class="todo-time">{{ item.createTime }}</div>
              </div>
              <el-tag 
                :type="getPriorityType(item.priority)"
                size="small"
              >
                {{ getPriorityText(item.priority) }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷菜单 -->
    <el-row :gutter="20" class="quick-menu-row">
      <el-col :span="24">
        <el-card title="快捷菜单" class="quick-menu-card">
          <template #header>
            <div class="card-header">
              <span>快捷菜单</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col 
              v-for="menu in quickMenus" 
              :key="menu.id"
              :xs="12" 
              :sm="6" 
              :lg="6"
            >
              <div class="quick-menu-item" @click="handleQuickMenuClick(menu)">
                <el-icon size="32" class="menu-icon">
                  <component :is="menu.icon" />
                </el-icon>
                <div class="menu-name">{{ menu.name }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getStatistics, getTodos, getQuickMenus, getChartData } from '@/api/dashboard'

const router = useRouter()
const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

// 数据
const statistics = ref({
  totalUsers: 0,
  totalOrders: 0,
  totalRevenue: 0,
  todayOrders: 0
})

const todos = ref<any[]>([])
const quickMenus = ref<any[]>([])
const chartData = ref<any>(null)

// 格式化金额
const formatMoney = (value: number) => {
  return value.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

// 获取优先级类型
const getPriorityType = (priority: string) => {
  const typeMap: Record<string, string> = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  }
  return typeMap[priority] || 'info'
}

// 获取优先级文本
const getPriorityText = (priority: string) => {
  const textMap: Record<string, string> = {
    high: '高',
    medium: '中',
    low: '低'
  }
  return textMap[priority] || '低'
}

// 初始化图表
const initChart = () => {
  if (!chartRef.value || !chartData.value) return
  
  chartInstance = echarts.init(chartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: chartData.value.series.map((item: any) => item.name)
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: chartData.value.xAxis
    },
    yAxis: [
      {
        type: 'value',
        name: '订单量',
        position: 'left'
      },
      {
        type: 'value',
        name: '收入',
        position: 'right'
      }
    ],
    series: chartData.value.series.map((item: any, index: number) => ({
      name: item.name,
      type: 'line',
      smooth: true,
      yAxisIndex: index,
      data: item.data,
      emphasis: {
        focus: 'series'
      },
      areaStyle: index === 0 ? {} : undefined
    }))
  }
  
  chartInstance.setOption(option)
}

// 处理快捷菜单点击
const handleQuickMenuClick = (menu: any) => {
  ElMessage.success(`点击了 ${menu.name}`)
  if (menu.path) {
    router.push(menu.path)
  }
}

// 加载数据
const loadData = async () => {
  try {
    // 加载统计数据
    const statsRes = await getStatistics()
    if (statsRes.code === '200') {
      statistics.value = statsRes.data
    }
    
    // 加载待办事项
    const todosRes = await getTodos()
    if (todosRes.code === '200') {
      todos.value = todosRes.data
    }
    
    // 加载快捷菜单
    const menusRes = await getQuickMenus()
    if (menusRes.code === '200') {
      quickMenus.value = menusRes.data
    }
    
    // 加载图表数据
    const chartRes = await getChartData()
    if (chartRes.code === '200') {
      chartData.value = chartRes.data
      // 延迟初始化图表，确保DOM已渲染
      setTimeout(() => {
        initChart()
      }, 100)
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 监听窗口大小变化
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
  }
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 84px);
}

.statistics-row {
  margin-bottom: 20px;
}

.statistics-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.statistics-item {
  display: flex;
  align-items: center;
  padding: 20px 0;
}

.statistics-icon {
  margin-right: 20px;
  padding: 12px;
  background-color: #f0f2f5;
  border-radius: 8px;
}

.statistics-content {
  flex: 1;
}

.statistics-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.statistics-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.middle-row {
  margin-bottom: 20px;
}

.chart-card,
.todo-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
}

.todo-list {
  max-height: 300px;
  overflow-y: auto;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-item.completed {
  opacity: 0.6;
}

.todo-content {
  flex: 1;
}

.todo-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.todo-time {
  font-size: 12px;
  color: #909399;
}

.quick-menu-row {
  margin-bottom: 0;
}

.quick-menu-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.quick-menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.quick-menu-item:hover {
  background-color: #f5f7fa;
  transform: translateY(-2px);
}

.menu-icon {
  margin-bottom: 12px;
  color: #409EFF;
}

.menu-name {
  font-size: 14px;
  color: #303133;
  text-align: center;
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 10px;
  }
  
  .statistics-item {
    padding: 15px 0;
  }
  
  .statistics-icon {
    margin-right: 15px;
    padding: 8px;
  }
  
  .statistics-value {
    font-size: 20px;
  }
  
  .chart-container {
    height: 250px;
  }
}
</style>