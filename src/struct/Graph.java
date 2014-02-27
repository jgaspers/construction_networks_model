package struct;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.io.*;
import network.*;
import util.*;
import java.util.regex.*;

public class Graph {

	private HashMap<String,Node> nodes;
	private HashMap<Integer,Path> paths;
	public LinkedList<Edge> edges;
	private MappingAssociator pathAssoc;
	private HashMap<String,SynSE> ses;
	private HashMap<String,String> inSE;
	private HashMap<String,Integer> decodedPaths;
	private HashMap<String,String> replacedSEs;
	
	public Graph(double entropyThresh){
		this.nodes = new HashMap<String,Node>();
		this.nodes.put("<START>",new Node("<START>"));
		this.nodes.put("<END>",new Node("<END>"));
		this.paths = new HashMap<Integer,Path>();
		this.edges = new LinkedList<Edge>();
		this.pathAssoc = new MappingAssociator(0.01,0.01,3,entropyThresh,this);
		this.ses = new HashMap<String,SynSE>();
		this.inSE = new HashMap<String,String>();
		this.decodedPaths = new HashMap<String,Integer>();
		this.replacedSEs = new HashMap<String,String>();
	}
	
	public void printPathWords(){
		for(Map.Entry<Integer, Path> en : paths.entrySet()){
			System.out.println(en.getValue().getWords());
			}
	}
	
	public void printGraph()
	{
		for(Map.Entry<Integer, Path> en : paths.entrySet()){
			System.out.println(en.getValue().getWords());
			}
		for(Map.Entry<String, SynSE> en : ses.entrySet()){
			System.out.println(en.getKey()+": "+en.getValue());
		}
	}
	
	public String toString()
	{
		String s = "";
		Iterator<String> it = pathAssoc.xIterator();
		
		while(it.hasNext()){
			String x = it.next();
			String res = pathAssoc.getAssociation(x);
			
			if(paths.containsKey(Integer.parseInt(x))){
				if(!res.equals("none:")){
					s += this.paths.get(Integer.parseInt(x)).getWords()+" "+res+pathAssoc.getWeight(x,res)+" "+pathAssoc.isSingleBestMeaning(x,res)+" "+pathAssoc.getMapping(x+res)+" "+pathAssoc.getUnmappedAssoc(x)+"\n";
			
				}
				else{
					s += this.paths.get(Integer.parseInt(x)).getWords()+" none\n";
				}
			}
		}	
	
		s+= "SEs: ";
		
		for(Map.Entry<String, SynSE> en : ses.entrySet()){
			s += en.getKey()+": "+en.getValue()+"\n";
		}
		
		return s;
	}
	
	public Path addSequence(LinkedList<String> words,Associator assoc,double entropyThresh,Vector<Predicate> meanings,double weightThresh){
		
		Iterator<String> it = words.iterator();
		LinkedList<String> newWords = new LinkedList<String>();
		Path resPath = null;
		
		while(it.hasNext()){
			String word = it.next();
			
			if(inSE.containsKey(word)){
				newWords.add(inSE.get(word));
			}
			else{
				newWords.add(word);
			}
		}
		
		if(this.paths.size()==0){
			resPath = this.addNewSequence(words,meanings);
		}

		else{
			
			LinkedList<Path> merge = new LinkedList<Path>();
		
			for(Map.Entry<Integer,Path> en : this.paths.entrySet()){
				Path path = en.getValue();
				
				if(path.canMerge(newWords,assoc,entropyThresh,ses,weightThresh,pathAssoc,meanings)){
					merge.add(path);
				}
			}
			
			if(merge.size()>0){
				
				if((merge.size()==1)&&merge.get(0).decode().equals(Util.concat(newWords))){
					resPath = merge.get(0);
				}
			
				else{
					int pathID = merge(newWords,merge,meanings,assoc,false);
					int lastID = pathID;
					resPath = this.paths.get(lastID);
				}
			}
			else{
				resPath = this.addNewSequence(newWords,meanings);
			}
		}
		
		
	if(meanings.size()>0){
		
		HashSet<String> seMembers = new HashSet<String>();
		
		LinkedList<Node> nodes = resPath.getNodes();
		for(int i=0;i<words.size()-1;i++){
			Node node = nodes.get(i+1);
			if(node.isSENode){
				String word = words.get(i);
				
				if(assoc.hasLearnedMeaningEntropy(word, entropyThresh,weightThresh)){
					seMembers.add(assoc.getAssociation(word));
				}
			}
		}
		
		HashSet<String> decodedMeaning = null;
		
		if(resPath.isGeneralizedPath()){
			decodedMeaning = decodeMeaningsGeneralized(meanings);
		}
		else{
			decodedMeaning = decodeMeaningsGeneralized(meanings);
		}


		pathAssoc.train(resPath.getId()+"",decodedMeaning);
		pathAssoc.trainMapping(resPath,entropyThresh,meanings,words,assoc);
		
		this.mergeIdenticalPaths();
		
		}
	
	return resPath;
	}
	
