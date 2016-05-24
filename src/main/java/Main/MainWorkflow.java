package Main;

import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer;

import Annotation.Annotator;
import Learning.LearningWorkflow;
import models.Document;

public class MainWorkflow {
	public static void main(String[] args) {
        //Create spark context
        SparkConf sparkConf = new SparkConf();
        sparkConf.setMaster("local[" + args[0] + "]");
        sparkConf.setAppName("EEx");
        sparkConf.set(SparkDl4jMultiLayer.AVERAGE_EACH_ITERATION, String.valueOf(true));
        try(JavaSparkContext sc = new JavaSparkContext(sparkConf)){
        
        //Set up
        List<String> articleTexts = readDocuments();
        JavaRDD<String> textRDD = sc.parallelize(articleTexts);
        JavaRDD<Document> documents = textRDD.map(f -> createDocument(f));
        
        //Pass to Learner
        LearningWorkflow lw = new LearningWorkflow(Integer.parseInt(args[0]));
        lw.createModel(documents, sc);
	}
}

	private static Document createDocument(String text) {
		Document d = new Document("","");
		d.setText(text);
       	Annotator anno = new Annotator(); 
       	d.setFrames(anno.annotateFrames(text));
       	d.setAnnotation(anno.annotateSpotlight(text));
       	return d;
	}

	private static List<String> readDocuments() {
		// TODO Auto-generated method stub
		return null;
	}
}