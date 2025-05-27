<template>
  <div v-if="tabsStore.openedTabs.length > 0" class="tab-bar-container bg-light border-bottom sticky-top">
    <ul class="nav nav-tabs" role="tablist">
      <li v-for="tab in tabsStore.openedTabs" :key="tab.id" class="nav-item">
        <a
          class="nav-link d-flex align-items-center"
          :class="{ active: tab.id === tabsStore.activeTabId }"
          href="#"
          @click.prevent="handleTabClick(tab)"
          role="tab"
        >
          <span class="tab-label">{{ tab.name }} </span>
          <button
            v-if="tab.closable"
            type="button"
            class="btn border-0 p-0 ms-2 tab-close-icon"
            aria-label="Close"
            @click.stop.prevent="handleTabClose(tab.id)"
          >
            <i class="fas fa-times"></i>
          </button>
        </a>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useTabsStore, type Tab } from '@/stores/tabs'

const tabsStore = useTabsStore()
const router = useRouter()

const handleTabClick = (tab: Tab) => {
  tabsStore.setActiveTab(tab.id)
  router.push(tab.path)
}

const handleTabClose = (tabId: string) => {
  tabsStore.removeTab(tabId)
  // removeTab 액션 내에서 필요한 경우 라우팅 처리
}

</script>

<style scoped>
.tab-bar-container {
  padding-left: 1rem; /* 사이드바 너비 고려 (선택 사항) */
  padding-right: 1rem;
  /* background-color: #f8f9fa; /* 예시 배경색 */
  /* box-shadow: 0 .125rem .25rem rgba(0,0,0,.075); */
  z-index: 1020; /* Navbar보다 낮은 z-index, 필요시 조정 */
}

.nav-tabs {
  border-bottom: none; /* 기본 border 제거 */
  flex-wrap: nowrap; /* 탭이 많아질 경우 가로 스크롤 필요 */
  overflow-x: auto;
  overflow-y: hidden;
  padding-top: 0.5rem;
  padding-bottom: 0.1rem; /* active 탭의 border가 잘리지 않도록 약간의 패딩 */
}

.nav-tabs .nav-item {
  margin-bottom: -1px; /* active 탭의 border와 하단 선이 겹치도록 */
}

.nav-tabs .nav-link {
  color: #495057;
  border: 1px solid transparent;
  border-top-left-radius: .25rem;
  border-top-right-radius: .25rem;
  padding: 0.5rem 0.8rem;
  font-size: 0.9rem;
  white-space: nowrap;
}

.nav-tabs .nav-link.active {
  color: #007bff;
  background-color: #fff; /* 활성 탭 배경색 */
  border-color: #dee2e6 #dee2e6 #fff; /* 위, 좌우 border */
  border-bottom: 2px solid #007bff; /* 활성 탭 하단 강조선 */
}

.nav-tabs .nav-link:hover:not(.active) {
  border-color: #e9ecef #e9ecef #dee2e6;
  background-color: #e9ecef;
}

.btn-close-sm {
  padding: 0.2rem;
  width: 1em;
  height: 1em;
  opacity: 0.5;
}

.nav-link:hover .btn-close-sm,
.nav-link.active .btn-close-sm {
  opacity: 0.8;
}

.btn-close-sm:focus {
  box-shadow: none;
}

.tab-label {
  max-width: 150px; /* 탭 이름 최대 너비 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block; /* ellipsis 적용을 위해 */
  vertical-align: middle;
}

/* 스크롤바 스타일 (선택 사항) */
.nav-tabs::-webkit-scrollbar {
  height: 5px; /* 스크롤바 높이 */
}

.nav-tabs::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.nav-tabs::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 5px;
}

.nav-tabs::-webkit-scrollbar-thumb:hover {
  background: #aaa;
}

.tab-close-icon {
  font-size: 0.8em; /* 아이콘 크기 조정 */
  line-height: 1; /* 아이콘 세로 정렬을 위해 */
  opacity: 0.6;
  transition: opacity 0.15s ease-in-out;
}

.nav-link:hover .tab-close-icon,
.nav-link.active .tab-close-icon {
  opacity: 1;
}

.tab-close-icon:focus {
  outline: none;
  box-shadow: none;
}
</style>
