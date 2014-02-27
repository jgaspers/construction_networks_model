package struct;

import java.util.*;
import util.*;
import network.*;

public class Path implements Comparable<Path>{

	private static int nrPaths;
	
	private int id;
	private LinkedList<Node> nodes;
	private MeaningRepresentation meaning;
	private int cnt;
	private boolean isGeneralizedPath;
	private HashSet<Integer> subsumedPaths;
		
	public Path(LinkedList<Node> nodes){
		this.nodes = new LinkedList<Node>();
		this.nodes.addAll(nodes);
		nrPaths++;
		this.id = nrPaths;
		this.cnt = 1;
		this.subsumedPaths = new HashSet<Integer>();
	}
	
	public Path(){
		this.nodes = new LinkedList<Node>();
		nrPaths++;
		this.id = nrPaths;
		this.cnt = 1;
		this.isGeneralizedPath = false;
		this.subsumedPaths = new HashSet<Integer>();
	}

	public int getNrSubsumedPaths(){
		return this.subsumedPaths.size();
	}
	
	public static int getNrPaths() {
		return nrPaths;
	}

	public static void setNrPaths(int nrPaths) {
		Path.nrPaths = nrPaths;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LinkedList<Node> getNodes() {
		return nodes;
	}

	public void setNodes(LinkedList<Node> nodes) {
		this.nodes = nodes;
	}
	
	public boolean isGeneralizedPath() {
		
		if(!isGeneralizedPath){
			Iterator<Node> it = nodes.iterator();
			while(it.hasNext()){
				if(it.next().isSENode()){
					this.isGeneralizedPath = true;
				}
			}
		}
		
		return isGeneralizedPath;
	}

	public void setGeneralizedPath(boolean generalizedPath) {
		this.isGeneralizedPath = generalizedPath;
	}

	public HashSet<Integer> getSubsumedPaths() {
		return subsumedPaths;
	}

	public void setSubsumedPaths(HashSet<Integer> subsumedPaths) {
		this.subsumedPaths = subsumedPaths;
	}

	public String toString(){
		
		String res = id+" ";
		Iterator<Node> it = this.nodes.iterator();
		
		while(it.hasNext()){
			Node node = it.next();
			res += node+"\n ";
			HashSet<Edge> inc = node.getIncomingEdges();
			Iterator<Edge> itE = inc.iterator();
			while(itE.hasNext()){
				Edge edge = itE.next();
				if(edge!=null&&edge.getPathId()==id){
					res += "incoming: "+edge+"\n";
				}
			}
				HashSet<Edge> out = node.getOutgoingEdges();
				itE = out.iterator();
				while(itE.hasNext()){
					Edge edge = itE.next();
					if(edge!=null&&edge.getPathId()==id){
						res += "outgoing: "+edge+"\n";
					}
				
			}
		}
		
		return res;
	}
	
	public Iterator<Node> iterator(){
		return this.nodes.iterator();
	}
	
	public int length(){
		return this.nodes.size()-2;
	}
	
	public String getWords(){
		String res = id+" ";
		Iterator<Node> it = this.nodes.iterator();
		
		while(it.hasNext()){
			Node node = it.next();
			res += node.getName()+" ";
		
			if(node.isSENode&&node.getSeNodes().size()>0){
				res += "[";
				HashSet<Node> seNodes = node.getSeNodes();
				Iterator<Node> itSE = seNodes.iterator();
				while(itSE.hasNext()){
					res += itSE.next().getName()+" ";
				}
				res = res.substring(0,res.length()-1);
				res += "] ";
			}
			
			
		}
		
		return res;
	}
	
	public LinkedList<String> getPathWords(){
		
		LinkedList<String> res = new LinkedList<String>();
		
		for(int i=1;i<nodes.size()-1;i++){
			res.add(nodes.get(i).getName());
		}
		
		return res;
	}
	
	public Node getNode(int index){
		if(index>this.nodes.size()-1){
			System.err.println("check Path getNode");
		}
		return this.nodes.get(index);
	}

	public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}
	
	public int compareTo(LinkedList<String> words){
		
		int equal = 0;
		
		if(words.size()!=this.length()){
			equal = -1;
		}
		
		else{
			for(int i=0;i<words.size();i++){
				if(!(words.get(i).equals(this.nodes.get(i+1).getName()))){
					
					Node node = this.nodes.get(i+1);
					
					if(node.isSENode){
						HashSet<Node> seNodes = node.getSeNodes();
						Iterator<Node> nodeIt = seNodes.iterator();
						boolean included = false;
						
						go: while(nodeIt.hasNext()){
							if(nodeIt.next().getName().equals(words.get(i))){
								included = true;
								break go;
							}
						}
						
						if(!included){
							equal = -1;
						}
						
					}
					else{
						equal = -1;
					}

				}
			}
		}
		
		return equal;
	}
	
