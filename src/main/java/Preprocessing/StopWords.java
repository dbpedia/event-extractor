package Preprocessing;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Main.MainWorkflow;
/**
 * Created by wojlukas
 * Adjusted by Vincent Bohlen (vincent.bohlen@fu-berlin.de)
 */
public class StopWords {
	
	private static HashSet<String> stopWords = new HashSet<String>();
    private final Logger LOGGER = LoggerFactory.getLogger(StopWords.class);

	private static final Pattern UNDESIRABLES = Pattern.compile("[\\]\\[(){},.;!?<>%]");

	public String removePunctuation(String x) {
	    return UNDESIRABLES.matcher(x).replaceAll("");
	}

	public StopWords() {
		stopWords.addAll(readFromCSV(getClass().getClassLoader().getResourceAsStream("stopwords-net.csv")));
		stopWords.addAll(readFromCSV(getClass().getClassLoader().getResourceAsStream("stopwords-dvita.csv")));
	}

	public StopWords(String fileName) {
		try(BufferedReader bufferedreader = new BufferedReader(new FileReader(fileName))){
			while (bufferedreader.ready()) {
				stopWords.add(bufferedreader.readLine());
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void addStopWord(String word) {
		stopWords.add(word);
	}

	public List<String> readFromCSV(InputStream fileName){
        List<String> words = new ArrayList<String>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(fileName))){
        	String line = br.readLine();
        	while (line != null) {
        		words.add(line.split(",")[0]);
        		line = br.readLine();
        	}
        } catch (IOException e) {
        	LOGGER.error(e.getMessage());
		}
		return words;
	}

	public String[] removeStopWords(String[] words) {
		ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(words));
		for (int i = 0; i < tokens.size(); i++) {
			if (stopWords.contains(tokens.get(i))) {
				tokens.remove(i);
			}
		}
		return (String[]) tokens.toArray(new String[tokens.size()]);
	}
	
	public HashSet<String> getStopWords(){
		return stopWords;
		
	}
	
   public void printStopWords(){
	   Iterator<String> it = stopWords.iterator();
	   
	   while (it.hasNext()) {
            String word = (String) it.next();
            System.out.println(word);
        }
	   
   }
	
	public static void main(String[] args) {
		StopWords sw = new StopWords();
		sw.printStopWords();
		
	}
}
