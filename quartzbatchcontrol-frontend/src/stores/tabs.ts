import { defineStore } from 'pinia'
import router from '@/router' // 라우터 인스턴스 import

export interface Tab {
  id: string // 일반적으로 route.fullPath 또는 route.name 사용
  name: string // 탭에 표시될 이름 (route.meta.title 또는 route.name)
  path: string // 탭 클릭 시 이동할 경로 (route.fullPath)
  closable: boolean // 탭을 닫을 수 있는지 여부 (route.meta.closable)
}

interface TabsState {
  openedTabs: Tab[]
  activeTabId: string | null
}

export const useTabsStore = defineStore('tabs', {
  state: (): TabsState => ({
    openedTabs: [],
    activeTabId: null,
  }),
  actions: {
    // 탭 추가 또는 활성화
    addTab(route: any) { // route 타입은 Vue Router의 RouteLocationNormalizedLoaded
      if (!route.meta || !route.meta.title) { // 탭으로 관리하지 않을 라우트 (예: 로그인 페이지)
        // console.log('Skipping tab for route without meta.title:', route.name);
        return;
      }

      const existingTab = this.openedTabs.find(tab => tab.path === route.fullPath);
      if (existingTab) {
        this.setActiveTab(existingTab.id);
      } else {
        const newTab: Tab = {
          id: route.fullPath,
          name: route.name || route.meta.title || 'New Tab',
          path: route.fullPath,
          closable: route.meta.closable !== undefined ? route.meta.closable : true,
        };
        this.openedTabs.push(newTab);
        this.setActiveTab(newTab.id);
      }
    },
    // 탭 제거
    removeTab(tabId: string) {
      const tabIndex = this.openedTabs.findIndex(tab => tab.id === tabId);
      if (tabIndex === -1) return;

      const removedTab = this.openedTabs.splice(tabIndex, 1)[0];
      if (removedTab.id === this.activeTabId) {
        if (this.openedTabs.length > 0) {
          // 닫힌 탭이 활성 탭이었으면, 이전 탭 또는 첫 번째 탭을 활성화
          const newActiveTab = this.openedTabs[Math.max(0, tabIndex - 1)] || this.openedTabs[0];
          this.setActiveTab(newActiveTab.id);
          router.push(newActiveTab.path);
        } else {
          this.activeTabId = null;
          // 모든 탭이 닫혔을 때 기본 페이지로 이동 (예: 대시보드)
          // router.push('/dashboard'); // 이 부분은 프로젝트 정책에 따라 결정
        }
      }
    },
    // 특정 탭 활성화
    setActiveTab(tabId: string | null) {
      this.activeTabId = tabId;
      // 활성화된 탭으로 라우터 이동은 탭 클릭 시 또는 removeTab에서 처리
    },
    // 외부에서 활성 탭으로 라우터 강제 이동이 필요할 때 사용 (옵션)
    navigateToActiveTab() {
      const activeTab = this.openedTabs.find(tab => tab.id === this.activeTabId);
      if (activeTab) {
        router.push(activeTab.path);
      }
    }
  },
  // Pinia Persist 플러그인 사용 시 (선택 사항)
  // persist: true, // 또는 persist: { storage: sessionStorage } 등
}) 