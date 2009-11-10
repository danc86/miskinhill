package au.com.miskinhill.search.webapp;

import static org.easymock.classextension.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.easymock.classextension.IMocksControl;
import org.junit.Test;

public class SearchResultsUnitTest {
    
    @Test
    public void buildShouldWorkForSearchesWithMoreThanMaxResults() throws Exception {
        IMocksControl mockControl = createControl();
        IndexReader reader = mockControl.createMock(IndexReader.class);
        ScoreDoc[] scoreDocs = new ScoreDoc[50];
        for (int i = 0; i < 50; i ++) {
            scoreDocs[i] = new ScoreDoc(i, 1.0f);
            Document doc = new Document();
            doc.add(new Field("url", "http://miskinhill.com.au/journals/test/1:1/article", Store.YES, Index.NO));
            doc.add(new Field("type", "http://miskinhill.com.au/rdfschema/1.0/Article", Store.YES, Index.NO));
            doc.add(new Field("anchor", "Some article", Store.YES, Index.NO));
            expect(reader.document(i)).andReturn(doc);
        }
        TopDocs topDocs = new TopDocs(51, scoreDocs, 1.0f); // 51 hits but only 50 (max) are returned
        
        mockControl.replay();
        SearchResults results = SearchResults.build(topDocs, reader);
        mockControl.verify();
        
        assertThat(results.size(), equalTo(50L));
    }

}
