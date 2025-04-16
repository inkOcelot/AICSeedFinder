<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import axios from "axios";
import { successMsg, warnMsg, errorMsg } from "./utils/msg";
import { useWindowSize } from "@vueuse/core";

// 常量
const enemyIdL10n = {
  SLIME: "史莱姆",
  MUSH: "蘑菇",
  MAGE: "愚者",
  PUPPY: "幼犬",
  GOLEM: "木偶",
  GOLEM_OD: "巨人(OD)",
  GOLEM_OD2: "巨人(OD2)",
  FOX: "妖狐",
  UNI: "剑山",
  SNAKE: "土蛇",
  SPONGE: "海绵",
  GECKO: "壁虎",
  FROG: "沼蛙",
  BOSS_NUSI: "森之领主",
  MKB: "三角木马",
  MECHGOLEM: "机甲木偶",
};

const enemyIdMap = [
  { label: "史莱姆", value: "SLIME" },
  { label: "蘑菇", value: "MUSH" },
  { label: "愚者", value: "MAGE" },
  { label: "幼犬", value: "PUPPY" },
  { label: "木偶", value: "GOLEM" },
  { label: "巨人(OD)", value: "GOLEM_OD" },
  { label: "巨人(OD2)", value: "GOLEM_OD2" },
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
  { label: "攻", value: 1 },
  { label: "防", value: 2 },
  { label: "稳", value: 4 },
  { label: "火", value: 256 },
  { label: "冰", value: 512 },
  { label: "雷", value: 1024 },
  { label: "粘", value: 2048 },
  { label: "毒", value: 4096 },
  { label: "隐", value: 65536 },
];

