package au.com.miskinhill.search.tokenizer;

import static org.junit.Assert.assertEquals;

import org.apache.lucene.analysis.Token;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class StringTokenizerUnitTest {

	@Test
	public void testEnglish() throws Exception {
		StringTokenizer t = new StringTokenizer("Hello to the World!", "en");
		Token tok = new Token();
		assertEquals(new Token("hello", 0, 5, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("world", 13, 18, "<ALPHANUM>"), t.next(tok));
		assertEquals(null, t.next(tok));
	}

	@Test
	public void testEnglishWithLocaleThingy() throws Exception {
		StringTokenizer t = new StringTokenizer("Hello to the World!", "en-AU");
		Token tok = new Token();
		assertEquals(new Token("hello", 0, 5, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("world", 13, 18, "<ALPHANUM>"), t.next(tok));
		assertEquals(null, t.next(tok));
	}

	@Test
	public void testRussian() throws Exception {
		StringTokenizer t = new StringTokenizer("Нас и судьбы безвестные ждут", "ru");
		Token tok = new Token();
		assertEquals(new Token("нас", 0, 3, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("судьб", 6, 12, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("безвестн", 13, 23, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("ждут", 24, 28, "<ALPHANUM>"), t.next(tok));
		assertEquals(null, t.next(tok));
	}

	@Test
	public void testNonexistentLanguage() throws Exception {
		StringTokenizer t = new StringTokenizer("Hello to the World!", "xx");
		Token tok = new Token();
		assertEquals(new Token("hello", 0, 5, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("to", 6, 8, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("the", 9, 12, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("world", 13, 18, "<ALPHANUM>"), t.next(tok));
		assertEquals(null, t.next(tok));
	}
	
	@Test
	public void testEmptyLanguage() throws Exception {
		StringTokenizer t = new StringTokenizer("Hello to the World!", "");
		Token tok = new Token();
		assertEquals(new Token("hello", 0, 5, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("to", 6, 8, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("the", 9, 12, "<ALPHANUM>"), t.next(tok));
		assertEquals(new Token("world", 13, 18, "<ALPHANUM>"), t.next(tok));
		assertEquals(null, t.next(tok));
	}
	
}
