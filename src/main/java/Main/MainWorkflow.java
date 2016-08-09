package Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;	
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.ml.feature.IDFModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Annotation.Annotator;
import Learning.LearningWorkflow;
import Learning.ModelEvaluate;
import models.Document;
import scala.collection.mutable.HashMap;
/**
 * Main Class to perform training and classification
 * @author Vincent Bohlen (vincent.bohlen@fu-berlin.de)
 *
 */
public class MainWorkflow implements Serializable{
	
    private final Logger LOGGER = LoggerFactory.getLogger(MainWorkflow.class);
    private JavaRDD<Document> testDocRDD;
    private Integer sparkCores;
    private Integer trainSetSize;
    private String modelPath;
    private String trainExamplesPath;
    private HashMap<Double,String> types = new HashMap<Double,String>();
    
    public MainWorkflow(){
    	Properties prop = new Properties();
    	try(FileInputStream input = new FileInputStream(getClass().getClassLoader().getResource("config.properties").getFile())){
    		prop.load(input);
    	} catch (FileNotFoundException e) {
    		LOGGER.error(e.getStackTrace().toString());
		} catch (IOException e) {
			LOGGER.error(e.getStackTrace().toString());
		}
    	if(prop.getProperty("sparkCores") != null){
        	this.sparkCores = new Integer(prop.getProperty("sparkCores"));
    	}
    	if(prop.getProperty("modelPath") != null){
        	this.modelPath=prop.getProperty("modelPath");
    	}
    	if(prop.getProperty("trainExamplesPath") != null){
    		this.trainExamplesPath=prop.getProperty("trainExamplesPath");
    	}
    	if(prop.getProperty("trainSetSize") != null){
    		this.trainSetSize= new Integer(prop.getProperty("trainSetSize"));
    	}
    }
    
    /**
     * Method to setup the Spark configuration
     * @return the resulting SparkConf
     */
    private SparkConf setUpSparkConf(){
        SparkConf sparkConf = new SparkConf();
        sparkConf.setMaster("local[" + sparkCores +"]");
        sparkConf.setAppName("EEx");
        return sparkConf;
    }
	
	/**
	 * Method to classify a given text
	 * @param text the text to classify
	 */
	public String classify(String text){	
		IDFModel idf = (IDFModel)Serializer.deserialize(modelPath+"idf.ser"); 
		types = (HashMap<Double,String>)Serializer.deserialize(modelPath+"types.ser");
		Document d = createDocument(text, -1);
		LinkedList<Document> l = new LinkedList<Document>();
		l.add(d);
		Double prediction;
		try(JavaSparkContext sc = new JavaSparkContext(setUpSparkConf())){
			LogisticRegressionModel logRegModel = LogisticRegressionModel.load(JavaSparkContext.toSparkContext(sc), modelPath+"logRegModel");
			LearningWorkflow lw = new LearningWorkflow(sc);
			JavaRDD<LabeledPoint> lp = lw.preprocess(sc.parallelize(l), idf, false);
			prediction = logRegModel.predict(lp.collect().get(0).features());
		}
		return types.get(prediction).get();
	}
	
	/**
	 * Method to start the training process
	 * @param evaluation true if you want to perform evaluation of the model (use only 60% of examples for training)
	 */
	public void train(boolean evaluation){
		//Cleanup/Delete old model
		try {
			delete(new File(modelPath+"logRegModel"));
		} catch (IOException e) {
			LOGGER.error(e.getStackTrace().toString());
		}
		logReg(evaluation);
	}
	/**
	 * Method to actually perform training of the logistic regression model
	 * @param evaluation true if you want to perform evaluation of the model (use only 60% of examples for training)
	 */
	private void logReg(boolean evaluation) {
		 try(JavaSparkContext sc = new JavaSparkContext(setUpSparkConf())){
        	JavaRDD<Document> documents = setUpMultiClassDocs(sc, evaluation);
        	Serializer.serialize(modelPath+"types.ser", types);
            //Pass to Learner
        	LearningWorkflow lw = new LearningWorkflow(sc);
        	IDFModel idf = lw.trainIDFModel(documents);
        	Serializer.serialize(modelPath + "idf.ser", idf);
            lw.preprocess(documents, idf, true);
            
            LogisticRegressionModel model = lw.createLogRegModel(types.size());
            model.save(JavaSparkContext.toSparkContext(sc), modelPath + "logRegModel");
            
            if(evaluation){
            	ModelEvaluate.evaluate(sc, model, idf, testDocRDD);
            }
		 }
	}
	
