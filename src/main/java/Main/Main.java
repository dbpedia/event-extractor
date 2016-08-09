package Main;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Annotation.Annotator;
import Crawler.EventCrawler;
import Preprocessing.StanfordUtils;
import models.dbpedia.SpotlightAnnotation;
import models.framenet.Frame;
/**
 * Class to perform crawling of wikipedia articles for training (helper)
 * @author Vincent Bohlen (vincent.bohlen@fu-berlin.de)
 *
 */
public class Main {
    	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

		public static void ser() {
		EventCrawler e = new EventCrawler("Category:Wildfires_by_year");
		e.crawl(false);
		try(ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("crawledStuff.ser")))){
			output.writeObject(e);
		}
		catch(Exception ex){}
		System.out.println(e.getTree().toString());
	}
	public static EventCrawler deser() {
		EventCrawler e = null;
		try(ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream("crawledStuff.ser")))){
			e = (EventCrawler)input.readObject();
		}
		catch(FileNotFoundException ex){			ex.printStackTrace();}
		catch(IOException ex){			ex.printStackTrace();} 
		catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return e;
	}
	public static void main(String[] args) {
		try(BufferedReader br = new BufferedReader(new FileReader("Wildfires.txt"))){
			try(BufferedWriter output = new BufferedWriter(new FileWriter("WildfiresAnno.txt", true))){
				Annotator anno = new Annotator();
				int i = 0;
				int j = 0;
				String text = br.readLine();
				while(j <= 21){
					 text = br.readLine();
					j++;
				}
		    	while (text != null && i < 20) {
				
		    		List<Frame> f = anno.annotateFrames(text);
					SpotlightAnnotation a = anno.annotateSpotlight(text);
					StringBuilder sb = new StringBuilder();
					sb.append(StanfordUtils.lemmatizeArticle(text));
					sb.append(" ");
		        	sb.append(a.getTypes());
		        	for(Frame frame : f){
		        		sb.append(frame.getTarget().getName());
		        		sb.append(" ");
		        	}
		        	output.write(sb.toString());
		        	output.write("\n");
		    		
		    	i++;	
				text = br.readLine();
	    	}
		}
	}catch(Exception ex){
		LOGGER.error(ex.getStackTrace().toString());
	}
		
	/*	try(BufferedWriter output = new BufferedWriter(new FileWriter("WildfiresAnno.txt", true))){
			Annotator anno = new Annotator();
			int i = 0;
			for(Document d : l){
				String text = d.getText().replace("\n", "").trim();
				List<Frame> f = anno.annotateFrames(text);
				SpotlightAnnotation a = anno.annotateSpotlight(text);
				StringBuilder sb = new StringBuilder();
				sb.append(StanfordUtils.lemmatizeArticle(text));
				sb.append(" ");
	        	sb.append(a.getTypes());
	        	for(Frame frame : f){
	        		sb.append(frame.getTarget().getName());
	        		sb.append(" ");
	        	}
	        	System.out.println(sb.toString());

	        	i++;
	        	if(i == 20){
	        		break;
	        	}
			}
		}
	        	output.write(text);
	        	output.write("\n");
	        	}
			}
		catch(Exception ex){System.out.println("e");
		}*/
	}
}