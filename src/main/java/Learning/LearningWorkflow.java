package Learning;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.storage.StorageLevel;
import org.canova.api.records.reader.RecordReader;
import org.canova.api.records.reader.impl.CSVRecordReader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.spark.canova.RecordReaderFunction;
import org.nd4j.linalg.dataset.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Preprocessing.StanfordUtils;
import models.Document;

import java.util.*;
public class LearningWorkflow {
    private static final Logger LOGGER = LoggerFactory.getLogger(LearningWorkflow.class);
    private int LABEL_INDEX =  4;
    private int NUM_OUTPUT_CLASSES = 3;
    private int RANDOM_SEED = 12345;
    private int nCores;
    
    public LearningWorkflow(int cores){
    	nCores = cores;
    }
    
    public MultiLayerNetwork createModel(JavaRDD<Document> docs, JavaSparkContext sc){
        
        //Preprocessing
        JavaRDD<Document> preprocessedDocuments = docs.map(f -> {
        	f.setText(StanfordUtils.lemmatizeArticle(f.getText()));
        	return f;
        });
        JavaRDD<String> csvData = preprocessedDocuments.map(f -> createCSVRepresentation(f));
        RecordReader recordReader = new CSVRecordReader(0,",");
        JavaRDD<DataSet> allData = csvData.map(new RecordReaderFunction(recordReader, LABEL_INDEX, NUM_OUTPUT_CLASSES));
       
        //Split in Trainingset and Testset
        //Bottleneck? begin
        List<DataSet> dataList = allData.collect();
        Collections.shuffle(dataList,new Random(RANDOM_SEED));

        int nTrain = dataList.size()/3;
        int nTest = dataList.size()-nTrain;
        Iterator<DataSet> iter = dataList.iterator();
        List<DataSet> train = new ArrayList<>(nTrain);
        List<DataSet> test = new ArrayList<>(nTest);

        int c = 0;
        while(iter.hasNext()){
            if(c++ <= nTrain) train.add(iter.next());
            else test.add(iter.next());
        }

        JavaRDD<DataSet> sparkDataTrain = sc.parallelize(train);
        sparkDataTrain.persist(StorageLevel.MEMORY_ONLY());
        //Bottlenet? end
        
        //Training
        CNNLearner cnn = new CNNLearner(nCores);
        MultiLayerNetwork model = cnn.train(sparkDataTrain, sc);
        
        DBNLearner dbn = new DBNLearner(nCores);
        MultiLayerNetwork model2 = dbn.train(sparkDataTrain, sc);
        
        
        //Testing
        ModelEvaluate.evaluate(model, test, 10);
        ModelEvaluate.evaluate(model2, test, 10);
        
        return model;

    }

	private String createCSVRepresentation(Document doc) {
		// TODO Auto-generated method stub
		return null;
	}
}
