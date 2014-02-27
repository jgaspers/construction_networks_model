package struct;

import network.*;

public class MappingEdge {

	private String x;
	private String y;
	private Associator phi;
	
	public MappingEdge(String x,String y){
		this.x = x;
		this.y = y;
		this.phi = new Associator(0.01,0.01,3);
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	public Associator getPhi() {
		return phi;
	}

	public void setPhi(Associator phi) {
		this.phi = phi;
	}
	
	public void trainPhi(String x,String y){

		this.phi.train(x,y);
		
	}
	
	public String getSlotASsocs(){
		return this.phi.getAssocs();
	}
	
	public void addToPhi(Associator assoc){
		this.phi.mergeInto(assoc);
	}
	
	public String getSlotValues(){
		return this.phi.getAssocs();
	}
}
