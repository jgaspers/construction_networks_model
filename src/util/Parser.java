package util;

import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;
import java.util.*;

public class Parser extends DefaultHandler{
	
	private static SAXParserFactory factory;
	private static SAXParser saxParser;
	private Example currentExample;
	private boolean parsingNL;
	private boolean parsingMR;
	private LinkedList<Example> exampleList;
	
	static{

		try{
			factory = SAXParserFactory.newInstance();
			saxParser = factory.newSAXParser();
		}
		catch (Exception e) {
			System.err.println("could not create parser");
		}
	}
	
	public Parser(){
		this.currentExample = null;
		this.exampleList = new LinkedList<Example>();
	}
	
	public void startElement(String namespaceURI, String localName,String qName, Attributes atts) throws SAXException{
         if(qName.equalsIgnoreCase("example")){
            this.currentExample = new Example();
         }
         if(qName.equalsIgnoreCase("nl")){
        	 this.parsingNL = true;
         }
         if(qName.equalsIgnoreCase("mrl")){
        	 this.parsingMR = true;
         }
	}
	
    public void endElement(String namespaceURI, String localName,String qName) throws SAXException
    {
    	if((qName.equalsIgnoreCase("mrl"))){
    		this.parsingMR = false;
    	}
    	if((qName.equalsIgnoreCase("nl"))){
    		this.parsingNL = false;
    	}
    	if((qName.equalsIgnoreCase("example"))){
    		this.exampleList.add(currentExample);
    		this.currentExample = null;
    	}
    }
    	
    public void characters(char ch[], int start, int length)
    {
    	
    	if(this.parsingMR || this.parsingNL){
    		
    		String s = new String(ch,start,length).trim();
    		Predicate predicate = null;
    		
            if (s.length() > 0) {
            	if(this.parsingMR){
            		s = s.replaceAll("\\s", "");
            		predicate = new Predicate(s.toLowerCase());
              		this.currentExample.setSemantics(predicate);
            	}
            	else{
            		this.currentExample.setText(s.replaceAll("'"," "));
            	}
            }
    	}
    }

     public LinkedList<Example> getExamples(File f) throws Exception{
    	 	this.exampleList = new LinkedList<Example>();
    		saxParser.parse(f, this);
    		return this.exampleList;
     }
}
