package util;

import java.io.*;
import java.util.*;

public class Util {
	
	public static void writeFile(File file, String input) throws IOException{
		
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
		out.write(input);
		out.close();
	}

	public static String readFile(File f) throws IOException{
		
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		String line = "";
		String res = "";
		
		while ((line = in.readLine())!= null){
			res = res.concat(line+"\n");
		}
		
		return res;
	}
	
	public static LinkedList<Example> getExamples(String[] games) throws Exception{

		String dir = "data/robocup/gold_standard";
		Parser parser = new Parser();
		File file = null;
		LinkedList<Example> examples = new LinkedList<Example>();		
		
		for(int i=0; i<games.length; i++){
			
			file = new File(dir+"/"+games[i]);
			examples.addAll(parser.getExamples(file));
			
		}
		
		return examples;
	}
	
	public static LinkedList<Example> getExamples(File file) throws Exception{

		Parser parser = new Parser();
		LinkedList<Example> examples = new LinkedList<Example>();
		examples.addAll(parser.getExamples(file));

		
		return examples;
	}
	
	public static String concat(LinkedList<String> words){
		String res = "";
		Iterator<String> it = words.iterator();
		String word = "";
		while(it.hasNext()){
			word = it.next();
			if(!word.equals("ROOT")){
				res+=word+" ";
			}
		}
		res = res.substring(0,res.length()-1);
		return res;
	}
	
	public static String concat(HashSet<String> words){
		String res = "";
		Iterator<String> it = words.iterator();
		String word = "";
		while(it.hasNext()){
			word = it.next();
			if(!word.equals("ROOT")){
				res+=word+" ";
			}
		}
		res = res.substring(0,res.length()-1);
		return res;
	}
	
	public static LinkedList<AmbiguousExample> convertExamples(LinkedList<Example> examples){
		
		LinkedList<AmbiguousExample> convertedExamples = new LinkedList<AmbiguousExample>();
	
		Iterator<Example> it = examples.iterator();
		while(it.hasNext()){
			Example example = it.next();
			Vector<Predicate> semantics = new Vector<Predicate>();
			semantics.add(example.getSemantics());
			AmbiguousExample aExample = new AmbiguousExample(example.getText(),semantics);
			convertedExamples.add(aExample);
		}
		
		return convertedExamples;
	}
}
