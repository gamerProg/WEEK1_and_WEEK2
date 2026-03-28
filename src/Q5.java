import java.util.*;
import java.util.concurrent.*;

class Event {
    String url;
    String userId;
    String source;

    Event(String url, String userId, String source) {
        this.url = url;
        this.userId = userId;
        this.source = source;
    }
}

class PageStats {
    String url;
    int views;
    int uniqueVisitors;

    PageStats(String url, int views, int uniqueVisitors) {
        this.url = url;
        this.views = views;
        this.uniqueVisitors = uniqueVisitors;
    }
}

class RealTimeAnalytics {

    private final Map<String, Integer> pageViews = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();
    private final Map<String, Integer> trafficSources = new ConcurrentHashMap<>();

    private final PriorityQueue<PageStats> topPages =
            new PriorityQueue<>(Comparator.comparingInt(p -> p.views));

    public synchronized void processEvent(Event event) {

        pageViews.put(event.url, pageViews.getOrDefault(event.url, 0) + 1);

        uniqueVisitors
                .computeIfAbsent(event.url, k -> ConcurrentHashMap.newKeySet())
                .add(event.userId);

        trafficSources.put(event.source,
                trafficSources.getOrDefault(event.source, 0) + 1);

        updateTopPages(event.url);
    }

    private void updateTopPages(String url) {

        int views = pageViews.get(url);
        int unique = uniqueVisitors.get(url).size();

        PageStats stats = new PageStats(url, views, unique);

        if (topPages.size() < 10) {
            topPages.add(stats);
        } else if (views > topPages.peek().views) {
            topPages.poll();
            topPages.add(stats);
        }
    }

    public void getDashboard() {

        List<PageStats> list = new ArrayList<>(topPages);
        list.sort((a, b) -> b.views - a.views);

        int totalSource = trafficSources.values().stream().mapToInt(i -> i).sum();

        System.out.println("Top Pages:");

        int rank = 1;
        for (PageStats p : list) {
            System.out.println(rank + ". " + p.url + " - " + p.views + " views (" + p.uniqueVisitors + " unique)");
            rank++;
        }

        System.out.println("\nTraffic Sources:");

        for (String source : trafficSources.keySet()) {
            int count = trafficSources.get(source);
            double percent = (count * 100.0) / totalSource;
            System.out.println(source + ": " + String.format("%.2f", percent) + "%");
        }
    }
}

public class Q5 {

    public static void main(String[] args) throws Exception {

        RealTimeAnalytics analytics = new RealTimeAnalytics();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("\n===== DASHBOARD UPDATE =====");
            analytics.getDashboard();
        }, 5, 5, TimeUnit.SECONDS);

        analytics.processEvent(new Event("/article/breaking-news", "user_1", "google"));
        analytics.processEvent(new Event("/article/breaking-news", "user_2", "facebook"));
        analytics.processEvent(new Event("/sports/championship", "user_3", "direct"));
        analytics.processEvent(new Event("/sports/championship", "user_4", "google"));
        analytics.processEvent(new Event("/sports/championship", "user_3", "google"));
        analytics.processEvent(new Event("/tech/ai-news", "user_5", "google"));
        analytics.processEvent(new Event("/tech/ai-news", "user_6", "direct"));
        analytics.processEvent(new Event("/tech/ai-news", "user_7", "facebook"));

        Thread.sleep(20000);
        scheduler.shutdown();
    }
}