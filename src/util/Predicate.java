package util;

import java.util.regex.*;
import java.util.*;


public class Predicate {

	protected String m_Predicate;
	
	protected List<String> m_Arguments;
	protected int[] slotAssos;
	
	public Predicate()
	{
		
	}
	

	public Predicate(String predicate, List<String> arguments)
	{
		m_Predicate = predicate;
		m_Arguments = new ArrayList<String>();
		m_Arguments.addAll(arguments);
		slotAssos = new int[m_Arguments.size()];
		for(int i=0;i<slotAssos.length;i++){
			slotAssos[i] = -1;
		}
	}
	
	
	public Predicate(String semantics)
	{
		semantics = semantics.replaceAll("\\s+", "");
		java.util.regex.Pattern p = java.util.regex.Pattern.compile("^(\\w+)\\((.*)\\)$");	
		Matcher matcher = p.matcher(semantics);
		String arguments;
		m_Arguments = new ArrayList<String>();
		String[] array;
		
		if (matcher.matches())
		{
			m_Predicate = matcher.group(1).trim();
			arguments = matcher.group(2);	
			array = arguments.split(",");
			
			for (int i = 0; i < array.length; i++)
			{
				m_Arguments.add(array[i].trim());
			}
		}
		
		else{
			m_Predicate = semantics;
		}
		
		slotAssos = new int[m_Arguments.size()];
		for(int i=0;i<slotAssos.length;i++){
			slotAssos[i] = -1;
		}
	}
	
	public String getPredicate(){
		return m_Predicate;
	}
	
	public int getDiff(Predicate predicate){
		
		int diff = 0;
	    List<String> list = predicate.getArguments();
	    int pos = 0;
		
	    if(list.size()>0){
	    	Iterator<String> it = list.iterator();
	    	while(it.hasNext()){
	    		if(!this.m_Arguments.get(pos).equals(it.next())){
	    			diff++;
	    		}
	    		pos++;
	    	}
		}
		return diff;
	}
	
	public Vector<Integer> getDifferentSlots(Predicate predicate){
		
		Vector<Integer> result = new Vector<Integer>();
	    List<String> list = predicate.getArguments();
	    int pos = 0;
		
	    if(list.size()>0){
	    	Iterator<String> it = list.iterator();
	    	while(it.hasNext()){
	    		if(!this.m_Arguments.get(pos).equals(it.next())){
	    			result.add(pos);
	    		}
	    		pos++;
	    	}
		}
		return result;
	}
	
	
	public String toString()
	{
		String string = "";
		
		if(m_Arguments.size()>0){
			string+= m_Predicate +"(";
		}
		
		String argument;
		
		for (Iterator<String> i  = m_Arguments.iterator(); i.hasNext();)
		{
			argument = i.next();
			
			string += argument +",";
			
		}
		
		if(m_Arguments.size()>0){
			string = string.substring(0,string.length()-1);
			string+= ")";
			
			for(int i=0;i<this.m_Arguments.size();i++){
				if(this.slotAssos[i]!=-1){
					string += "\n"+this.m_Arguments.get(i)+": "+this.slotAssos[i]+"\n";
					string = string.replace("\n\n", "\n");
				}
			}
			string = string.trim();
		}
		
		else{
			string = m_Predicate;
		}
		
		return string;
		
	}

	public String getM_Predicate() {
		return m_Predicate;
	}

	public void setM_Predicate(String mPredicate) {
		m_Predicate = mPredicate;
	}

	public List<String> getArguments() {
		return m_Arguments;
	}

	public void setM_Arguments(List<String> mArguments) {
		m_Arguments = mArguments;
	}
	
	public int getNrOfSlots(){
		return this.m_Arguments.size();
	}
	
	public String getArgumentValue(int pos){
		return this.m_Arguments.get(pos);
	}
	
	public void setArgument(int pos, String argument){
		m_Arguments.set(pos,argument);
	}
	
	LinkedList<String> getContext(){
		
		LinkedList<String> context = new LinkedList<String>();
		Iterator<String> it = this.m_Arguments.iterator();
		context.add(m_Predicate);
		
		while(it.hasNext()){
			context.add(it.next());
		}
		
		return context;
	}
	
	public Predicate mask(){
		
		int nr = 1;
		ArrayList<String> arguments = new ArrayList<String>();
		Iterator<String> it = this.m_Arguments.iterator();
		
		while(it.hasNext()){
			it.next();
			arguments.add("X"+nr);
			nr++;
		}

		return new Predicate(this.m_Predicate,arguments);
	}
	
	public int getSlotAsso(int pos){
		return this.slotAssos[pos];
	}
	
	public void setSlotAsso(int pos,int value){
		this.slotAssos[pos] = value;
	}
	
	public Predicate clone(){
		return new Predicate(m_Predicate,this.getArguments());
	}
	
	public int getSlotNr(String arg){
		
		Iterator<String> it = this.m_Arguments.iterator();
		int res = -1;
		int i = 0;
		
		while(it.hasNext()){
			if(it.next().equals(arg)){
				res = i;
				break;
			}
			i++;
		}
		
		return res;
	}
	
}
