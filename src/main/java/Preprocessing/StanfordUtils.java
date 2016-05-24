package Preprocessing;

import java.util.HashSet;
import java.util.List;

public class StanfordUtils {
	private static StopWords sw = new StopWords();
    private static StanfordLemmatizer sl = new StanfordLemmatizer();

	public static String lemmatizeArticle(String article) {
		HashSet<String> stopwords = sw.getStopWords();
		StringBuilder sb = new StringBuilder();

		List<String> lemmas = sl.lemmatize(article);

		for (String lemma : lemmas) {
			lemma = lemma.toLowerCase();
			if (!stopwords.contains(lemma) && lemma.length() > 2) {
				sb.append(lemma);
				sb.append(" ");
			}
		}
		return sb.toString().trim();
	}
	
	public static void main(String[] args) {
		System.out.println(lemmatizeArticle("Heavy rainfall in the early February 2016 has caused major flooding in the state of Sarawak, Johor, Malacca and parts of Negeri Sembilan."));
	}
}
