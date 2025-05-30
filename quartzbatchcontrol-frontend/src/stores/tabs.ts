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
      console.log('[DEBUG tabs.ts] addTab called with route:', JSON.parse(JSON.stringify(route)), 'Time:', new Date().toISOString()); // addTab 호출 로그 추가
      if (!route.meta || !route.meta.title) { // 탭으로 관리하지 않을 라우트 (예: 로그인 페이지)
        // console.log('Skipping tab for route without meta.title:', route.name);
        return;
      }

      const routeName = route.name || route.meta.title;
      const existingTabByNameAndPath = this.openedTabs.find(tab =>
        tab.name === routeName && tab.path.split('?')[0] === route.path.split('?')[0]
      );

      if (existingTabByNameAndPath) {
        // 이름과 기본 경로가 같은 탭이 이미 존재
        if (existingTabByNameAndPath.path === route.fullPath) {
          // fullPath까지 완전히 동일하면 그냥 활성화
          this.setActiveTab(existingTabByNameAndPath.id);
        } else {
          // fullPath가 다르면 (예: 쿼리 변경), 기존 탭의 경로와 ID를 업데이트하고 활성화
          existingTabByNameAndPath.path = route.fullPath;
          existingTabByNameAndPath.id = route.fullPath; // ID도 fullPath 기준으로 업데이트
          this.setActiveTab(existingTabByNameAndPath.id);
          // 만약 라우터 자체의 경로도 업데이트해야 한다면,
          // 하지만 addTab은 이미 라우팅이 변경된 후에 호출되므로, router.push는 필요 없을 수 있음.
          // 필요시 router.replace(route.fullPath) 등을 고려. 여기서는 상태만 업데이트.
        }
      } else {
        // 새로운 탭 추가
        const newTab: Tab = {
          id: route.fullPath, // ID로 fullPath 사용
          name: routeName || 'New Tab',
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
          router.push('/dashboard'); // 이 부분은 프로젝트 정책에 따라 결정
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