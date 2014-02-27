package main;

import network.*;
import util.*;
import java.util.*;
import struct.*;
import java.io.*;

public class Model {

	private Associator aleAssociator;
	private double entropyThresh;
	private Graph graph;
	private double weightThresh;
	private double pathThresh;
	private double entropyThresh2;
	private double precision;
	private double recall;
	private int grammarSize;
	private String eval;
	private String extractedGrammar;
	private boolean syntactic;
	
	public Model(double entropyThresh,double weigthThresh,double pathThresh,double entropyThresh2){
		this.entropyThresh = entropyThresh;
		this.pathThresh = pathThresh;
		this.entropyThresh2 = entropyThresh2;
		this.weightThresh = weigthThresh;
		this.aleAssociator = new Associator(0.01,0.01,1);
		this.graph = new Graph(entropyThresh);
		this.syntactic = true;
		MeaningRepresentation.reset();
	}
	
	public Model(double entropyThresh,double weigthThresh,double pathThresh,double entropyThresh2,boolean syntactic){
		this.entropyThresh = entropyThresh;
		this.pathThresh = pathThresh;
		this.entropyThresh2 = entropyThresh2;
		this.weightThresh = weigthThresh;
		this.aleAssociator = new Associator(0.01,0.01,1);
		this.graph = new Graph(entropyThresh);
		this.syntactic = syntactic;
	}
	
	public void run(LinkedList<AmbiguousExample> examples){
		
		Iterator<AmbiguousExample> it = examples.iterator();
		HashSet<String> arguments = new HashSet<String>();

		while(it.hasNext()){
			
			AmbiguousExample example = it.next();
			HashSet<String> ales = example.getSubsequences(2);
			
			if(example.getArgumentTypes().size()>0){
				arguments.addAll(example.getArgumentTypes());
			}
			
			if(example.getContextTypes().size()>0){
				HashSet<String> argTypes = example.getContextTypes();
				argTypes.add("none");
				this.aleAssociator.train(ales, argTypes);
			}
		
			HashSet<String> haveCombinedMeaning = new HashSet<String>();
			Iterator<String> aleIt = ales.iterator();
			LinkedList<String> words = example.getSentence().getWords();
			
			// preprocessing
			while(aleIt.hasNext()){
				String ale = aleIt.next();
				
				if(aleAssociator.containsX(ale)&&aleAssociator.hasLearnedMeaningEntropy(ale,entropyThresh,weightThresh)){
					
					String[] split = ale.split(" ");
					double combinedWeight = aleAssociator.getWeight(ale,aleAssociator.getAssociation(ale));
					
					if(split.length==2){
						double weight1 = aleAssociator.getWeight(split[0],aleAssociator.getAssociation(split[0]));
						double weight2 = aleAssociator.getWeight(split[1],aleAssociator.getAssociation(split[1]));
						
						if((!graph.inSE(split[0])&&!graph.inSE(split[1]))&&(combinedWeight>weight1&&combinedWeight>weight2)){
							
							if(arguments.contains(aleAssociator.getAssociation(ale))){
								haveCombinedMeaning.add(ale);
							}
						}
					}
				}
			}
			
			LinkedList<String> newWords = new LinkedList<String>();
			
			if(haveCombinedMeaning.size()>0){	
				
				for(int i=0;i<words.size();i++){
					if(i<words.size()-1){
						String combined = words.get(i)+" "+words.get(i+1);
						if(haveCombinedMeaning.contains(combined)){
							newWords.add(combined);
							i++;
						}
						else{
							newWords.add(words.get(i));
						}
					}
					else{
						newWords.add(words.get(i));
					}
				}
				
				words = newWords;
			}	
			
			Path resPath = graph.addSequence(words,aleAssociator,entropyThresh,example.getSemantics(),weightThresh);
			
			if(this.syntactic){
				graph.mergeSyntactic(resPath,entropyThresh2,pathThresh,aleAssociator);
			}

		}

		graph.extractMapping();
	
		extractedGrammar = graph.extractGrammar();

	}
	
