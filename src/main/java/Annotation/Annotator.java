package Annotation;

import de.fuberlin.inf.agcsw.dbpedia.annotation.SpotlightAnnotator;
import de.fuberlin.inf.agcsw.dbpedia.annotation.models.SpotlightAnnotation;

public class Annotator {
    private static final String URL_SPOTLIGHT = "http://de.dbpedia.org/spotlight/rest/annotate";
    private static final int supportThreshold = 0;
    private static final double confidenceThreshold = 0.5;
    
	public SpotlightAnnotation annotateSpotlight(String text){
        SpotlightAnnotator spotlightAnnotator = new SpotlightAnnotator(URL_SPOTLIGHT);
        SpotlightAnnotation annotation = spotlightAnnotator.annotate(text, supportThreshold, confidenceThreshold);
        return annotation;
	}
	public void annotateFrames(String text){
		
	}
	
}
