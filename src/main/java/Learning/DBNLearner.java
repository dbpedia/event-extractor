package Learning;

import java.util.Collections;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RBM;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBNLearner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBNLearner.class);
    private int SEED = 123;
    private int OUTPUT_NUM = 10;
    private int BATCH_SIZE = 50;
    private int ITERATIONS = 10;
    private int NUM_ROWS;
    private int NUM_COLUMNS;
    private int LISTENER_FREQ = BATCH_SIZE / 5;
 
    private int nCores;
    
    public DBNLearner(int nCores){
    	this.nCores = nCores;
    }

    public MultiLayerNetwork train(JavaRDD<DataSet> train, JavaSparkContext sc) {

    	//Set up network configuration
		LOGGER.info("Build model....");
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
		     .seed(SEED)
		     .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
		     .gradientNormalizationThreshold(1.0)
		     .weightInit(WeightInit.XAVIER)
		     .iterations(ITERATIONS)
		     .momentum(0.5)
		     .momentumAfter(Collections.singletonMap(3, 0.9))
		     .optimizationAlgo(OptimizationAlgorithm.CONJUGATE_GRADIENT)
		     .list()
		     .layer(0, new RBM.Builder().nIn(NUM_ROWS*NUM_COLUMNS).nOut(500)
		                  .lossFunction(LossFunction.RMSE_XENT)
		                  .visibleUnit(RBM.VisibleUnit.BINARY)
		                  .hiddenUnit(RBM.HiddenUnit.BINARY)
		                  .build())
		     .layer(1, new RBM.Builder().nIn(500).nOut(250)
		                  .lossFunction(LossFunction.RMSE_XENT)
		                  .visibleUnit(RBM.VisibleUnit.BINARY)
		                  .hiddenUnit(RBM.HiddenUnit.BINARY)
		                  .build())
		     .layer(2, new RBM.Builder().nIn(250).nOut(200)
		                  .lossFunction(LossFunction.RMSE_XENT)
		                  .visibleUnit(RBM.VisibleUnit.BINARY)
		                  .hiddenUnit(RBM.HiddenUnit.BINARY)
		                  .build())
		     .layer(3, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD).activation("softmax")
		                  .nIn(200).nOut(OUTPUT_NUM).build())
		     .pretrain(true).backprop(false)
		     .build();
	
	    MultiLayerNetwork NET = new MultiLayerNetwork(conf);
	    NET.init();
	    NET.setListeners(Collections.singletonList((IterationListener) new ScoreIterationListener(LISTENER_FREQ)));
	    
	    //Create Spark multi layer network from configuration
	    SparkDl4jMultiLayer sparkNetwork = new SparkDl4jMultiLayer(sc, NET);
	
	    //Train network
	    LOGGER.info("--- Starting network training ---");
	    NET = sparkNetwork.fitDataSet(train, nCores * BATCH_SIZE, nCores);
	    return NET;
    }
}
