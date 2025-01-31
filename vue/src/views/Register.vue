<template>
  <div class="login-container">
    <div class="form-container">
      <div class="form-title">欢迎注册六路车站</div>
      <el-form :model="user" :rules="rules" ref="registerRef" class="form">
        <el-form-item prop="username" class="form-item">
          <el-input prefix-icon="el-icon-user" size="medium" v-model="user.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item prop="password" class="form-item">
          <el-input prefix-icon="el-icon-lock" size="medium" v-model="user.password" show-password placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item prop="confirmPass" class="form-item">
          <el-input prefix-icon="el-icon-lock" size="medium" v-model="user.confirmPass" show-password placeholder="请确认密码"></el-input>
        </el-form-item>
        <el-form-item prop="phone" class="form-item">
          <el-input prefix-icon="el-icon-mobile-phone" size="medium" v-model="user.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item prop="email" class="form-item">
          <el-input prefix-icon="el-icon-message" size="medium" v-model="user.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item prop="emailCode" class="form-item">
          <div class="verify-code">
            <el-input prefix-icon="el-icon-circle-check" size="medium" v-model="user.emailCode" placeholder="请输入邮箱验证码" style="flex: 1;"></el-input>
            <el-button 
              type="primary" 
              :disabled="!!timer || !user.email" 
              @click="sendEmailCode" 
              style="margin-left: 10px; width: 120px">
              {{ timer ? `${countdown}s后重试` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item class="form-item">
          <el-button type="primary" @click="register" class="register-button">注 册</el-button>
        </el-form-item>
        <div class="login-link">已有账号？<el-link type="primary" @click="$router.push('/login')">立即登录</el-link></div>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data() {
    // 验证密码是否一致
    const validatePassword = (rule, confirmPass, callback) => {
      if (confirmPass === '') {
        callback(new Error('请确认密码'))
      } else if (confirmPass !== this.user.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    // 验证手机号
    const validatePhone = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入手机号'))
      } else if (!/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error('请输入正确的手机号'))
      } else {
        callback()
      }
    }
    // 验证邮箱
    const validateEmail = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入邮箱'))
      } else if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
        callback(new Error('请输入正确的邮箱格式'))
      } else {
        callback()
      }
    }
    return {
      timer: null,
      countdown: 60,
      user: {
        username: '',
        password: '',
        confirmPass: '',
        phone: '',
        email: '',
        emailCode: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 2, max: 10, message: '用户名长度在2-10个字符之间', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
        ],
        confirmPass: [{ validator: validatePassword, trigger: 'blur' }],
        phone: [{ validator: validatePhone, trigger: 'blur' }],
        email: [{ validator: validateEmail, trigger: 'blur' }],
        emailCode: [
          { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
          { len: 6, message: '验证码长度应为6位', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    // 发送邮箱验证码
    async sendEmailCode() {
      try {
        // 先验证邮箱格式
        await this.$refs.registerRef.validateField('email')
        
        const res = await this.$request.post('/sendEmailCode', {
          email: this.user.email
        })
        
        if (res.code === '200') {
          this.$message.success('验证码已发送，请查收邮件')
          // 开始倒计时
          this.countdown = 60
          this.timer = setInterval(() => {
            if (this.countdown > 0) {
              this.countdown--
            } else {
              clearInterval(this.timer)
              this.timer = null
            }
          }, 1000)
        }
      } catch (error) {
        console.debug('发送验证码失败')
      }
    },
    
    register() {
      this.$refs['registerRef'].validate(async (valid) => {
        if (valid) {
          const { username, password, phone, email, emailCode } = this.user
          try {
            const res = await this.$request.post('/register', { 
              username, 
              password, 
              phone,
              email,
              emailCode
            })
            if (res.code === '200') {
              this.$message.success('注册成功')
              this.$router.push('/login')
            }
          } catch (error) {
            console.debug('注册请求完成')
          }
        }
      })
    }
  },
  beforeDestroy() {
    // 组件销毁前清除定时器
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
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

.form-item {
  margin-bottom: 20px;
}

.register-button {
  width: 100%;
}

.login-link {
  text-align: center;
  margin-top: 10px;
  color: #606266;
}

.verify-code {
  display: flex;
  align-items: center;
}
</style>
