<script setup>
import { ref } from "vue";
import axios from "axios";
import { infoMsg, successMsg, warnMsg, errorMsg } from "./utils/msg";

// 常量
const enemyIdMap = [
  { label: "史莱姆", value: "SLIME" },
  { label: "蘑菇", value: "MUSH" },
  { label: "愚者", value: "MAGE" },
  { label: "幼犬", value: "PUPPY" },
  { label: "木偶", value: "GOLEM" },
  { label: "巨人", value: "GOLEM_OD" },
  { label: "巨人", value: "GOLEM_OD2" },
  { label: "妖狐", value: "FOX" },
  { label: "剑山", value: "UNI" },
  { label: "土蛇", value: "SNAKE" },
  { label: "海绵", value: "SPONGE" },
  { label: "壁虎", value: "GECKO" },
  { label: "沼蛙", value: "FROG" },
  { label: "森之领主", value: "BOSS_NUSI" },
  { label: "三角木马", value: "MKB" },
  { label: "机甲木偶", value: "MECHGOLEM" },
];

const attrsMap = [
  { label: "攻", value: "1" },
  { label: "防", value: "2" },
  { label: "稳", value: "4" },
  { label: "火", value: "256" },
  { label: "冰", value: "512" },
  { label: "雷", value: "1024" },
  { label: "粘", value: "2048" },
  { label: "毒", value: "4096" },
  { label: "隐", value: "65536" },
];

const operatorMap = [
  { label: "=", value: "eq" },
  { label: "≠", value: "neq" },
  { label: "<", value: "lt" },
  { label: "≤", value: "lte" },
  { label: ">", value: "gt" },
  { label: "≥", value: "gte" },
];

// tab标签页
const activeTab = ref("conf");
const mode = ref("1");
const ruleTab = ref("seed");

// 参数配置
const baseURL = ref("localhost");
const port = ref("2641");

// 单线程
const buffer = ref(8);

// 多线程
const threads = ref(8);
const windowSize = ref(4);
const chunkBuffer = ref(8);
const multBuffer = ref(8);

// 堆大小
const size = ref(20);

// 后端连接
const apiClient = () =>
  axios.create({
    baseURL: baseURL.value + ":" + port.value,
    timeout: 0,
    headers: { "Content-Type": "application/json" },
  });

const getPath = () => {
  return apiClient.get("/path");
};

const searchSeeds = (data) => {
  return apiClient.post("/seedfinder", data);
};

// 文件路径
const path = ref("");

// 规则
const seedRuleData = ref([]);

const addSeedRule = () => {
  seedRuleData.value.push({
    enemy: {
      enemyId: null,
      isOverride: null,
      attrs: [],
    },
    operator: "eq",
    value: 0,
    score: 0,
  });
};
</script>

