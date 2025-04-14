<template>
  <div id="aic-seed-finder-container">
    <el-container>
      <!-- 头部区域 -->
      <el-header height="80px">
        <div class="header-content">
          <h1>Alice In Cradle 种子查找器</h1>
        </div>
      </el-header>

      <el-main>
        <el-steps :active="activeStep" finish-status="success" align-center>
          <el-step title="选择文件"></el-step>
          <el-step title="配置参数"></el-step>
          <el-step title="添加规则"></el-step>
        </el-steps>

        <div class="step-content">
          <!-- 第一步：选择文件 -->
          <div v-show="activeStep === 0" class="step-panel">
            <el-button type="primary" @click="getClipboardPath">获取剪贴板中文件路径</el-button>
            <div v-if="selectedFile" style="margin-top: 10px;">
              <el-tag closable @close="clearSelectedFile">{{ selectedFile }}</el-tag>
            </div>
          </div>

          <!-- 第二步：配置参数 -->
          <div v-show="activeStep === 1" class="step-panel">
            <el-form label-width="120px">
              <el-form-item label="运行模式">
                <el-select v-model="config.mode" placeholder="请选择运行模式">
                  <el-option label="单线程" :value="1"></el-option>
                  <el-option label="多线程分块" :value="2"></el-option>
                </el-select>
              </el-form-item>

              <!-- 单线程配置 -->
              <div v-if="config.mode === 1">
                <el-form-item label="缓冲区大小">
                  <el-input-number v-model="config.buffer" :min="1"></el-input-number>
                </el-form-item>
              </div>

              <!-- 多线程配置 -->
              <div v-else-if="config.mode === 2">
                <el-form-item label="线程数">
                  <el-input-number v-model="config.threads" :min="1"></el-input-number>
                </el-form-item>
                <el-form-item label="窗口大小(MB)">
                  <el-input-number v-model="config.windowSize" :min="1"></el-input-number>
                </el-form-item>
                <el-form-item label="分区缓冲大小(MB)">
                  <el-input-number v-model="config.chunkBuffer" :min="1"></el-input-number>
                </el-form-item>
                <el-form-item label="缓冲区大小(MB)">
                  <el-input-number v-model="config.buffer" :min="1"></el-input-number>
                </el-form-item>
              </div>

              <el-form-item label="堆大小">
                <el-input-number v-model="config.size" :min="1"></el-input-number>
              </el-form-item>
            </el-form>
          </div>

          <!-- 第三步：添加规则 -->
          <div v-show="activeStep === 2" class="step-panel">
            <el-tabs v-model="activeRuleTab">
              <el-tab-pane label="Seed规则" name="seed">
                <el-button type="primary" @click="addSeedRule">添加Seed规则</el-button>
                <el-table :data="rules.seed" border style="width: 100%; margin-top: 10px;">
                  <el-table-column prop="enemyId" label="怪物ID">
                    <template #default="{ row }">
                      <el-select v-model="row.enemyId" placeholder="请选择怪物">
                        <el-option v-for="(value, key) in enemyL10n" :key="key" :label="value" :value="key"></el-option>
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column prop="operator" label="操作符">
                    <template #default="{ row }">
                      <el-select v-model="row.operator" placeholder="请选择操作符">
                        <el-option label="等于" value="eq"></el-option>
                        <el-option label="不等于" value="neq"></el-option>
                        <el-option label="小于" value="lt"></el-option>
                        <el-option label="小于等于" value="lte"></el-option>
                        <el-option label="大于" value="gt"></el-option>
                        <el-option label="大于等于" value="gte"></el-option>
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column prop="value" label="值">
                    <template #default="{ row }">
                      <el-input-number v-model="row.value" :min="0"></el-input-number>
                    </template>
                  </el-table-column>
                  <el-table-column prop="score" label="分数">
                    <template #default="{ row }">
                      <el-input-number v-model="row.score"></el-input-number>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作">
                    <template #default="{ $index }">
                      <el-button type="danger" @click="removeSeedRule($index)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>

              <el-tab-pane label="Enemy规则" name="enemy">
                <el-button type="primary" @click="addEnemyRule">添加Enemy规则</el-button>
                <el-table :data="rules.enemy" border style="width: 100%; margin-top: 10px;">
                  <el-table-column prop="enemyId" label="怪物ID">
                    <template #default="{ row }">
                      <el-select v-model="row.enemyId" placeholder="请选择怪物" clearable>
                        <el-option v-for="(value, key) in enemyL10n" :key="key" :label="value" :value="key"></el-option>
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column prop="isOverride" label="污染体">
                    <template #default="{ row }">
                      <el-checkbox v-model="row.isOverride"></el-checkbox>
                    </template>
                  </el-table-column>
                  <el-table-column prop="attrs" label="属性">
                    <template #default="{ row }">
                      <el-select v-model="row.attrs" multiple placeholder="请选择属性" style="width: 100%">
                        <el-option v-for="(value, key) in attrs" :key="value" :label="key" :value="value"></el-option>
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column prop="order" label="序号">
                    <template #default="{ row }">
                      <el-input-number v-model="row.order" :min="0" :max="20" :controls="false"></el-input-number>
                    </template>
                  </el-table-column>
                  <el-table-column prop="score" label="分数">
                    <template #default="{ row }">
                      <el-input-number v-model="row.score"></el-input-number>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作">
                    <template #default="{ $index }">
                      <el-button type="danger" @click="removeEnemyRule($index)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>

        <div class="action-buttons">
          <el-button v-if="activeStep > 0" @click="prevStep">上一步</el-button>
          <el-button v-if="activeStep < 2" type="primary" @click="nextStep">下一步</el-button>
          <el-button v-if="activeStep === 2" type="success" @click="submitForm" :loading="loading">开始查找</el-button>
        </div>

        <!-- 结果展示 -->
        <div v-if="resultData" class="result-container">
          <h3>查找结果 (耗时: {{ resultData.duration }}ms)</h3>
          <el-table :data="resultData.data" border style="width: 100%">
            <el-table-column prop="attempt" label="尝试次数" width="100"></el-table-column>
            <el-table-column label="种子">
              <template #default="{ row }">
                <div>种子1: {{ row.seed1.join(', ') }}</div>
                <div>种子2: {{ row.seed2.join(', ') }}</div>
                <div>种子3: {{ row.seed3.join(', ') }}</div>
              </template>
            </el-table-column>
            <el-table-column label="敌人列表">
              <template #default="{ row }">
                <div v-for="(enemy, index) in row.enemies" :key="index">
                  {{ enemyL10n[enemy.enemyId] || enemy.enemyId }} -
                  属性: {{ formatAttrs(enemy.attr) }} -
                  {{ enemy.overdrive ? '污染体' : '普通' }} -
                  分数: {{ enemy.score }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="score" label="总分" width="100"></el-table-column>
          </el-table>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import { getPath, searchSeeds } from './api/index.js'

export default {
  name: 'App',
  data() {
    return {
      activeStep: 0,
      activeRuleTab: 'seed',
      selectedFile: null,
      loading: false,
      resultData: null,
      enemyL10n: {
        'SLIME': '史莱姆',
        'MUSH': '蘑菇',
        'MAGE': '愚者',
        'PUPPY': '幼犬',
        'GOLEM': '木偶',
        'GOLEM_OD': '巨人',
        'GOLEM_OD2': '巨人',
        'FOX': '妖狐',
        'UNI': '剑山',
        'SNAKE': '土蛇',
        'SPONGE': '海绵',
        'GECKO': '壁虎',
        'FROG': '沼蛙',
        'BOSS_NUSI': '森之领主',
        'MKB': '三角木马',
        'MECHGOLEM': '机甲木偶',
      },
      attrs: {
        '攻': 1,
        '防': 2,
        '稳': 4,
        '火': 256,
        '冰': 512,
        '雷': 1024,
        '粘': 2048,
        '毒': 4096,
        '隐': 65536,
      },
      config: {
        mode: 1,
        threads: 4,
        windowSize: 4,
        chunkBuffer: 8,
        buffer: 64,
        size: 20
      },
      rules: {
        seed: [],
        enemy: []
      }
    }
  },
  methods: {
    nextStep() {
      if (this.activeStep < 2) {
        this.activeStep++
      }
    },
    prevStep() {
      if (this.activeStep > 0) {
        this.activeStep--
      }
    },
    async getClipboardPath() {
      try {
        const res = await getPath()
        if (res.success) {
          this.selectedFile = res.path
        } else {
          ElMessage.error(res.msg || '获取文件路径失败')
        }
      } catch (error) {
        ElMessage.error('请求失败: ' + error.message)
      }
    },
    clearSelectedFile() {
      this.selectedFile = null
    },
    addSeedRule() {
      this.rules.seed.push({
        enemyId: '',
        operator: 'gt',
        value: 1,
        score: -60
      })
    },
    removeSeedRule(index) {
      this.rules.seed.splice(index, 1)
    },
    addEnemyRule() {
      this.rules.enemy.push({
        enemyId: '',
        isOverride: false,
        attrs: [],
        order: 0,
        score: -20
      })
    },
    removeEnemyRule(index) {
      this.rules.enemy.splice(index, 1)
    },
    formatAttrs(attrValue) {
      if (!attrValue) return '无'
      const activeAttrs = []
      for (const [name, value] of Object.entries(this.attrs)) {
        if (attrValue & value) {
          activeAttrs.push(name)
        }
      }
      return activeAttrs.length ? activeAttrs.join(', ') : '无'
    },
    async submitForm() {
      if (!this.selectedFile) {
        ElMessage.error('请先选择文件')
        return
      }

      const payload = {
        type: 'local',
        path: this.selectedFile,
        conf: { ...this.config },
        size: this.config.size,
        cond: {
          seed: this.rules.seed,
          enemy: this.rules.enemy
        }
      }

      // 清理不需要的配置项
      if (payload.conf.mode === 1) {
        delete payload.conf.threads
        delete payload.conf.windowSize
        delete payload.conf.chunkBuffer
      }

      try {
        this.loading = true
        const res = await searchSeeds(payload)
        if (res.success) {
          this.resultData = res
          ElMessage.success(res.msg || '查找成功')
        } else {
          ElMessage.error(res.msg || '查找失败')
        }
      } catch (error) {
        ElMessage.error('请求失败: ' + error.message)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

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