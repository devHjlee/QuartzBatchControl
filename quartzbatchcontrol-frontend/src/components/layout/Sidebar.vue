<template>
  <ul class="navbar-nav bg-gradient-primary sidebar sidebar-dark accordion" id="accordionSidebar">
    <!-- Sidebar - Brand -->
    <a class="sidebar-brand d-flex align-items-center justify-content-center" href="#">
      <div class="sidebar-brand-icon rotate-n-15">
        <i class="fas fa-laugh-wink"></i>
      </div>
      <div class="sidebar-brand-text mx-3">Admin</div>
    </a>

    <!-- Divider -->
    <hr class="sidebar-divider my-0" />

    <!-- Nav Item - Dashboard -->
    <li class="nav-item">
      <router-link class="nav-link" to="/dashboard">
        <i class="fas fa-fw fa-tachometer-alt"></i>
        <span>Dashboard</span>
      </router-link>
    </li>

    <!-- Divider -->
    <hr class="sidebar-divider" />

    <!-- Heading -->
    <div class="sidebar-heading">Interface</div>

    <!-- Nav Item - Pages Collapse Menu -->
    <li class="nav-item">
      <a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseBatch" aria-expanded="true" aria-controls="collapseBatch">
        <i class="fas fa-fw fa-cog"></i>
        <span>Batch</span>
      </a>
      <div id="collapseBatch" class="collapse" aria-labelledby="headingTwo" data-parent="#accordionSidebar">
        <div class="bg-white py-2 collapse-inner rounded">
          <h6 class="collapse-header">Custom Components:</h6>
          <router-link class="collapse-item" to="/batch">Batch 관리</router-link>
          <router-link class="collapse-item" to="/batch-log">Batch Log</router-link>
        </div>
      </div>
    </li>

    <li class="nav-item">
      <a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseQuartz" aria-expanded="true" aria-controls="collapseQuartz">
        <i class="fas fa-calendar fa-2x"></i>
        <span>Quartz</span>
      </a>
      <div id="collapseQuartz" class="collapse" aria-labelledby="headingTwo" data-parent="#accordionSidebar">
        <div class="bg-white py-2 collapse-inner rounded">
          <h6 class="collapse-header">Custom Components:</h6>
          <router-link class="collapse-item" to="/job">Quartz 관리</router-link>
          <router-link class="collapse-item" to="/job-log">Quartz Log</router-link>
        </div>
      </div>
    </li>

    <!-- Nav Item - Utilities Collapse Menu -->
<!--    <li class="nav-item">-->
<!--      <a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseUtilities" aria-expanded="true" aria-controls="collapseUtilities">-->
<!--        <i class="fas fa-fw fa-wrench"></i>-->
<!--        <span>Utilities</span>-->
<!--      </a>-->
<!--      <div id="collapseUtilities" class="collapse" aria-labelledby="headingUtilities" data-parent="#accordionSidebar">-->
<!--        <div class="bg-white py-2 collapse-inner rounded">-->
<!--          <h6 class="collapse-header">Custom Utilities:</h6>-->
<!--          <router-link class="collapse-item" to="/utilities-color">Colors</router-link>-->
<!--          <router-link class="collapse-item" to="/utilities-border">Borders</router-link>-->
<!--          <router-link class="collapse-item" to="/utilities-animation">Animations</router-link>-->
<!--          <router-link class="collapse-item" to="/utilities-other">Other</router-link>-->
<!--        </div>-->
<!--      </div>-->
<!--    </li>-->
  </ul>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'

onMounted(() => {
  // Bootstrap의 collapse 기능을 위한 이벤트 리스너 설정
  // const collapseElements = document.querySelectorAll('[data-toggle="collapse"]')
  // collapseElements.forEach(element => {
  //   element.addEventListener('click', (e) => {
  //     e.preventDefault()
  //     const target = element.getAttribute('data-target')
  //     if (target) {
  //       const collapseElement = document.querySelector(target)
  //       if (collapseElement) {
  //         collapseElement.classList.toggle('show')
  //       }
  //     }
  //   })
  // })
})
</script>

<style scoped>
.sidebar {
  width: 14rem !important;
}

.sidebar .nav-item .nav-link {
  padding: 0.75rem 1rem;
  display: block;
  width: 100%;
  text-align: left;
  padding: 1rem;
  width: 14rem;
}

.sidebar .nav-item .nav-link i {
  margin-right: 0.25rem;
}

.sidebar .nav-item .nav-link span {
  font-size: 0.85rem;
  display: inline;
}

.sidebar .nav-item .nav-link[data-toggle="collapse"]::after {
  width: 1rem;
  text-align: center;
  float: right;
  vertical-align: 0;
  border: 0;
  font-weight: 900;
  content: '\f107';
  font-family: 'Font Awesome 5 Free';
}

.sidebar .nav-item .nav-link[data-toggle="collapse"].collapsed::after {
  content: '\f105';
}

.sidebar .sidebar-heading {
  text-align: left;
  padding: 0 1rem;
  font-weight: 800;
  font-size: 0.65rem;
}

.sidebar #sidebarToggle {
  width: 2.5rem;
  height: 2.5rem;
  text-align: center;
  margin-bottom: 1rem;
  cursor: pointer;
}

.sidebar #sidebarToggle::after {
  font-weight: 900;
  content: '\f104';
  font-family: 'Font Awesome 5 Free';
  margin-right: 0.1rem;
}

.sidebar #sidebarToggle:hover {
  width: 2.5rem;
  height: 2.5rem;
  text-align: center;
  margin-bottom: 1rem;
  cursor: pointer;
}

.sidebar.toggled {
  width: 0 !important;
  overflow: hidden;
}
</style>
