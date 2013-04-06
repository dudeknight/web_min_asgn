package querySearch;

import java.util.ArrayList;

@SuppressWarnings(value = {"unused"})
public class PreAnalyzer {

	private static String loadFile= "";
	private static Boolean loadFlag = false;
	ArrayList<String> stopWords;


	public PreAnalyzer(){		
		stopWords = new ArrayList<String>();
	}

	public PreAnalyzer(Boolean b, String file){
		loadFile = file;
		loadFlag = b;
		stopWords = new ArrayList<String>();
		if (b){
			loadFromFile();	
		}
	}


	public void loadFromFile(){


	}

	public void addStopWords (ArrayList<String> newStopWords){
		stopWords.addAll(newStopWords);	
	}

	public void addStopWord (String stopWord){
		stopWords.add(stopWord);
	}

	public String analyze(String line){
		String[] tokens = line.split("[ .,:;|-]+");
		String result = "";
		Boolean b = false;
		for (String i :tokens){
			if (!stopWords.contains(i)){
				if (b) {
					result = result + " ";					
				}
				b = true;
				result = result + i;
			}
		}
		return result;
	}


	public static void main (String[] args){
		String testString = "This is   the:problem.com";
		PreAnalyzer xc= new PreAnalyzer();
		System.out.println(xc.analyze(testString));
	}
}