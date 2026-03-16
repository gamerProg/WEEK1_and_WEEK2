import java.util.*;

class VideoData {
    String videoId;
    String content;

    VideoData(String videoId,String content){
        this.videoId=videoId;
        this.content=content;
    }
}

class LRUCache<K,V> extends LinkedHashMap<K,V>{
    private int capacity;

    LRUCache(int capacity){
        super(capacity,0.75f,true);
        this.capacity=capacity;
    }

    protected boolean removeEldestEntry(Map.Entry<K,V> eldest){
        return size()>capacity;
    }
}

public class Q10 {

    static LRUCache<String,VideoData> L1=new LRUCache<>(10000);
    static LRUCache<String,VideoData> L2=new LRUCache<>(100000);

    static Map<String,VideoData> database=new HashMap<>();
    static Map<String,Integer> accessCount=new HashMap<>();

    static int l1Hits=0;
    static int l2Hits=0;
    static int l3Hits=0;
    static int totalRequests=0;

    public static VideoData getVideo(String id){

        totalRequests++;

        if(L1.containsKey(id)){
            l1Hits++;
            accessCount.put(id,accessCount.getOrDefault(id,0)+1);
            return L1.get(id);
        }

        if(L2.containsKey(id)){
            l2Hits++;
            VideoData v=L2.get(id);

            int count=accessCount.getOrDefault(id,0)+1;
            accessCount.put(id,count);

            if(count>5){
                L1.put(id,v);
            }

            return v;
        }

        VideoData v=database.get(id);

        if(v!=null){
            l3Hits++;
            L2.put(id,v);
            accessCount.put(id,1);
        }

        return v;
    }

    public static void updateVideo(String id,String content){
        VideoData v=new VideoData(id,content);
        database.put(id,v);
        L1.remove(id);
        L2.remove(id);
    }

    public static Map<String,String> getStatistics(){

        Map<String,String> stats=new HashMap<>();

        double l1Rate= totalRequests==0?0:(l1Hits*100.0/totalRequests);
        double l2Rate= totalRequests==0?0:(l2Hits*100.0/totalRequests);
        double l3Rate= totalRequests==0?0:(l3Hits*100.0/totalRequests);

        stats.put("L1 Hit Rate",String.format("%.2f%%",l1Rate));
        stats.put("L2 Hit Rate",String.format("%.2f%%",l2Rate));
        stats.put("L3 Hit Rate",String.format("%.2f%%",l3Rate));

        return stats;
    }

    public static void main(String[] args){

        database.put("video_123",new VideoData("video_123","dataA"));
        database.put("video_999",new VideoData("video_999","dataB"));

        System.out.println(getVideo("video_123"));
        System.out.println(getVideo("video_123"));
        System.out.println(getVideo("video_999"));

        System.out.println(getStatistics());
    }
}