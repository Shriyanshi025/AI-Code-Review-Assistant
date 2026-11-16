package com.codereview.backend.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StuckUserTracker {

    private final Map<String, AttemptState> userStateMap = new ConcurrentHashMap<>();

    public int getAndUpdateStuckCount(String contextKey, String code, String ruleId) {
        if (contextKey == null || ruleId == null) {
            return 1;
        }

        int codeHash = code != null ? code.trim().hashCode() : 0;

        AttemptState previous = userStateMap.get(contextKey);
        if (previous == null || !ruleId.equals(previous.lastRuleId)) {
            userStateMap.put(contextKey, new AttemptState(codeHash, ruleId, 1));
            return 1;
        }

        // Increment stuck count for repeated attempt with same rule/issue
        int newCount = previous.stuckCount + 1;
        userStateMap.put(contextKey, new AttemptState(codeHash, ruleId, newCount));
        return newCount;
    }

    public void reset(String contextKey) {
        if (contextKey != null) {
            userStateMap.remove(contextKey);
        }
    }

    private record AttemptState(int codeHash, String lastRuleId, int stuckCount) {}
}
