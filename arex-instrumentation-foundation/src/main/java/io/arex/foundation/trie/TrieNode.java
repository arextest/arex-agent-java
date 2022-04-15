package io.arex.foundation.trie;

class TrieNode<T> {
    private final Char2TrieNodeMap childes;
    private T value;

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public TrieNode() {
        this.childes = new Char2TrieNodeMap();
    }

    public void addChild(char letter, TrieNode child) {
        childes.put(letter, child);
    }

    public TrieNode<T> getChild(char letter) {
        if (childes == null || childes.size() == 0) {
            return null;
        }

        return childes.get(letter);
    }
}