	public LinkedList<Path> getMergablePathsSyntactic(int pathID,double entropyThresh,double rateThresh){
		
		LinkedList<Path> merge = new LinkedList<Path>();
		Path givenPath = this.paths.get(pathID);
		
		if(this.pathAssoc.containsX(pathID+"")){
			String assoc = this.pathAssoc.getAssociation(pathID+"");
			
			if(this.pathAssoc.entropyProportion(pathID+"")<entropyThresh && this.pathAssoc.rate(pathID+"", assoc)>rateThresh){
				
				for(Map.Entry<Integer,Path> en : this.paths.entrySet()){
				
					Path path = en.getValue();
					String pathAssoc = "";
					if(this.pathAssoc.containsX(en.getKey()+"")){
						pathAssoc = this.pathAssoc.getAssociation(en.getKey()+"");
					}
				
					if(path.getId()!=pathID&&givenPath.canMergeSyntactic(path)&&assoc.equals(pathAssoc)){
						merge.add(path);
					}
				}
			}
		}
		
		return merge;
	}
	
	private int merge(LinkedList<String> words,LinkedList<Path> paths,Vector<Predicate> meanings,Associator assoc, boolean syntactic){

		String word = "";
		String nodeName = "";
		Node node = null;
		LinkedList<Node> pathNodes = new LinkedList<Node>();
		pathNodes.add(nodes.get("<START>"));
		HashSet<Integer> pathIds = new HashSet<Integer>();
		HashSet<String> decodedPs = new HashSet<String>();
		HashSet<String> laInput = new HashSet<String>();

		Iterator<Path> pIt = paths.iterator();
		while(pIt.hasNext()){
			Path p = pIt.next();
			pathIds.add(p.getId());
			decodedPs.add(p.decode());
			laInput.add(p.getId()+"");
		}
		
		for(int i=0;i<words.size();i++){
			word = words.get(i);
			HashSet<String> names = new HashSet<String>();
			names.add(word);
			Iterator<Path> pathIt = paths.iterator();
			
			while(pathIt.hasNext()){	
				Path path = pathIt.next();
				node = path.getNode(i+1);
				nodeName = node.getName();
				names.add(nodeName);
			}

			if(names.size()>1){ 
				String seName = "";
				
				if(syntactic){
					SynSE se = new SynSE();
					seName = se.getName();
					Iterator<String> nameIt = names.iterator();
					while(nameIt.hasNext()){
						String name = nameIt.next();
						
						Pattern p = Pattern.compile("SYN");
						Matcher m = p.matcher(name);
						if(!m.find()){
							se.addMember(name);
							this.inSE.put(name,se.getName());
						}
					}
					
					this.ses.put(seName,se);
				}
				else{
					SE se = new SE();
					seName = se.getName();
					Iterator<String> nameIt = names.iterator();
					while(nameIt.hasNext()){
						String name = nameIt.next();
						Pattern p = Pattern.compile("SE");
						Matcher m = p.matcher(name);
						if(!m.find()){
							se.addMember(name,assoc.getAssociation(name));
							this.inSE.put(name,se.getName());
							
						}
						
					}
					this.ses.put(seName,se);
				}
				
				SynSE se = this.ses.get(seName);
				Node seNode = new Node(seName,true);
				this.nodes.put(seName,seNode);
				pathNodes.add(seNode);
				replace(seNode,names,pathIds,assoc);
				HashSet<String> replaceAlso = new HashSet<String>();
				for(Map.Entry<String,SynSE> en : this.ses.entrySet()){
					
					SynSE seC = en.getValue();
					if((se.isSynSE()&&seC.isSynSE()) || (!se.isSynSE()&&!seC.isSynSE())){
						if(en.getKey()!=seName&&seC.inCommon(se)){
							replaceAlso.add(en.getKey());
						}
					}
				}
				
				if(replaceAlso.size() > 0){
					replace(seNode,replaceAlso,pathIds,assoc);
				}

			}
			else{
				pathNodes.add(node);
			}
			
		}
		
		pathNodes.add(nodes.get("<END>"));	
		Path mergedPath = new Path(pathNodes);	
		Iterator<Path> pathIt = paths.iterator();
	
		while(pathIt.hasNext()){
			Path delP = pathIt.next();
			this.paths.remove(delP.getId());
		}
	
		this.paths.put(mergedPath.getId(),mergedPath);
	
		Iterator<Node> nIt = mergedPath.getNodes().iterator();
		LinkedList<Node> nNodes = new LinkedList<Node>();
	
		while(nIt.hasNext()){
			Node n = nIt.next();
			if(replacedSEs.containsKey(n.getName())){
				String replacedSE = replacedSEs.get(n.getName());
				Node replacement = this.nodes.get(replacedSE);
				nNodes.add(replacement);
			}
			else{
				nNodes.add(n);
			}
		}
		
		mergedPath.setNodes(nNodes);
		double[] xValues = this.pathAssoc.mergeXRows(laInput);
		this.pathAssoc.addXRow(mergedPath.getId()+"", xValues);
		this.pathAssoc.joinEdges(laInput,mergedPath.getId()+"");
		this.pathAssoc.deleteXRows(laInput);
		
		return mergedPath.getId();
		
 	}
	
