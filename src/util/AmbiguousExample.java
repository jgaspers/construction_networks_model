package util;

import java.util.*;

public class AmbiguousExample{

	private Sentence sentence;
	private Vector<Predicate> semantics;
	
	public AmbiguousExample(Sentence sentence, Vector<Predicate> semantics){
		this.sentence = sentence;
		this.semantics = semantics;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}

	public Vector<Predicate> getSemantics() {
		return semantics;
	}

	public void setSemantics(Vector<Predicate> semantics) {
		this.semantics = semantics;
	}
	
	public String toString(){
		
		String res = this.sentence+"\n";
		Iterator<Predicate> it = this.semantics.iterator();
		
		while(it.hasNext()){
			res += it.next()+"\n";
		}
		
		return res;
	}

	public LinkedList<String> getWords(){
		return this.getSentence().getWords();
	}
	
	public LinkedList<String> getContext(){
		
		LinkedList<String> context = new LinkedList<String>();
		Iterator<Predicate> it = this.semantics.iterator();
		
		while(it.hasNext()){
			context.addAll(it.next().getContext());
		}
		
		return context;
	}
	
	public HashSet<String> getContextTypes(){
		
		HashSet<String> context = new HashSet<String>();
		
		Iterator<Predicate> it = this.semantics.iterator();
		
		while(it.hasNext()){
			context.addAll(it.next().getContext());
		}
		
		return context;
	}
	
	public HashSet<String> getArgumentTypes(){
		
		HashSet<String> context = new HashSet<String>();
		
		Iterator<Predicate> it = this.semantics.iterator();
		
		while(it.hasNext()){
			context.addAll(it.next().getArguments());
		}
		
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
		return this.semantics;
	}
	
	public HashSet<String> getSubsequences(int sequenceLength){
		
		HashSet<String> subsequences = new HashSet<String>();
		LinkedList<String> words = sentence.getWords();
		
		for(int i=1;i<=sequenceLength;i++){
		
			for(int j=0;j<words.size();j++){
				
				String sequence = "";
				
				go: for(int k=j;k<j+i;k++){
				
					if(k>words.size()-1){
						break go;
					}
					sequence += words.get(k)+" ";
				}
				
				sequence = sequence.substring(0,sequence.length()-1);
				subsequences.add(sequence);
				
			}
		}
		
		return subsequences;
	}
	
	public void lowercase(){
		String lower = sentence.toString().toLowerCase();
		this.sentence = new Sentence(lower);
	}

	
	public int length(){
		return this.sentence.size();
	}
}
