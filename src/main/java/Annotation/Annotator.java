package Annotation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
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
import Main.MainWorkflow;
import Preprocessing.SentenceSplitter;
import de.fuberlin.inf.agcsw.dbpedia.annotation.SpotlightAnnotator;
import de.fuberlin.inf.agcsw.dbpedia.annotation.models.SpotlightAnnotation;
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
        	this.urlSemviz=prop.getProperty("urlSemViz");
    	}
    }
    
	public SpotlightAnnotation annotateSpotlight(String text){
        SpotlightAnnotator spotlightAnnotator = new SpotlightAnnotator(urlSpotlight);
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
