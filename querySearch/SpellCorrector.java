package querySearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.spell.SuggestMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class SpellCorrector {
	private SpellChecker spellChecker = null;
	private IndexReader ir = null;
	private CharArraySet stopWords = null;

	public SpellCorrector(IndexReader irx) throws IOException{
		File dirTemp = new File("../dict");
		StandardAnalyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_42);
		stopWords = standardAnalyzer.getStopwordSet();
		Directory dictDir = FSDirectory.open(dirTemp);
		this.ir = irx;
		spellChecker = new SpellChecker(dictDir);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_42, 
				standardAnalyzer);
		spellChecker.indexDictionary(new LuceneDictionary(ir, "query_stem"), iwc, false);
	}	

	public String spellCorrect(String query) throws IOException{
		String[] words = query.split(" ");
		String res = "";
		Boolean first = false;
		for (String i : words){
			if (first){
				res += " ";
			}
			if (stopWords.contains(i)){
				res += i;
			} else {
				String[] xyz = spellChecker.suggestSimilar(i, 5, ir,
						"query_stem", SuggestMode.SUGGEST_WHEN_NOT_IN_INDEX);
				// System.out.println(xyz.toString());
				// System.out.println(xyz.length);
				if (xyz != null && xyz.length != 0){
					res += xyz[0];
				} else {
					res += i;
				}
			}
			first = true;
		}
		return res;		
	}

	public static void main(String[] args) throws IOException{
		String indDir = "../indDir";
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indDir)));
		SpellCorrector spellCorrector = new SpellCorrector(reader);
		System.out.println("dict Completed");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		int testCases = 20;
		while (testCases != 0){
			System.out.println(spellCorrector.spellCorrect(in.readLine()));
			testCases--;
		}
		in.close();
	}

}
