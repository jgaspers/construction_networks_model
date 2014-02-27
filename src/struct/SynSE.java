package struct;

import java.util.*;

public class SynSE{
	
	private String name;
	protected HashSet<String> members;
	private static int nrSEs;

	public SynSE(){
		this.members = new HashSet<String>();
		nrSEs++;
		this.name = "SYN"+nrSEs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public HashSet<String> getMembers() {
		return members;
	}


	public void setMembers(HashSet<String> members) {
		this.members = members;
	}


	public void addMember(String member){
		this.members.add(member);

	}
	
	public void addMembers(HashSet<String> members){
		this.members.addAll(members);
	}
	
	
	
	public String toString(){
		
		String result = "";
		
		Iterator<String> it = this.members.iterator();
		
		while(it.hasNext()){
			result += it.next()+" : ";
		}
		return result;
	}
		
	public void add(SynSE ec){

		this.members.addAll(ec.members);

	}
	
	public boolean canMerge(SynSE se){
		
		boolean canMerge = false;
		Iterator<String> it = this.members.iterator();
		
		while(it.hasNext()){
			if(se.containsMember(it.next())){
				canMerge = true;
			}
		}
		
		return canMerge;
	}
	
	public boolean containsMember(String member){
		return this.members.contains(member);
	}
	
	
	public boolean inCommon(SynSE se){
	
		HashSet<String> allMembers = new HashSet<String>();
		allMembers.addAll(this.members);
		allMembers.addAll(se.members);
		boolean inCommon = false;
		
		if(allMembers.size()<this.members.size()+se.members.size()){
			inCommon = true;
		}
		
		
		return inCommon;
	}
	
	public boolean isSynSE(){
		return true;
	}
}
