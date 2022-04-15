package io.arex.foundation.trie;

class Char2TrieNodeMap {
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];
    public static final TrieNode[] EMPTY_OBJECT_ARRAY = new TrieNode[0];

    private transient char[] key;
    private transient TrieNode[] value;
    private int size;

    public Char2TrieNodeMap() {
        this(EMPTY_CHAR_ARRAY, EMPTY_OBJECT_ARRAY);
    }

    public Char2TrieNodeMap(int capacity) {
        this.key = new char[capacity];
        this.value = new TrieNode[capacity];
        this.size = 0;
    }

    public Char2TrieNodeMap(char[] key, TrieNode[] value) {
        this.key = key;
        this.value = value;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths");
        }
        this.size = key.length;
    }

    public int size() {
        return size;
    }

    public TrieNode get(char letter) {
        char[] key = this.key;
        int i = this.size;

        do {
            if (i-- == 0) {
                return null;
            }
        } while(key[i] != letter);

        return this.value[i];
    }

    public boolean containsKey(char letter) {
        return get(letter) != null;
    }

    // not thread safe
    public void put(char letter, TrieNode value) {
        ensureCapacity();

        this.key[this.size] = letter;
        this.value[this.size] = value;
        ++this.size;
    }

    private void ensureCapacity() {
        if (this.size == this.key.length) {
            char[] newKey = new char[this.size == 0 ? 7 : this.size * 2];
            TrieNode[] newValue = new TrieNode[this.size == 0 ? 7 : this.size * 2];

            for(int i = this.size; i-- != 0; newValue[i] = this.value[i]) {
                newKey[i] = this.key[i];
            }

            this.key = newKey;
            this.value = newValue;
        }
    }

}
