import { defineStore } from 'pinia'
import router from '@/router' // 라우터 인스턴스 import
import type { RouteLocationNormalizedLoaded } from 'vue-router' // Vue Router 타입

/**
 * 탭 객체의 인터페이스 정의
 */
export interface Tab {
  id: string // 일반적으로 route.fullPath 또는 route.name 사용
  name: string // 탭에 표시될 이름 (route.meta.title 또는 route.name)
  path: string // 탭 클릭 시 이동할 경로 (route.fullPath)
  closable: boolean // 탭을 닫을 수 있는지 여부 (route.meta.closable)
}

/**
 * 탭 스토어의 상태 인터페이스 정의
 */
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
    /**
     * 새 탭을 추가하거나 기존 탭을 활성화합니다.
     * - 라우트 정보에 `meta.title`이 없으면 탭으로 관리하지 않습니다.
     * - 이미 열린 탭:
     *   - 이름과 기본 경로(쿼리 제외)가 동일하고 fullPath까지 같으면 해당 탭을 활성화합니다.
     *   - 이름과 기본 경로가 같지만 fullPath(쿼리 등)가 다르면, 기존 탭의 경로와 ID를 업데이트하고 활성화합니다.
     * - 새 탭이면 목록에 추가하고 활성화합니다.
     * @param route Vue Router의 현재 라우트 객체 (`RouteLocationNormalizedLoaded`)
     */
    addTab(route: RouteLocationNormalizedLoaded) {
      if (!route.meta || !route.meta.title) {
        // meta.title이 없는 라우트는 탭으로 관리하지 않음 (예: 로그인 페이지, 특정 팝업 라우트 등)
        return;
      }

      const routeName = route.meta.title as string || route.name as string; // 타입 단언 추가
      // 쿼리 파라미터를 제외한 기본 경로로 기존 탭 검색
      const baseRoutePath = route.path.split('?')[0];
      const existingTab = this.openedTabs.find(tab =>
        (tab.name === routeName || tab.path.split('?')[0] === baseRoutePath) && // 이름 또는 기본 경로가 일치하고
        tab.path.split('?')[0] === baseRoutePath // 기본 경로가 확실히 일치하는 경우 (좀 더 엄격한 조건)
      );

      if (existingTab) {
        // 이름과 기본 경로가 같은 탭이 이미 존재할 경우
        if (existingTab.path === route.fullPath) {
          // fullPath까지 완전히 동일하면 해당 탭을 활성화
          this.setActiveTab(existingTab.id);
        } else {
          // fullPath가 다르면 (예: 쿼리 파라미터 변경), 기존 탭의 경로와 ID를 새 fullPath로 업데이트하고 활성화
          existingTab.path = route.fullPath;
          existingTab.id = route.fullPath; // ID도 fullPath 기준으로 업데이트
          this.setActiveTab(existingTab.id);
        }
      } else {
        // 새로운 탭 추가
        const newTab: Tab = {
          id: route.fullPath, // 탭 ID는 route.fullPath 사용
          name: routeName || 'New Tab', // route.name이 없을 경우 대비
          path: route.fullPath,
          closable: route.meta.closable !== undefined ? !!route.meta.closable : true, // closable 명시적 boolean 처리
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
          // 닫힌 탭이 활성 탭이었으면, 이전 탭(없으면 첫 번째 탭)을 활성화
          const newActiveTabIndex = Math.max(0, tabIndex - 1);
          const newActiveTab = this.openedTabs[newActiveTabIndex] || this.openedTabs[0];
          if (newActiveTab) { // newActiveTab이 존재할 경우에만 실행
             this.setActiveTab(newActiveTab.id);
             router.push(newActiveTab.path);
          }
        } else {
          // 모든 탭이 닫힌 경우
          this.activeTabId = null;
          router.push('/dashboard'); // 프로젝트 정책에 따라 기본 페이지로 이동
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
      if (this.activeTabId) {
        const activeTab = this.openedTabs.find(tab => tab.id === this.activeTabId);
        if (activeTab) {
          router.push(activeTab.path);
        }
      }
    }
  },
  // Pinia Persist 플러그인 사용 시 (선택 사항)
  // persist: true, // 또는 persist: { storage: sessionStorage } 등
}) 