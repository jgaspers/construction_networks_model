package network;

import java.util.*;
import util.*;

public class Associator {
	
	protected double[][] weights;
	protected HashMap<String,Integer> mappingX;
	protected HashMap<String,Integer> mappingY;
	protected double eta;
	protected double c;
	// initialization for weights of new nodes (neurons):
	// 1: principle of contrast (first nodes are initialized randomly)
	// 2: random * ETA
	// 3: zero
	protected int initType;
	
	public Associator(double eta, double c){
		this.eta = eta;
		this.mappingX = new HashMap<String,Integer>();
		this.mappingY = new HashMap<String,Integer>();
		this.weights = new double[0][0];
		this.c = c;
		this.initType = 1;
	}
	
	public Associator(double eta){
		this.eta = eta;
		this.mappingX = new HashMap<String,Integer>();
		this.mappingY = new HashMap<String,Integer>();
		this.weights = new double[0][0];
		this.c = eta;
		this.initType = 1;
	}
	
	public Associator(double eta, double c, int initType){
		this.eta = eta;
		this.mappingX = new HashMap<String,Integer>();
		this.mappingY = new HashMap<String,Integer>();
		this.weights = new double[0][0];
		this.c = c;
		this.initType = initType;
	}
	
	public double[] toVector(HashSet<String> values, String type){
		
		double[] res = null;
		double value = 1.0;
		
		if(type.equals("x")){
			res = new double[this.mappingX.size()];
			
			for(int i=0;i<res.length;i++){
				res[i] = 0.0;
			}
			
			for(Map.Entry<String,Integer> en : this.mappingX.entrySet()){
				if(values.contains(en.getKey())){
					res[en.getValue()] = value;
				}
			}
		}
		
		else{
			if(type.equals("y")){
				res = new double[this.mappingY.size()];
			
				for(int i=0;i<res.length;i++){
					res[i] = 0.0;
				}
			
				for(Map.Entry<String,Integer> en : this.mappingY.entrySet()){
					if(values.contains(en.getKey())){
						res[en.getValue()] = value;
					}
				}
			}	
		}
		
		return res;
	}
	
	public double[] multiplyWithWeigths(double[] vector, boolean transpose){
		
		double[] res = null;
		double value = 0.0;
		
		if(!transpose){
			res = new double[this.mappingX.size()];
			
			for(int i=0;i<mappingX.size();i++){
				value = 0.0;
				for(int j=0;j<mappingY.size();j++){
					value += this.weights[i][j]*vector[j];
				}
				res[i] = value;
			}
		}
		
		else{
			res = new double[this.mappingY.size()];
			
			for(int i=0;i<mappingY.size();i++){
				value = 0.0;
				for(int j=0;j<mappingX.size();j++){
					value += this.weights[j][i]*vector[j];
				}
				res[i] = value;
			}
		}
		
		for(int i=0;i<res.length;i++){
			res[i] = round(res[i]);
		}
		return res;
	}
	
	public double[] subtractVectors(double[] vector1, double[] vector2){
		
		if(vector1.length != vector2.length){
			System.err.println("ungleiche Vektorlaenge");
			System.exit(0);
		}
		
		double[] res = new double[vector1.length];
		
		for(int i=0;i<res.length;i++){
			res[i] = vector1[i] - vector2[i];

		}
		
		return res;
	}
	
