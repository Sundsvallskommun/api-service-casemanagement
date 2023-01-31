package se.sundsvall.casemanagement.api;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Value("${logger.max.payload.length:10000}")
    private int maxPayloadLength;

    private static final Logger LOG = LoggerFactory.getLogger(LoggingFilter.class);

    private String getStringValue(byte[] contentAsByteArray) {
        String contentAsString = new String(contentAsByteArray, 0, contentAsByteArray.length, StandardCharsets.UTF_8);
        String maskedContentAsString = contentAsString.replaceAll("\"file\"\\s?:\\s?\".+\"", "\"file\" : \"<masked base 64>\"");
        return maskedContentAsString.length() > maxPayloadLength ? maskedContentAsString.substring(0, maxPayloadLength) + "<maximum payload logging length reached>..." : maskedContentAsString;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(requestWrapper, responseWrapper);

        String requestBody = getStringValue(requestWrapper.getContentAsByteArray());
        String responseBody = getStringValue(responseWrapper.getContentAsByteArray());

        HttpHeaders requestHeaders = Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        h -> Collections.list(request.getHeaders(h)),
                        (oldValue, newValue) -> newValue,
                        HttpHeaders::new
                ));

        HttpHeaders responseHeaders = response.getHeaderNames()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        h -> response.getHeaders(h).stream().toList(),
                        (oldValue, newValue) -> newValue,
                        HttpHeaders::new
                ));

        LOG.info("""
                        Logging incoming request
                        Incoming request: {} {}
                        Incoming request headers: {}
                        Incoming request body: {}
                        """,
                request.getMethod(), request.getRequestURL(), requestHeaders, requestBody);

        LOG.info("""
                        Logging outgoing response
                        Outgoing response status: {}
                        Outgoing response headers: {}
                        Outgoing response body: {}
                        """,
                response.getStatus(), responseHeaders, responseBody);

        responseWrapper.copyBodyToResponse();
    }

}