<template>
  <div id="aic-seed-finder-container">
    <el-container :direction="vertical">
      <el-header height="100px">
        <!-- Header content -->
        <h1>Alice In Cradle 种子查找器</h1>
      </el-header>
      <el-main class="centered-main">
        <!-- Main content -->
        <el-tabs v-model="activeTab" class="tabs">
          <el-tab-pane label="参数配置" name="conf">
            <div class="row">
              <span class="f4"
                ><el-input
                  v-model="baseURL"
                  placeholder="请输入后端URL"
                  size="normal"
                  clearable
                  @change=""
                  ><template #prepend>http://</template></el-input
                ></span
              >
              <span class="f1"
                ><el-input-number
                  v-model="port"
                  :min="0"
                  :max="65535"
                  controls-position="right"
                  placeholder="请输入端口号"
                  size="normal"
              /></span>
            </div>
            <div class="row">
              <span class="f1"
                ><el-button @click="infoMsg('这是消息')" style="width: 100%"
                  >获取剪贴板中文件路径</el-button
                ></span
              >
              <span class="f3">
                <el-input
                  v-model="path"
                  placeholder="请输入文件路径"
                  clearable
                  @change=""
                ></el-input>
              </span>
            </div>
            <div class="row">
              <el-tabs v-model="mode" type="card">
                <el-tab-pane label="单线程" name="1">
                  <div class="row">
                    <el-text
                      >通过缓冲流以单线程处理种子文件,
                      适合较小的种子文件</el-text
                    >
                  </div>
                  <div class="row">
                    <span class="f1 row">
                      <span class="f1"><el-text>缓冲区大小</el-text></span>
                      <span class="f1"
                        ><el-input-number
                          v-model="buffer"
                          :min="1"
                          controls-position="right"
                        >
                          <template #suffix>
                            <span>MB</span>
                          </template>
                        </el-input-number></span
                      >
                    </span>
                    <span class="f2"></span>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="多线程" name="2">
                  <div class="row">
                    <el-text>
                      通过文件分区以及内存映射技术，以多线程方式处理种子文件，适合较大的文件
                    </el-text>
                  </div>
                  <div class="row">
                    <span class="f1"><el-text>线程数</el-text></span>
                    <span class="f1"
                      ><el-input-number
                        v-model="threads"
                        :min="1"
                        controls-position="right"
                      >
                      </el-input-number
                    ></span>
                    <span class="f1"><el-text>窗口大小</el-text></span>
                    <span class="f1"
                      ><el-input-number
                        v-model="windowSize"
                        :min="1"
                        controls-position="right"
                      >
                        <template #suffix>
                          <span>MB</span>
                        </template>
                      </el-input-number></span
                    >
                  </div>
                  <div class="row">
                    <span class="f1"><el-text>分区缓冲大小</el-text></span>
                    <span class="f1"
                      ><el-input-number
                        v-model="chunkBuffer"
                        :min="1"
                        controls-position="right"
                      >
                        <template #suffix>
                          <span>MB</span>
                        </template>
                      </el-input-number></span
                    >
                    <span class="f1"><el-text>读取缓冲大小</el-text></span>
                    <span class="f1">
                      <el-input-number
                        v-model="multBuffer"
                        :min="1"
                        controls-position="right"
                      >
                        <template #suffix>
                          <span>MB</span>
                        </template>
                      </el-input-number>
                    </span>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
            <div class="row">
              <span class="f1">
                <el-text>堆大小</el-text>
              </span>
              <span class="f1">
                <el-input-number
                  v-model="size"
                  :min="1"
                  controls-position="right"
                />
              </span>
              <span class="f4"></span>
            </div>
          </el-tab-pane>
          <el-tab-pane label="得分规则" name="cond">
            <el-tabs v-model="ruleTab" type="card">
              <el-tab-pane label="种子规则" name="seed">
                种子规则
                <el-table
                  :data="seedRuleData"
                  style="width: 100%"
                  max-height="100%"
                >
                  <el-table-column
                    fixed
                    prop="enemyId"
                    label="怪物ID"
                    width="120"
                  >
                    <template #default="{ row }">
                      <el-select
                        v-model="row.enemy.enemyId"
                        placeholder="无限制"
                      >
                        <el-option label="无限制" :value="null"></el-option>
                        <el-option
                          v-for="entry in enemyIdMap"
                          :label="entry.label"
                          :value="entry.value"
                        ></el-option>
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column prop="isOverride" label="污染体" width="120">
                    <template #default="{ row }">
                      <el-select
                        v-model="row.enemy.isOverride"
                        placeholder="无限制"
                      >
                        <el-option label="无限制" :value="null"></el-option>
                        <el-option label="是" :value="true"></el-option>
                        <el-option label="否" :value="false"></el-option>
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column prop="attrs" label="属性" width="180">
                    <template #default="{ row }">
                      <el-select
                        v-model="row.enemy.attrs"
                        multiple
                        style="width: 100%"
                      >
                        <el-option
                          v-for="attr in attrsMap"
                          :label="attr.label"
                          :value="attr.value"
                        />
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column prop="operator" label="操作符" width="80">
                    <template #default="{ row }">
                      <el-select v-model="row.operator">
                        <el-option
                          v-for="operator in operatorMap"
                          :label="operator.label"
                          :value="operator.value"
                        />
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column prop="value" label="值" width="80">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="row.value"
                        controls-position="right"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column prop="score" label="得分" width="80">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="row.score"
                        controls-position="right"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column fixed="right" label="操作" min-width="80">
                    <template #default="row">
                      <el-button
                        link
                        type="primary"
                        size="small"
                        @click.prevent="deleteSeedRule(row.$index)"
                      >
                        删除
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-button style="width: 100%" @click="addSeedRule">
                  添加规则
                </el-button>
              </el-tab-pane>
              <el-tab-pane label="怪物规则" name="enemy">怪物规则</el-tab-pane>
            </el-tabs>
          </el-tab-pane>
          <el-tab-pane label="提交 & 结果显示" name="submit"
            >提交 & 结果显示</el-tab-pane
          >
        </el-tabs>
      </el-main>
      <el-footer height="">
        <!-- Footer content -->
      </el-footer>
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

.el-tabs {
  width: 70vw;
}

.el-input-number {
  width: 100%;
}

.row {
  display: flex;
  gap: 12px;
  margin: 10px 0;
}

.f1 {
  display: inline-flex;
  flex: 1;
}

.f2 {
  display: inline-flex;
  flex: 2;
}

.f3 {
  display: inline-flex;
  flex: 3;
}

.f4 {
  display: inline-flex;
  flex: 4;
}

.el-container {
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

.el-tabs {
  height: 100%;
  /* border-radius: 20px; */
  background-color: rgba(0, 0, 0, 0);
  overflow: hidden;
}

.centered-main {
  display: flex;
  flex-direction: column;
  width: 80vw;
  height: 80vh;
  align-items: center; /* 水平居中 */
  padding: 20px 100px; /* 上下20px，左右100px */
  margin: 0 auto; /* 水平居中 */
  border-radius: 20px;

  /* 半透明磨砂效果 */
  background-color: rgba(14, 14, 14, 0.7); /* 调整透明度，0.7是70%不透明 */
  backdrop-filter: blur(10px); /* 磨砂模糊效果，数值越大越模糊 */
  -webkit-backdrop-filter: blur(10px); /* 兼容Safari */

  /* 可选：添加边框效果增强磨砂质感 */
  border: 1px solid rgba(255, 255, 255, 0.1);

  overflow: hidden;
}
</style>
