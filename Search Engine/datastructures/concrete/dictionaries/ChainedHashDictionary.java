package datastructures.concrete.dictionaries;

import datastructures.concrete.KVPair;
import datastructures.interfaces.IDictionary;
import misc.exceptions.NoSuchKeyException;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * See the spec and IDictionary for more details on what each method should do
 */
public class ChainedHashDictionary<K, V> implements IDictionary<K, V> {
    // You may not change or rename this field: we will be inspecting
    // it using our private tests.
    private IDictionary<K, V>[] chains;

    private int tableSize;
    private int elementSize;
    
    public ChainedHashDictionary() {
        this.tableSize = 10;
        this.chains = makeArrayOfChains(this.tableSize);
        this.elementSize = 0;
    }

    /**
     * This method will return a new, empty array of the given size
     * that can contain IDictionary<K, V> objects.
     *
     * Note that each element in the array will initially be null.
     */
    @SuppressWarnings("unchecked")
    private IDictionary<K, V>[] makeArrayOfChains(int size) {
        // Note: You do not need to modify this method.
        // See ArrayDictionary's makeArrayOfPairs(...) method for
        // more background on why we need this method.
        return (IDictionary<K, V>[]) new IDictionary[size];
    }
    
    private int getHashValue(K key) {
        if (key == null) {
            return 0;
        }
        int givenHashCode = key.hashCode();
        return Math.abs(givenHashCode % this.tableSize);
    }

    @Override
    /**
     * Returns the value corresponding to the given key.
     *
     * @throws NoSuchKeyException if the dictionary does not contain the given key.
     */
    public V get(K key) {
        int hash = getHashValue(key);
        
        if (!containsKey(key)) {
            throw new NoSuchKeyException();
        }
        
        IDictionary<K, V> bucket = this.chains[hash];
        return bucket.get(key);

    }

    @Override
    /**
     * Adds the key-value pair to the dictionary. If the key already exists in the dictionary,
     * replace its value with the given one.
     */
    public void put(K key, V value) {
        int hash = getHashValue(key);
        
        if (this.chains[hash] == null) {
            this.chains[hash] = new ArrayDictionary<K, V>();
        }
        IDictionary<K, V> bucket = this.chains[hash];
        int sizeInitial = bucket.size(); //new

        bucket.put(key, value);
        if (sizeInitial != bucket.size()) {
            this.elementSize++;
        }
        int loadFactor = this.elementSize / this.tableSize;
        if (loadFactor > 1) {
            this.chains = resize();
        }
    }
    
    private IDictionary<K, V>[] resize() {
        this.tableSize = this.tableSize * 2;
        IDictionary<K, V>[] newChains = new IDictionary[this.tableSize];
        
        for (int i = 0; i < this.chains.length; i++) {
            while (i < this.chains.length && this.chains[i] == null) {
                i++;
            }
            if (i < this.chains.length) {
                IDictionary<K, V> bucket = this.chains[i];
                for (KVPair<K, V> pair: bucket) {
                    K key = pair.getKey();
                    V value = pair.getValue();
                    int newHash = getHashValue(key);
                    
                    if (newChains[newHash] == null) {
                        newChains[newHash] = new ArrayDictionary<K, V>();
                    }
                    newChains[newHash].put(key, value);
                }
            }
        }
        return newChains;
    }

    @Override
    /**
     * Remove the key-value pair corresponding to the given key from the dictionary.
     *
     * @throws NoSuchKeyException if the dictionary does not contain the given key.
     */
    public V remove(K key) {
        int hash = getHashValue(key);
        
        if (!containsKey(key)) {
            throw new NoSuchKeyException();
        }
        
        IDictionary<K, V> bucket = this.chains[hash];
        V removedValue = bucket.remove(key);
        this.elementSize--;
        return removedValue;
    }

    @Override
    /**
     * Returns 'true' if the dictionary contains the given key and 'false' otherwise.
     */
    public boolean containsKey(K key) {
        int hash = getHashValue(key);
        IDictionary<K, V> bucket = this.chains[hash];
        return bucket != null && bucket.containsKey(key);
    }

    @Override
    /**
     * Returns the number of elements in the hash dictionary 
     */
    public int size() {
        return this.elementSize;
    }

    @Override
    public Iterator<KVPair<K, V>> iterator() {
        // Note: you do not need to change this method
        return new ChainedIterator<>(this.chains);
    }

    /**
     * Hints:
     *
     * 1. You should add extra fields to keep track of your iteration
     *    state. You can add as many fields as you want. If it helps,
     *    our reference implementation uses three (including the one we
     *    gave you).
     *
     * 2. Before you try and write code, try designing an algorithm
     *    using pencil and paper and run through a few examples by hand.
     *
     *    We STRONGLY recommend you spend some time doing this before
     *    coding. Getting the invariants correct can be tricky, and
     *    running through your proposed algorithm using pencil and
     *    paper is a good way of helping you iron them out.
     *
     * 3. Think about what exactly your *invariants* are. As a
     *    reminder, an *invariant* is something that must *always* be 
     *    true once the constructor is done setting up the class AND 
     *    must *always* be true both before and after you call any 
     *    method in your class.
     *
     *    Once you've decided, write them down in a comment somewhere to
     *    help you remember.
     *
     *    You may also find it useful to write a helper method that checks
     *    your invariants and throws an exception if they're violated.
     *    You can then call this helper method at the start and end of each
     *    method if you're running into issues while debugging.
     *
     *    (Be sure to delete this method once your iterator is fully working.)
     *
     * Implementation restrictions:
     *
     * 1. You **MAY NOT** create any new data structures. Iterators
     *    are meant to be lightweight and so should not be copying
     *    the data contained in your dictionary to some other data
     *    structure.
     *
     * 2. You **MAY** call the `.iterator()` method on each IDictionary
     *    instance inside your 'chains' array, however.
     */
    private static class ChainedIterator<K, V> implements Iterator<KVPair<K, V>> {
        private IDictionary<K, V>[] chains;
        private int position;
        private Iterator<KVPair<K, V>> iter;
        private IDictionary<K, V> resultBucket;
        
        public ChainedIterator(IDictionary<K, V>[] chains) {
            this.chains = chains;
            this.position = 0;
            
            while (this.position + 1 < this.chains.length && this.chains[this.position] == null) {
                this.position++;
            }
            if (this.position + 1 <= this.chains.length) {
                this.resultBucket = this.chains[this.position];
                if (resultBucket != null) {
                    this.iter = resultBucket.iterator(); 
                }
            }  
        }

        @Override
        public boolean hasNext() {
            if (this.chains.length > 0 && this.iter != null) {
                while (this.position < this.chains.length) {
                    if (this.iter.hasNext()) {
                        return this.iter.hasNext();
                    } else {
                        this.position++;
                        if (this.position < this.chains.length) {
                            this.resultBucket = this.chains[this.position];
                            if (resultBucket != null) {
                                this.iter = resultBucket.iterator(); 
                            }
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public KVPair<K, V> next() { 
            if (!hasNext()) {
                throw new NoSuchElementException();
            } else {
                return this.iter.next();
            }
        }
    }
}
