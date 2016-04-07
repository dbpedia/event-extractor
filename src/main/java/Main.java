import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import crawler.EventCrawler;
import models.Tree;

public class Main {
		public static void ser() {
		EventCrawler e = new EventCrawler("Category:Natural_disasters");
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
		//EventCrawler e = deser();
		//System.out.println(e.getTree());
		//System.out.println(e.getDocuments());
	}

}
