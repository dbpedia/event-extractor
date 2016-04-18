package crawler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import models.Tree;

public class EventCrawler implements Serializable{

	private ArrayDeque<models.Tree> toVisit = new ArrayDeque<models.Tree>();
	private ArrayDeque<models.Tree> toVisitCategory = new ArrayDeque<models.Tree>();
	private LinkedList<String> visited = new LinkedList<String>();
	private LinkedList<models.Document> documents = new LinkedList<models.Document>();
	private Tree<models.Document> tree;
	private LinkedList<String> blackList = new LinkedList<String>();
	private LinkedList<String> combineList = new LinkedList<String>();
	private boolean category = false;
	private final String CONTENT_API_BASE_URL = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&explaintext=&titles=";
	private final String WIKIPEDIA_BASE_URL = "https://en.wikipedia.org";
	
	public EventCrawler(String start){
		this();
		models.Document startingtPoint = new models.Document(start, WIKIPEDIA_BASE_URL + "/wiki/"+start);
		tree = new Tree<models.Document>(startingtPoint, null);
		toVisit.addFirst(tree);
	}
	public EventCrawler(){
		initializeBlackList();
		initializeCombineList();
	}
	private final void initializeBlackList() {
		try(BufferedReader br = new BufferedReader(new FileReader(getClass().getClassLoader().getResource("blacklist.txt").getFile()))){
	    	String line = br.readLine();
	    	while (line != null) {
	    		blackList.add(line);
			line = br.readLine();
	    	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private final void initializeCombineList() {
		try(BufferedReader br = new BufferedReader(new FileReader(getClass().getClassLoader().getResource("combine.txt").getFile()))){
	    	String line = br.readLine();
	    	while (line != null) {
	    		combineList.add(line);
			line = br.readLine();
	    	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void crawl(boolean categoryTree) {
		while(!toVisit.isEmpty()){
			models.Tree first = toVisit.getFirst();
			if((models.Document)first.getData() != null && !visited.contains(((models.Document)first.getData()).getTitle()) && first.getData() != null && !blackList.contains(((models.Document)first.getData()).getTitle())){
				System.out.println("Queuesize: "+toVisit.size()+", Visiting: " +((models.Document)toVisit.getFirst().getData()).getTitle());
				try {
					if(!categoryTree && combineList.contains(((models.Document)first.getData()).getTitle())){
						category = true;
						crawlArticlesForCategory(first);
						category = false;
					}
					else if(!combineList.contains(((models.Document)first.getData()).getTitle())){
						process(((models.Document)first.getData()).getUrl(),first, categoryTree);				
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(first.getData() != null){
				visited.add(((models.Document)first.getData()).getTitle());	
			}
			toVisit.removeFirst();
		}
	}
	public void crawlArticlesForCategory(Tree parent){
		toVisitCategory.add(parent);
		while(!toVisitCategory.isEmpty()){
			models.Tree first = toVisitCategory.getFirst();
			if(first.getData() != null && !visited.contains(((models.Document)first.getData()).getTitle()) && first.getData() != null && !blackList.contains(((models.Document)first.getData()).getTitle())){
				System.out.println("Category-Queuesize: "+toVisitCategory.size()+", Visiting: " +((models.Document)toVisitCategory.getFirst().getData()).getTitle());
				try {
					process(((models.Document)first.getData()).getUrl(), parent, false);					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(first.getData() != null){
				visited.add(((models.Document)first.getData()).getTitle());	
			}
			toVisitCategory.removeFirst();
		}
		toVisitCategory.clear();
	}
	private void process(String url, Tree parent, boolean categoryTree){
		ArrayDeque visit;
		if(category) { visit = toVisitCategory;} else { visit = toVisit;}
		Document doc = Jsoup.parse(getHTML(url));
		Elements body = doc.select("div.mw-content-ltr");
		if(!body.isEmpty() && (!body.select("#mw-pages").isEmpty() || !body.select("#mw-subcategories").isEmpty())){
			Elements links = body.select("a[href]"); // a with href
			for(Element link : links){
				models.Document docu = null;
				try{
					docu = new models.Document(link.attr("href").split("/wiki/")[1], WIKIPEDIA_BASE_URL + link.attr("href"));
				}
				catch(ArrayIndexOutOfBoundsException e){
				//Catch unimportant stuff like editiing pages, etc.
				}
				Tree<models.Document> t = new Tree<models.Document>(docu, parent);	
				if(!link.attr("href").contains("File:") && !link.attr("href").contains("Help:") && !link.attr("href").contains("Wikipedia:") && !link.attr("href").contains("Special:") && !link.attr("href").contains("Template:")){
					if(categoryTree){
						if(link.attr("href").contains("Category:")){
							visit.addLast(t);
						}
					}
					else{
						visit.addLast(t);					
					}
				}
			}
		}
		//Get text if article
		else if(!categoryTree){		
			String pageTitle = url.replace(WIKIPEDIA_BASE_URL+"/wiki/", "");
			JSONObject json = new JSONObject(getHTML(CONTENT_API_BASE_URL+pageTitle));
			if(json.getJSONObject("query").getJSONObject("pages") != null){
				for(String key : json.getJSONObject("query").getJSONObject("pages").keySet()){
					if(json.getJSONObject("query").getJSONObject("pages").getJSONObject(key).has("extract")){
						String text = json.getJSONObject("query").getJSONObject("pages").getJSONObject(key).getString("extract");
						if(text != null){
							models.Document docu = new models.Document(pageTitle, url);
							docu.setText(text);
							Tree t = new Tree<models.Document>(docu, parent.getParent());
							if(!visited.contains(pageTitle)){
								t.addNode();
							}
							visited.add(pageTitle);
							documents.add(docu);
						}
					}
				}
			}
		}
	}
	private String getHTML(String urlToRead){
		StringBuilder result = new StringBuilder();
	    URL url;
		try {
			url = new URL(urlToRead);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    conn.setRequestMethod("GET");
		    try(BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))){
			    String line;
			    while ((line = rd.readLine()) != null) {
			       result.append(line);
			    }
		    }
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			//usually wikimedia links. unusable.
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return result.toString();
	}
	public Tree<models.Document> getTree(){
		return tree;
	}
	public List<models.Document> getDocuments(){
		return documents;
	}
	public void getDocumentsByType(){
	
	}
//	public void crawl(){
//	while(!toVisit.isEmpty() && crawlDepth > 0){
//		models.Tree first = toVisit.getFirst();
//		if(!visited.contains(first) && first.getData() != null && !blacklist.contains(((models.Document)first.getData()).getTitle())){
//			System.out.println("Queuesize: "+toVisit.size()+", Visiting: " +((models.Document)toVisit.getFirst().getData()).getTitle());
//			first.addNode();
//			try {
//				process(((models.Document)first.getData()).getUrl(), first);					
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			crawlDepth--;
//		}
//		visited.add(first);
//		toVisit.removeFirst();
//	}
//}
}