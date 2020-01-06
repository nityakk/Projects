package datastructures.concrete;

import datastructures.concrete.dictionaries.ChainedHashDictionary;
import datastructures.interfaces.IDictionary;
import datastructures.interfaces.ISet;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ChainedHashSet<T> implements ISet<T> {
    // This should be the only field you need
    private IDictionary<T, Boolean> map;

    public ChainedHashSet() {
        // No need to change this method
        this.map = new ChainedHashDictionary<>();
    }

    /**
     * @author Catherine Yoo
     * 
     * If the given item does not already exist in the set, 
     * adds it to the set. If not, does nothing. 
     */
    @Override
    public void add(T item) {
        if (!this.map.containsKey(item)) {
            this.map.put(item, true);
        }
    }

    /**
     * @author Catherine Yoo
     * 
     * Removes the given item from the set. 
     * 
     * @throws NoSuchElementException if the set does not contain the given item
     */
    @Override
    public void remove(T item) {
        if (!this.map.containsKey(item)) {
            throw new NoSuchElementException();
        }
        this.map.remove(item);
    }

    /**
     * @author Catherine Yoo
     * 
     * Returns 'true' if the set contains the given item and false otherwise.
     */
    @Override
    public boolean contains(T item) {
        return this.map.containsKey(item);
    }

    /**
     * @author Catherine Yoo
     * 
     * Returns the total number of items in the set.
     */
    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public Iterator<T> iterator() {
        return new SetIterator<>(this.map.iterator());
    }

    private static class SetIterator<T> implements Iterator<T> {
        // This should be the only field you need
        private Iterator<KVPair<T, Boolean>> iter;

        public SetIterator(Iterator<KVPair<T, Boolean>> iter) {
            // No need to change this method.
            this.iter = iter;
        }

        @Override
        /**
         * @author Catherine Yoo
         * 
         * Returns 'true' if the iterator still has elements to look at;
         * returns 'false' otherwise.
         */
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        /**
         * @author Catherine Yoo
         * 
         * Returns the next item in the iteration and internally updates the
         * iterator to advance one element forward.
         *
         * @throws NoSuchElementException if we have reached the end of the iteration and
         *         there are no more elements to look at.
         */
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            KVPair<T, Boolean> item = iter.next();
            T result = item.getKey();
            return result;
        }
    }
}
