import java.util.*;

class DNSEntry {
    String domain;
    String ipAddress;
    long expiryTime;

    public DNSEntry(String domain, String ipAddress, long ttlSeconds) {
        this.domain = domain;
        this.ipAddress = ipAddress;
        this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

public class Q3 {

    private final int capacity;

    private Map<String, DNSEntry> cache;

    private long hits = 0;
    private long misses = 0;

    public Q3(int capacity) {

        this.capacity = capacity;

        this.cache = new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > Q3.this.capacity;
            }
        };

        startCleanupThread();
    }

    public synchronized String resolve(String domain) {

        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            hits++;
            return "Cache HIT → " + entry.ipAddress;
        }

        if (entry != null && entry.isExpired()) {
            cache.remove(domain);
        }

        misses++;

        String ip = queryUpstreamDNS(domain);

        DNSEntry newEntry = new DNSEntry(domain, ip, 300);
        cache.put(domain, newEntry);

        return "Cache MISS → " + ip;
    }

    private String queryUpstreamDNS(String domain) {

        Random rand = new Random();

        return "172.217.14." + rand.nextInt(255);
    }

    public void getCacheStats() {

        long total = hits + misses;

        double hitRate = total == 0 ? 0 : (hits * 100.0) / total;

        System.out.println("Hits: " + hits);
        System.out.println("Misses: " + misses);
        System.out.println("Hit Rate: " + hitRate + "%");
    }

    private void startCleanupThread() {

        Thread cleaner = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);

                    synchronized (this) {
                        Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();

                        while (it.hasNext()) {
                            Map.Entry<String, DNSEntry> entry = it.next();
                            if (entry.getValue().isExpired()) {
                                it.remove();
                            }
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        cleaner.setDaemon(true);
        cleaner.start();
    }

    public static void main(String[] args) throws Exception {

        Q3 cache = new Q3(5);

        System.out.println(cache.resolve("google.com"));
        System.out.println(cache.resolve("google.com"));

        Thread.sleep(2000);

        System.out.println(cache.resolve("openai.com"));
        System.out.println(cache.resolve("google.com"));

        cache.getCacheStats();
    }
}