package struct;

import java.util.*;

public class SE extends SynSE{
	
	private String name;
	protected HashSet<String> meanings;
	protected HashMap<String,String> mapping;
	private static int nrSEs;

	public SE(){
		this.members = new HashSet<String>();
		this.mapping = new HashMap<String,String>();
		this.meanings = new HashSet<String>();
		nrSEs++;
		this.name = "SE"+nrSEs;
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


	public void addMember(String member,String meaning){
		this.members.add(member);
		this.meanings.add(meaning);
		this.mapping.put(member,meaning);
	}
	
	public void addMembers(HashSet<String> members){
		this.members.addAll(members);
	}
	
	public String getMeaning(String member){
		return this.mapping.get(member);
	}
	
	public String toString(){
		
		String result = "";
		
		Iterator<String> it = this.members.iterator();
		
		while(it.hasNext()){
			result += it.next()+" : ";
		}
		return result;
	}
	
	public void add(SE ec){
		this.members.addAll(ec.members);
		this.meanings.addAll(ec.meanings);
		this.mapping.putAll(ec.mapping);
	}
	
	public void add(SynSE ec){
		
		if(!ec.isSynSE()){
			SE test = (SE) ec;
			this.members.addAll(test.members);
			this.meanings.addAll(test.meanings);
			this.mapping.putAll(test.mapping);
		}
	}
	
	public boolean canMerge(SE se){
		
		boolean canMerge = false;
		Iterator<String> it = this.members.iterator();
		
		while(it.hasNext()){
			if(se.containsMember(it.next())){
				canMerge = true;
			}
		}
		
		it = this.meanings.iterator();
		
		while(it.hasNext()){
			if(se.containsMeaning(it.next())){
				canMerge = true;
			}
		}
		
		return canMerge;
	}
	
	public boolean containsMember(String member){
		return this.members.contains(member);
	}
	
	public boolean containsMeaning(String meaning){
		return this.meanings.contains(meaning);
	}
	
	public boolean inCommon(SE se){
	
		HashSet<String> allMembers = new HashSet<String>();
		allMembers.addAll(this.members);
		allMembers.addAll(se.members);
		boolean inCommon = false;
		
		if(allMembers.size()<this.members.size()+se.members.size()){
			inCommon = true;
		}
		
		HashSet<String> allMeanings = new HashSet<String>();
		allMeanings.addAll(this.meanings);
		allMeanings.addAll(se.meanings);
		
		if(allMeanings.size()<this.meanings.size()+se.meanings.size()){
			inCommon = true;
		}
		
		return inCommon;
	}
	
	
	public boolean inCommon(SynSE se){
	
		HashSet<String> allMembers = new HashSet<String>();
		allMembers.addAll(this.members);
		allMembers.addAll(se.members);
		boolean inCommon = false;
		
		if(allMembers.size()<this.members.size()+se.members.size()){
			inCommon = true;
		}
		
		if(!se.isSynSE()){
			SE test = (SE) se;
		
			HashSet<String> allMeanings = new HashSet<String>();
			allMeanings.addAll(this.meanings);
			allMeanings.addAll(test.meanings);
		
			if(allMeanings.size()<this.meanings.size()+test.meanings.size()){
				inCommon = true;
			}
		}
		
		return inCommon;
	}
	
	public boolean isSynSE(){
		return false;
	}
}
