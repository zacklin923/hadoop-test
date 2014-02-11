package org.flume;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * User: keyki
 */
public class FlumeEventContentLoggerInterceptor implements Interceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlumeEventContentLoggerInterceptor.class);

    @Override
    public void initialize() {
    }

    @Override
    public Event intercept(Event event) {
        LOGGER.info("HEADERS: {}", event.getHeaders());
        LOGGER.info("BODY: {}", event.getBody());
        return event;
    }

    @Override
    public List<Event> intercept(List<Event> events) {
        return events;
    }

    @Override
    public void close() {
    }

    public static class Builder implements Interceptor.Builder {

        @Override
        public Interceptor build() {
            return new FlumeEventContentLoggerInterceptor();
        }

        @Override
        public void configure(Context context) {
        }

    }

}
