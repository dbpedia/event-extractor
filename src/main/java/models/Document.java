package models;

import java.io.Serializable;

public class Document implements Serializable{

	private String url;
	private String title;
	private Tree<String> tree;
	private String text;
	
	public Document(String url, Tree<String> tree){
		this.setUrl(url);
		this.setTree(tree);
		this.setTitle(tree.getData());
	}
	public Document(String title, String url){
		this.setTitle(title);
		this.setUrl(url);
	}
	@Override
	public String toString(){
		return getTitle();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Tree<String> getTree() {
		return tree;
	}

	public void setTree(Tree<String> tree) {
		this.tree = tree;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
