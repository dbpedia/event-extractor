package Learning;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.feature.IDFModel;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.Document;
import scala.Tuple2;
/**
 * Class to evaluate trained models
 * @author Vincent Bohlen (vincent.bohlen@fu-berlin.de)
 *
 */
public class ModelEvaluate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelEvaluate.class);
	
	/**
	 * Evaluation method for the trained model
	 * @param sc JavaSparkContext
	 * @param model the model to evaluate
	 * @param idf 
	 */
	public static void evaluate(JavaSparkContext sc, LogisticRegressionModel model, IDFModel idf, JavaRDD<Document> testDocs) {
		LearningWorkflow lw = new LearningWorkflow(sc);
    	JavaRDD<LabeledPoint> test = lw.preprocess(testDocs, idf, true);
    	
    	JavaRDD<Tuple2<Object, Object>> scoreAndLabels = test.map(p -> {
    	          Double prediction = model.predict(p.features());
    	          return new Tuple2<Object, Object>(prediction, p.label());
    	});
    	MulticlassMetrics metrics = new MulticlassMetrics(scoreAndLabels.rdd());
 		double pre = metrics.precision();
 		double rec = metrics.recall();
 		double fmeasure = metrics.fMeasure();
 		
 		LOGGER.info("Results: "+ scoreAndLabels.count());
 		LOGGER.info(scoreAndLabels.collect().toString());
 		LOGGER.info("Precission = " + pre);
 		LOGGER.info("Recall = " + rec);
 		LOGGER.info("FMeasure = " + fmeasure);
 		
 		/* Keep for preferred visualization
 		System.out.println("Results: "+ scoreAndLabels.count());
 		System.out.println(scoreAndLabels.collect().toString());
 		System.out.println("Precission = " + pre);
 		System.out.println("Recall = " + rec);
 		System.out.println("FMeasure = " + fmeasure);*/
	}
}
