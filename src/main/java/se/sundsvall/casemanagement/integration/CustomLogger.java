package se.sundsvall.casemanagement.integration;

import feign.Request;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CustomLogger extends feign.Logger {

    private final Logger logger;

    public CustomLogger(String name) {
        this(LoggerFactory.getLogger(name));
    }

    CustomLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        if (logger.isInfoEnabled()) {
            super.logRequest(configKey, logLevel, request);
        }
    }

    @Override
    protected Response logAndRebufferResponse(String configKey,
                                              Level logLevel,
                                              Response response,
                                              long elapsedTime) throws IOException {
        if (logger.isInfoEnabled()) {
            return super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime);
        }
        return response;
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(String.format(methodTag(configKey) + format, args));
        }
    }
}