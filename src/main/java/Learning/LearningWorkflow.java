package Learning;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.IDFModel;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.classification.SVMWithSGD;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Preprocessing.StanfordUtils;
import models.Document;
import models.framenet.Frame;
import scala.Tuple2;
/**
 * Class to handle the preprocessing and training
 * @author Vincent Bohlen (vincent.bohlen@fu-berlin.de)
 *
 */
public class LearningWorkflow{
    private static final Logger LOGGER = LoggerFactory.getLogger(LearningWorkflow.class);
    private JavaSparkContext sc;
    JavaRDD<LabeledPoint> train;
    
    public LearningWorkflow(JavaSparkContext sc){
		this.sc = sc;
	}
    /**
     * Training of TFIDF representation. Use pre-annotated docs for faster calculation.
     * @param docs the documents to preprocess
     * @return 
     */
    public IDFModel trainIDFModel(JavaRDD<Document> docs){
        //Preprocessing
     	JavaRDD<Document> preprocessedDocuments = docs.map(f -> {
    		f.setText(StanfordUtils.lemmatizeArticle(f.getText()));
    		return f;
    		});
        JavaRDD<Row> jrdd = docs.map(f -> RowFactory.create(f.getLabel(), f.getText()));
		
		StructType schema = new StructType(new StructField[]{
		  new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
		  new StructField("sentence", DataTypes.StringType, false, Metadata.empty())
		});
		SQLContext sqlContext = new SQLContext(sc);
		DataFrame sentenceData = sqlContext.createDataFrame(jrdd, schema);
		Tokenizer tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words");
		DataFrame wordsData = tokenizer.transform(sentenceData);
		int numFeatures = 20;
		HashingTF hashingTF = new HashingTF()
		  .setInputCol("words")
		  .setOutputCol("rawFeatures")
		  .setNumFeatures(numFeatures);
		DataFrame featurizedData = hashingTF.transform(wordsData);
		IDF idf = new IDF().setInputCol("rawFeatures").setOutputCol("features");
		IDFModel idfModel = idf.fit(featurizedData);
        return idfModel;
    }
    
    /**
     * Preprocessing: Stop-word removal, lemmatizing, TF-IDF calculation, (possibly addition of annotations)
     * @param docs the documents to preprocess
     * @return 
     */
    public JavaRDD<LabeledPoint> preprocess(JavaRDD<Document> docs, IDFModel idfModel, boolean training){
        //Preprocessing
    	JavaRDD<Document> preprocessedDocuments = null;
    	if(training){
    	preprocessedDocuments = docs.map(f -> {
    		f.setText(StanfordUtils.lemmatizeArticle(f.getText()));
    		return f;
    		});
    	}
    	else{	
	        preprocessedDocuments = docs.map(f -> {
	       	StringBuilder sb = new StringBuilder();
	        	sb.append(StanfordUtils.lemmatizeArticle(f.getText()));
	        	sb.append(" ");
	        	sb.append(f.getAnnotation().getTypes());
	        	for(Frame frame : f.getFrames()){
	        		sb.append(frame.getTarget().getName());
	        		sb.append(" ");
	        	}
	        	f.setText(sb.toString().trim());
	        	return f;
	       });
    	}
        JavaRDD<Row> jrdd = preprocessedDocuments.map(f -> RowFactory.create(f.getLabel(), f.getText()));
		
		StructType schema = new StructType(new StructField[]{
		  new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
		  new StructField("sentence", DataTypes.StringType, false, Metadata.empty())
		});
		SQLContext sqlContext = new SQLContext(sc);
		DataFrame sentenceData = sqlContext.createDataFrame(jrdd, schema);
		Tokenizer tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words");
		DataFrame wordsData = tokenizer.transform(sentenceData);
		int numFeatures = 20;
		HashingTF hashingTF = new HashingTF()
		  .setInputCol("words")
		  .setOutputCol("rawFeatures")
		  .setNumFeatures(numFeatures);
		DataFrame featurizedData = hashingTF.transform(wordsData);
		DataFrame rescaledData = idfModel.transform(featurizedData);
		JavaRDD<Row> rows = rescaledData.rdd().toJavaRDD();
		JavaRDD<LabeledPoint>  data = rows.map(f -> new LabeledPoint(f.getDouble(0), f.getAs(4)));
        train = data;
        train.cache();
        return train;
    }
    /**
     * //Training of the logReg model
     * @return the model
     */
    public LogisticRegressionModel createLogRegModel(int numClasses){
    	LogisticRegressionModel model = new LogisticRegressionWithLBFGS()
    		      .setNumClasses(numClasses)
    		      .run(train.rdd());
    	return model;
    }
}