	public double runEval(LinkedList<Example> testExamples){
		
		Iterator<Example> it = testExamples.iterator();
		int correct = 0;
		int all = 0;
		int produced = 0;
		int wrong = 0;
		int noParse = 0;
		this.eval = "";
		int wrongMeaning = 0;
		int noMeaning = 0;

		while(it.hasNext()){
			
			Example example = it.next();
			
			eval += "Test example: "+example+"\n";
			LinkedList<String> words = example.getWords();
			LinkedList<String> newWords = new LinkedList<String>();
			LinkedList<String> updatedWords = new LinkedList<String>();
			all++;
		
			for(int i=0;i<words.size();i++){
				if(i<words.size()-1){
					String combined = words.get(i)+" "+words.get(i+1);
					if(graph.inSE(combined)){
						newWords.add(graph.getSEName(combined));
						updatedWords.add(words.get(i)+" "+words.get(i+1));
						i++;
					}
					else{
						if(graph.inSE(words.get(i))){
							newWords.add(graph.getSEName(words.get(i)));
							updatedWords.add(words.get(i));
						}
						else{
							newWords.add(words.get(i));
							updatedWords.add(words.get(i));
						}
					}
				}
				else{
					if(graph.inSE(words.get(i))){
						newWords.add(graph.getSEName(words.get(i)));
						updatedWords.add(words.get(i));
					}
					else{
						newWords.add(words.get(i));
						updatedWords.add(words.get(i));
					}
				}
			}
			
			if(graph.containsPath(util.Util.concat(newWords))){
				
				String pathId = graph.getPathId(util.Util.concat(newWords));
				String mr = graph.getMR(pathId);
				
				if(!mr.equals("none:")){
					produced++;
					
					MeaningRepresentation result = MeaningRepresentation.encode(mr);
					HashMap<String,String> mapping = graph.getMappingX(pathId+mr);
					
					for(Map.Entry<String,String> en : mapping.entrySet()){

						if(!en.getValue().equals("none")){
							int nr = Integer.parseInt(en.getKey())-1;
							result.setSlot(en.getValue(),this.aleAssociator.getAssociation(updatedWords.get(nr)));	
						}
					}
					
					if(example.getSemantics().toString().equals(result.toEvalString())){
							correct++;
							this.eval += "correct: "+example.getSemantics()+"\n";
						}
						else{
							wrong++;
							wrongMeaning++;
							this.eval += "wrong meaning: "+result.toEvalString()+"\n";						
							}
					}
				
				else{
						wrong++;
						noMeaning++;
						this.eval += "wrong, no meaning "+util.Util.concat(newWords)+"\n";
				}
			
			}
			else{
				noParse++;
				wrong++;
				this.eval += "wrong, sentence not contained in graph, parsed sentence: "+util.Util.concat(newWords)+"\n";

			}
			
		}

	double recall = correct*1.0/all;
	double prec = correct*1.0/produced;
	if(produced == 0){
		prec = 1.0;
	}
	this.recall = recall;
	this.precision = prec;
	this.grammarSize = graph.grammarSize();
	
	double fM = 0.0;
	if((prec+recall)>0){
		fM = 2*prec*recall/(prec+recall);
	}
	this.eval += "recall: "+recall+" precision: "+precision+" f1: "+fM+" nr rules: "+grammarSize+"\n";
	this.eval += "no parse: "+noParse+" wrong meaning: "+wrongMeaning+" no meaning: "+noMeaning+" wrong "+wrong+"\n";
	this.eval += "no parse: "+noParse*1.0/wrong+" wrong meaning: "+wrongMeaning*1.0/wrong+" no meaning: "+noMeaning*1.0/wrong+"\n";

	return fM;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}
	
	public int getGrammarSize() {
		return grammarSize;
	}

	public void setGrammarSize(int grammarSize) {
		this.grammarSize = grammarSize;
	}

	public void saveGrammar(File file){
		try{
			Util.writeFile(file,this.extractedGrammar);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void saveEval(File file){
		try{
			Util.writeFile(file,this.eval);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}
