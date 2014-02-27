package struct;

public class Edge {

	private static int nrEdges;
	
	private int pathId;
	private Node fromNode;
	private Node toNode;
	private Edge nextEdge;
	private Edge prevEdge;
	private int id;
	private boolean hasMeaning;
	private int cnt;
	
	public Edge(Node fromNode,Node toNode,int pathId,Edge prevEdge){
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.pathId = pathId;
		this.prevEdge = prevEdge;
		nrEdges++;
		this.id = nrEdges;
		this.hasMeaning = false;
	}

	public static int getNrEdges() {
		return nrEdges;
	}

	public void setMeaning(MeaningRepresentation mr){
		this.hasMeaning = true;
	}
	
	public static void setNrEdges(int nrEdges) {
		Edge.nrEdges = nrEdges;
	}

	public int getPathId() {
		return pathId;
	}

	public void setPathId(int pathId) {
		this.pathId = pathId;
	}

	public Node getFromNode() {
		return fromNode;
	}

	public void setFromNode(Node fromNode) {
		this.fromNode = fromNode;
	}

	public Node getToNode() {
		return toNode;
	}

	public void setToNode(Node toNode) {
		this.toNode = toNode;
	}

	public Edge getNextEdge() {
		return nextEdge;
	}

	public void setNextEdge(Edge nextEdge) {
		this.nextEdge = nextEdge;
	}

	public Edge getPrevEdge() {
		return prevEdge;
	}

	public void setPrevEdge(Edge prevEdge) {
		this.prevEdge = prevEdge;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	
	public String toString(){
		String res = "EDGE: "+id+" path id "+pathId;
		res += " from node: "+fromNode.getName()+" to node: "+toNode.getName();
		/*if(prevEdge!=null){
			res += "id of prev edge "+prevEdge.getId();
		}
		if(nextEdge != null){
			res += " id of next edge: "+nextEdge.getId();
		}*/
		
		return res;
	}

	public boolean isHasMeaning() {
		return hasMeaning;
	}

	public void setHasMeaning(boolean hasMeaning) {
		this.hasMeaning = hasMeaning;
	}

	public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}
	
	public void incrementCnt(){
		this.cnt++;
	}
}
