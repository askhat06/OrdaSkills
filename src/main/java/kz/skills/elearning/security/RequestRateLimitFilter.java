package kz.skills.elearning.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.skills.elearning.config.RateLimitProperties;
import kz.skills.elearning.dto.ApiErrorResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class RequestRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final Map<String, Deque<Instant>> requestBuckets = new ConcurrentHashMap<>();

    public RequestRateLimitFilter(RateLimitProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitRoute route = resolveRoute(request);
        if (route == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Instant now = Instant.now();
        Instant cutoff = now.minus(properties.getWindow());
        String clientId = resolveClientId(request);
        String bucketKey = route.routeKey() + "|" + clientId;

        Deque<Instant> bucket = requestBuckets.computeIfAbsent(bucketKey, ignored -> new ConcurrentLinkedDeque<>());
        prune(bucket, cutoff);

        if (bucket.size() >= route.maxRequests()) {
            writeTooManyRequests(response);
            return;
        }

        bucket.addLast(now);
        filterChain.doFilter(request, response);
    }

    public void clearBuckets() {
        requestBuckets.clear();
    }

    private void prune(Deque<Instant> bucket, Instant cutoff) {
        while (true) {
            Instant first = bucket.peekFirst();
            if (first == null || !first.isBefore(cutoff)) {
                return;
            }
            bucket.pollFirst();
        }
    }

    private String resolveClientId(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private RateLimitRoute resolveRoute(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return null;
        }

        String servletPath = request.getServletPath();
        if ("/api/auth/login".equals(servletPath)) {
            return new RateLimitRoute("login", properties.getLoginMaxRequests());
        }
        if ("/api/auth/register".equals(servletPath)) {
            return new RateLimitRoute("register", properties.getRegisterMaxRequests());
        }
        if ("/api/enrollments".equals(servletPath)) {
            return new RateLimitRoute("enrollment", properties.getEnrollmentMaxRequests());
        }
        return null;
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse payload = new ApiErrorResponse(
                LocalDateTime.now(ZoneOffset.UTC),
                HttpServletResponse.SC_TOO_MANY_REQUESTS,
                "Too Many Requests",
                "Rate limit exceeded. Please try again later.",
                Map.of()
        );
        objectMapper.writeValue(response.getWriter(), payload);
    }

    private record RateLimitRoute(String routeKey, int maxRequests) {
    }
}
