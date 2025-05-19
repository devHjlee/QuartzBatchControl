<!-- src/App.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import Sidebar from '@/components/layout/Sidebar.vue'
import Navbar from '@/components/layout/Navbar.vue'
import Footer from '@/components/layout/Footer.vue'
import LogoutModal from '@/components/layout/LogoutModal.vue'

const isAuthenticated = computed(() => useAuthStore().isLoggedIn)
</script>

<template>
  <div id="wrapper">
    <Sidebar v-if="isAuthenticated" />

    <div id="content-wrapper" class="d-flex flex-column">
      <div id="content">
        <Navbar v-if="isAuthenticated" />
        <router-view />
      </div>
      <Footer v-if="isAuthenticated" />
    </div>

    <LogoutModal />
  </div>
</template>

<style scoped></style>
