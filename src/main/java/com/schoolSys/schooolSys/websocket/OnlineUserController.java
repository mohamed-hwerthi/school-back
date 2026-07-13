package com.schoolSys.schooolSys.websocket;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * REST controller for querying online user status.
 */
@RestController
@RequestMapping("/api/online-users")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OnlineUserController {

    private final OnlineUserTracker onlineUserTracker;

    @GetMapping
    public ApiResponse<Set<String>> getOnlineUsers() {
        return ApiResponse.ok(onlineUserTracker.getOnlineUsers());
    }

    @GetMapping("/count")
    public ApiResponse<Map<String, Integer>> getOnlineCount() {
        return ApiResponse.ok(Map.of("count", onlineUserTracker.getOnlineCount()));
    }

    @GetMapping("/{username}/status")
    public ApiResponse<Map<String, Boolean>> isOnline(@PathVariable String username) {
        return ApiResponse.ok(Map.of("online", onlineUserTracker.isOnline(username)));
    }
}
