package Learning;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CNNLearner {
    private static final Logger LOGGER = LoggerFactory.getLogger(CNNLearner.class);
    private int SEED = 123;
    private int N_CHANNELS = 1;
    private int OUTPUT_NUM = 10;
    private int BATCH_SIZE = 50;
    private int ITERATIONS = 1;
    private int nCores;
    public CNNLearner(int nCores){
    	this.nCores = nCores;
    }

    public MultiLayerNetwork train(JavaRDD<DataSet> train, JavaSparkContext sc) {
	
	    //Set up network configuration
	    LOGGER.info("Build model....");
	    MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
	            .seed(SEED)
	            .iterations(ITERATIONS)
	            .regularization(true).l2(0.0005)
	            .learningRate(0.1)
	            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	            .updater(Updater.ADAGRAD)
	            .list()
	            .layer(0, new ConvolutionLayer.Builder(5, 5)
	                    .nIn(N_CHANNELS)
	                    .stride(1, 1)
	                    .nOut(20)
	                    .weightInit(WeightInit.XAVIER)
	                    .activation("relu")
	                    .build())
	            .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
	                    .kernelSize(2, 2)
	                    .build())
	            .layer(2, new ConvolutionLayer.Builder(5, 5)
	                    .nIn(20)
	                    .nOut(50)
	                    .stride(2,2)
	                    .weightInit(WeightInit.XAVIER)
	                    .activation("relu")
	                    .build())
	            .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
	                    .kernelSize(2, 2)
	                    .build())
	            .layer(4, new DenseLayer.Builder().activation("relu")
	                    .weightInit(WeightInit.XAVIER)
	                    .nOut(200).build())
	            .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
	                    .nOut(OUTPUT_NUM)
	                    .weightInit(WeightInit.XAVIER)
	                    .activation("softmax")
	                    .build())
	            .backprop(true).pretrain(false);
	    new ConvolutionLayerSetup(builder,28,28,1);
	
	    MultiLayerConfiguration conf = builder.build();
	    MultiLayerNetwork NET = new MultiLayerNetwork(conf);
	    NET.init();
	    NET.setUpdater(null);   //Workaround for minor bug in 0.4-rc3.8
	
	    //Create Spark multi layer network from configuration
	    SparkDl4jMultiLayer sparkNetwork = new SparkDl4jMultiLayer(sc, NET);
	
	    //Train network
	    LOGGER.info("--- Starting network training ---");
	    int nEpochs = 5;
	    for( int i=0; i<nEpochs; i++ ){
	        NET = sparkNetwork.fitDataSet(train, nCores * BATCH_SIZE, nCores);
	        System.out.println("----- Epoch " + i + " complete -----");
	    }
	    return NET;
	}
}
