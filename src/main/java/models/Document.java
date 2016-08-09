package models;

import java.io.Serializable;
import java.util.List;

import de.fuberlin.inf.agcsw.dbpedia.annotation.models.SpotlightAnnotation;
import models.framenet.Frame;
/**
 * Class to pass the collection of information about a text through the training and classification workflow
 * @author Vincent Bohlen (vincent.bohlen@fu-berlin.de)
 *
 */
public class Document implements Serializable{

	private String url;
	private String title;
	private String text;
	private SpotlightAnnotation annotation;
	private List<Frame> frames;
	private double label;
	
	public Document(String title, String url){
		this.setTitle(title);
		this.setUrl(url);
	}
	@Override
	public String toString(){
		return getTitle();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	public SpotlightAnnotation getAnnotation() {
		return annotation;
	}
	public void setAnnotation(SpotlightAnnotation annotation) {
		this.annotation = annotation;
	}
	public List<Frame> getFrames() {
		return frames;
	}
	public void setFrames(List<Frame> frames) {
		this.frames = frames;
	}
	public double getLabel() {
		return label;
	}
	public void setLabel(double label) {
		this.label = label;
	}
}
