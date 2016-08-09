package Preprocessing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
/**
 * Created by wojlukas
 */
public class StopWords {
	
	private static HashSet<String> stopWords = new HashSet<String>();
	
	private static final Pattern UNDESIRABLES = Pattern.compile("[\\]\\[(){},.;!?<>%]");

	public String removePunctuation(String x) {
	    return UNDESIRABLES.matcher(x).replaceAll("");
	}

	public StopWords() {
		stopWords.addAll(readFromCSV(getClass().getClassLoader().getResource("stopwords-net.csv").getFile()));
		stopWords.addAll(readFromCSV(getClass().getClassLoader().getResource("stopwords-dvita.csv").getFile()));
	}

	public StopWords(String fileName) {
		try {
			BufferedReader bufferedreader = new BufferedReader(new FileReader(fileName));
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

	public List<String> readFromCSV(String fileName){
        List<String> words = new ArrayList<String>();

        try {
            Files.lines(Paths.get(fileName))
                .filter(line -> line.length() > 1)
                .forEach(line -> {
                    words.add(line.split(",")[0]);
                });
        } catch (IOException e) {
            e.printStackTrace();
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
