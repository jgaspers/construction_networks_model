package testing;

import java.io.File;
import java.util.LinkedList;
import main.*;
import util.*;

public class RoboCup {
	
	public static LinkedList<AmbiguousExample> getRobocupTrainExamples(String[] games) throws Exception{

		String dir = "data/robocup/train/";
		File file = null;
		
		LinkedList<AmbiguousExample> examples = new LinkedList<AmbiguousExample>();		
		
		for(int i=0; i<games.length; i++){
			TrainParser parser = new TrainParser(file);
			file = new File(dir+"/"+games[i]);
			examples.addAll(parser.getExamples(file));
		}
		
		return examples;
	}
	
	public static void main(String[] args) throws Exception{

		LinkedList<AmbiguousExample> examples = null;
		System.out.println("results for evaluation and grammars are saved to folder data/grammars");
		System.out.println("Train: 2001-2003, Test: 2004");
		String[] games = {"2001final-train","2002final-train", "2003final-train"};
		double fM = 0.0;
		double currentF = 0.0;
		
		try{
			examples = getRobocupTrainExamples(games);
		}
		catch(Exception e){
			e.printStackTrace();
		}
    
		Model algo = new Model(0.78,0.04,0.01,0.3);
		algo.run(examples);
		String[] goldGame = {"2004final-gold"};
		LinkedList<Example> testExamples = Util.getExamples(goldGame);
		currentF = algo.runEval(testExamples);
		System.out.println("run 1 "+currentF);
		
		algo.run(examples);
		currentF = algo.runEval(testExamples);
		System.out.println("run 2: "+currentF);
		
		algo.run(examples);
		currentF = algo.runEval(testExamples);
		System.out.println("run 3: "+currentF);
	
		fM += currentF;
		algo.saveGrammar(new File("data/grammars/01-02-03.grammar"));
		
		System.out.println("Train: 2001,2002,2004, Test: 2003");
		String[] games2 = {"2001final-train","2002final-train","2004final-train"};
	
		try{
			examples = getRobocupTrainExamples(games2);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		algo = new Model(0.71,0.05,0.02,0.48);
		algo.run(examples);
		
		String[] goldGame2 = {"2003final-gold"};
		testExamples = Util.getExamples(goldGame2);
		currentF = algo.runEval(testExamples);
		System.out.println("run 1: "+currentF);
		
		algo.run(examples);
		currentF = algo.runEval(testExamples);
		System.out.println("run 2: "+currentF);
		
		algo.run(examples);
		currentF = algo.runEval(testExamples);
		System.out.println("run 3: "+currentF);

		fM += currentF;

		algo.saveGrammar(new File("data/grammars/01-02-04.grammar"));
		
		System.out.println("Train: 2001,2003,2004, Test: 2002");
		String[] games3 = {"2004final-train", "2003final-train", "2001final-train"};
		
		try{
			examples = getRobocupTrainExamples(games3);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		algo = new Model(0.74,0.03,0.02,0.51);
		algo.run(examples);
		
		String[] goldGame3 = {"2002final-gold"};
		testExamples = Util.getExamples(goldGame3);
		currentF = algo.runEval(testExamples);
		
		System.out.println("run 1: "+currentF);
		
		algo.run(examples);
		currentF = algo.runEval(testExamples);
		System.out.println("run 2: "+currentF);
		
		algo.run(examples);
		currentF = algo.runEval(testExamples);
		System.out.println("run 3: "+currentF);
		
		fM += currentF;
		
		algo.saveGrammar(new File("data/grammars/01-03-04.grammar"));
		
		System.out.println("Train: 2002-2004, Test: 2001");
		String[] games4 = {"2002final-train", "2003final-train","2004final-train"};
		
		try{
			examples = getRobocupTrainExamples(games4);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		algo = new Model(0.74,0.05,0.02,0.48);
		algo.run(examples);
		
		String[] goldGame4 = {"2001final-gold"};
		testExamples = Util.getExamples(goldGame4);
		currentF = algo.runEval(testExamples);

		System.out.println("run 1: "+currentF);
		
		algo.run(examples);
		currentF = algo.runEval(testExamples);
		System.out.println("run 2: "+currentF);
		
		algo.run(examples);
		currentF = algo.runEval(testExamples);
		System.out.println("run 3: "+currentF);
		
		fM += currentF;
		
		algo.saveGrammar(new File("data/grammars/02-03-04.grammar"));
		
		System.out.println("res: "+fM/4*100);

	
	}
		
}
