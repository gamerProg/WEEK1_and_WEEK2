import java.util.concurrent.*;
import java.util.*;

class TokenBucket {
    private final int maxTokens;
    private final double refillRate;
    private double tokens;
    private long lastRefillTime;

    public TokenBucket(int maxTokens, double refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.tokens = maxTokens;
        this.lastRefillTime = System.nanoTime();
    }

    public synchronized Result allowRequest() {
        refill();
        if (tokens >= 1) {
            tokens -= 1;
            return new Result(true, (int) tokens, 0);
        } else {
            long retryAfter = (long) ((1 - tokens) / refillRate);
            return new Result(false, 0, retryAfter);
        }
    }

    private void refill() {
        long now = System.nanoTime();
        double seconds = (now - lastRefillTime) / 1_000_000_000.0;
        double newTokens = seconds * refillRate;
        if (newTokens > 0) {
            tokens = Math.min(maxTokens, tokens + newTokens);
            lastRefillTime = now;
        }
    }

    public synchronized Status getStatus() {
        refill();
        int used = maxTokens - (int) tokens;
        long reset = System.currentTimeMillis() / 1000 + (long)((maxTokens - tokens) / refillRate);
        return new Status(used, maxTokens, reset);
    }
}

class Result {
    boolean allowed;
    int remaining;
    long retryAfter;

    public Result(boolean allowed, int remaining, long retryAfter) {
        this.allowed = allowed;
        this.remaining = remaining;
        this.retryAfter = retryAfter;
    }

    public String toString() {
        if (allowed) return "Allowed (" + remaining + " requests remaining)";
        return "Denied (0 requests remaining, retry after " + retryAfter + "s)";
    }
}

class Status {
    int used;
    int limit;
    long reset;

    public Status(int used, int limit, long reset) {
        this.used = used;
        this.limit = limit;
        this.reset = reset;
    }

    public String toString() {
        return "{used:" + used + ", limit:" + limit + ", reset:" + reset + "}";
    }
}

class RateLimiter {
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final int limit = 1000;
    private final double refillRate = 1000.0 / 3600.0;

    public Result checkRateLimit(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, id -> new TokenBucket(limit, refillRate));
        return bucket.allowRequest();
    }

    public Status getRateLimitStatus(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, id -> new TokenBucket(limit, refillRate));
        return bucket.getStatus();
    }
}

public class Q6 {
    public static void main(String[] args) {
        RateLimiter limiter = new RateLimiter();

        for (int i = 0; i < 5; i++) {
            System.out.println(limiter.checkRateLimit("abc123"));
        }

        System.out.println(limiter.getRateLimitStatus("abc123"));
    }
}