package querySearch;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.similarities.DefaultSimilarity;

public class DocSimilarity {

	private IndexReader reader = null;
	private DefaultSimilarity sim = null;
	

	public DocSimilarity (IndexReader ir){		
		reader = ir;
		sim = new DefaultSimilarity();
	}

	public DocSimilarity (){
		sim = new DefaultSimilarity();
	}	

	public void setIndexReader(IndexReader ir){
		reader = ir;
	}

	public IndexReader getIndexReader(){
		return reader;
	}

	public double ftf(long x){
		return 1.0 + Math.log((double) x);
	}

	public double getDocSimilarity(int d1, int d2) throws Exception{
		if (reader == null){
			throw new Exception("wrong Init");
		}
		double score = 0.0;
		// denominator calculation step for d1
		Terms d1Terms = reader.getTermVector(d1, "query_stem");
		TermsEnum d1enum = d1Terms.iterator(TermsEnum.EMPTY);
		int numDocs = reader.numDocs();
		double denom1 = 0.0;
		for (int i = 0; i < d1Terms.size(); i++){
			d1enum.next();
			long termFreq = d1enum.totalTermFreq();
			double temp = sim.tf(termFreq);
			Term curTerm = new Term("query_stem", d1enum.term());
			int docFreq = reader.docFreq(curTerm);
			temp = temp * Math.sqrt(sim.idf(docFreq, numDocs));
			denom1 += temp * temp;
		}
		// System.out.println("d1 " + denom1);
		denom1 = sim.queryNorm((float) denom1);
		// System.out.println("d11 " + denom1);
		// denominator calculation step for d1
		Terms d2Terms = reader.getTermVector(d2, "query_stem");
		TermsEnum d2enum = d2Terms.iterator(TermsEnum.EMPTY);
		double denom2 = 0.0;
		for (int i = 0; i < d2Terms.size(); i++){
			d2enum.next();
			long termFreq = d2enum.totalTermFreq();
			double temp = sim.tf(termFreq);
			Term curTerm = new Term("query_stem", d2enum.term());
			int docFreq = reader.docFreq(curTerm);
			temp = temp * Math.sqrt(sim.idf(docFreq, numDocs));
			denom2 += temp * temp;
		}
		denom2 = sim.queryNorm((float) denom2);
		// merging the two docs
		// System.out.println(denom1 + " " + denom2);
		int i1 = 0, i2 = 0;
		d1enum = d1Terms.iterator(TermsEnum.EMPTY); d1enum.next();
		d2enum = d2Terms.iterator(TermsEnum.EMPTY); d2enum.next();
		Term d1Term = new Term("query_stem", d1enum.term());
		Term d2Term = new Term("query_stem", d2enum.term());		
		while (i1 < d1Terms.size() && i2 < d2Terms.size()){
			if (d1Term.compareTo(d2Term) < 0){
				d1enum.next(); i1++;
				d1Term = new Term("query_stem", d1enum.term());
			} else if (d1Term.compareTo(d2Term) > 0){
				d2enum.next(); i2++;		
				d2Term = new Term("query_stem", d2enum.term());
			} else {
				double temp1 = sim.tf(d1enum.totalTermFreq());
				double temp2 = sim.tf(d2enum.totalTermFreq());
				int docFreq1 = reader.docFreq(d1Term);				
				score += (sim.idf(docFreq1, numDocs) * temp1 * temp2); 
				d1enum.next(); i1++; d1Term = new Term("query_stem", d1enum.term());
				d2enum.next(); i2++; d2Term = new Term("query_stem", d2enum.term());
			}
		}
		// System.out.println(denom1 + " " + denom2 + " " + score);
		return score * denom1 * denom2;
	}
	
	public void printvf(int doc){
		
		
	}
	
	public static void main(String[] a){
		Term x1 = new Term("qeury_stem", "abc");
		Term x2 = new Term("qeury_stem", "defgg");
		System.out.println(x1.compareTo(x2));
	}
}
