import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import App from './App.vue'

import 'element-plus/dist/index.css'

import 'element-plus/theme-chalk/dark/css-vars.css'
import '@/styles/element-plus-theme/index.scss';

import '@/assets/font.css'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
  }

app.use(ElementPlus, {
    locale: zhCn
})

app.mount('#app')