package Main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.SVMModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Learning.LearningWorkflow;
import models.Document;

public class MainWorkflow {
	
	private static int MAX_DOCS = 200;
    private static final Logger LOGGER = LoggerFactory.getLogger(MainWorkflow.class);
    
	public static void main(String[] args) {
        //Creating Spark Context"
        SparkConf sparkConf = new SparkConf();
        sparkConf.setMaster("local[" + args[0] + "]");
        sparkConf.setAppName("EEx");
        try(JavaSparkContext sc = new JavaSparkContext(sparkConf)){
        
        //Setting up documents
        JavaRDD<Document> documents = null;
		try {
			
			List<String> articleTexts = readDocuments("Floods");
	        JavaRDD<String> textRDD = sc.parallelize(articleTexts);
	        JavaRDD<Document> floodRDD = textRDD.map(f -> createDocument(f, 0));
	        
	        articleTexts = readDocuments("Wildfires");
			textRDD = sc.parallelize(articleTexts);
			JavaRDD<Document>  fireRDD = textRDD.map(f -> createDocument(f, 1));
			
			articleTexts = readDocuments("Earthquakes");
	        textRDD = sc.parallelize(articleTexts);
	        JavaRDD<Document> quakeRDD = textRDD.map(f -> createDocument(f, 1));
			//Union
	        documents = floodRDD.union(fireRDD).union(quakeRDD);
		} catch (IOException e) {
			LOGGER.error("IOEXCeption during reading of documents");
		}
        
        //Pass to Learner"
        LearningWorkflow lw = new LearningWorkflow(sc);
        lw.preprocess(documents);
        SVMModel model = lw.createModel();
        lw.evalModel(model);
	}
}
	/**
	 * Helper method to enrich the files
	 * @param text the document text
	 * @param label the classification label
	 * @return the aggregated document
	 */
	private static Document createDocument(String text, double label) {
		Document d = new Document("","");
		d.setText(text);
		d.setLabel(label);
       //	Annotator anno = new Annotator(); 
       //	d.setFrames(anno.annotateFrames(text));
       //	d.setAnnotation(anno.annotateSpotlight(text));
       	return d;
	}
	
	/**
	 * Helper method to read the texts
	 * @param topic Eventtype of the file to be read
	 * @return a list of Strings (the document texts)
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static List<String> readDocuments(String topic) throws FileNotFoundException, IOException {
		LinkedList<String> list = new LinkedList<String>();
		try(BufferedReader br = new BufferedReader(new FileReader(topic+".txt"))){
	    	String line = br.readLine();
	    	int i = 1;
	    	while (line != null && i <= MAX_DOCS) {
			list.add(line);
			line = br.readLine();
			i++;
	    	}
		}
		return list;
	}
}