const attrsMapWithColor = [
  { label: "<span style='color: #FF7D7D'>攻</span>", value: 1 }, // 柔和红
  { label: "<span style='color: #7DA7FF'>防</span>", value: 2 }, // 柔和蓝
  { label: "<span style='color: #B0B0B0'>稳</span>", value: 4 }, // 浅灰
  { label: "<span style='color: #FF9E80'>火</span>", value: 256 }, // 柔和橙红
  { label: "<span style='color: #81D4FA'>冰</span>", value: 512 }, // 柔和冰蓝
  { label: "<span style='color: #FFF176'>雷</span>", value: 1024 }, // 柔和黄
  { label: "<span style='color: #AED581'>粘</span>", value: 2048 }, // 柔和绿
  { label: "<span style='color: #CE93D8'>毒</span>", value: 4096 }, // 柔和紫
  { label: "<span style='color: #78909C'>隐</span>", value: 65536 }, // 柔和灰蓝
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
const port = ref(2641);

// 单线程
const buffer = ref(8);

// 多线程
const threads = ref(8);
const windowSize = ref(4);
const chunkBuffer = ref(8);
const multBuffer = ref(8);

// 堆大小
const size = ref(20);

// 保存参数配置
const saveConfig = () => {
  const config = {
    baseURL: baseURL.value,
    port: port.value,
    path: path.value,
    mode: mode.value,
    buffer: buffer.value,
    threads: threads.value,
    windowSize: windowSize.value,
    chunkBuffer: chunkBuffer.value,
    multBuffer: multBuffer.value,
    size: size.value,
    seedRules: seedRuleData.value,
    enemyRules: enemyRuleData.value,
  };
  localStorage.setItem("config", JSON.stringify(config));
  successMsg("配置参数已保存成功");
};

const loadConfig = () => {
  const config = JSON.parse(localStorage.getItem("config"));
  if (config === null) {
    return false;
  } else {
    baseURL.value = config.baseURL;
    port.value = config.port;
    path.value = config.path;
    mode.value = config.mode;
    buffer.value = config.buffer;
    threads.value = config.threads;
    windowSize.value = config.windowSize;
    chunkBuffer.value = config.chunkBuffer;
    multBuffer.value = config.multBuffer;
    size.value = config.size;
    seedRuleData.value = config.seedRules;
    enemyRuleData.value = config.enemyRules;
    successMsg("已从存储中加载参数配置");
    return true;
  }
};

const manualLoadConfig = () => {
  const flag = loadConfig();
  if (!flag) {
    warnMsg("存储中没有参数配置");
    return;
  }
};

const deleteConfig = () => {
  try {
    localStorage.removeItem("config");
    successMsg("参数配置已删除");
  } catch (err) {
    warnMsg("存储空间中不存在参数配置");
  }
};

// 后端连接
const apiClient = () =>
  axios.create({
    baseURL: `http://${baseURL.value}:${port.value}`,
    timeout: 0,
    headers: { "Content-Type": "application/json" },
  });

const getPath = () => {
  return apiClient().get("/path");
};

const searchSeeds = (data) => {
  return apiClient().post("/seedfinder", data);
};

// 文件路径
const path = ref("");

const getClipboradPath = async () => {
  try {
    const res = await getPath();
    if (!res.data.success) {
      warnMsg(res.data.msg);
      return;
    }
    path.value = res.data.data.path;
    successMsg("文件路径获取成功");
  } catch (err) {
    errorMsg(err);
  }
};

// 规则
const seedRuleData = ref([]);

const addSeedRule = () => {
  seedRuleData.value.push({
    enemy: {
      enemyId: null,
      isOverdrive: null,
      attrs: [],
    },
    operator: "eq",
    value: 0,
    score: 0,
  });
};

const deleteSeedRule = (index) => {
  seedRuleData.value.splice(index, 1);
};

const clearSeedRule = () => {
  seedRuleData.value = [];
};

const enemyRuleData = ref([]);

const addEnemyRule = () => {
  enemyRuleData.value.push({
    enemyId: null,
    isOverdrive: null,
    attrs: [],
    order: null,
    score: 0,
  });
};

const deleteEnemyRule = (index) => {
  enemyRuleData.value.splice(index, 1);
};

const clearEnemyRule = () => {
  enemyRuleData.value = [];
};

// 保存配置
const configName = ref("");
const ruleConfig = ref([]);
const chooseConfigName = ref("");

const saveRuleConfig = () => {
  if (configName.value === "") {
    warnMsg("请输入配置名");
    return;
  }
  if (seedRuleData.value.length === 0 && enemyRuleData.value.length === 0) {
    warnMsg("存储的配置为空");
    return;
  }
  ruleConfig.value.push({
    name: configName.value,
    cond: {
      seed: seedRuleData.value,
      enemy: enemyRuleData.value,
    },
  });
  localStorage.setItem("ruleConfig", JSON.stringify(ruleConfig.value));
  successMsg("配置保存成功");
};

const updateRuleConfig = () => {
  try {
    const result = JSON.parse(localStorage.getItem("ruleConfig"));
    if (result === null) {
      ruleConfig.value = [];
    } else {
      ruleConfig.value = result;
    }
  } catch (err) {
    localStorage.setItem("ruleConfig", JSON.stringify([]));
    ruleConfig.value = [];
  }
};

const deleteRuleConfig = () => {
  if (chooseConfigName.value === "") {
    warnMsg("选择的配置名为空");
    return;
  }
  ruleConfig.value = ruleConfig.value.filter(
    (rule) => rule.name != chooseConfigName.value
  );
  localStorage.setItem("ruleConfig", JSON.stringify(ruleConfig.value));
  chooseConfigName.value = "";

  successMsg("配置删除成功");
};

const loadRuleConfig = () => {
  if (chooseConfigName.value === "") {
    warnMsg("选择的配置名为空");
    return;
  }
  const loadConfig = ruleConfig.value.filter(
    (rule) => rule.name === chooseConfigName.value
  )[0];
  seedRuleData.value = loadConfig.cond.seed;
  enemyRuleData.value = enemyRuleData.cond.enemy;

  successMsg("配置已加载");
};

// 获取结果数据
const resultData = ref([]);

const formatMilliseconds = (ms) => {
  if (Math.abs(ms) < 1000) {
    return `${ms}毫秒`;
  }

  let seconds = ms / 1000;
  if (seconds < 60) {
    const wholeSeconds = Math.floor(seconds);
    const fraction = (seconds - wholeSeconds).toFixed(3).substring(2);
    return `${wholeSeconds}.${fraction}秒`;
  }

  let minutes = seconds / 60;
  if (minutes < 60) {
    const wholeMinutes = Math.floor(minutes);
    const remainingSeconds = (minutes - wholeMinutes) * 60;
    return `${wholeMinutes}分钟${remainingSeconds.toFixed(2)}秒`;
  }

  let hours = minutes / 60;
  if (hours < 24) {
    const wholeHours = Math.floor(hours);
    const remainingMinutes = (hours - wholeHours) * 60;
    return `${wholeHours}小时${remainingMinutes.toFixed(1)}分钟`;
  }

  const days = hours / 24;
  const wholeDays = Math.floor(days);
  const remainingHours = (days - wholeDays) * 24;
  return `${wholeDays}天${remainingHours.toFixed(1)}小时`;
};

const getResult = async () => {
  // 填文件名
  if (path.value === "") {
    warnMsg("文件路径为空");
    return;
  }

  if (!path.value.toLowerCase().endsWith(".aicseed")) {
    warnMsg("文件后缀错误");
    return;
  }

  let conf;
  if (mode.value === "1") {
    conf = {
      mode: 1,
      buffer: buffer.value,
    };
  } else if (mode.value === "2") {
    conf = {
      mode: 2,
      threads: threads.value,
      windowSize: windowSize.value,
      chunkBuffer: chunkBuffer.value,
      buffer: multBuffer.value,
    };
  }

  const req = {
    type: "local",
    path: path.value,
    conf: conf,
    size: size.value,
    cond: {
      seed: seedRuleData.value,
      enemy: enemyRuleData.value,
    },
  };

  try {
    const res = await searchSeeds(req);

    if (!res.data.success) {
      warnMsg(res.data.msg);
      return;
    }

    const result = res.data.data;

    // 替换为更友好的显示
    result.forEach((seed) => {
      seed.enemies.forEach((enemy) => {
        enemy.enemyId = enemyIdL10n[enemy.enemyId];

        if (enemy.overdrive) {
          enemy.overdrive = "<strong>是</strong>";
        } else {
          enemy.overdrive = "<span style='color: #808080'>否</span>";
        }

        const attr = enemy.attr;
        let attrs = [];
        attrsMapWithColor.forEach((map) => {
          if (attr & map.value) attrs.push(map.label);
        });

        if (attrs.length === 0) {
          enemy.attr = "<span style='color: #808080'>无</span>";
        } else {
          enemy.attr = attrs.join(", ");
        }
      });
    });

    resultData.value = result;

    successMsg(
      `种子数据已分析完毕, 耗时${formatMilliseconds(res.data.duration)}`
    );
  } catch (err) {
    errorMsg(err);
  }
};

// 关于界面
const aboutVisible = ref(false);

// 表格最大高度动态变化
const viewportHeight = useWindowSize().height;

const ruleTableHeight = ref(300);
const resultTableHeight = ref(300);

const updateTableHeight = () => {
  ruleTableHeight.value = viewportHeight.value - 380;
  resultTableHeight.value = viewportHeight.value - 280;
};

onMounted(() => {
  updateRuleConfig();
  loadConfig();
  updateTableHeight();
  window.addEventListener("resize", updateTableHeight);
});
</script>

<template>
  <div id="aic-seed-finder-container">
    <el-dialog v-model="aboutVisible" title="关于" width="500">
      <div class="row">
        <span class="row horiz f1">
          <el-avatar :size="100" src="./src/assets/avatar.png" />
        </span>
      </div>
      <div class="rowMin">
        <span class="rowMin horiz f1">
          <span>Alice In Cradle 种子查找器</span>
        </span>
      </div>
      <div class="rowMin">
        <span class="rowMin horiz f1">
          <span>版本: v1.0 (20250416)</span>
        </span>
      </div>
      <div class="row">
        <span class="rowMin horiz f1">
          <span>作者: InkOcelot</span>
        </span>
      </div>
      <div class="row">
        <span class="rowMin horiz f1">
          <span>项目地址: </span>
          <el-link
            href="https://github.com/inkOcelot/AICSeedFinder"
            target="_blank"
            >GitHub</el-link
          >
          <el-link
            href="https://gitee.com/inkocelot/aicseed-finder"
            target="_blank"
            >Gitee</el-link
          >
        </span>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="aboutVisible = false">好</el-button>
        </div>
      </template>
    </el-dialog>
    <el-container>
      <el-header height="80px">
        <!-- Header content -->
        <h1>Alice In Cradle 种子查找器</h1>
        <div class="fixedToRight">
          <el-icon
            :size="20"
            @click="aboutVisible = true"
            style="cursor: pointer"
            ><InfoFilled
          /></el-icon>
        </div>
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
              /></span>
              <span class="f1">
                <el-button @click="saveConfig" style="width: 100%"
                  >保存参数配置</el-button
                >
              </span>
              <span class="f1">
                <el-button @click="manualLoadConfig" style="width: 100%"
                  >加载参数配置</el-button
                >
              </span>
              <span class="f1">
                <el-button @click="deleteConfig" style="width: 100%"
                  >删除参数配置</el-button
                >
              </span>
            </div>
            <div class="row">
              <span class="f1"
                ><el-button @click="getClipboradPath" style="width: 100%"
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
              <el-tabs
                v-model="mode"
                type="border-card"
                style="border-radius: 10px"
              >
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
            <!-- 保存配置 -->
            <div class="row">
              <span class="f1">
                <el-text>配置名</el-text>
              </span>
              <span class="f3">
                <el-input v-model="configName" placeholder="请输入配置名" />
              </span>
              <span class="f3">
                <el-select
                  v-model="chooseConfigName"
                  placeholder="请选择配置"
                  @focus="updateRuleConfig"
                >
                  <el-option
                    v-for="conf in ruleConfig"
                    :label="conf.name"
                    :value="conf.name"
                  />
                </el-select>
              </span>
              <span class="f1">
                <el-button @click="saveRuleConfig">保存配置</el-button>
              </span>
              <span class="f1">
                <el-button @click="deleteRuleConfig">删除配置</el-button>
              </span>
              <span class="f1">
                <el-button @click="loadRuleConfig">加载配置</el-button>
              </span>
            </div>
            <el-tabs v-model="ruleTab" type="card">
              <el-tab-pane label="种子规则" name="seed">
                <!-- 种子规则 -->
                <div class="row">
                  <span class="f1">
                    <el-button style="width: 100%" @click="addSeedRule">
                      添加规则
                    </el-button>
                  </span>
                  <span class="f1">
                    <el-button style="width: 100%" @click="clearSeedRule">
                      清空规则
                    </el-button>
                  </span>
                  <span class="f4"></span>
                </div>

                <el-table
                  :data="seedRuleData"
                  style="width: 100%"
                  :max-height="ruleTableHeight + 'px'"
                  border
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
                  <el-table-column
                    prop="isOverdrive"
                    label="污染体"
                    width="120"
                  >
                    <template #default="{ row }">
                      <el-select
                        v-model="row.enemy.isOverdrive"
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
                        placeholder="无限制"
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
                  <el-table-column prop="value" label="值" width="120">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="row.value"
                        controls-position="right"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column prop="score" label="得分" width="120">
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
              </el-tab-pane>
              <el-tab-pane label="怪物规则" name="enemy">
                <!-- 怪物规则 -->
                <div class="row">
                  <span class="f1">
                    <el-button style="width: 100%" @click="addEnemyRule">
                      添加规则
                    </el-button>
                  </span>
                  <span class="f1">
                    <el-button style="width: 100%" @click="clearEnemyRule">
                      清空规则
                    </el-button>
                  </span>
                  <span class="f4"></span>
                </div>

                <el-table
                  :data="enemyRuleData"
                  style="width: 100%"
                  :max-height="ruleTableHeight + 'px'"
                  border
                >
                  <el-table-column
                    fixed
                    prop="enemyId"
                    label="怪物ID"
                    width="120"
                  >
                    <template #default="{ row }">
                      <el-select v-model="row.enemyId" placeholder="无限制">
                        <el-option label="无限制" :value="null"></el-option>
                        <el-option
                          v-for="entry in enemyIdMap"
                          :label="entry.label"
                          :value="entry.value"
                        ></el-option>
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column
                    prop="isOverdrive"
                    label="污染体"
                    width="120"
                  >
                    <template #default="{ row }">
                      <el-select v-model="row.isOverdrive" placeholder="无限制">
                        <el-option label="无限制" :value="null"></el-option>
                        <el-option label="是" :value="true"></el-option>
                        <el-option label="否" :value="false"></el-option>
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column prop="attrs" label="属性" width="300">
                    <template #default="{ row }">
                      <el-select
                        v-model="row.attrs"
                        multiple
                        style="width: 100%"
                        placeholder="无限制"
                      >
                        <el-option
                          v-for="attr in attrsMap"
                          :label="attr.label"
                          :value="attr.value"
                        />
                      </el-select>
                    </template>
                  </el-table-column>
                  <el-table-column prop="order" label="序号" width="120">
                    <template #default="{ row }">
                      <el-input-number
                        v-model="row.order"
                        controls-position="right"
                        placeholder="无限制"
                        :min="1"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column prop="score" label="得分" width="120">
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
                        @click.prevent="deleteEnemyRule(row.$index)"
                      >
                        删除
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
            </el-tabs>
          </el-tab-pane>
          <el-tab-pane label="提交 & 结果显示" name="submit">
            <div class="row">
              <span class="f1">
                <el-button style="width: 20%" @click="getResult">
                  提交数据
                </el-button>
              </span>
            </div>
            <!-- 数据展示 -->
            <div class="row">
              <el-table
                :data="resultData"
                style="width: 100%; overflow: auto"
                :max-height="resultTableHeight + 'px'"
                border
              >
                <el-table-column type="expand" width="80px">
                  <template #default="props" style="width: 80%">
                    <div class="inner-table">
                      <p>种子1: {{ props.row.seed1.join(", ") }}</p>
                      <p>种子2: {{ props.row.seed2.join(", ") }}</p>
                      <p>种子3: {{ props.row.seed3.join(", ") }}</p>
                      <h3>怪物列表</h3>
                      <el-table :data="props.row.enemies" :border="true">
                        <el-table-column
                          fixed
                          label="#"
                          prop="order"
                          width="80px"
                        />
                        <el-table-column
                          label="怪物ID"
                          prop="enemyId"
                          width="120px"
                        />
                        <el-table-column label="污染体" width="80px">
                          <template #default="props">
                            <div v-html="props.row.overdrive"></div>
                          </template>
                        </el-table-column>
                        <el-table-column label="属性">
                          <template #default="props">
                            <div v-html="props.row.attr"></div>
                          </template>
                        </el-table-column>
                        <el-table-column
                          fixed="right"
                          label="得分"
                          prop="score"
                          width="100px"
                        />
                      </el-table>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="#" prop="attempt" width="150px" />
                <el-table-column label="种子" prop="seed2" />
                <el-table-column
                  fixed="right"
                  label="得分"
                  prop="score"
                  width="150px"
                />
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-main>
      <el-footer height="20px">
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

.rowMin {
  display: flex;
  gap: 12px;
}

.horiz {
  justify-content: center;
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

.el-header {
  position: relative;
  margin-bottom: 20px;

  /* 半透明磨砂效果 */
  background-color: rgba(14, 14, 14, 0.7);
  /* 调整透明度，0.7是70%不透明 */
  backdrop-filter: blur(10px);
  /* 磨砂模糊效果，数值越大越模糊 */
  -webkit-backdrop-filter: blur(10px);
  /* 兼容Safari */

  h1 {
    text-align: center;
  }
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
  align-items: center;
  /* 水平居中 */
  padding: 20px 100px;
  /* 上下20px，左右100px */
  margin: 0 auto;
  /* 水平居中 */
  border-radius: 20px;

  /* 半透明磨砂效果 */
  background-color: rgba(14, 14, 14, 0.7);
  /* 调整透明度，0.7是70%不透明 */
  backdrop-filter: blur(10px);
  /* 磨砂模糊效果，数值越大越模糊 */
  -webkit-backdrop-filter: blur(10px);
  /* 兼容Safari */

  /* 可选：添加边框效果增强磨砂质感 */
  border: 1px solid rgba(255, 255, 255, 0.1);

  overflow: hidden;
}

.inner-table {
  width: 80%;
  /* 水平居中 */
  padding: 20px 20px;
  /* 上下20px，左右100px */
  margin: 0 auto;
}

.fixedToRight {
  position: absolute;
  top: 30px;
  right: 30px;
}
</style>
