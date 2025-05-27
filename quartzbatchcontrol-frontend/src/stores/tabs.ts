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
      if (!route.meta || !route.meta.title || !route.name) { // 탭으로 관리하지 않을 라우트 (예: 로그인, 이름 없는 라우트)
        // console.log('Skipping tab for route without meta.title or name:', route);
        return;
      }

      const tabIdentifier = route.name as string; // 탭의 고유 ID를 라우트 이름으로 사용
      const existingTab = this.openedTabs.find(tab => tab.id === tabIdentifier);

      if (existingTab) {
        // 이미 해당 이름의 탭이 존재하면, 해당 탭을 활성화하고 fullPath (쿼리 포함)만 업데이트
        existingTab.path = route.fullPath; // 최신 fullPath (쿼리 포함)로 업데이트
        existingTab.name = route.meta.title || route.name; // 최신 title로 업데이트 (라우트 meta의 title이 우선)
        this.setActiveTab(existingTab.id);
        // TabBar.vue에서 탭 클릭 시 router.push(existingTab.path)가 호출되므로, 여기서 별도 push는 불필요.
        // JobTable.vue는 route.query.searchFromBatchTable 변경을 watch하고 있으므로,
        // router.afterEach에서 addTab이 호출되고, activeTabId가 변경되면
        // TabBar.vue에서 해당 탭으로 UI가 변경되고, JobTable.vue의 watch가 동작할 것으로 예상.
      } else {
        const newTab: Tab = {
          id: tabIdentifier, // 탭 ID를 라우트 이름으로 설정
          name: route.meta.title || route.name, // 라우트 meta의 title 우선
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