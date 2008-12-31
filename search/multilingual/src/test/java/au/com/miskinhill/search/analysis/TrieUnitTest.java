package au.com.miskinhill.search.analysis;

import static org.junit.Assert.*;

import org.junit.Test;

import au.com.miskinhill.search.analysis.Trie;

public class TrieUnitTest {
	
	@Test
	public void testEmptyTrie() {
		Trie<String> t = new Trie<String>("asdf");
		assertEquals("asdf", t.get(""));
		assertEquals("asdf", t.get("somekey"));
	}
	
	@Test
	public void testNoPrefix() {
		Trie<String> t = new Trie<String>("root");
		t.put("en", "English");
		t.put("de", "Deutsch");
		assertEquals("root", t.get("pl"));
	}
	
	@Test
	public void testPrefixButNoMatch() {
		Trie<String> t = new Trie<String>("root");
		t.put("en", "English");
		t.put("de", "Deutsch");
		assertEquals("root", t.get("es"));
	}
	
	@Test
	public void testPrefixMatch() {
		Trie<String> t = new Trie<String>("root");
		t.put("en", "English");
		t.put("de", "Deutsch");
		assertEquals("English", t.get("en-AU"));
	}
	
	@Test
	public void testExactMatch() {
		Trie<String> t = new Trie<String>("root");
		t.put("en", "English");
		t.put("en-AU", "Australian");
		t.put("de", "Deutsch");
		assertEquals("Australian", t.get("en-AU"));
	}
	
	@Test
	public void testDifferentPrefixMatch() {
		Trie<String> t = new Trie<String>("root");
		t.put("en", "English");
		t.put("en-AU", "Australian");
		t.put("de", "Deutsch");
		assertEquals("English", t.get("en-GB"));
	}

}