	private int merge(LinkedList<Path> paths,Associator assoc, boolean syntactic){

		Path path = paths.get(0);
		String nodeName = "";
		Node node = null;
		LinkedList<Node> pathNodes = new LinkedList<Node>();
		pathNodes.add(nodes.get("<START>"));
		HashSet<Integer> pathIds = new HashSet<Integer>();
		HashSet<String> decodedPs = new HashSet<String>();
		HashSet<String> laInput = new HashSet<String>();
		Iterator<Path> pIt = paths.iterator();
		while(pIt.hasNext()){
			Path p = pIt.next();
			pathIds.add(p.getId());
			decodedPs.add(p.decode());
			laInput.add(p.getId()+"");
		}

		for(int i=0;i<path.length();i++){
	
			HashSet<String> names = new HashSet<String>();
			Iterator<Path> pathIt = paths.iterator();
			
			while(pathIt.hasNext()){	
				Path p = pathIt.next();
				node = p.getNode(i+1);
				nodeName = node.getName();
				names.add(nodeName);
			}
			
			if(names.size()>1){
				
				String seName = "";
				
				if(syntactic){

					SynSE se = new SynSE();
					seName = se.getName();
					Iterator<String> nameIt = names.iterator();
					while(nameIt.hasNext()){
						String name = nameIt.next();
						Pattern p = Pattern.compile("SYN");
						Matcher m = p.matcher(name);
						if(!m.find()){
							se.addMember(name);
							this.inSE.put(name,se.getName());
						}
					}
					this.ses.put(seName,se);
					
				}
				else{
					SE se = new SE();
					seName = se.getName();
					Iterator<String> nameIt = names.iterator();
					while(nameIt.hasNext()){
						String name = nameIt.next();
						se.addMember(name,assoc.getAssociation(name));
						this.inSE.put(name,se.getName());
					}
					this.ses.put(seName,se);
					
				}
				
				SynSE se = this.ses.get(seName);
				Node seNode = new Node(seName,true);
				this.nodes.put(seName,seNode);
				pathNodes.add(seNode);
				replace(seNode,names,pathIds,assoc);
				HashSet<String> replaceAlso = new HashSet<String>();

				for(Map.Entry<String,SynSE> en : this.ses.entrySet()){
					
					SynSE seC = en.getValue();
			
					if((se.isSynSE()&&seC.isSynSE()) || (!se.isSynSE()&&!seC.isSynSE())){
						if(en.getKey()!=seName&&seC.inCommon(se)){
							replaceAlso.add(en.getKey());
						}
					}
				}
				
				if(replaceAlso.size() > 0){
					replace(seNode,replaceAlso,pathIds,assoc);
				}
			
			}
			else{
				pathNodes.add(node);
			}
			
		}
		
		pathNodes.add(nodes.get("<END>"));	
		Path mergedPath = new Path(pathNodes);
		Iterator<Path> pathIt = paths.iterator();
	
		while(pathIt.hasNext()){
			Path delP = pathIt.next();
			this.paths.remove(delP.getId());
		}
	
		this.paths.put(mergedPath.getId(),mergedPath);
		Iterator<Node> nIt = mergedPath.getNodes().iterator();
		LinkedList<Node> nNodes = new LinkedList<Node>();
	
		while(nIt.hasNext()){
			Node n = nIt.next();
			if(replacedSEs.containsKey(n.getName())){
				String replacedSE = replacedSEs.get(n.getName());
				Node replacement = this.nodes.get(replacedSE);
				nNodes.add(replacement);
			}
			else{
				nNodes.add(n);
			}
		}
		
		mergedPath.setNodes(nNodes);
		double[] xValues = this.pathAssoc.mergeXRows(laInput);
		this.pathAssoc.addXRow(mergedPath.getId()+"", xValues);
		this.pathAssoc.joinEdges(laInput,mergedPath.getId()+"");
		this.pathAssoc.deleteXRows(laInput);
		
		return mergedPath.getId();
		
 	}
	