	public int compareTo(Path path){
		
		int equal = 0;
		
		if(path.length()==this.length()){
			go: for(int i=0;i<this.length();i++){
				if(!(this.nodes.get(i).getName().equals(path.nodes.get(i)))){
					equal = -1;
					break go;
				}
			}
		}
		else{
			equal = -1;
		}
		
		return equal;
	}
	
	public void addSubsumedPath(int pathId){
		if(!this.isGeneralizedPath){
			System.err.println("check Path addSubsumedPath");
		}
		this.subsumedPaths.add(pathId);
	}

	public MeaningRepresentation getMeaning() {
		return meaning;
	}

	public void setMeaning(MeaningRepresentation meaning) {
		this.meaning = meaning;
	}
	
	public boolean canMerge(LinkedList<String> words,Associator assoc,double entropyThresh,HashMap<String,SynSE> ses, double weightThresh,MappingAssociator pathAssoc,Vector<Predicate> meanings){
		
		boolean canMerge = true;
		int cnt = 0;
		
		if(words.size()!=this.length()){
			canMerge = false;
		}
		
		else{

		String pathMeaning = pathAssoc.getUnmappedAssoc(this.id+"");
		
		go: for(int i=0;i<words.size();i++){
				
			String word = words.get(i);
			
			if(!(word.equals(this.nodes.get(i+1).getName()))){
				Node node = this.nodes.get(i+1);
				String nodeName = node.getName();
				
				if(!(assoc.containsX(word)&&assoc.hasLearnedMeaningEntropy(word,entropyThresh,weightThresh)&&!assoc.getAssociation(word).equals("none"))){
					canMerge = false;
					break go;
				}
				
				if(!node.isSENode){
					if(!(assoc.containsX(nodeName)&&assoc.hasLearnedMeaningEntropy(nodeName,entropyThresh,weightThresh)&&!assoc.getAssociation(nodeName).equals("none"))){
						canMerge = false;
						break go;
					}
				}
				
				else{
					if(!ses.containsKey(node.getName())){
		
					}
					else{
						if(ses.get(node.getName()).isSynSE()){
							canMerge = false;
							break go;
						}
					}
				}
			
				cnt++;
				String wordMeaning = assoc.getAssociation(word);
				boolean inMR = false;
				Iterator<Predicate> itM = meanings.iterator();

				while(itM.hasNext()){
					Predicate pred = itM.next();
					meaning = new MeaningRepresentation(pred);
					String decodedMeaning = MeaningRepresentation.decodeGeneralized(meaning);
					
					if(decodedMeaning.equals(pathMeaning)){
						
						Iterator<String> it = meaning.slotIterator();
	
						while(it.hasNext()){
							if(meaning.getSlotValue(it.next()).equals(wordMeaning)){
								inMR = true;
							}
						}
					}
				}
				if(!inMR){
					canMerge = false;
					break go;
				}
			}
		}

		if(cnt>1){
			canMerge = false;
		}
		}
		return canMerge;
		
	}
	
	public String decode(){
		String res = "";
		
		for(int i=1;i<nodes.size()-1;i++){
			Node node = nodes.get(i);
			res += node.getName()+" ";
		
		}
		res = res.substring(0,res.length()-1);
		
		return res;
	}
	
	public String decodeComma(){
		String res = "";
		
		for(int i=1;i<nodes.size()-1;i++){
			Node node = nodes.get(i);
			res += node.getName()+",";
		
		}
		res = res.substring(0,res.length()-1);
		
		return res;
	}
	
	public boolean canMergeSyntactic(Path path){
		
		boolean canMerge = true;
		int cnt = 0;
		LinkedList<Node> pathNodes = path.getNodes();
		
		if(nodes.size()!=pathNodes.size()){
			canMerge = false;
		}
		
		else{
		
			go: for(int i=0;i<nodes.size();i++){
				
				Node pathNode = pathNodes.get(i);
				Node node = this.nodes.get(i);
				
				
				if(!(pathNode.getName().equals(node.getName()))){
					if(node.isSENode || pathNode.isSENode()){
						canMerge = false;
						break go;
					}
					else{
						cnt++;
					}
			}
			}
		if(cnt > 1){
			canMerge = false;
		}
		
	}
	
		return canMerge;
	}
	
	public boolean isGeneralized(){
		boolean general = false;
		
		Iterator<Node> it = this.nodes.iterator();
		
		while(it.hasNext()){
			if(it.next().isSENode){
				general = true;
				break;
			}
		}
		
		return general;
	}
}
