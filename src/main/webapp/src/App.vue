<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 搜索表单数据
const searchForm = reactive({
  seedName: '',
  seedType: [],
  uploadDateRange: [],
  fileSizeRange: [0, 100],
  hashValue: '',
  seeders: null
})

// 种子类型选项
const seedTypes = ref([
  { value: 'movie', label: '电影' },
  { value: 'tv', label: '电视剧' },
  { value: 'music', label: '音乐' },
  { value: 'game', label: '游戏' },
  { value: 'software', label: '软件' },
  { value: 'document', label: '文档' },
  { value: 'other', label: '其他' }
])

// 文件大小滑块标记
const sizeMarks = reactive({
  0: '0GB',
  25: '25GB',
  50: '50GB',
  75: '75GB',
  100: '100GB+'
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const totalResults = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const showAdvancedSearch = ref(false)
const exportLoading = ref(false)

// 搜索方法
const handleSearch = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    tableData.value = generateMockData()
    totalResults.value = 125
    loading.value = false
    ElMessage.success('搜索完成')
  }, 1000)
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    seedName: '',
    seedType: [],
    uploadDateRange: [],
    fileSizeRange: [0, 100],
    hashValue: '',
    seeders: null
  })
}

// 分页变化
const handleSizeChange = (val) => {
  pageSize.value = val
  handleSearch()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  handleSearch()
}

