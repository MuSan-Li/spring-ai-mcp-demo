<template>
  <el-container>
    <el-header>
      <el-menu
        mode="horizontal"
        :default-active="activeMenu"
        router
        class="header-menu"
      >
        <el-menu-item index="/tools">
          <el-icon><Tools /></el-icon>
          <span>工具管理</span>
        </el-menu-item>
        <el-menu-item index="/markets">
          <el-icon><Shop /></el-icon>
          <span>市场管理</span>
        </el-menu-item>
        <el-menu-item index="/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI 聊天</span>
        </el-menu-item>
      </el-menu>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Tools, Shop, ChatDotRound } from '@element-plus/icons-vue'

const route = useRoute()

const activeMenu = computed(() => {
  return route.path.startsWith('/tools') ? '/tools' :
         route.path.startsWith('/markets') ? '/markets' :
         route.path.startsWith('/chat') ? '/chat' : '/tools'
})
</script>

<style scoped>
.header-menu {
  border-bottom: 1px solid #e6e6e6;
}

.el-header {
  padding: 0;
  height: 60px;
  line-height: 60px;
}

.el-main {
  padding: 0;
  height: calc(100vh - 60px);
  overflow: hidden;
  }
</style>

<style>
.el-container {
  height: 100vh;
}
</style>
