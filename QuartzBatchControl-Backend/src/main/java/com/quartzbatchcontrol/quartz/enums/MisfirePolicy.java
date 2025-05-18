package com.quartzbatchcontrol.quartz.enums;

public enum MisfirePolicy {
    FIRE_AND_PROCEED,    // 놓쳤으면 지금 실행하고 다음 주기로
    DO_NOTHING,          // 놓친 트리거는 무시하고 다음 주기로 넘어감
    IGNORE               // 가능한 한 빨리 놓친 것까지 연속 실행 (주의 필요)
}
