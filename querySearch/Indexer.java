package querySearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


public class Indexer {
	private String dir = "../";
	private String filesBegin = "user-ct-test-collection-";
	private String filesEnd = ".txt"; 
	private int fcount = 10;
	private Directory indexDir;
	private PreAnalyzer preAnalyzer = new PreAnalyzer();
	private FieldType myType = new FieldType();

	public Indexer(){
		File dirTemp;
		try {
			dirTemp = new File("indDir");
			indexDir = FSDirectory.open(dirTemp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		myType.setStored(false);
		myType.setIndexed(true);
		myType.setStoreTermVectors(true);
	}

	private IndexWriter indexWriter = null;

	public IndexWriter getIndexWriter(boolean create) throws IOException {		
		if (indexWriter == null) {
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_42, 
					new StandardAnalyzer(Version.LUCENE_42));
			if (create){
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				iwc.setOpenMode(OpenMode.APPEND);				
			}
			indexWriter = new IndexWriter(indexDir, iwc);			
		}
		return indexWriter;
	}

	private void indexQuery(String query, boolean create) throws Exception{
		Document doc = new Document();
		Field queryTot = new StringField("query_tot", query, Field.Store.YES);
		doc.add(queryTot);
		String queryAnalyzed = preAnalyzer.analyze(query);		
		Field queryStem = new Field("query_stem", queryAnalyzed, myType);		
		doc.add(queryStem);
		IndexWriter tempWriter = getIndexWriter(create);
		//tempWriter.addDocument(doc);
		tempWriter.updateDocument(new Term("query_tot", query) ,doc);		
	}
	
	public void closeIndexWriter() throws Exception{
		if (indexWriter != null){
			indexWriter.close(); 
		}		
	}

	public void buildIndex() throws Exception {
		for (int i = 1; i <= fcount; i++){
			Date strt = new Date();
			String val = String.valueOf(i);
			while (val.length() != 2){
				val = "0" + val;				
			}
			String filePath = dir + filesBegin + val + filesEnd;			
			BufferedReader in = new BufferedReader(new FileReader(filePath));
			String curLine = in.readLine();						
			while (curLine != null){
				String temp = curLine;
				String[] parts = temp.split("\t");
				String query = parts[1];
				// String queryTime = parts[2];
				indexQuery(query, true);
				curLine = in.readLine();
			}
			in.close();
			Date end = new Date();
			System.out.println("The file completed " + i);
			System.out.println(end.getTime() - strt.getTime() + "milli seconds");

		}
		closeIndexWriter();
	}
	
	public static void main(String[] args){
		Date strt = new Date();
		Indexer xyz = new Indexer();
		try {
			xyz.buildIndex();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date end = new Date();
		System.out.println("Total time is " + (end.getTime() - strt.getTime()) + "milli seconds");
	}
}
