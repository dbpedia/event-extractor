package Annotation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import Annotation.FramenetParser;
import Annotation.SentenceSplitter;
import de.fuberlin.inf.agcsw.dbpedia.annotation.SpotlightAnnotator;
import de.fuberlin.inf.agcsw.dbpedia.annotation.models.SpotlightAnnotation;
import models.framenet.Frame;

public class Annotator {
    private static final String URL_SPOTLIGHT = "http://de.dbpedia.org/spotlight/rest/annotate";
    private static final String URL_SEMVIZ = "http://dbpedia:32790/api/v1/parse";
    private static final int SUPPORT_THRESHOLD = 0;
    private static final double CONFIDENCE_THRESHOLD = 0.5;
    
	public SpotlightAnnotation annotateSpotlight(String text){
        SpotlightAnnotator spotlightAnnotator = new SpotlightAnnotator(URL_SPOTLIGHT);
        SpotlightAnnotation annotation = spotlightAnnotator.annotate(text, SUPPORT_THRESHOLD, CONFIDENCE_THRESHOLD);
        return annotation;
	}
	
	public List<Frame> annotateFrames(String text){
        SentenceSplitter splitter= new SentenceSplitter();
        List<String> sentenceList = splitter.divideIntoChunks(splitter.splitIntoSentences(text), 5);
        List<Frame> frames = new LinkedList<Frame>();
        sentenceList.forEach(s -> {
            try {
                String framenetAnnotationJson = getFramenetAnnotation(s);
                frames.addAll(FramenetParser.parseFramenetResponse(framenetAnnotationJson));
            }catch(Exception e){
            	
            }          
        });
        return frames;
	}
	
	private String getFramenetAnnotation(String text) throws IOException, URISyntaxException {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            URIBuilder uriBuilder = new URIBuilder(URL_SEMVIZ).setParameter("sentence", text);
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

            StringBuffer response = new StringBuffer();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent()))){
                String inputLine;

                while ((inputLine = reader.readLine()) != null) {
                    response.append(inputLine);
                }

            }

            return response.toString();
        } 
    }
}
