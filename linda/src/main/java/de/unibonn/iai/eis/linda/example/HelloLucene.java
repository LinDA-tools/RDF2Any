package de.unibonn.iai.eis.linda.example;
import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import de.unibonn.iai.eis.linda.helper.LuceneHelper;
public class HelloLucene {
	  @SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException, ParseException {
		  
		 // 0. Specify the analyzer for tokenizing text.
	    //    The same analyzer should be used for indexing and searching
	    StandardAnalyzer analyzer = new StandardAnalyzer(Version.LATEST);

	    // 1. create the index
	    File indexPath = new File(LuceneHelper.homeDir()+"/example-indexes");
	    Directory index = new SimpleFSDirectory(indexPath);

	    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);

	    IndexWriter w = new IndexWriter(index, config);
	    addDoc(w, "Lucene in Action", "193398817");
	    addDoc(w, "Lucene for Dummies", "55320055Z");
	    addDoc(w, "Managing Gigabytes", "55063554A");
	    addDoc(w, "The Art of Computer Science", "9900333X");
	    w.close();
	    System.out.println("finished creating indexes ... ");

	    // 2. query
	    String querystr = args.length > 0 ? args[0] : "9900333X";

	    // the "title" arg specifies the default field to use
	    // when no field is explicitly specified in the query.
	    Query q = new QueryParser(Version.LUCENE_40, "isbn", analyzer).parse(querystr);
	    //BooleanQuery qry = new BooleanQuery();
	    //qry.add(new TermQuery(new Term("isbn", querystr)), BooleanClause.Occur.MUST);
	    //Query q = new QueryParser(Version.LATEST, "isbn", analyzer).parse(qry.toString());
	    // 3. search
	    int hitsPerPage = 10;
	    IndexReader reader = DirectoryReader.open(index);
	    IndexSearcher searcher = new IndexSearcher(reader);
	    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
	    searcher.search(q, collector);
	    ScoreDoc[] hits = collector.topDocs().scoreDocs;
	    //System.out.println(qry.toString());
	    // 4. display results
	    System.out.println("Found " + hits.length + " hits.");
	    for(int i=0;i<hits.length;++i) {
	      int docId = hits[i].doc;
	      Document d = searcher.doc(docId);
	      System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("title"));
	    }

	    // reader can only be closed when there
	    // is no need to access the documents any more.
	    reader.close();
	   	
	  }

	  private static void addDoc(IndexWriter w, String title, String isbn) throws IOException {
	    Document doc = new Document();
	    doc.add(new TextField("title", title, Field.Store.YES));

	    // use a string field for isbn because we don't want it tokenized
	    doc.add(new TextField("isbn", isbn, Field.Store.YES));
	    w.addDocument(doc);
	  }
	}
