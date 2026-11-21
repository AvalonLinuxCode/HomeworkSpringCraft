<template>
  <div class="front-shell">
    <header class="front-header">
      <router-link to="/" class="logo">在线教育平台</router-link>
      <nav class="nav">
        <router-link to="/" exact-active-class="active">首页</router-link>
        <router-link to="/courses" active-class="active">课程</router-link>
        <router-link to="/instructors" active-class="active">讲师</router-link>
        <router-link to="/admin" class="admin-link">后台管理</router-link>
      </nav>
    </header>
    <main class="front-main">
      <router-view />
    </main>
    <footer class="front-footer">© 2026 在线教育平台 · 软件工程课程实验</footer>
  </div>
</template>

<style scoped>
.front-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.front-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 32px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}
.logo {
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
}
.nav {
  display: flex;
  gap: 24px;
  font-size: 14px;
}
.nav a {
  color: #606266;
}
.nav a.active,
.nav a:hover {
  color: #409eff;
}
.admin-link {
  color: #909399 !important;
}
.front-main {
  flex: 1;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 32px;
}
.front-footer {
  text-align: center;
  padding: 16px;
  color: #909399;
  font-size: 12px;
  border-top: 1px solid #e4e7ed;
  background: #fff;
}
</style>