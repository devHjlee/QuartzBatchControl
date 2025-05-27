<!-- src/App.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useTabsStore } from '@/stores/tabs'
import Sidebar from '@/components/layout/Sidebar.vue'
import Navbar from '@/components/layout/Navbar.vue'
import TabBar from '@/components/layout/TabBar.vue'
import Footer from '@/components/layout/Footer.vue'
import LogoutModal from '@/components/layout/LogoutModal.vue'

const authStore = useAuthStore()
const tabsStore = useTabsStore()

const isAuthenticated = computed(() => authStore.isLoggedIn)
</script>

<template>
  <div id="wrapper">
    <Sidebar v-if="isAuthenticated" />

    <div id="content-wrapper" class="d-flex flex-column">
      <div id="content">
        <Navbar v-if="isAuthenticated" />
        <TabBar v-if="isAuthenticated" />
        <div class="container-fluid mt-3">
          <router-view v-slot="{ Component, route }">
            <keep-alive :include="tabsStore.openedTabs.map(tab => tab.name)">
              <component :is="Component" :key="route.name || route.fullPath" />
            </keep-alive>
          </router-view>
        </div>
      </div>
      <Footer v-if="isAuthenticated" />
    </div>

    <LogoutModal />
  </div>
</template>

<style scoped></style>
