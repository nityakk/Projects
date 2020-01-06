package datastructures.concrete.dictionaries;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import datastructures.concrete.KVPair;
import datastructures.interfaces.IDictionary;
import misc.exceptions.NoSuchKeyException;

public class ArrayDictionary<K, V> implements IDictionary<K, V> {
    private Pair<K, V>[] pairs;

    private int size;

    public ArrayDictionary() {
        this.pairs = makeArrayOfPairs(10);
        this.size = 0;
    }

    /**
     * This method will return a new, empty array of the given size
     * that can contain Pair<K, V> objects.
     *
     * Note that each element in the array will initially be null.
     */
    @SuppressWarnings("unchecked")
    private Pair<K, V>[] makeArrayOfPairs(int arraySize) {
        return (Pair<K, V>[]) (new Pair[arraySize]);

    }

    @Override
    /**
     * @author Catherine Yoo
     * @param K key
     * 
     * @throws NoSuchKeyException if the dictionary does not contain the given key. 
     *
     * Returns the value associated with the given key.
     */
    public V get(K key) {
        int index = getIndex(key);
        if (index < 0) { 
            throw new NoSuchKeyException();
        } 
        return pairs[index].value;
    }

    @Override    
    /**
     * @author Catherine Yoo
     * @param K key
     * @param V value
     * 
     * If the key does not already exist in the dictionary, 
     * creates a new key-value pair and inserts in the end of the dictionary. 
     * If the key exists, replace its current value with the given one. 
     */
    public void put(K key, V value) {
        int index = getIndex(key);
        if (index < 0) { 
            ensureCapacity(size + 1);
            Pair<K, V> pair = new Pair<K, V>(key, value);
            pairs[this.size] = pair;
            this.size++;
         } else { 
             pairs[index].value = value; 
         }
    }
    
    /**
     * @author Catherine Yoo
     * @param int cap
     * 
     * Checks the capacity of the dictionary.
     * If the dictionary is full and a new key is inserted,
     * create a new dictionary double the size and copy over
     * the current dictionary. 
     * */
    private void ensureCapacity(int cap) {
        if (cap > pairs.length) {
            int newCap = pairs.length * 2 + 1;
            if (cap > newCap) {
                newCap = cap;
            }
            pairs = Arrays.copyOf(pairs, newCap);
        }
    }
    
    /**
     * @author Catherine Yoo
     * @param K key
     * 
     * Returns the index of the given key. 
     * If the key does not exist in the dictionary,
     * return -1. 
     * */
    private int getIndex(K key) {
        for (int i = 0; i < this.size; i++) {
            if (key == null) {
                if (pairs[i].key == null) {
                    return i;
                }
            } else {
                if (pairs[i].key != null) {
                    if (key.equals(pairs[i].key)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    @Override
    /**
     * @author Catherine Yoo
     * @param K key 
     * 
     * @throws NoSuchKeyException if the given key does not exist in the dictionary. 
     * 
     * Removes the given key's key-value pair from the dictionary. 
     * Then returns the value of the removed key-value pair. 
     */
    public V remove(K key) {
        int index = getIndex(key);
        if (index < 0) {
            throw new NoSuchKeyException();
        }        
        V val = pairs[index].value;
        pairs[index] = pairs[size - 1];
        pairs[size - 1] = null;
        this.size--;
        return val;
    }

    @Override
    /**
     * @author Catherine Yoo
     * @param K key 
     * 
     * Returns 'true' if the dictionary contains the given key and 'false' otherwise.
     */
    public boolean containsKey(K key) {
        return getIndex(key) >= 0;
    }

    @Override
    /**
     * @author Catherine Yoo
     * 
     * Returns the total number of key-value pairs in this dictionary.
     */
    public int size() {
        return this.size;
    }
    
    /**
     * @author Catherine Yoo
     * 
     */
    @Override
    public Iterator<KVPair<K, V>> iterator() {
        return new ArrayDictionaryIterator<>(this.pairs);
    }

    private static class Pair<K, V> {
        public K key;
        public V value;

        // You may add constructors and methods to this class as necessary.
        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return this.key + "=" + this.value;
        }
    }
    
    private static class ArrayDictionaryIterator<K, V> implements Iterator<KVPair<K, V>> {
        private Pair<K, V>[] pairs;
        private int position;

        public ArrayDictionaryIterator(Pair<K, V>[] pairs) {
            this.pairs = pairs;
            position = 0;
        }

        @Override
        /**
         * @author Catherine Yoo
         * 
         * Returns 'true' if the iterator still has elements to look at;
         * returns 'false' otherwise.
         */
        public boolean hasNext() {
            return (this.position < this.pairs.length) && (pairs[position] != null);
        }

        @Override
        /**
         * @author Catherine Yoo
         * 
         * Returns the next item in the iteration and internally updates the
         * iterator to advance one element forward. Also converts Pair object
         * to KVPair object. 
         *
         * @throws NoSuchElementException if we have reached the end of the iteration and
         *         there are no more elements to look at.
         */
        public KVPair<K, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            
            Pair<K, V> resultPair = pairs[position++];
            KVPair<K, V> result = new KVPair<K, V>(resultPair.key, resultPair.value);
            return result;
        }
    }

}
