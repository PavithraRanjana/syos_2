package com.syos.web.listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * HTTP Session lifecycle listener.
 * Tracks active sessions for monitoring.
 */
@WebListener
public class SessionListener implements HttpSessionListener {

    private static final Logger logger = LoggerFactory.getLogger(SessionListener.class);
    private static final AtomicInteger activeSessions = new AtomicInteger(0);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        int count = activeSessions.incrementAndGet();
        logger.debug("Session created: {}. Active sessions: {}",
            se.getSession().getId(), count);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        int count = activeSessions.decrementAndGet();
        String username = (String) se.getSession().getAttribute("username");
        logger.debug("Session destroyed: {} (user: {}). Active sessions: {}",
            se.getSession().getId(), username != null ? username : "anonymous", count);
    }

    /**
     * Returns the current number of active sessions.
     */
    public static int getActiveSessionCount() {
        return activeSessions.get();
    }
}
