package com.skf.scm.notification.store;

import com.skf.scm.notification.model.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple bounded in-memory feed of recent alerts, purely for the dashboard's
 * "recent activity" panel. Not a system of record — for audit/history,
 * a real deployment would persist these to Postgres or a time-series store.
 * Kept in-memory here to keep the demo dependency-light.
 */
@Component
public class NotificationStore {

    private static final int MAX_SIZE = 200;
    private final Deque<NotificationEvent> buffer = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void add(NotificationEvent event) {
        lock.lock();
        try {
            buffer.addFirst(event);
            while (buffer.size() > MAX_SIZE) {
                buffer.removeLast();
            }
        } finally {
            lock.unlock();
        }
    }

    public List<NotificationEvent> getRecent(int limit) {
        lock.lock();
        try {
            return buffer.stream().limit(limit).toList();
        } finally {
            lock.unlock();
        }
    }
}
