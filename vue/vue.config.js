const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8090,
    proxy: {
      '/api': {
        target: 'http://localhost:9090',
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        }
      },
      '/uploads': {
        target: 'http://localhost:9090',
        changeOrigin: true,
        pathRewrite: {
          '^/uploads': '/static/uploads'
        }
      }
    }
  }
})