package Main;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import Annotation.Annotator;
import crawler.EventCrawler;
import models.Document;
import models.Tree;

public class Main {
		public static void ser() {
		EventCrawler e = new EventCrawler("Category:Oil_spills");
		e.crawl(false);
		try(ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("crawledStuff.ser")))){
			output.writeObject(e);
		}
		catch(Exception ex){}
		System.out.println(e.getTree().toString());
	}
	public static EventCrawler deser() {
		EventCrawler e = null;
		try(ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream("crawledStuff.ser")))){
			e = (EventCrawler)input.readObject();
		}
		catch(FileNotFoundException ex){			ex.printStackTrace();}
		catch(IOException ex){			ex.printStackTrace();} 
		catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return e;
	}
	public static void main(String[] args) {
		ser();
		EventCrawler e = deser();
		System.out.println(e.getTree());
		/*List<Document> l = e.getDocuments();
		try(BufferedWriter output = new BufferedWriter(new FileWriter("EarthquakeDocs.txt", true))){
			for(Document d : l){
				String text = d.getText().replace("\n", "").trim();
				String title = d.getTitle().replace("\n", "").trim();
				output.write(title + "<col>"+text);
				output.write("\n");
			}
		}
		catch(Exception ex){System.out.println("e");}
		}*/
	}
		

}