	public void initPrincipleOfContrast(HashSet<String> xValues, HashSet<String> yValues){
		
		if((mappingX.size()==0) || (mappingY.size()==0)){
			Iterator<String> it = xValues.iterator();
			
			while(it.hasNext()){
				this.mappingX.put(it.next(), this.mappingX.size());
			}
			
			it = yValues.iterator();
			
			while(it.hasNext()){
				this.mappingY.put(it.next(), this.mappingY.size());
			}
			
			this.weights = new double[this.mappingX.size()][this.mappingY.size()];
			
			for(int i=0;i<this.mappingX.size();i++){
				for(int j=0;j<this.mappingY.size();j++){
			
					this.weights[i][j] = 0.0;
				}
			}
		}
		
		// network already contains nodes 
		else{
			// new MR (y) nodes are processed first
			Iterator<String> it = yValues.iterator();
			String current = "";
			int oldSizeYValues = this.mappingY.size();
			double[][] newWeights = null;
			
			while(it.hasNext()){
				current = it.next();
				
				if(!mappingY.containsKey(current)){
					this.mappingY.put(current,this.mappingY.size());
				}
			}
			
			if(this.mappingY.size()!=oldSizeYValues){
				newWeights = new double[this.mappingX.size()][this.mappingY.size()];
			
				for(Map.Entry<String, Integer> enX : this.mappingX.entrySet()){
				
					boolean computed = false;
					double initWeight = 0.0;
				
					for(Map.Entry<String, Integer> enY : this.mappingY.entrySet()){
						if((enY.getValue() < oldSizeYValues)){
							newWeights[enX.getValue()][enY.getValue()] = weights[enX.getValue()][enY.getValue()];
						}
						else{
							// compute initial weight by applying principle of contrast
							if(!computed){
								double max = 0.0;
						
								for(int i=0;i<oldSizeYValues;i++){
									if(weights[enX.getValue()][i] >= max){
						
										max = weights[enX.getValue()][i];
									}
								}
							
								int nrOfNewNodes = this.mappingY.size()-oldSizeYValues;
								initWeight = ((1-max)*c)/nrOfNewNodes;
								newWeights[enX.getValue()][enY.getValue()] = initWeight; 
								computed = true;
							}
							else{
								newWeights[enX.getValue()][enY.getValue()] = initWeight; 
							}
						}
					}
				}
				this.weights = newWeights;
			}	
			
			// add and init new X nodes
			it = xValues.iterator();
			int oldSizeXValues = this.mappingX.size();
			
			while(it.hasNext()){
				current = it.next();
				
				if(!mappingX.containsKey(current)){
					this.mappingX.put(current,this.mappingX.size());
				}
			}
			
			if(oldSizeXValues!=this.mappingX.size()){
				newWeights = new double[this.mappingX.size()][this.mappingY.size()];
			
				for(Map.Entry<String, Integer> enY : this.mappingY.entrySet()){
				
					boolean computed = false;
					double initWeight = 0.0;
				
					for(Map.Entry<String, Integer> enX : this.mappingX.entrySet()){
						if((enX.getValue() < (oldSizeXValues))){
							newWeights[enX.getValue()][enY.getValue()] = weights[enX.getValue()][enY.getValue()];
						}
						else{
							// compute initial weight by applying principle of contrast
							if(!computed){
								double max = 0.0;
						
								for(int i=0;i<oldSizeXValues;i++){
									if(weights[i][enY.getValue()] >= max){
										max = weights[i][enY.getValue()];
									}
								}

								int nrOfNewNodes = this.mappingX.size()-oldSizeXValues;
								initWeight = ((1-max)*c)/nrOfNewNodes;
								newWeights[enX.getValue()][enY.getValue()] = initWeight; 
								computed = true;
							}
							else{
								newWeights[enX.getValue()][enY.getValue()] = initWeight; 
							
							}
						}
					}
				}	
			
			this.weights = newWeights;

			}
		}

	}
	
	public void initRandom(HashSet<String> xValues, HashSet<String> yValues){
		Iterator<String> it = xValues.iterator();
		String current = "";
		int sizeXOld = this.mappingX.size();
		int sizeYOld = this.mappingY.size();
		
		while(it.hasNext()){
			current = it.next();
			
			if(!this.mappingX.containsKey(current)){
				this.mappingX.put(current,this.mappingX.size());
			}
		}
		
		it = yValues.iterator();
		while(it.hasNext()){
			current = it.next();
			
			if(!this.mappingY.containsKey(current)){
				this.mappingY.put(current,this.mappingY.size());
			}
		}
		
		if(this.mappingX.size()!=sizeXOld || this.mappingY.size()!=sizeYOld){
		
			double[][] newWeights = new double[this.mappingX.size()][this.mappingY.size()];
		
			for(Map.Entry<String, Integer> enX : this.mappingX.entrySet()){
				for(Map.Entry<String, Integer> enY : this.mappingY.entrySet()){
				
					if((enX.getValue()<sizeXOld)&&(enY.getValue()<sizeYOld)){
						newWeights[enX.getValue()][enY.getValue()] = weights[enX.getValue()][enY.getValue()];
					}
					else{
						newWeights[enX.getValue()][enY.getValue()] = Math.random()*eta;
					}
				}
			}
			this.weights = newWeights;
		}
	}
	
