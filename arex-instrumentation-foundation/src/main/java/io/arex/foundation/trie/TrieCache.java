package io.arex.foundation.trie;

import io.arex.foundation.util.StringUtil;

public class TrieCache<T> {
    private final TrieNode<T> root;

    public TrieCache() {
        root = new TrieNode<T>();
    }

    public void add(String key, T value) {
        if (StringUtil.isEmpty(key)) {
            return;
        }

        TrieNode<T> current = root;
        TrieNode<T> node;
        for (int i = 0; i < key.length(); i++) {
            node = current.getChild(key.charAt(i));
            if (node == null) {
                node = new TrieNode<T>();
                current.addChild(key.charAt(i), node);
            }
            current = node;

            if (key.length() - 1 == i) {
                current.setValue(value);
            }
        }
    }

    public T get(String key) {
        TrieNode<T> current = root;
        for (int i = 0; i < key.length(); i++) {
            current = current.getChild(key.charAt(i));
            if (current == null) {
                return null;
            }
        }

        return current != null ? current.getValue() : null;
    }
}
