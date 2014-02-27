package util;

import java.util.*;

public class Example{
	
	private Sentence sentence;
	private Predicate semantics;
	
	public Example(Sentence text, Predicate semantics){
		this.sentence = text;
		this.semantics = semantics;
	}
	
	public Example(String text, Predicate semantics){
		this.sentence = new Sentence(text);
		this.semantics = semantics;
	}
	
	public Example(){
		
	}

	public Sentence getText() {
		return sentence;
	}

	public void setText(String text) {
		this.sentence = new Sentence(text);
	}

	public Predicate getSemantics() {
		return semantics;
	}

	public void setSemantics(Predicate semantics) {
		this.semantics = semantics;
	}
	
	public String toString(){
		return this.sentence+" : "+this.semantics;
	}
	
	public LinkedList<String> getWords(){
		return this.sentence.getWords();
	}
	
	public LinkedList<String> getContext(){
		return this.semantics.getContext();
	}
	
	public HashSet<String> getContextTypes(){
		HashSet<String> context = new HashSet<String>();
		context.addAll(this.semantics.getContext());

		return context;
	}
	
	public HashSet<String> getWordTypes(){
		
		HashSet<String> words = new HashSet<String>();
		words.addAll(this.sentence.getWords());
		
		return words;
	}
	
	public String getAllText(){
		return this.sentence.toString();
	}
	
	public Vector<Predicate> getAllSemantics(){
		Vector<Predicate> result = new Vector<Predicate>();
		result.add(this.semantics);
		return result;
	}
	
	public void lowercase(){
		String lower = sentence.toString().toLowerCase();
		this.sentence = new Sentence(lower);
	}
}
