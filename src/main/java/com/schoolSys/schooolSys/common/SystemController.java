package com.schoolSys.schooolSys.common;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now().toString());
        status.put("javaVersion", System.getProperty("java.version"));
        status.put("freeMemory", Runtime.getRuntime().freeMemory() / (1024 * 1024) + "MB");
        status.put("totalMemory", Runtime.getRuntime().totalMemory() / (1024 * 1024) + "MB");
        status.put("maxMemory", Runtime.getRuntime().maxMemory() / (1024 * 1024) + "MB");
        status.put("processors", Runtime.getRuntime().availableProcessors());
        return ResponseEntity.ok(ApiResponse.ok(status));
    }

    @GetMapping("/info")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("timestamp", LocalDateTime.now().toString());
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("javaVendor", System.getProperty("java.vendor"));
        info.put("osName", System.getProperty("os.name"));
        info.put("osVersion", System.getProperty("os.version"));
        info.put("osArch", System.getProperty("os.arch"));

        // Memory info
        Runtime runtime = Runtime.getRuntime();
        Map<String, String> memory = new LinkedHashMap<>();
        memory.put("free", runtime.freeMemory() / (1024 * 1024) + "MB");
        memory.put("total", runtime.totalMemory() / (1024 * 1024) + "MB");
        memory.put("max", runtime.maxMemory() / (1024 * 1024) + "MB");
        memory.put("used", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) + "MB");
        info.put("memory", memory);

        // Database info
        Map<String, Object> dbInfo = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            dbInfo.put("url", conn.getMetaData().getURL());
            dbInfo.put("databaseProductName", conn.getMetaData().getDatabaseProductName());
            dbInfo.put("databaseProductVersion", conn.getMetaData().getDatabaseProductVersion());
            dbInfo.put("driverName", conn.getMetaData().getDriverName());
            dbInfo.put("connected", true);
        } catch (Exception e) {
            dbInfo.put("connected", false);
            dbInfo.put("error", e.getMessage());
        }
        info.put("database", dbInfo);

        info.put("processors", runtime.availableProcessors());

        return ResponseEntity.ok(ApiResponse.ok(info));
    }
}
