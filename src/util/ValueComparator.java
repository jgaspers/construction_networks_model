package util;

import java.util.Comparator;
import java.util.*;

	public class  ValueComparator implements Comparator<String>{

		HashMap<String, Integer> test;
		
 		public ValueComparator(HashMap<String,Integer> test){
 			this.test = test;
		}
			public int compare(String s1, String s2){

				if(  test.get(s1) >  test.get(s2))

				return 1;

				else if( test.get(s1) < test.get(s2) )

				return -1;

				else

				return 0;

				}
	}
