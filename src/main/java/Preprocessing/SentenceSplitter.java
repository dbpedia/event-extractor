package Preprocessing;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
/**
 * Created by wojlukas
 */
public class SentenceSplitter {

    private StanfordCoreNLP pipeline;

    public SentenceSplitter() {
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit");

        this.pipeline = new StanfordCoreNLP(props);
    }

    public List<String> splitIntoSentences(String documentText) {
        List<String> sentenceList = new ArrayList<>();

        Annotation document = new Annotation(documentText);

        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        for(CoreMap sentence : sentences) {
            sentenceList.add(sentence.toString());
        }

        return sentenceList;
    }

    public List<String> divideIntoChunks(List<String> sentences, int chunkSize) {
        List<String> result = new ArrayList<>();

        int fullChunks = sentences.size() / chunkSize;
        int remainingLastChunk = sentences.size() % chunkSize;

        for (int i = 0; i < fullChunks; i++) {
            result.add(sentences.subList(chunkSize*i, chunkSize*i + chunkSize).stream().collect(Collectors.joining(" ")));
        }

        if (remainingLastChunk != 0) {
            result.add(sentences.subList(sentences.size() - remainingLastChunk, sentences.size()).stream().collect(Collectors.joining(" ")));
        }

        return result;
    }

    public static void main(String[] args) {
        SentenceSplitter slem = new SentenceSplitter();
        System.out.println(slem.divideIntoChunks(Arrays.asList("A","B","C","D","E","F", "G", "H"), 4));
    }
}
