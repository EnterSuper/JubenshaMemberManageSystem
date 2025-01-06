<template>
  <div class="login-container">
    <div class="form-container">
      <div class="form-title">{{ isAdmin ? '管理员登录' : '会员登录' }}</div>
      <el-form :model="user" :rules="rules" ref="loginRef">
        <el-form-item prop="username">
          <el-input v-model="user.username" :placeholder="isAdmin ? '请输入用户名' : '请输入手机号'" prefix-icon="el-icon-user">
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="user.password" show-password placeholder="请输入密码" prefix-icon="el-icon-lock">
          </el-input>
        </el-form-item>
        <el-form-item prop="code">
          <div class="verify-code">
            <el-input placeholder="请输入验证码" prefix-icon="el-icon-circle-check" size="medium" v-model="user.code" style="flex: 1; margin-right: 10px;"></el-input>
            <div style="flex: 1;">
              <valid-code @update:value="getCode" ref="validCode"></valid-code>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" style="width: 100%" @click="login">登 录</el-button>
        </el-form-item>
        
        <div style="display: flex; justify-content: space-between">
          <el-link type="primary" @click="isAdmin = !isAdmin">
            {{ isAdmin ? '会员登录' : '管理员登录' }}
          </el-link>
          <el-link v-if="!isAdmin" type="primary" @click="$router.push('/register')">注册账号</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  components: {
    ValidCode: () => import('@/components/ValidCode')
  },
  data() {
    const validateUsername = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入账号'))
      } else if (!this.isAdmin && !/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error('请输入正确的手机号'))
      } else {
        callback()
      }
    };
    const validateCode = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入验证码'))
      } else {
        callback()
      }
    };
    return {
      isAdmin: false,
      code: '',
      codeKey: '',
      user: {
        username: '',
        password: '',
        code: '',
        codeKey: ''
      },
      rules: {
        username: [{ validator: validateUsername, trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        code: [{ validator: validateCode, trigger: 'blur' }]
      }
    }
  },
  methods: {
    getCode(code) {
      console.log('收到新的验证码:', code)
      this.user.code = ''
    },
    login() {
      this.$refs['loginRef'].validate((valid) => {
        if (valid) {
          const loginData = {
            username: this.user.username,
            password: this.user.password,
            code: this.user.code,
            loginType: this.isAdmin ? 'admin' : 'member'
          }
          console.log('发送登录请求的数据:', loginData)
          
          this.$request.post('/login', loginData).then(res => {
            if (res.code === '200') {
              const userData = res.data
              localStorage.setItem("honey-user", JSON.stringify(userData))
              
              // 根据用户角色和登录类型决定重定向路径
              const redirectPath = userData.role === 'admin' ? '/admin/home' : '/member/home'
              
              // 检查是否有重定向参数
              const redirect = this.$route.query.redirect
              this.$router.replace(redirect || redirectPath)
              
              this.$message.success('登录成功')
            } else {
              this.$message.error(res.msg)
              this.$refs.validCode.refreshCode()
              this.user.code = ''
            }
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f2f5;
}

.form-container {
  background-color: #fff;
  width: 400px;
  padding: 30px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.form-title {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.verify-code {
  display: flex;
  gap: 10px;
}

.verify-code .el-input {
  flex: 1;
}

.code-area {
  width: 100px;
  height: 40px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
