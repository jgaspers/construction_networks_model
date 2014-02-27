package network;

import java.util.*;
import java.util.regex.*;
import struct.*;
import util.*;

public class MappingAssociator extends Associator {

	// Finden einer Kante über einen Key:
	// Key besteht aus x und y concatenated
	private HashMap<String,MappingEdge> mappingEdgesX;
	static int count=0;
	double entropyThresh;
	private Graph graph;
	
	public MappingAssociator(double eta, double c, int initType,double entropyThresh,Graph graph){
		super(eta,c,initType);
		this.graph = graph;
		this.entropyThresh = entropyThresh;
		this.mappingEdgesX = new HashMap<String,MappingEdge>();
	}

	public HashMap<String, MappingEdge> getMappingEdgesX() {
		return mappingEdgesX;
	}

	public void setMappingEdgesX(HashMap<String, MappingEdge> mappingEdgesX) {
		this.mappingEdgesX = mappingEdgesX;
	}
	
	public void trainMapping(Path path,double entropyThresh,Vector<Predicate> meanings,LinkedList<String> words,Associator wAssoc){
		
		String pathID = path.getId()+"";
		MeaningRepresentation meaning = null;
		Matcher matcher = null;
		Pattern p = Pattern.compile("SE");
		LinkedList<Node> pNodes = path.getNodes();

		for(int i=1;i<pNodes.size()-1;i++){
			
			String pWord = pNodes.get(i).getName();
			matcher = p.matcher(pWord);
			
			if(matcher.find()){
				Iterator<Predicate> it = meanings.iterator();
				String word = words.get(i-1);
				
				while(it.hasNext()){
					Predicate pred = it.next();
					meaning = new MeaningRepresentation(pred);
					String decodedMeaning = MeaningRepresentation.decodeGeneralized(meaning);

					String key = pathID+decodedMeaning;
					
					if(!mappingEdgesX.containsKey(key)){
						MappingEdge edge = new MappingEdge(pathID,decodedMeaning);
						this.mappingEdgesX.put(key, edge);
					}
					
					if(wAssoc.hasLearnedMeaningEntropy(word,entropyThresh)){
						String y = wAssoc.getAssociation(word);
						HashMap<String,String> slotValues = meaning.getSlots();
						String slot = "none";
						
						for(Map.Entry<String, String> en : slotValues.entrySet()){
							if(en.getValue().equals(y)){
								slot = en.getKey();
							
							}
						}
						
						this.mappingEdgesX.get(key).trainPhi(i+"", slot);
					
					}
				}	
			}
	
		}
		
	}

	
	public String getMapping(String y){
		
		String res = "none";
		
		if(this.mappingEdgesX.containsKey(y)){
			MappingEdge edge = this.mappingEdgesX.get(y);
			res = edge.getSlotASsocs();
		}
		
		return res;
	}
	
	public String getSlotValue(String y,String slotName){
		
		Associator assoc = null;
			
		if(this.mappingEdgesX.containsKey(y)){
			MappingEdge edge = this.mappingEdgesX.get(y);
			assoc = edge.getPhi();
		}
		
		String res = "empty";
		
		if(assoc.containsX(slotName)){
			res = assoc.getAssociation(slotName); 
		}
		
		return res;
	}
	
	public void joinEdges(HashSet<String> xValues,String x){
		
		for(Map.Entry<String, Integer> en : this.mappingY.entrySet()){
			String key = x+en.getKey();
			MappingEdge edge = new MappingEdge(x,en.getKey());
			this.mappingEdgesX.put(key,edge);
			Iterator<String> it = xValues.iterator();
			
			while(it.hasNext()){
				String currentX = it.next();
				String currentKey = currentX+en.getKey();
				
				if(this.mappingEdgesX.containsKey(currentKey)){
					edge.addToPhi(this.mappingEdgesX.get(currentKey).getPhi());
				}
			}
		
		}
	}
	
	public HashMap<String,String> getPhi(String y){
	
		HashMap<String,String> mapping = new HashMap<String,String>();
		
		if(this.mappingEdgesX.containsKey(y)){
			MappingEdge edge = this.mappingEdgesX.get(y);
			mapping = edge.getPhi().extractMappingX();
		}
		
		return mapping;
	}
	
