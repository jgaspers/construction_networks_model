package struct;

import java.util.*;
import java.util.regex.*;
import util.*;

public class MeaningRepresentation {

	private HashMap<String,String> slots;
	private String predicate;
	private int nrValues;
	private static HashMap<String,MeaningRepresentation> allMrs;

	
	static{
		allMrs = new HashMap<String,MeaningRepresentation>();
	}
	
	public static void reset(){
		allMrs = new HashMap<String,MeaningRepresentation>();
	}
	
	public MeaningRepresentation(Predicate mr){
		this.predicate = mr.getM_Predicate();
		this.nrValues++;
		this.nrValues = 0;
		List<String> args = mr.getArguments();
		Iterator<String> it = args.iterator();
		int i = 0;
		this.slots = new HashMap<String,String>();
		while(it.hasNext()){
			i++;
			
			this.slots.put("noun"+i, it.next());
			this.nrValues++;
		}
		
		String decoded = decode(this);
		if(!allMrs.containsKey(decoded)){
			allMrs.put(decoded,this);
		}
	}

	public MeaningRepresentation(String predicate, HashMap<String,String> slots){
		this.nrValues = 0;
		this.predicate = predicate;
		this.nrValues++;
		this.slots = new HashMap<String,String>();
		
		for(Map.Entry<String,String> en : slots.entrySet()){
			this.slots.put(en.getKey(),en.getValue());
			this.nrValues++;
		}
		
		String decoded = decode(this);
		if(!allMrs.containsKey(decoded)){
			allMrs.put(decoded,this);
		}
	}
	
	public MeaningRepresentation(){
		this.predicate = "NOUNS_ONLY";
		this.nrValues = 0;
		this.slots = new HashMap<String,String>();
		
		String decoded = decode(this);
		if(!allMrs.containsKey(decoded)){
			allMrs.put(decoded,this);
		}
	}
	
	public MeaningRepresentation(String predicate){
		this.predicate = predicate;
		this.nrValues = 0;
		this.nrValues++;
		this.slots = new HashMap<String,String>();
		
		String decoded = decode(this);
		if(!allMrs.containsKey(decoded)){
			allMrs.put(decoded,this);	
		}
	}
	
	public HashMap<String, String> getSlots() {
		return slots;
	}