	public void initZero(HashSet<String> xValues, HashSet<String> yValues){
		Iterator<String> it = xValues.iterator();
		String current = "";
		int sizeXOld = this.mappingX.size();
		int sizeYOld = this.mappingY.size();
		
		while(it.hasNext()){
			current = it.next();
			
			if(!this.mappingX.containsKey(current)){
				this.mappingX.put(current,this.mappingX.size());
			}
		}
		
		it = yValues.iterator();
		while(it.hasNext()){
			current = it.next();
			
			if(!this.mappingY.containsKey(current)){
				this.mappingY.put(current,this.mappingY.size());
			}
		}
		
		if(this.mappingX.size()!=sizeXOld || this.mappingY.size()!=sizeYOld){
		
			double[][] newWeights = new double[this.mappingX.size()][this.mappingY.size()];
		
			for(Map.Entry<String, Integer> enX : this.mappingX.entrySet()){
				for(Map.Entry<String, Integer> enY : this.mappingY.entrySet()){
				
					if((enX.getValue()<sizeXOld)&&(enY.getValue()<sizeYOld)){
						newWeights[enX.getValue()][enY.getValue()] = weights[enX.getValue()][enY.getValue()];
					}
					else{
						newWeights[enX.getValue()][enY.getValue()] = 0.0;
					}
				}
			}
			this.weights = newWeights;
		}
	}
	

