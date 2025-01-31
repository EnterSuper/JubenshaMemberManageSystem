<template>
  <div class="login-container">
    <div class="form-container">
      <div class="form-title">{{ isAdmin ? '管理员登录' : '会员登录' }}</div>
      <el-form :model="user" :rules="rules" ref="loginRef">
        <el-form-item prop="username">
          <el-input v-model="user.username" :placeholder="isAdmin ? '请输入用户名' : '请输入用户名/手机号'" prefix-icon="el-icon-user">
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
          <div>
            <el-link v-if="!isAdmin" type="primary" @click="$router.push('/register')" style="margin-right: 15px">注册账号</el-link>
            <el-link type="primary" @click="$router.push('/forgot-password')">忘记密码？</el-link>
          </div>
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
      } else if (!this.isAdmin && !(/^1[3-9]\d{9}$/.test(value) || /^[a-zA-Z0-9_]{2,10}$/.test(value))) {
        callback(new Error('请输入正确的用户名或手机号'))
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
      this.$refs['loginRef'].validate(async (valid) => {
        if (valid) {
          const loginData = {
            username: this.user.username,
            password: this.user.password,
            code: this.user.code,
            loginType: this.isAdmin ? 'admin' : 'member',
            accountType: this.isAdmin ? 'username' : (/^1[3-9]\d{9}$/.test(this.user.username) ? 'phone' : 'username')
          }
          
          try {
            const res = await this.$request.post('/login', loginData)
            if (res.code === '200') {
              const userData = res.data
              localStorage.setItem("honey-user", JSON.stringify({
                ...userData,
                token: userData.token || userData.id,
                loginTime: new Date().getTime()
              }))
              
              // 根据用户角色和登录类型决定重定向路径
              const redirectPath = userData.role === 'admin' ? '/admin/home' : '/'
              
              // 检查是否有重定向参数
              const redirect = this.$route.query.redirect
              this.$router.replace(redirect || redirectPath)
              
              this.$message.success('登录成功')
            }
          } catch (error) {
            // 只有在非预期错误时才记录错误
            if (!error.message?.includes('用户名或密码错误')) {
              console.error('登录异常:', error)
            }
          } finally {
            // 无论成功还是失败，都刷新验证码
            this.$refs.validCode.refreshCode()
            this.user.code = ''
          }
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