	public void setSlots(HashMap<String, String> slots) {
		this.slots = slots;
		this.nrValues += slots.size();
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {

		if(this.predicate.equals("NOUNS_ONLY")){
			this.nrValues++;
		}
		this.predicate = predicate;
		
		String decoded = decode(this);
		if(!allMrs.containsKey(decoded)){
			allMrs.put(decoded,this);
		}
	}
	
	public Iterator<String> slotIterator(){
		return this.slots.keySet().iterator();
	}
	
	
	public String toString(){
		
		String res = "";
		
		res += predicate+" ";
		
		for(Map.Entry<String,String> en : this.slots.entrySet()){
			res += en.getKey()+": "+en.getValue()+" ";
		}
		
		return res;
	}
	
	public boolean isEmpty(){
		boolean isEmpty = true;
		if((!this.predicate.equals(""))||((this.slots.size()>0)&&(this.predicate.equals("NOUNS_ONLY")))){
			isEmpty = false;
		}
		return isEmpty;
	}
	
	public int getNrValues(){
		return this.nrValues;
	}
	
	public void setSlot(String name,String referent){
		if(this.slots.containsKey(name)){
		//	System.err.println("check MeaningRepresentation!");
		}
		this.slots.put(name,referent);
		
		String decoded = decode(this);
		if(!allMrs.containsKey(decoded)){
			allMrs.put(decoded,this);
		}
	}
	
	public void mask(String name,String referent){
		
		this.slots.put(name,referent);
		
		String decoded = decode(this);
		if(!allMrs.containsKey(decoded)){
			allMrs.put(decoded,this);
		}
	}
	
	public boolean differInSlotOnly(MeaningRepresentation meaning,String slotName){
		
		boolean differInSlotOnly = true;
		
		if(!this.predicate.equals(meaning.predicate)){
			differInSlotOnly = false;
		}
		else{
			if(!meaning.slots.containsKey(slotName)||!this.slots.containsKey(slotName)){
				differInSlotOnly = false;
			}
			
			else{
				
				if(getSlotValue(slotName).equals(meaning.getSlotValue(slotName))){
					differInSlotOnly = false;
				}
				
				else{
					go: for(Map.Entry<String,String> en : this.slots.entrySet()){
					
						if(!en.getKey().equals(slotName)){
							if((!meaning.slots.containsKey(en.getKey()))||(!(meaning.slots.get(en.getKey()).equals(en.getValue())))){
								differInSlotOnly = false;
								break go;
							}
						}
					}
				}
			}
		}
		
		return differInSlotOnly;
	}
	
	public String getSlotValue(String slotName){
		if(!slots.containsKey(slotName)){
			System.err.println("check MeaningRepresentation getSlotValue");
		}
		return slots.get(slotName);
	}
	
	public MeaningRepresentation clone(){
		MeaningRepresentation res = new MeaningRepresentation(this.predicate);
		res.slots = new HashMap<String,String>();
		for(Map.Entry<String,String> en : this.slots.entrySet()){
			res.slots.put(en.getKey(),en.getValue());
		}
		return res;
	}
	
	public MeaningRepresentation join(MeaningRepresentation mr,String seName){

		Iterator<String> slotIt = mr.slotIterator();
		MeaningRepresentation result = this.clone();
		MeaningRepresentation masked = this.clone();
		
		while(slotIt.hasNext()){
		
			String slotName = slotIt.next();
			
			if(this.differInSlotOnly(mr,slotName)){
				result.slots.put(slotName, seName);
				masked.slots.put(slotName,"SE");
			}
		}
		
		String decoded = decode(result);
		if(!allMrs.containsKey(decoded)){
			allMrs.put(decoded,masked);
		}
		return result;
	}
	
	public String getDifferentSlot(MeaningRepresentation mr){

		Iterator<String> slotIt = mr.slotIterator();
		String res = "";
		
		go: while(slotIt.hasNext()){
		
			String slotName = slotIt.next();
			
			if(this.differInSlotOnly(mr,slotName)){
				res = slotName;
				break go;
			}
		}
		
		return res;
	}
	
	public static MeaningRepresentation encode(String decoded){
		if(!allMrs.containsKey(decoded)){
			System.err.println("check MeaningRepresentation encode");
		}
		
		return allMrs.get(decoded).clone();
	}
	
	public static String decode(MeaningRepresentation mr){
		String res = mr.getPredicate()+":";
		Pattern p = Pattern.compile("SE");
		Matcher matcher = null;
		
		for(Map.Entry<String,String> en : mr.slots.entrySet()){
			matcher = p.matcher(en.getValue());
			if(matcher.find()){
				res += en.getKey()+"#"+"SE";
			}
			else{
				res += en.getKey()+"#"+en.getValue();
			}
		}
		return res;
		
	}
	
	public static String decodeGeneralized(MeaningRepresentation mr){
		String res = mr.getPredicate()+":";
		MeaningRepresentation gen = mr.clone();
		
		for(Map.Entry<String,String> en : mr.slots.entrySet()){
				res += en.getKey()+"#SE";
				gen.setSlot(en.getKey(), "#SE");
		}
		
		allMrs.put(res,gen);
		
		return res;
		
	}
	
	public HashSet<String> getSlotValues(){
		
		HashSet<String> res = new HashSet<String>();
		
		for(Map.Entry<String,String> en : this.slots.entrySet()){
			res.add(en.getValue());
		}
		
		return res;
	}
	
	public boolean hasSlots(){
		boolean hasSlots = false;
		if(this.slots.size()>0){
			hasSlots = true;
		}
		return hasSlots;
	}
	
	public String toEvalString(){
		String res = this.predicate;
		if(this.slots.size()>0){
			res+="(";
			for(int i=1;i<=this.slots.size();i++){
				res += this.slots.get("noun"+i)+",";
			}
			res = res.substring(0,res.length()-1);
			res += ")";
		}
		return res;
	}

	public int getNrOfSlots(){
		return this.slots.size();
	}
	
	public int getSlotNr(String meaning){
		
		int res = -1;
		
		for(Map.Entry<String,String> en : this.slots.entrySet()){
			if(meaning.equals(en.getValue())){
				res = en.getKey().charAt(en.getKey().length()-1);
			}
		}
		
		return res;
	}
}