	public void train(HashSet<String> xValues, HashSet<String> yValues){

		if((xValues.size()==0) || (yValues.size()==0)){
			System.err.println("Associator: only x values OR y values given, weights will not be updated");
		}
		
		else{
			
			switch(this.initType){
				case 1: this.initPrincipleOfContrast(xValues,yValues);break;
				case 2: this.initRandom(xValues,yValues);break;
				case 3: this.initZero(xValues,yValues);break;
				}
			
				double[] vectorX = this.toVector(xValues, "x");
				double[] vectorY = this.toVector(yValues, "y");
		
				double[] vectorXOld = this.multiplyWithWeigths(vectorY, false);
				double[] vectorYOld = this.multiplyWithWeigths(vectorX, true);
		
				for(int i=0;i<this.mappingX.size();i++){
					for(int j=0;j<this.mappingY.size();j++){
						this.weights[i][j] += this.round(eta*(vectorX[i]-vectorXOld[i])*(vectorY[j]-vectorYOld[j])); 
						
						if(this.weights[i][j]<0){
							this.weights[i][j] = 0.0;
						}
						
						this.weights[i][j] = this.round(this.weights[i][j]);
				}
			}	
		}
	}
	
	
	public void printWeights(){
		System.out.println("weights");
		for(int i=0;i<mappingX.size();i++){
			for(int j=0;j<mappingY.size();j++){
				System.out.print(this.weights[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	public String getAssociation(String word){

		String res = "";
		double bestRes = 0.0;
		int xNr = this.mappingX.get(word);
		int yNr = 0;
		double currentRes = 0.0;
	
		for(int i=0;i<this.mappingY.size();i++){
			currentRes = this.weights[xNr][i];
			if(currentRes >= bestRes){
				
				bestRes = currentRes;
				yNr = i;
			}
		}
	
		for(Map.Entry<String, Integer> entry : this.mappingY.entrySet()){
			if(entry.getValue() == yNr){
				res = entry.getKey();
				break;
			}
		}
	
		return res;
	}
	
	public void printAssocs(){
		for(Map.Entry<String, Integer> enX : this.mappingX.entrySet()){
			
			String word = enX.getKey();
			String assoc = getAssociation(word);
			double weight = getWeight(word,assoc);
			
			if(isLearnedMeaningEntropy(enX.getKey(),getAssociation(enX.getKey()),0.74)&&weight>0.06){
				System.out.println(enX.getKey()+" "+getAssociation(enX.getKey())+" "+getWeight(enX.getKey(),getAssociation(enX.getKey())));
			}
			else{
				System.err.println(enX.getKey()+" "+getAssociation(enX.getKey())+" "+weight);
			}
		}
		
	}

	public void printAssocs(String x){
		int xNr = this.mappingX.get(x);
		System.out.print("ASSOC: "+this.getAssociation(x)+" ");
		
		for(Map.Entry<String,Integer> en : this.mappingY.entrySet()){
			System.out.print(en.getKey()+": "+this.weights[xNr][en.getValue()]);
		}
		
		System.out.println();
		
	}
	
	public void printAssocsBack(){
		for(Map.Entry<String, Integer> enY : this.mappingY.entrySet()){
			System.out.println(enY.getKey()+" "+getAssociationBack(enY.getKey()));
		}
	}
	
	public void printMappings(){
		for(Map.Entry<String, Integer> enX : this.mappingX.entrySet()){
			System.out.println(enX.getKey()+" "+enX.getValue());
		}
		for(Map.Entry<String, Integer> enY : this.mappingY.entrySet()){
			System.out.println(enY.getKey()+" "+enY.getValue());
		}
	}
	
	
	protected double round(double x){
		return Math.round(x*10000)*1.0/10000;
	}
	
	public boolean isArgMaxMeaning(String x,String y){
		
		boolean isArgMaxMeaning = false;
		double bestRes = 0.0;
		int xNr = this.mappingX.get(x);
		double currentRes = 0.0;
		int yNr = this.mappingY.get(y);
		double second = 0.0;
		
		for(int i=0;i<this.mappingY.size();i++){
			currentRes = weights[xNr][i];

			if(currentRes >= bestRes){
				bestRes = currentRes;
				
			}
			
			if(currentRes<bestRes&&currentRes>second){
				second = currentRes;
			}
		}
		
		if(weights[xNr][yNr]==bestRes){
			isArgMaxMeaning = true;
		}
		
		return isArgMaxMeaning;
	}
	
	public String getAssociationBack(String word){

		String res = "";
		double bestRes = 0.0;
		int yNr = this.mappingY.get(word);
		int xNr = 0;
		double currentRes = 0.0;
	
		for(int i=0;i<this.mappingX.size();i++){
			currentRes = this.weights[i][yNr];
			if(currentRes >= bestRes){
				
				bestRes = currentRes;
				xNr = i;
			}
		}
	
		for(Map.Entry<String, Integer> entry : this.mappingX.entrySet()){
			if(entry.getValue() == xNr){
				res = entry.getKey();
				break;
			}
		}
		
		return res;
	}
	
	public void train(String x, String y){
		HashSet<String> xValue = new HashSet<String>();
		xValue.add(x);
		HashSet<String> yValue = new HashSet<String>();
		yValue.add(y);
		this.train(xValue, yValue);
	}
	
	public HashMap<String,Double> getWeights(String x){
		HashMap<String,Double> result = new HashMap<String,Double>();
		
		if(!this.mappingX.containsKey(x)){
			System.err.println("Associator: "+x+"is not contained!");
		}
		
		int xNr = this.mappingX.get(x);
		
		for(Map.Entry<String,Integer> en : this.mappingY.entrySet()){
			result.put(en.getKey(),weights[xNr][en.getValue()]);
		}
		
		return result;
	}
	
	public boolean isLearnedMeaningThresh(String x, String y,double thresh){
		
		boolean isLearnedMeaning = false;
		int xNr = this.mappingX.get(x);
		int yNr = this.mappingY.get(y);
		
		if(this.weights[xNr][yNr]>thresh){
			isLearnedMeaning = true;
		}
		
		return isLearnedMeaning;
	}

	public boolean isLearnedMeaningDist(String x, String y,double dist){
		
		boolean isLearnedMeaning = true;
		double bestRes = 0.0;
		int xNr = this.mappingX.get(x);
		double currentRes = 0.0;
		int yNr = this.mappingY.get(y);
		
		for(int i=0;i<this.mappingY.size();i++){
			currentRes = weights[xNr][i];

			if(currentRes >= bestRes){
				bestRes = currentRes;
			}
		}
		
		if(weights[xNr][yNr]==bestRes){
			go: for(int i=0;i<this.mappingY.size();i++){
				currentRes = weights[xNr][i];

				if(((bestRes - currentRes) < dist)&&(i!=yNr)){
					isLearnedMeaning = false;
					break go;
				}
			}
		}
		else{
			isLearnedMeaning = false;
		}
		
		return isLearnedMeaning;
	}
	
	public boolean isSingleBestMeaning(String x,String y){
		
		boolean isSingleBestMeaning = true;
		double bestRes = 0.0;
		int xNr = this.mappingX.get(x);
		double currentRes = 0.0;
		int yNr = this.mappingY.get(y);
		
		for(int i=0;i<this.mappingY.size();i++){
			currentRes = weights[xNr][i];
	
			if(currentRes >= bestRes){
				bestRes = currentRes;
			}
		}
	
		if(weights[xNr][yNr]==bestRes){
			go: for(int i=0;i<this.mappingY.size();i++){
				currentRes = weights[xNr][i];

				if((currentRes == bestRes)&&(i!=yNr)){
					isSingleBestMeaning = false;
					break go;
				}
			}
		}
		else{
			isSingleBestMeaning = false;
		}

		return isSingleBestMeaning;
	}
	
	public double getEntropy(String x,String y){
		
		double res = 0.0;
		int xNr = this.mappingX.get(x);
		int yNr = this.mappingY.get(y);
		
		if(this.weights[xNr][yNr]>0.0){
			res = this.weights[xNr][yNr]*(Math.log(weights[xNr][yNr])/Math.log(2))*(-1.0);
		}
		
		return res;
	}
	
	public double getEntropy(int xNr,int yNr){
		double res = 0.0;
		
		if(this.weights[xNr][yNr]>0.0){
			res = this.weights[xNr][yNr]*(Math.log(weights[xNr][yNr])/Math.log(2))*(-1.0);
		}
		
		return res;
	}
	
	public double getEntropy(double value){
		double res = 0.0;
		
		if(value>0.0){
			res = value*(Math.log(value)/Math.log(2))*(-1.0);
		}
		
		return res;
	}
	
	public boolean isLearnedMeaningEntropy(String x,String y,double perc){
		
		boolean isLearnedMeaning = false;
		
		if(this.hasLearnedMeaningEntropy(x,perc)&&this.isSingleBestMeaning(x,y)){
			isLearnedMeaning = true;
		}
		
		return isLearnedMeaning;
	}
	
	public boolean hasLearnedMeaningEntropy(String x,double perc,double thresh){

		boolean isLearned = false;
		
		if(hasLearnedMeaningEntropy(x,perc)&&getWeight(x,this.getAssociation(x))>thresh){
			isLearned = true;
		}
		
		return isLearned;
	}
	
	public double entropyProportion(String x){
		
		int xNr = this.mappingX.get(x);
		double entropyAll = 0.0;
		double normFactor = 0.0;
		double maxEntropy = 0.0;
		double smallerZero = 0.0;
		
		// maximum entropy
		for(int i=0;i<this.mappingY.size();i++){
			maxEntropy += this.getEntropy(1.0/this.mappingY.size());
		}
		
		// current entropy
		double[] values = new double[this.mappingY.size()];
		
		for(int i=0;i<this.mappingY.size();i++){
			
			normFactor += this.weights[xNr][i];
			
			
			values[i] = this.weights[xNr][i];
			if(values[i]<smallerZero){
				smallerZero = values[i];
			}
		}
		
		if(smallerZero<0.0){
			normFactor = 0.0;
			for(int i=0;i<values.length;i++){
				values[i] += Math.abs(smallerZero);
				normFactor += values[i];
			}
		}
		
		for(int i=0;i<values.length;i++){
			entropyAll += getEntropy(values[i]/normFactor);

		}
	
		return (entropyAll/maxEntropy);
	}
	
	public boolean hasLearnedMeaningEntropy(String x,double perc){
		
		int xNr = this.mappingX.get(x);
		double entropyAll = 0.0;
		boolean isLearnedMeaning = false;
		double normFactor = 0.0;
		
		double maxEntropy = 0.0;
		double smallerZero = 0.0;
		
		// maximum entropy
		for(int i=0;i<this.mappingY.size();i++){
			maxEntropy += this.getEntropy(1.0/this.mappingY.size());
		}
		
		// current entropy
		double[] values = new double[this.mappingY.size()];
		
		for(int i=0;i<this.mappingY.size();i++){
			
			normFactor += this.weights[xNr][i];
			
			
			values[i] = this.weights[xNr][i];
			if(values[i]<smallerZero){
				smallerZero = values[i];
			}
		}
		
		if(smallerZero<0.0){
			normFactor = 0.0;
			for(int i=0;i<values.length;i++){
				values[i] += Math.abs(smallerZero);
				normFactor += values[i];
			}
		}
		
		for(int i=0;i<values.length;i++){
			entropyAll += getEntropy(values[i]/normFactor);

		}
		
		if(((entropyAll/maxEntropy) < perc)){
			isLearnedMeaning = true;
		}

		return isLearnedMeaning;
	}

	public Iterator<String> xIterator(){
		return this.mappingX.keySet().iterator();
	}
	
	public Iterator<String> yIterator(){
		return this.mappingY.keySet().iterator();
	}
	
	public double getWeight(String x,String y){
		int xNr = this.mappingX.get(x);
		int yNr = this.mappingY.get(y);
		return this.weights[xNr][yNr];
	}
	
	public boolean containsX(String x){
		return this.mappingX.containsKey(x);
	}
	
	public boolean containsY(String y){
		return this.mappingY.containsKey(y);
	}
	
	public void train(String x,HashSet<String> yValues){
		HashSet<String> xValues = new HashSet<String>();
		xValues.add(x);
		this.train(xValues, yValues);
	}
	
	public double[] getXRow(String x){
		
		if(!this.mappingX.containsKey(x)){
			System.err.println("check assoc getXValues");
		}
		
		int xNr = this.mappingX.get(x);
		
		double[] res = new double[this.mappingY.size()];
		for(int i=0;i<this.mappingY.size();i++){
			res[i] = this.weights[xNr][i];
		}
		
		return res;
	}
	
	public double[] mergeXRows(HashSet<String> xValues){
		
		Iterator<String> it = xValues.iterator();
		double[] res = new double[this.mappingY.size()];
		
		for(int i=0;i<this.mappingY.size();i++){
			res[i] = 0.0;
		}
		
		while(it.hasNext()){
			
			String x = it.next();
			if(!this.mappingX.containsKey(x)){
				System.err.println("check assoc getXValues "+x);
			}
		
			int xNr = this.mappingX.get(x);
		
			for(int i=0;i<this.mappingY.size();i++){
				res[i] += this.weights[xNr][i];
			}
		}
		
		for(int i=0;i<this.mappingY.size();i++){
			
			res[i] = round(res[i]);
		
		}

		return res;
	}
	
	
	public void deleteXRows(HashSet<String> xValues){
		
		Iterator<String> it = xValues.iterator();
		HashSet<String> checked = new HashSet<String>();
		TreeMap<String,Integer> sortedXMapping = new TreeMap<String,Integer>(new ValueComparator(this.mappingX));
		sortedXMapping.putAll(this.mappingX);
		
		while(it.hasNext()){
			String x = it.next();
		
			if(this.mappingX.containsKey(x)){
				checked.add(x);
			}
		}
	
		HashMap<String,Integer> newXMapping = new HashMap<String,Integer>();
		double newWeights[][] = new double[this.mappingX.size()-checked.size()][this.mappingY.size()];
		int i = 0;
		
		for(Map.Entry<String, Integer> en : sortedXMapping.entrySet()){

			if(!checked.contains(en.getKey())){
				newXMapping.put(en.getKey(),i);
				
				for(int j=0;j<this.mappingY.size();j++){
					newWeights[i][j] = this.weights[en.getValue()][j];
				}
				
				i++;
			}
		}
		
		this.weights = newWeights;
		this.mappingX = newXMapping;
	}
	
	public void addXRow(String x,double[] xValues){
		if(xValues.length!=this.mappingY.size()){
			System.err.println("check addXRow Associator");
		}
		
		if(mappingX.containsKey(x)){
			System.err.println("check Associator addXRow 2");
		}

		this.mappingX.put(x,this.mappingX.size());
		double[][] newWeights = new double[this.mappingX.size()][this.mappingY.size()];
		
		for(int i=0;i<this.mappingX.size();i++){
			for(int j=0;j<this.mappingY.size();j++){
				if(i<this.mappingX.size()-1){
					newWeights[i][j] = this.weights[i][j];
				}
				else{
					newWeights[i][j] = xValues[j];
				}
			}
		}
		
		this.weights = newWeights;
	}
	
	public void setXRow(String x,double[] xValues){
		if(xValues.length!=this.mappingY.size()){
			System.err.println("check addXRow Associator");
		}
		
		if(mappingX.containsKey(x)){
			int xNr = this.mappingX.get(x);
			
			for(int i=0;i<xValues.length;i++){
				if(xValues[i] > 1){
					this.weights[xNr][i] = 1;
				}
				else{
					this.weights[xNr][i] = xValues[i];
				}
			}
		}
		
		else{
			this.addXRow(x,xValues);
		}
	}

	public String getAssocsBack(){
		
		String res = "";
		
		for(Map.Entry<String, Integer> enY : this.mappingY.entrySet()){
			res += enY.getKey()+" "+getAssociationBack(enY.getKey())+" ";
		}
		
		return res;
	}
	
	public String getAssocs(){
		
		String res = "";
		
		for(Map.Entry<String, Integer> enX : this.mappingX.entrySet()){
				res += enX.getKey()+" -> "+getAssociation(enX.getKey())+" "+getWeight(enX.getKey(),getAssociation(enX.getKey()))+" ";
			}
		
		return res;
	}

	public void mergeInto(Associator assoc){
		
		int oldXSize = this.mappingX.size();
		int oldYSize = this.mappingY.size();
		double[][] newWeights = null;
		
		for(Map.Entry<String, Integer> enX : assoc.mappingX.entrySet()){
			if(!this.mappingX.containsKey(enX.getKey())){
				this.mappingX.put(enX.getKey(),this.mappingX.size());
			}
		}
		
		for(Map.Entry<String, Integer> enY : assoc.mappingY.entrySet()){
			if(!this.mappingY.containsKey(enY.getKey())){
				this.mappingY.put(enY.getKey(),this.mappingY.size());
			}
		}
		
		
		if(this.mappingX.size()!=oldXSize||this.mappingY.size()!=oldYSize){
		
			int newSizeX = this.mappingX.size();
			int newSizeY = this.mappingY.size();
			newWeights = new double[newSizeX][newSizeY];
			
			for(int i=0;i<newSizeX;i++){
				for(int j=0;j<newSizeY;j++){
					
					if(i<oldXSize && j<oldYSize){
						newWeights[i][j] = this.weights[i][j];
					}
					else{
						newWeights[i][j] = 0.0;
					}
				}
			}
		}
		else{
			newWeights = weights;
		}
			
		for(Map.Entry<String, Integer> enX : assoc.mappingX.entrySet()){
				for(Map.Entry<String, Integer> enY : assoc.mappingY.entrySet()){
				
					String x = enX.getKey();
					String y = enY.getKey();
				
					newWeights[this.mappingX.get(x)][this.mappingY.get(y)] += assoc.getWeight(x, y);
				}
			}
			this.weights = newWeights;
			
			for(int i=0;i<this.mappingX.size();i++){
				for(int j=0;j<this.mappingY.size();j++){
					if(this.weights[i][j] > 1){
						this.weights[i][j] = 1;
					}
				}
			}
		
	}
	
	public HashMap<String,String> extractMappingX(){
		HashMap<String,String> mapping = new HashMap<String,String>();
		for(Map.Entry<String,Integer> en : this.mappingX.entrySet()){
			mapping.put(en.getKey(),this.getAssociation(en.getKey()));
		}
		return mapping;
	}
	
	public int getNrInY(){
		
		int res = this.mappingY.size();
		
		if(this.mappingY.containsKey("none")){
			res--;
		}
		
		if(this.mappingY.containsKey("noun1")&&this.mappingY.containsKey("noun2")){
			System.err.println(this.getAssociationBack("noun1")+" "+this.getAssociationBack("noun2"));
			if(this.getAssociationBack("noun1").equals(this.getAssociationBack("noun2"))){
				res = -1;
			}
		}
		
		return res; 
	}

}