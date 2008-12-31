package au.com.miskinhill.search.analysis;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.junit.Test;

public class PerLanguageAnalyzerWrapperUnitTest {
	
	private Analyzer defaultAnalyzer = createMock(Analyzer.class);
	private Analyzer enAnalyzer = createMock(Analyzer.class);
	private Analyzer ruAnalyzer = createMock(Analyzer.class);

	@Test
	public void testGetAnalyzers() {
		PerLanguageAnalyzerWrapper plaw = 
				new PerLanguageAnalyzerWrapper(defaultAnalyzer);
		plaw.addAnalyzer("en", enAnalyzer);
		plaw.addAnalyzer("ru", ruAnalyzer);
		assertThat(plaw.getAnalyzers(), 
				hasItems(defaultAnalyzer, enAnalyzer, ruAnalyzer));
	}
	
	@Test
	public void testTokenStreamNoLanguage() {
		expect(defaultAnalyzer.tokenStream(
				isA(String.class), isA(Reader.class))).andReturn(null);
		replay(defaultAnalyzer, enAnalyzer, ruAnalyzer);
		PerLanguageAnalyzerWrapper plaw = 
				new PerLanguageAnalyzerWrapper(defaultAnalyzer);
		plaw.addAnalyzer("en", enAnalyzer);
		plaw.addAnalyzer("ru", ruAnalyzer);
		plaw.tokenStream("asdf", new StringReader(""));
		verify();
	}
	
	@Test
	public void testTokenStreamEmptyLanguage() {
		expect(defaultAnalyzer.tokenStream(
				isA(String.class), isA(Reader.class))).andReturn(null);
		replay(defaultAnalyzer, enAnalyzer, ruAnalyzer);
		PerLanguageAnalyzerWrapper plaw = 
				new PerLanguageAnalyzerWrapper(defaultAnalyzer);
		plaw.addAnalyzer("en", enAnalyzer);
		plaw.addAnalyzer("ru", ruAnalyzer);
		plaw.tokenStream("", "asdf", new StringReader(""));
		verify();
	}
	
	@Test
	public void testTokenStreamNullLanguage() {
		expect(defaultAnalyzer.tokenStream(
				isA(String.class), isA(Reader.class))).andReturn(null);
		replay(defaultAnalyzer, enAnalyzer, ruAnalyzer);
		PerLanguageAnalyzerWrapper plaw = 
				new PerLanguageAnalyzerWrapper(defaultAnalyzer);
		plaw.addAnalyzer("en", enAnalyzer);
		plaw.addAnalyzer("ru", ruAnalyzer);
		plaw.tokenStream(null, "asdf", new StringReader(""));
		verify();
	}
	
	@Test
	public void testTokenStreamSomeLanguage() {
		expect(enAnalyzer.tokenStream(
				isA(String.class), isA(Reader.class))).andReturn(null);
		replay(defaultAnalyzer, enAnalyzer, ruAnalyzer);
		PerLanguageAnalyzerWrapper plaw = 
				new PerLanguageAnalyzerWrapper(defaultAnalyzer);
		plaw.addAnalyzer("en", enAnalyzer);
		plaw.addAnalyzer("ru", ruAnalyzer);
		plaw.tokenStream("en", "asdf", new StringReader(""));
		verify();
	}
	
}