	private void replace(Node seNode,HashSet<String> names,HashSet<Integer> excludedIds,Associator assoc){
		
		Iterator<String> it = names.iterator();
		String seName = seNode.getName();
		SynSE oldSE = null;
		SynSE se = ses.get(seName);
	
		while(it.hasNext()){
			String name = it.next();
			
			while(replacedSEs.containsKey(name)){
				name = replacedSEs.get(name);
			}
		
			for(Map.Entry<Integer, Path> en : this.paths.entrySet()){
				
				if(excludedIds.contains(en.getKey())){
					continue;
				}
				Path path = en.getValue();
				LinkedList<Node> newNodes = new LinkedList<Node>();
				LinkedList<Node> oldNodes = path.getNodes();
				Iterator<Node> itP = oldNodes.iterator();
				boolean changed = false;
		
				while(itP.hasNext()){
					Node node = itP.next();
					String nodeName = node.getName();
					
					if(nodeName.equals(name)){
						newNodes.add(seNode);
						changed = true;
					}
					else{
						newNodes.add(node);
					}
				}
				
				if(changed){
					path.setNodes(newNodes);
				}
			}
			
			
			
			if(ses.containsKey(name)){
				oldSE = ses.get(name);
				HashSet<String> seMember = oldSE.getMembers();
				Iterator<String> memberIt = seMember.iterator();
				se.add(oldSE);
			
				while(memberIt.hasNext()){
					String member = memberIt.next();
					inSE.put(member,seName);
				}
				
				this.ses.remove(name);
				this.replacedSEs.put(name,seName);
					
			}
		}
	}
	
	
	public void savePathWords(File file){
		
		try{
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
		
		for(Map.Entry<Integer, Path> en : paths.entrySet()){
			if(en.getValue().isGeneralizedPath()){
			out.write(en.getValue().getWords()+" "+en.getValue().getCnt()+" "+en.getValue().getMeaning()+"\n");
		}
			}
		out.close();
		}
		catch(IOException e){
			
		}
	}

