package util;

import java.util.*;

public class Sentence {

	private LinkedList<String> words;
	
	public Sentence(String s){
		String[] split = s.split("\\s");
		words = new LinkedList<String>();
		if(split.length>0){
			for(int i=0;i<split.length;i++){
				split[i] = split[i].trim();
				if(!split[i].equals("")){
					words.addLast(split[i]);
				}
			}
		}
	}
	
	public Sentence(LinkedList<String> words){
		this.words = new LinkedList<String>();
		this.words.addAll(words);
	}

	public LinkedList<String> getWords() {
		LinkedList<String> res = new LinkedList<String>();
		res.addAll(words);
		return res;
	}

	public void setWords(LinkedList<String> words) {
		this.words = words;
	}
	
	public Iterator<String> iterator(){
		return this.words.iterator();
	}
	
	public String toString(){
		
		String result = "";
		Iterator<String> it = this.words.iterator();
		String next = "";
		
		while(it.hasNext()){
			next = it.next();
			result = result+ next+" ";
		}
		return result.trim();
	}
	
	public String get(int pos){
		return this.words.get(pos);
	}
	
	public Sentence set(int pos, String word){
		
		LinkedList<String> tmp = new LinkedList<String>();
		tmp.addAll(this.words);
		tmp.set(pos,word);
		Sentence result = new Sentence(tmp);
		return result;
	}
	
	public int size(){
		return this.words.size();
	}
	
}