	/**
	 * Method to set up the documents for training the model
	 * @param sc JavaSparkContext
	 * @param evaluation true if you want to perform evaluation of the model (use only 60% of examples for training)
	 * @return a JavaRDD containing the set up documents
	 */
	private JavaRDD<Document> setUpMultiClassDocs(JavaSparkContext sc, boolean evaluation) {
		  JavaRDD<Document>[] splits = null;
		  JavaRDD<Document> unionRDD = null;
		  File trainingFolder = new File(trainExamplesPath);
		  File[] categories = trainingFolder.listFiles();
		  try {
			  for(int i = 0; i< categories.length; i++){
				  List<String> train = new ArrayList<String>();
				  readMultiClassDocuments(categories[i], train);
				  JavaRDD<String> textRDD = sc.parallelize(train);
				  double category = i;
				  JavaRDD<Document> categoryRDD = textRDD.map(f -> createDocument(f, category));
				  types.put(category, categories[i].getName());
				  if(unionRDD == null){
					  unionRDD = categoryRDD;
				  }
				  else{
					  unionRDD = unionRDD.union(categoryRDD);
				  }
			  }
			  if(evaluation){
				  splits = unionRDD.randomSplit(new double[] {0.6, 0.4}, 11L);
				  testDocRDD = splits[1];
				  return splits[0];
			  }
			} catch (IOException e) {
				LOGGER.error("IOEXCeption during reading of documents");
			}
			return unionRDD;
	}
	
	/**
	 * Helper method to enrich the files
	 * @param text the document text
	 * @param label the classification label
	 * @return the aggregated document
	 */
	private Document createDocument(String text, double label) {
		Document d = new Document("","");
		d.setText(text);
		if(label != -1){
			d.setLabel(label);
		}
		else{
			Annotator anno = new Annotator(); 
			d.setFrames(anno.annotateFrames(text));
			d.setAnnotation(anno.annotateSpotlight(text));
		}
       	return d;
	}
	
	/**
	 * Helper method to read the texts
	 * @param topic Eventtype of the file to be read
	 * @return a list of Strings (the document texts)
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private List<String> readMultiClassDocuments(File path, List<String> train) throws FileNotFoundException, IOException {
		
		if(trainSetSize == null){
			trainSetSize = path.listFiles().length;
		}
		for(int i=0; i < trainSetSize ; i++){
			try(BufferedReader br = new BufferedReader(new FileReader(path.listFiles()[i]))){
	    		train.add(br.readLine());
			}
		}
		return train;
	}
	
	/**
	 * Helper method to clean up the old model.
	 * @param folder folder of the model to delete
	 */
    private void delete(File folder) throws IOException{
    	if(folder.isDirectory()){
    		if(folder.list().length==0){
    		   folder.delete();
    		   LOGGER.info("Directory is deleted : " + folder.getAbsolutePath());
    		}else{
        	   String files[] = folder.list();
        	   for (String temp : files) {
        	      File fileDelete = new File(folder, temp);
        	     delete(fileDelete);
        	   }
        	   if(folder.list().length==0){
           	     folder.delete();
           	     LOGGER.info("Directory is deleted : " + folder.getAbsolutePath());
        	   }
    		}
    	}else{
    		folder.delete();
    		LOGGER.info("File is deleted : " + folder.getAbsolutePath());
    	}
    }

	public static void main(String[] args) {
        MainWorkflow mw = new MainWorkflow();
        mw.train(true);
        String classs = mw.classify("A 1,500-acre wildfire raged near Pilot Rock Conservation Camp above Silverwood Lake on Sunday, fire officials said.");
        System.out.println(classs);
	}
	
}