	public Path addNewSequence(LinkedList<String> words,Vector<Predicate> meanings){
		
		Iterator<String> it = words.iterator();
		String word =  "";
		Node node = null;
		LinkedList<Node> pathNodes = new LinkedList<Node>();
		pathNodes.add(this.nodes.get("<START>"));
		Path path = new Path();
		int pathId = path.getId();
		Node fromNode = this.nodes.get("<START>");
		Edge prevEdge = null;
		
		while(it.hasNext()){
			word = it.next();
			
			if(this.nodes.containsKey(word)){
				node = this.nodes.get(word);
			}
			else{
				node = new Node(word);
				this.nodes.put(word,node);
			}
			
			Edge edge = new Edge(fromNode,node,pathId,prevEdge);
			this.edges.add(edge);
			
			node.addIncomingEdge(edge);
			fromNode.addOutgoingEdge(edge);
			
			if(prevEdge!=null){
				prevEdge.setNextEdge(edge);
			}
			pathNodes.add(node);
			
			fromNode = node;
			prevEdge = edge;
			
		}
		
		node = this.nodes.get("<END>");
		Edge edge = new Edge(fromNode,node,pathId,prevEdge);
		this.edges.add(edge);
		
		node.addIncomingEdge(edge);
		fromNode.addOutgoingEdge(edge);
		prevEdge.setNextEdge(edge);
		pathNodes.add(node);
		
		path.setNodes(pathNodes);
		this.paths.put(pathId,path);
		
		return path;
	}
	
	public void mergeSyntactic(Path path,double entropyThresh,double rateThresh,Associator assoc){
	
		int pathId = path.getId();
		LinkedList<Path> merge = this.getMergablePathsSyntactic(pathId,entropyThresh,rateThresh);

		if(merge.size()>0){
		
			if(!(merge.size()==1&&merge.get(0).decode().equals(path.decode()))){
				pathId = merge(merge,assoc,true);
				int lastID = pathId;
				LinkedList<Path> toMerge = this.getMergablePathsSyntactic(pathId,entropyThresh,rateThresh);
			
				go: while(toMerge.size()>0){
					
					if((toMerge.size()==1)&&lastID==toMerge.get(0).getId()){
					
						break go;
					}
					
					lastID = pathId;
					pathId = this.merge(toMerge,assoc,true);
					toMerge = this.getMergablePathsSyntactic(pathId,entropyThresh,rateThresh);
				}
			}
		}
		mergeIdenticalPaths();
	}
	
	public HashSet<String> decodeMeanings(Vector<Predicate> predicates){
		
		Iterator<Predicate> itP = predicates.iterator();
		MeaningRepresentation meaning = null;
		HashSet<String> meanings = new HashSet<String>();
		
		while(itP.hasNext()){
			Predicate pred = itP.next();
			meaning = new MeaningRepresentation(pred);
			meanings.add(MeaningRepresentation.decode(meaning));
		}
		
		return meanings;
	}

	public HashSet<String> decodeMeaningsGeneralized(Vector<Predicate> predicates){
		
		Iterator<Predicate> itP = predicates.iterator();
		MeaningRepresentation meaning = null;
		HashSet<String> meanings = new HashSet<String>();
		
		while(itP.hasNext()){
			Predicate pred = itP.next();
			meaning = new MeaningRepresentation(pred);
			meanings.add(MeaningRepresentation.decodeGeneralized(meaning));
		}
		
		MeaningRepresentation none = new MeaningRepresentation("none");
		meanings.add(MeaningRepresentation.decode(none));
		
		return meanings;
	}
	
	public void printPathAssocs(){
		
		Iterator<String> it = pathAssoc.xIterator();
		while(it.hasNext()){
			String x = it.next();
			String res = pathAssoc.getAssociation(x);
			
			if(paths.containsKey(Integer.parseInt(x))){
				if(!res.equals("none:")){
					System.out.println(this.paths.get(Integer.parseInt(x)).getWords()+" "+res+pathAssoc.getWeight(x,res)+" "+pathAssoc.isSingleBestMeaning(x,res)+" "+pathAssoc.getMapping(x+res));
			
				}
				else{
					System.out.println(this.paths.get(Integer.parseInt(x)).getWords()+" none");
				}
			}
		
		}	
		
		for(Map.Entry<String, SynSE> en : ses.entrySet()){
			System.out.println(en.getKey()+": "+en.getValue());
		}
	}
	
	public String getPathAssocs(){
		
		Iterator<String> it = pathAssoc.xIterator();
		String s = "";
		
		while(it.hasNext()){
			String x = it.next();
			String res = pathAssoc.getAssociation(x);
			
			if(paths.containsKey(Integer.parseInt(x))){
				s += this.paths.get(Integer.parseInt(x)).getWords()+" "+res+pathAssoc.getWeight(x,res)+" "+pathAssoc.isSingleBestMeaning(x,res)+"\n";
			}
		}
		
		return s;
	}
	
