package Learning;

import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;

public class ModelEvaluate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelEvaluate.class);
	
	public static void evaluate(SVMModel[] models, JavaRDD<LabeledPoint> test){
   	 	//Testing
        // Compute raw scores on the test set.
        JavaRDD<Tuple2<Object, Object>> scoreAndLabels = test.map(p -> {
        	double score = 0.0;
        	double label = -1;
    		for(SVMModel model : models){  
    			model.clearThreshold();
    			Double curScore = model.predict(p.features());
    			if(curScore > score){
    				score = curScore;
    				label = p.label();
    			}
    		}
			System.out.println(score+ ": " + label);
			return new Tuple2<Object, Object>(score, label);
          }
        ) ;
	}
}