// 下载种子
const handleDownload = (row) => {
  ElMessageBox.confirm(`确定要下载种子 "${row.name}" 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    ElMessage.success(`开始下载: ${row.name}`)
    // 这里添加实际下载逻辑
  })
}

// 导出结果
const handleExport = () => {
  exportLoading.value = true
  setTimeout(() => {
    exportLoading.value = false
    ElMessage.success('导出成功')
  }, 1500)
}

// 生成模拟数据
const generateMockData = () => {
  const mockTypes = ['movie', 'tv', 'music', 'game', 'software', 'document', 'other']
  const mockData = []

  for (let i = 0; i < 10; i++) {
    const type = mockTypes[Math.floor(Math.random() * mockTypes.length)]
    mockData.push({
      id: i + 1,
      name: `AIC种子示例文件_${type}_${Math.floor(Math.random() * 10000)}`,
      type: seedTypes.value.find(t => t.value === type).label,
      size: `${(Math.random() * 100).toFixed(2)} GB`,
      uploadDate: new Date(Date.now() - Math.floor(Math.random() * 30 * 24 * 60 * 60 * 1000)).toLocaleDateString(),
      seeders: Math.floor(Math.random() * 100),
      leechers: Math.floor(Math.random() * 500),
      hash: `abcdef1234567890${Math.floor(Math.random() * 10000000000)}`
    })
  }

  return mockData
}
</script>

<template>
  <div class="aic-seed-finder-container">
    <el-container>
      <!-- 头部区域 -->
      <el-header height="80px">
        <div class="header-content">
          <h1>AIC种子查找器</h1>
          <el-tag type="info" effect="dark">v1.0.0</el-tag>
        </div>
      </el-header>

      <!-- 主体区域 -->
      <el-main>
        <!-- 搜索区域 -->
        <el-card class="search-card">
          <template #header>
            <div class="card-header">
              <span>种子搜索</span>
            </div>
          </template>

          <el-form :model="searchForm" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="种子名称">
                  <el-input v-model="searchForm.seedName" placeholder="请输入种子名称或关键词" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="种子类型">
                  <el-select v-model="searchForm.seedType" placeholder="请选择种子类型" clearable multiple>
                    <el-option v-for="type in seedTypes" :key="type.value" :label="type.label" :value="type.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="上传时间">
                  <el-date-picker v-model="searchForm.uploadDateRange" type="daterange" range-separator="至"
                    start-placeholder="开始日期" end-placeholder="结束日期" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="文件大小">
                  <el-slider v-model="searchForm.fileSizeRange" range :min="0" :max="100" :marks="sizeMarks" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="resetSearch">重置</el-button>
              <el-button type="success" @click="showAdvancedSearch = !showAdvancedSearch">
                {{ showAdvancedSearch ? '隐藏' : '高级' }}搜索
              </el-button>
            </el-form-item>

            <!-- 高级搜索区域 -->
            <el-collapse-transition>
              <div v-show="showAdvancedSearch">
                <el-divider />
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="哈希值">
                      <el-input v-model="searchForm.hashValue" placeholder="请输入种子哈希值" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="做种人数">
                      <el-input-number v-model="searchForm.seeders" :min="0" controls-position="right" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
            </el-collapse-transition>
          </el-form>
        </el-card>

        <!-- 搜索结果区域 -->
        <el-card class="result-card">
          <template #header>
            <div class="card-header">
              <span>搜索结果</span>
              <div>
                <el-tag type="info">共 {{ totalResults }} 条结果</el-tag>
                <el-button type="primary" size="small" :loading="exportLoading" @click="handleExport">
                  导出结果
                </el-button>
              </div>
            </div>
          </template>

          <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading">
            <el-table-column prop="name" label="种子名称" width="300" sortable />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="size" label="大小" width="120" sortable />
            <el-table-column prop="uploadDate" label="上传时间" width="180" sortable />
            <el-table-column prop="seeders" label="做种" width="80" sortable />
            <el-table-column prop="leechers" label="下载" width="80" sortable />
            <el-table-column prop="hash" label="哈希值" width="280" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" type="success" @click="handleDownload(scope.row)">
                  下载
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页控件 -->
          <div class="pagination-container">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]" :total="totalResults" layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange" @current-change="handleCurrentChange" />
          </div>
        </el-card>
      </el-main>

      <!-- 底部区域 -->
      <el-footer height="60px">
        <div class="footer-content">
          <span>© 2023 AIC种子查找器 - 仅供学习交流使用</span>
          <el-link type="primary" :underline="false" href="#">帮助文档</el-link>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<style scoped>
/* 重置body和html的默认样式 */
:global(body), :global(html) {
  margin: 0;
  padding: 0;
  height: 100%;
  overflow: hidden; /* 防止页面整体滚动 */
}

.aic-seed-finder-container {
  position: relative;
  height: 100vh; /* 使用视窗高度 */
  width: 100vw; /* 使用视窗宽度 */
  background-image: url('https://source.unsplash.com/random/1920x1080/?nature,technology');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  padding: 0; /* 移除内边距 */
  margin: 0; /* 移除外边距 */
  overflow: auto; /* 只在容器内滚动 */
}

.aic-seed-finder-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.3);
  z-index: 0;
}

.el-container {
  position: relative;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 8px;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1);
  color: #ffffff;
}

.el-header {
  background-color: rgba(64, 158, 255, 0.8);
  color: white;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12);
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-main {
  padding: 20px;
  background-color: rgba(245, 247, 250, 0.1);
}

.search-card,
.result-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.2);
  background-color: rgba(255, 255, 255, 0.1);
  border: none;
  color: #ffffff;
}

.search-card :deep(.el-card__header),
.result-card :deep(.el-card__header) {
  background-color: rgba(0, 0, 0, 0.2);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: #ffffff;
}

.search-card :deep(.el-form-item__label),
.result-card :deep(.el-table th) {
  color: #ffffff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.el-footer {
  background-color: rgba(233, 238, 243, 0.8);
  color: #333;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid rgba(220, 223, 230, 0.5);
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
}

.footer-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 修改表格样式 */
:deep(.el-table) {
  background-color: transparent;
  color: #ffffff;
}

:deep(.el-table th) {
  background-color: rgba(0, 0, 0, 0.3);
  color: #ffffff;
}

:deep(.el-table tr) {
  background-color: rgba(255, 255, 255, 0.05);
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: rgba(255, 255, 255, 0.03);
}

:deep(.el-table--enable-row-hover .el-table__body tr:hover>td) {
  background-color: rgba(64, 158, 255, 0.2);
}

/* 修改输入框和选择框样式 */
:deep(.el-input__inner),
:deep(.el-select__wrapper),
:deep(.el-date-editor .el-input__inner) {
  background-color: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  color: #ffffff;
}

:deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.5);
}

/* 修改按钮样式 */
:deep(.el-button) {
  border-color: rgba(255, 255, 255, 0.2);
}

/* 修改分页样式 */
:deep(.el-pagination__total),
:deep(.el-pagination__jump) {
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-pager li) {
  background-color: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-pager li.is-active) {
  background-color: rgba(64, 158, 255, 0.8);
  color: #ffffff;
}
</style>