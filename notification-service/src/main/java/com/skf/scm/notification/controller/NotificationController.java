package com.skf.scm.notification.controller;

import com.skf.scm.notification.model.NotificationEvent;
import com.skf.scm.notification.store.NotificationStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST fallback for the dashboard's initial page load (before the WebSocket
 * connection establishes) — live updates after that come through /topic/alerts.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationStore store;

    @GetMapping
    public ResponseEntity<List<NotificationEvent>> getRecent(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(store.getRecent(limit));
    }
}