	public double rate(String word,String y){
		
		double rating = 0.0;
		int xNr = this.mappingX.get(word);
		int yNr = this.mappingY.get(y);
		
		MeaningRepresentation mr = MeaningRepresentation.encode(y);
		int nrSlots = mr.getNrOfSlots();
		double currentRes = this.weights[xNr][yNr];
		String path = graph.getPathWords(Integer.parseInt(word));
		
		Pattern pattern = Pattern.compile("SE");
		Matcher matcher = pattern.matcher(path);
		int ses = 0;
		
		
		while(matcher.find()){
			ses++;
		}
		
		if(nrSlots > 0){
			
			MappingEdge edge = null;
			if(this.mappingEdgesX.containsKey(word+y)){
				edge = this.mappingEdgesX.get(word+y);
				Associator phi = edge.getPhi();
				HashMap<String,String> mapping = phi.extractMappingX();
				HashSet<String> differentMeanings = new HashSet<String>();

				go: for(Map.Entry<String,String> en : mapping.entrySet()){
					String se = en.getKey();
					String asso = phi.getAssociation(se);
				
					if(!asso.equals("none")){
						if(differentMeanings.contains(asso)){
							rating = 0.0;
							break go;
						}
						else{
							differentMeanings.add(asso);
							rating += phi.getWeight(se, asso);
						
						}
					}
				}
		
				if(nrSlots != differentMeanings.size() || nrSlots != ses){
					rating = 0.0;
				}
			}
				
		}
		
		if(rating == 0){	
			currentRes = 0;
	
		}
		else{
			currentRes += rating;
		}
		
		if(currentRes < 0){
			System.err.println("Mapping Associator check rate");
		}
		
		return currentRes;
	}
	
	public String getAssociation(String word){

		String res = "";
		double bestRes = 0.0;
		double currentRes = 0.0;
		String y = "";
		boolean valueExists = false;
		
		for(Map.Entry<String, Integer> entry : this.mappingY.entrySet()){
			
			y = entry.getKey();
			currentRes = this.rate(word, y);
			
			if(currentRes > bestRes){
				bestRes = currentRes;
				res = y; 
				valueExists = false;
			}
			else{
				if(currentRes == bestRes){
					valueExists = true;
				}
			}
		}
		
		if(bestRes <= 0.0 || valueExists){
			res = "none:";
		}
	
		return res;
	}
	
	public String getUnmappedAssoc(String x){
		return super.getAssociation(x);
	}
	
	
	
	public int getSlotNr(String y,String nodeName){
	
		HashMap<String,String> mapping = new HashMap<String,String>();
		int res = -1;
		
		if(this.mappingEdgesX.containsKey(y)){
			MappingEdge edge = this.mappingEdgesX.get(y);
			mapping = edge.getPhi().extractMappingX();
			
			if(mapping.containsKey(nodeName)){
				String slotNr = mapping.get(nodeName);
				res = slotNr.charAt(slotNr.length()-1);
			}
		}
		
		return res;
	}
	
	
	public void train(HashSet<String> xValues, HashSet<String> yValues){

		if((xValues.size()==0) || (yValues.size()==0)){
			System.err.println("Associator: only x values OR y values given, weights will not be updated");
		}
		
		else{
			
			switch(this.initType){
				case 1: this.initPrincipleOfContrast(xValues,yValues);break;
				case 2: this.initRandom(xValues,yValues);break;
				case 3: this.initZero(xValues,yValues);break;
				}
			
				double[] vectorX = this.toVector(xValues, "x");
				double[] vectorY = this.toVector(yValues, "y");
		
				double[] vectorXOld = this.multiplyWithWeigths(vectorY, false);
				double[] vectorYOld = this.multiplyWithWeigths(vectorX, true);
		
				for(int i=0;i<this.mappingX.size();i++){
					for(int j=0;j<this.mappingY.size();j++){
						this.weights[i][j] += this.round(eta*(vectorX[i]-vectorXOld[i])*(vectorY[j]-vectorYOld[j])); 
						if(this.weights[i][j]<0){
							this.weights[i][j] = 0.0;
						}
						
						this.weights[i][j] = this.round(this.weights[i][j]);
				}
			}	
		}
	}
	
	public String test(){
		String res = "";
		if(this.mappingY.containsKey("pass:noun2#SEnoun1#SE")){
			for(Map.Entry<String, Integer> en : this.mappingX.entrySet()){
				String path = en.getKey();
				res += path+": "+getWeight(path,"pass:noun2#SEnoun1#SE")+" x ";
			}
		}
		return res;
	}
	
}
