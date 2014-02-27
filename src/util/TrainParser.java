package util;

import java.util.*;
import java.io.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class TrainParser extends DefaultHandler{
	
	private HashMap<Integer,Predicate> semantics;
	private Vector<String> tmpNLs;
	private static SAXParserFactory factory;
	private static SAXParser saxParser;
	private boolean parsingNL;
	private boolean parsingSemIDs;
	private boolean parsingSem;
	private LinkedList<AmbiguousExample> exampleList;
	private String tmp;
	private int id;
	
	public TrainParser(File file){
		this.semantics = new HashMap<Integer, Predicate>();
		this.tmpNLs = new Vector<String>();
	}
	
	static{

		try{
			factory = SAXParserFactory.newInstance();
			saxParser = factory.newSAXParser();
		}
		catch (Exception e) {
			System.err.println("could not create parser");
		}
	}
	
	public void startElement(String namespaceURI, String localName,String qName, Attributes atts) throws SAXException{

         if(qName.equalsIgnoreCase("nl")){
        	 this.parsingNL = true;
         }
         if(qName.equalsIgnoreCase("semid")){
        	 this.parsingSemIDs = true;
         }
         
        	if(qName.equalsIgnoreCase("sem")){
            	this.parsingSem = true;
            	this.id = Integer.parseInt(atts.getValue("id"));
            }
	}
	
    public void endElement(String namespaceURI, String localName,String qName) throws SAXException
    {
    	if((qName.equalsIgnoreCase("nl"))){
    		this.parsingNL = false;
    	}
    	if((qName.equalsIgnoreCase("semid"))){
    		this.parsingSemIDs = false;
    	}
    	if((qName.equalsIgnoreCase("sem"))){
    		this.parsingSem = false;
    	}
    	
    }
    	

    public void characters(char ch[], int start, int length)
    {
    	
    	if(this.parsingSemIDs || this.parsingNL){
    		
    		String s = new String(ch,start,length).trim();
    		
            if (s.length() > 0) {
            	if(this.parsingNL){
            		this.tmp = s.replaceAll("'", " ");
            	}
            	else{
            		this.tmp = this.tmp + "#"+s;
            		this.tmpNLs.add(tmp);
            	}
            }
    	}
    	
    	if(this.parsingSem){
    		
    		String s = new String(ch,start,length).trim().toLowerCase();
            if (s.length() > 0) {
            	this.semantics.put(this.id,new Predicate(s.replaceAll("\\s", "")));
            }
    	}
    }

     public LinkedList<AmbiguousExample> getExamples(File f) throws Exception{
    	 	
    	 	Iterator<String> it = null;
    	 	AmbiguousExample example = null;
    	 	String[] split = null;
    	 	String tmp = "";
    	 	String nl = "";
    	 	String sems = "";
    	 	Vector<Predicate> currentSemantics = null;
    	 	Predicate currentPredicate = null;
    	 	
    	 	this.exampleList = new LinkedList<AmbiguousExample>();
    		saxParser.parse(f, this);
    		
    		it = this.tmpNLs.iterator();
    		
    		while(it.hasNext()){
    			currentSemantics = new Vector<Predicate>();
    			tmp = it.next();
    			split = tmp.split("#");
    			nl = split[0];
    			sems = split[1];
    			split = sems.split("\\s");
    			
    			if(split.length>0){
    				for(int i=0;i<split.length;i++){
    					currentPredicate = this.semantics.get(Integer.parseInt(split[i].trim()));
    					currentSemantics.add(currentPredicate);
    				}
    				
    				example = new AmbiguousExample(new Sentence(nl),currentSemantics);
    				this.exampleList.add(example);
    			}
    		}

    		return this.exampleList;
     }
	
}
