import java.io.*;
import java.util.*;

public class Q4 {

    private final int NGRAM_SIZE = 5;
    private Map<String, Set<String>> ngramIndex = new HashMap<>();

    public void analyzeDocument(String docId, String content) {

        List<String> ngrams = extractNGrams(content);

        System.out.println("Extracted " + ngrams.size() + " n-grams");

        Map<String, Integer> similarityCount = new HashMap<>();

        for (String ngram : ngrams) {
            Set<String> docs = ngramIndex.getOrDefault(ngram, new HashSet<>());

            for (String otherDoc : docs) {
                if (!otherDoc.equals(docId)) {
                    similarityCount.put(otherDoc, similarityCount.getOrDefault(otherDoc, 0) + 1);
                }
            }

            docs.add(docId);
            ngramIndex.put(ngram, docs);
        }

        // Calculate similarity percentage and display
        for (Map.Entry<String, Integer> entry : similarityCount.entrySet()) {
            double similarity = (entry.getValue() * 100.0) / ngrams.size();
            String status = similarity > 50 ? "PLAGIARISM DETECTED" : "suspicious";
            System.out.printf("Found %d matching n-grams with \"%s\" → Similarity: %.1f%% (%s)\n",
                    entry.getValue(), entry.getKey(), similarity, status);
        }
    }

    private List<String> extractNGrams(String content) {

        String[] words = content.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().split("\\s+");
        List<String> ngrams = new ArrayList<>();

        for (int i = 0; i <= words.length - NGRAM_SIZE; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < NGRAM_SIZE; j++) {
                if (j > 0) sb.append(" ");
                sb.append(words[i + j]);
            }
            ngrams.add(sb.toString());
        }

        return ngrams;
    }

    public static void main(String[] args) throws IOException {

        Q4 detector = new Q4();

        String essay1 = "This is a sample essay that contains some common phrases used in writing.";
        String essay2 = "Some common phrases used in writing can indicate potential plagiarism in an essay.";
        String essay3 = "This is a completely original essay with unique content and ideas.";

        detector.analyzeDocument("essay_123.txt", essay1);
        detector.analyzeDocument("essay_089.txt", essay2);
        detector.analyzeDocument("essay_092.txt", essay3);
    }
}