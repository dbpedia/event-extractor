package Annotation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Annotation.FramenetParser;
import Preprocessing.SentenceSplitter;
import Preprocessing.StanfordUtils;
import models.dbpedia.SpotlightAnnotation;
import models.dbpedia.SpotlightResource;
import models.framenet.Frame;
/**
 * Class for annotating given texts with spotlight and semviz
 * @author Vincent Bohlen (vincent.bohlen@fu-berlin.de)
 *
 */
public class Annotator {
    private final Logger LOGGER = LoggerFactory.getLogger(Annotator.class);
    private String urlSpotlight;
    private String urlSemviz;
    private static final int SUPPORT_THRESHOLD = 0;
    private static final double CONFIDENCE_THRESHOLD = 0.5;
    
    public Annotator(){
    	Properties prop = new Properties();
    	try(FileInputStream input = new FileInputStream(getClass().getClassLoader().getResource("config.properties").getFile())){
    		prop.load(input);
    	} catch (FileNotFoundException e) {
    		LOGGER.error(e.getStackTrace().toString());
		} catch (IOException e) {
			LOGGER.error(e.getStackTrace().toString());
		}
    	if(prop.getProperty("sparkCores") != null){
        	this.urlSpotlight = prop.getProperty("urlSpotlight");
    	}
    	if(prop.getProperty("modelPath") != null){
        	this.urlSemviz=prop.getProperty("urlSemviz");
    	}
    }
    
	public SpotlightAnnotation annotateSpotlight(String text){
        SpotlightAnnotator spotlightAnnotator = new SpotlightAnnotator();
        SpotlightAnnotation annotation = spotlightAnnotator.annotate(text, SUPPORT_THRESHOLD, CONFIDENCE_THRESHOLD, urlSpotlight);
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
	
	public static String annotateBothToString(String text){
		StringBuilder sb = new StringBuilder();
    	sb.append(StanfordUtils.lemmatizeArticle(text));
    	Annotator a = new Annotator();
    	sb.append(" ");
    	for(Frame frame : a.annotateFrames(text)){
    		sb.append(frame.getTarget().getName());
    		sb.append(" ");
    	}
    	HashSet<String> set = new HashSet<>();
    	for(SpotlightResource r : a.annotateSpotlight(text).getResources()){
    		for(String s : r.getTypesString().split(",")){
    			String[] type = s.split(":");
    			if(type.length > 1){
    				set.add(s.split(":")[1]);
    			}
    		}
    	}
    	for(String rawType : set){
    		sb.append(rawType);	    	
    		sb.append(" ");
    	}
    	return sb.toString();
	}
	
	private String getFramenetAnnotation(String text) throws IOException, URISyntaxException {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            URIBuilder uriBuilder = new URIBuilder(urlSemviz).setParameter("sentence", text);
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
