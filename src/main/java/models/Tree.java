package models;

import java.io.Serializable;
import java.util.ArrayList;

public class Tree<T> implements Serializable {
    private Tree<T> parent;
    private T data;
    private ArrayList<Tree<T>> children = new ArrayList<Tree<T>>();

    public Tree(T data, Tree<T> parent) {
    	this.parent = parent;
    	this.setData(data);
    }
    public void addNode() {
    	if(parent != null){
    		parent.addChild(this);
    	}
	}
	public void addChild(Tree<T> tree){
    	children.add(tree);
    }
	public void removeChild(Tree<T> tree){
		children.remove(tree);
	}
	public Tree<T> getParent(){
		return parent;
	}
    @Override
    public String toString(){	    	
    	return toStringHelper(0);
    }
    private String toStringHelper(int depth){
    	String tab = "";
    	int depthcount = depth;
    	while(depthcount > 0){
    		tab = tab + "\t";
    		depthcount--;
    	}
    	String treeString = "";
    	treeString = tab + getData().toString() ;
    	
    	if(!this.children.isEmpty()){
    		treeString += "[\n";
    	}
    	for(Tree child : children){
    		treeString = treeString + child.toStringHelper(depth+1);
    	}
    	if(!this.children.isEmpty()){
    		treeString += tab + "]";
    	}
    	treeString +=  "\n";
    	return treeString;
    }
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
}