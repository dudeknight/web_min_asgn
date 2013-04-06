package querySearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class SearchRunner {
	
	// private String indDir = "/home/dvdreddy/classes/web_min/ass/indDir";
	private String indDir = "../indDir";
	private IndexReader reader = null;
	private IndexSearcher searcher = null;
	private Analyzer analyzer = null;
	private QueryParser parser = null;
	private String field = "query_stem";
	private int maxHits = 40;
	private PreAnalyzer preAnalyzer;
	private SpellCorrector spellCorrector = null;
	private DocSimilarity docSim = null;
	
	public SearchRunner() throws Exception{
		init();
	}
	
	public double ftf(long x){
		return 1.0 + Math.log((double) x);
	}
	
	public void init() throws Exception{
		reader =  DirectoryReader.open(FSDirectory.open(new File(indDir)));
		searcher = new IndexSearcher(reader);
		analyzer = new StandardAnalyzer(Version.LUCENE_42);
		parser = new QueryParser(Version.LUCENE_42, field, analyzer);
		preAnalyzer = new PreAnalyzer();
		spellCorrector = new SpellCorrector(reader);
		docSim = new DocSimilarity(reader);
		// System.out.println(reader.numDocs() + " " + reader.numDeletedDocs() + " " + reader.maxDoc());
	}
	
	public void getTest(int docid) throws IOException{
		Terms	cvzx = reader.getTermVector(docid, "query_stem");
		System.out.println(cvzx.getClass());
		System.out.println(cvzx.getSumDocFreq());
		TermsEnum vc = cvzx.iterator(TermsEnum.EMPTY);
		DefaultSimilarity df = new DefaultSimilarity();
		for (int j = 0; j < cvzx.size(); j++){
			vc.next();
			Term xx = new Term("query_stem", vc.term());
			double tf = ftf(vc.totalTermFreq());
			float idf = df.idf(reader.docFreq(xx), reader.numDocs());
			System.out.println(vc.term().utf8ToString() + " " + vc.docFreq() + " " + 
			vc.totalTermFreq() + " " + reader.docFreq(xx) + " " + tf + " " + idf);
		}
	}
	
	public void executeQuery() throws Exception{
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		int totQueries = Integer.parseInt(in.readLine().trim());
		while (totQueries > 0){
			String lineInp = in.readLine().trim();
			String line = lineInp.split("[\t]+")[1];
			// System.out.println("got the line as : " + line);
			line = preAnalyzer.analyze(line);
			line = spellCorrector.spellCorrect(line);
			Query query = parser.parse(line);
			// System.out.println("quering for " + query.toString());
			// Date strt = new Date();
			TopDocs res = searcher.search(query, maxHits);
			// Date end = new Date();
			// System.out.println("Time for Search = " + (end.getTime() - strt.getTime()));
			ScoreDoc[] hits = res.scoreDocs;
			ArrayList<Integer> resDocs = new ArrayList<Integer>();
			ArrayList<Integer> remDocs = new ArrayList<Integer>();
			int numTotalHits = res.totalHits;
			int outHits = Math.min(numTotalHits, maxHits);
			// System.out.println(numTotalHits + " total matching documents");
			for (int i = 0; i < outHits; i++){
				int curDoc = hits[i].doc;
				if (resDocs.size() >= 10){
					break;
				}
				Boolean match = false;
				for (int pDoc : resDocs){
					if ((docSim.getDocSimilarity(pDoc, curDoc)) > 0.9){
						match = true; break;
					}
				}
				if (match){
					remDocs.add(curDoc);
				} else {
					resDocs.add(curDoc);
				}				
			}
			// System.out.println(resDocs.size() + " " + remDocs.size());
			if (resDocs.size() < 10){
				for (int pDoc : remDocs){
					resDocs.add(pDoc);
					if (resDocs.size() >= 10){
						break;
					}
				}				
			}
			System.out.println(lineInp);
			System.out.println(resDocs.size());
			for (int i : resDocs){
				Document doc = searcher.doc(i);
				System.out.println(doc.get("query_tot"));
				/*
				String simPrint = "";
				getTest(i);
				
				for (int j : resDocs){
					double sim = docSim.getDocSimilarity(j, i);
					simPrint += String.format("%1$,.3f", sim) + " ";
					if (j == i)
						break;
				}
				System.out.println(simPrint);
				*/
			}
			totQueries--;
		}
	}
	
	public static void main(String[] args) throws Exception{
				SearchRunner x = new SearchRunner(); 
				x.executeQuery();
	}
}
