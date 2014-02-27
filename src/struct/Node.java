package struct;

import java.util.*;
import java.util.regex.*;

public class Node {

	protected String name;
	protected HashSet<Edge> incomingEdges;
	protected HashSet<Edge> outgoingEdges;
	HashSet<Node> seNodes;
	protected boolean isSENode;
	// ID die für die Bezeichnung neuer SE Nodes verwendet wird
	// Ob das immer der tatsächlichen Anzahl entspricht ist nicht getestet!!
	private static int nrSENodes = 0;
	private String referntSlot = "";
	
	public Node(String name){
		this.name = name;
		this.incomingEdges = new HashSet<Edge>();
		this.outgoingEdges = new HashSet<Edge>();
		this.isSENode = false;
		this.seNodes = new HashSet<Node>(); 

	}

	public Node(String name, boolean seNode, String referentSlot){

		this.incomingEdges = new HashSet<Edge>();
		this.outgoingEdges = new HashSet<Edge>();
		this.isSENode = seNode;
		this.seNodes = new HashSet<Node>(); 

		if(seNode){
			nrSENodes++;
		}
		
		this.name = name+nrSENodes;
		this.referntSlot = referentSlot;
	}
	
	public Node(String name, boolean seNode){

		this.incomingEdges = new HashSet<Edge>();
		this.outgoingEdges = new HashSet<Edge>();
		this.isSENode = seNode;
		this.seNodes = new HashSet<Node>(); 

		if(seNode){
			nrSENodes++;
		}
		
		this.name = name;
	}

	public Node(boolean seNode){

		this.incomingEdges = new HashSet<Edge>();
		this.outgoingEdges = new HashSet<Edge>();
		this.isSENode = seNode;
		this.seNodes = new HashSet<Node>(); 

		if(seNode){
			nrSENodes++;
		}
		
		this.name = "SE"+nrSENodes;
	}

	
	public static int getNrSENodes() {
		return nrSENodes;
	}

	public HashSet<Node> getSeNodes() {
		return seNodes;
	}


	public void setSeNodes(HashSet<Node> seNodes) {
		this.seNodes = seNodes;
	}


	public boolean isSENode() {
		Pattern p = Pattern.compile("SE");
		Matcher m = p.matcher(this.name);
		if(m.find()){
			this.isSENode = true;
		}
		else{
			this.isSENode = false;
		}
		return isSENode;
	}


	public void setSENode(boolean isSENode,String referentSlot) {
		this.referntSlot = referentSlot;
		if(!this.isSENode&&isSENode){
			nrSENodes++;
		}
		this.isSENode = isSENode;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashSet<Edge> getIncomingEdges() {
		return incomingEdges;
	}

	public void setIncomingEdges(HashSet<Edge> incomingEdges) {
		this.incomingEdges = incomingEdges;
	}

	public HashSet<Edge> getOutgoingEdges() {
		return outgoingEdges;
	}

	public void setOutgoingEdges(HashSet<Edge> outgoingEdges) {
		this.outgoingEdges = outgoingEdges;
	}
	
	public void addIncomingEdge(Edge edge){
		this.incomingEdges.add(edge);
	}
	
	public void addOutgoingEdge(Edge edge){
		this.outgoingEdges.add(edge);
	}
	
	public String toString(){
		String res = "Node: "+this.name;
		if(this.isSENode){
			res += " [maps to: "+this.referntSlot+"]";
		}
		/*res += " incoming: ";
		Iterator<Edge> it = this.incomingEdges.iterator();
		while(it.hasNext()){
			res += it.next()+" ";
		}
		res += " outgoing: ";
		it = this.outgoingEdges.iterator();
		while(it.hasNext()){
			res += it.next()+" ";
		}*/
		return res;
	}
	
	public void addSENode(Node node){
		if(!this.isSENode){
			System.err.println("check Node addSENode");
		}
		this.seNodes.add(node);
	}

	public String getReferntSlot() {
		return referntSlot;
	}

	public void setReferntSlot(String referntSlot) {
		this.referntSlot = referntSlot;
	}

	public static void setNrSENodes(int nrSENodes) {
		Node.nrSENodes = nrSENodes;
	}
	
	public void addIncomingEdges(HashSet<Edge> edges){
		this.incomingEdges.addAll(edges);
	}
	
	public void addOutgoingEdges(HashSet<Edge> edges){
		this.outgoingEdges.addAll(edges);
	}
	
}