	public boolean inSE(String word){
		return this.inSE.containsKey(word);
	}
	
	public String getSEName(String word){
		return this.inSE.get(word);
	}

	public void extractMapping(){

		for(Map.Entry<Integer, Path> en : this.paths.entrySet()){
			this.decodedPaths.put(en.getValue().decode(), en.getValue().getId());
		}
		
	}
	
	public boolean containsPath(String words){
		return this.decodedPaths.containsKey(words);
	}

	public String getMR(String pathId){
		return this.pathAssoc.getAssociation(pathId);
	}
	
	public String getPathId(String decodedPath){
		return this.decodedPaths.get(decodedPath)+"";
	}
	
	public String getSlotValue(String y,String slotName){
		return this.pathAssoc.getSlotValue(y, slotName);
	}
	
	public HashMap<String,String> getMappingX(String y){
		return this.pathAssoc.getPhi(y);
	}

	public void mergeIdenticalPaths(){
		
		HashMap<String,HashSet<Integer>> pathCollection = new HashMap<String,HashSet<Integer>>();
		for(Map.Entry<Integer, Path> en : this.paths.entrySet()){
			HashSet<Integer> ids = new HashSet<Integer>();
			String decodedPath = en.getValue().decodeComma(); 
			
			if(pathCollection.containsKey(decodedPath)){
				ids = pathCollection.get(decodedPath);
			}
			
			ids.add(en.getKey());
			pathCollection.put(decodedPath, ids);
		}
		
		HashMap<Integer,Path> updatedPaths = new HashMap<Integer,Path>();
		
		for(Map.Entry<String,HashSet<Integer>> en : pathCollection.entrySet()){
			HashSet<Integer> ids = en.getValue();
			
			if(ids.size()>1){
				Iterator<Integer> it = ids.iterator();
				int resPath = it.next();
				
				HashSet<String> laInput = new HashSet<String>();
				it = ids.iterator();
				while(it.hasNext()){
					laInput.add(it.next()+"");
				}
				
				double[] xValues = this.pathAssoc.mergeXRows(laInput);
				this.pathAssoc.deleteXRows(laInput);
				this.pathAssoc.joinEdges(laInput,resPath+"");
				this.pathAssoc.addXRow(resPath+"", xValues);
				
				updatedPaths.put(resPath, this.paths.get(resPath));
			}
			else{
				Iterator<Integer> it = ids.iterator();
				int resPath = it.next();
				updatedPaths.put(resPath, this.paths.get(resPath));
			}
		}
		
		this.paths = updatedPaths;
	}
	
	public double entropyProportion(String word){
		return this.pathAssoc.entropyProportion(word);
	}
	
	public String getPathWords(int pathId){
		return this.paths.get(pathId).getWords();
	}
	
	public int grammarSize(){
		return paths.size();
	}
	
	public String extractGrammar(){

		HashMap<String,Vector<Path>> sorted = new HashMap<String,Vector<Path>>();
		String res = "";
		
		for(Map.Entry<Integer,Path> en : this.paths.entrySet()){
			Path path = en.getValue();
			String pathMeaning = this.pathAssoc.getAssociation(en.getKey()+"");
			
			Vector<Path> paths = new Vector<Path>();
			
			if(sorted.containsKey(pathMeaning)){
				paths = sorted.get(pathMeaning);
			}
			
			paths.add(path);
			sorted.put(pathMeaning,paths);
			
		}
		
		for(Map.Entry<String,Vector<Path>> en : sorted.entrySet()){
			res += "Meaning: "+en.getKey()+" nr rules: "+en.getValue().size()+"\n";
			Iterator<Path> it = en.getValue().iterator();
			while(it.hasNext()){
				Path path = it.next();
				String id = path.getId()+""; 
				res += path.getPathWords()+" "+pathAssoc.getMapping(id+en.getKey())+" "+pathAssoc.entropyProportion(id)+" rating: "+pathAssoc.rate(id,en.getKey())+"\n";
			}
			res += "---------------------\n";
		}

		for(Map.Entry<String, SynSE> en : ses.entrySet()){
			res += en.getKey()+": "+en.getValue()+"\n";
		}
		
		return res;
	}
}
	