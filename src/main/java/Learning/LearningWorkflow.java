package Learning;


import java.io.Serializable;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.IDFModel;
import org.apache.spark.ml.feature.Tokenizer;
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
import scala.Tuple2;

public class LearningWorkflow{
    private static final Logger LOGGER = LoggerFactory.getLogger(LearningWorkflow.class);
    private JavaSparkContext sc;
    JavaRDD<LabeledPoint> train;
    JavaRDD<LabeledPoint> test;
    
    public LearningWorkflow(JavaSparkContext sc){
		this.sc = sc;
	}
    /**
     * Preprocessing, Stop-word removal, lemmatizing, TF-IDF calculation, splitting docs into training and test set
     * @param docs the documents to preprocess
     */
    public void preprocess(JavaRDD<Document> docs){
        //Preprocessing
        JavaRDD<Document> preprocessedDocuments = docs.map(f -> {
        	f.setText(StanfordUtils.lemmatizeArticle(f.getText()));
        	return f;
        });
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
		IDF idf = new IDF().setInputCol("rawFeatures").setOutputCol("features");
		IDFModel idfModel = idf.fit(featurizedData);
		DataFrame rescaledData = idfModel.transform(featurizedData);
		JavaRDD<Row> rows = rescaledData.rdd().toJavaRDD();
        JavaRDD<LabeledPoint>  data = rows.map(f -> new LabeledPoint(f.getDouble(0), f.getAs(4)));
        
        //Split initial RDD into two... [60% training data, 40% testing data].
        train = data.sample(false, 0.6, 11L);
        train.cache();
        test = data.subtract(train);
    }
    /**
     * //Training of the model
     * @return the model
     */
    public SVMModel createModel(){
        int numIterations = 100;
        SVMModel model = SVMWithSGD.train(train.rdd(), numIterations);
        return model;
    }
    /**
     * evaluation of the given model
     * @param model the model to evaluate
     */
    public void evalModel(SVMModel model){
    	model.clearThreshold();
    	 //Testing
         // Compute raw scores on the test set.
         JavaRDD<Tuple2<Object, Object>> scoreAndLabels = test.map(p -> {
               Double score = model.predict(p.features());
               return new Tuple2<Object, Object>(score, p.label());
           }
         ) ;
         // Get evaluation metrics.
         BinaryClassificationMetrics metrics =
           new BinaryClassificationMetrics(JavaRDD.toRDD(scoreAndLabels));
         double auROC = metrics.areaUnderROC();
         
         System.out.println("Area under ROC = " + auROC);
    }
}
