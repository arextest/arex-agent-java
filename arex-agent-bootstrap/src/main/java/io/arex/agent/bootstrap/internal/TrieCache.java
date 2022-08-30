package io.arex.agent.bootstrap.internal;

public class TrieCache<T> implements Cache<String, T> {

    private TrieNode<T> root;

    public TrieCache() {
        root = new TrieNode();
    }

    public TrieCache(String init) {
        root = new TrieNode(1);

        TrieNode<T> current = root;
        TrieNode<T> node;
        for (int i = 0; i < init.length(); i++) {
            node = current.getChild(init.charAt(i));
            if (node == null) {
                node = new TrieNode<>(1);
                current.addChild(init.charAt(i), node);
            }
            current = node;
        }
    }

    public void put(String key, T value) {
        if (key == null || key.length() == 0) {
            return;
        }

        TrieNode<T> current = root;
        TrieNode<T> node;
        for (int i = 0; i < key.length(); i++) {
            node = current.getChild(key.charAt(i));
            if (node == null) {
                node = new TrieNode<>();
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

    @Override
    public boolean contains(String key) {
        return get(key) != null;
    }

    public void clear() {
        root = new TrieNode<T>();
    }

    static class TrieNode<T> {
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

        public TrieNode(int capacity) {
            this.childes = new Char2TrieNodeMap(capacity);
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

    static class Char2TrieNodeMap {

        static final char[] EMPTY_CHAR_ARRAY = new char[0];
        static final TrieNode[] EMPTY_OBJECT_ARRAY = new TrieNode[0];

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
}
