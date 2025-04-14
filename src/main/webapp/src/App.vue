<script setup>
import { ref } from 'vue'

// 值
const filePath = ref()

</script>

<template>
  <div class="aic-seed-finder-container">
    <el-container>
      <!-- 头部区域 -->
      <el-header height="80px">
        <div class="header-content">
          <h1>Alice In Cradle 种子查找器</h1>
        </div>
      </el-header>

      <!-- 主体区域 -->
      <el-main>
        <!-- 搜索区域 -->
        <el-card class="search-card">
          <template #header>
            <div class="card-header">
              <span>参数配置</span>
            </div>
          </template>

          <el-form :model="searchForm" label-width="120px">
            <el-row :gutter="20">
              <el-button>获取剪贴板中文件路径</el-button>
            </el-row>
            <el-row :gutter="20">
              
            </el-row>

            <el-form-item>
            </el-form-item>


          </el-form>
        </el-card>

        <!-- 搜索结果区域 -->
        <!-- <el-card class="result-card">
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

          <div class="pagination-container">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]" :total="totalResults" layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange" @current-change="handleCurrentChange" />
          </div>
        </el-card> -->
      </el-main>

      <!-- 底部区域 -->
      <!-- <el-footer height="60px">
        <div class="footer-content">
          <span>2025 AIC种子查找器</span>
          <el-link type="primary" :underline="false" href="#">帮助文档</el-link>
        </div>
      </el-footer> -->
    </el-container>
  </div>
</template>

<style scoped>
:global(body),
:global(html) {
  margin: 0;
  padding: 0;
  height: 100%;
  overflow: hidden;
}

.aic-seed-finder-container {
  position: relative;
  height: 100vh;
  width: 100vw;
  background-image: url("./assets/background.png");
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  padding: 0;
  margin: 0;
  overflow: auto;
}
</style>