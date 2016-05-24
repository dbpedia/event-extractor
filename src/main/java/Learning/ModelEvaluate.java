package Learning;

import java.util.List;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelEvaluate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelEvaluate.class);

	public static void evaluate(MultiLayerNetwork model, List<DataSet> test, int outputNum){
		Evaluation eval = new Evaluation(outputNum);
	    for(DataSet ds : test){
	        INDArray output = model.output(ds.getFeatureMatrix());
	        eval.eval(ds.getLabels(),output);
	    }
	    LOGGER.info(eval.stats());
	}
}
