package au.com.miskinhill.search.analysis;

import java.util.HashMap;
import java.util.Map;

// TODO move this into its own/a common utilities module?
public class Trie<T> {
	
	private TrieNode root;

	/**
	 * Creates a new trie with the given value associated with the empty string
	 * (i.e. it will be returned by default, if no longer matching prefix is
	 * found).
	 */
	public Trie(T rootValue) {
		root = new TrieNode(rootValue);
	}
	
	/**
	 * Associates the given value with the given key.
	 */
	public void put(CharSequence key, T value) {
		if (value == null)
			throw new IllegalArgumentException("null values cannot be stored");
		int i = 0;
		TrieNode curr = root;
		while (i < key.length()) {
			TrieNode child = curr.children.get(key.charAt(i));
			if (child == null) {
				TrieNode new_ = new TrieNode(null);
				curr.children.put(key.charAt(i), new_);
				curr = new_;
			} else {
				curr = child; 
			}
			i ++;
		}
		curr.value = value;			
	}

	/**
	 * Returns the value associated with the longest prefix match for the given
	 * key.
	 */
	public T get(CharSequence key) {
		int i = 0;
		TrieNode curr = root;
		T retval = root.value;
		while (i < key.length()) {
			curr = curr.children.get(key.charAt(i));
			if (curr == null) {
				return retval;
			} else if (curr.value != null) {
				retval = curr.value;
			}
			i ++;
		}
		return retval;
	}
	
	private class TrieNode {
		public T value;
		public Map<Character, TrieNode> children = new HashMap<Character, TrieNode>();
		public TrieNode(T value) {
			this.value = value;
		}
	}

}
