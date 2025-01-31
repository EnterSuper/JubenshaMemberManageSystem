import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';

import '@/assets/css/global.css'
import '@/assets/css/iconfont/iconfont.css'
// import '@/assets/css/theme/index.css'  // 找不到
import request from '@/utils/request'

Vue.config.productionTip = false
Vue.use(ElementUI,{size:'small' });

Vue.prototype.$request = request;

// 添加全局日志处理器
const originalConsoleLog = console.log;
const originalConsoleError = console.error;
const originalConsoleWarn = console.warn;

console.log = function(...args) {
  originalConsoleLog.apply(console, ['[DEBUG]', new Date().toISOString(), ...args]);
};

console.error = function(...args) {
  originalConsoleError.apply(console, ['[ERROR]', new Date().toISOString(), ...args]);
};

console.warn = function(...args) {
  originalConsoleWarn.apply(console, ['[WARN]', new Date().toISOString(), ...args]);
};

// 添加全局错误处理
Vue.config.errorHandler = function(err, vm, info) {
  console.error('Vue Error:', err);
  console.error('Error Info:', info);
};

// 添加全局警告处理
Vue.config.warnHandler = function(msg, vm, trace) {
  console.warn('Vue Warning:', msg);
  console.warn('Warning Trace:', trace);
};

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
