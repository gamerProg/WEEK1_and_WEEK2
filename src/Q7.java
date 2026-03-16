import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    PriorityQueue<Query> top = new PriorityQueue<>((a,b) -> a.freq - b.freq);
}

class Query {
    String text;
    int freq;

    Query(String t, int f) {
        text = t;
        freq = f;
    }
}

public class Q7 {

    private TrieNode root = new TrieNode();
    private Map<String, Integer> freqMap = new HashMap<>();
    private static final int K = 10;

    public void insert(String query, int freq) {
        freqMap.put(query, freq);
        TrieNode node = root;

        for(char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);

            node.top.offer(new Query(query, freq));
            if(node.top.size() > K) node.top.poll();
        }
    }

    public List<String> search(String prefix) {
        TrieNode node = root;

        for(char c : prefix.toCharArray()) {
            if(!node.children.containsKey(c)) return typoSearch(prefix);
            node = node.children.get(c);
        }

        List<Query> list = new ArrayList<>(node.top);
        list.sort((a,b) -> b.freq - a.freq);

        List<String> res = new ArrayList<>();
        for(Query q : list) res.add(q.text);
        return res;
    }

    public void updateFrequency(String query) {
        int newFreq = freqMap.getOrDefault(query,0) + 1;
        freqMap.put(query,newFreq);
        insert(query,newFreq);
    }

    private List<String> typoSearch(String word) {
        List<String> res = new ArrayList<>();
        int best = Integer.MAX_VALUE;

        for(String q : freqMap.keySet()) {
            int d = editDistance(word,q);
            if(d < best) {
                best = d;
                res.clear();
                res.add(q);
            } else if(d == best && res.size() < K) {
                res.add(q);
            }
        }

        res.sort((a,b) -> freqMap.get(b) - freqMap.get(a));
        if(res.size() > K) return res.subList(0,K);
        return res;
    }

    private int editDistance(String a, String b) {
        int[][] dp = new int[a.length()+1][b.length()+1];

        for(int i=0;i<=a.length();i++) dp[i][0]=i;
        for(int j=0;j<=b.length();j++) dp[0][j]=j;

        for(int i=1;i<=a.length();i++) {
            for(int j=1;j<=b.length();j++) {
                if(a.charAt(i-1)==b.charAt(j-1))
                    dp[i][j]=dp[i-1][j-1];
                else
                    dp[i][j]=1+Math.min(dp[i-1][j-1],Math.min(dp[i-1][j],dp[i][j-1]));
            }
        }

        return dp[a.length()][b.length()];
    }

    public static void main(String[] args) {
        Q7 ac = new Q7();

        ac.insert("java tutorial",1234567);
        ac.insert("javascript",987654);
        ac.insert("java download",456789);

        System.out.println(ac.search("jav"));

        ac.updateFrequency("java 21 features");
        ac.updateFrequency("java 21 features");
        ac.updateFrequency("java 21 features");
    }
}