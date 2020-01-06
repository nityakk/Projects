package datastructures.concrete;

import datastructures.interfaces.IList;
import misc.exceptions.EmptyContainerException;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Note: For more info on the expected behavior of your methods, see
 * the source code for IList.
 */
public class DoubleLinkedList<T> implements IList<T> {
    private Node<T> front;
    private Node<T> back;
    private int size;

    public DoubleLinkedList() {
        this.front = null;
        this.back = null;
        this.size = 0;
    }

    @Override
    /**
     * @author Nitya Krishna Kumar
     * @param T item
     * Adds the given item to the end of the list
     */
    public void add(T item) {
        if (this.front == null) {
            this.front = new Node<T>(item);
            this.back = front;
        } else {
            Node<T> curr = this.back;
            this.back.next = new Node<T>(item);
            this.back = back.next;
            this.back.prev = curr;
        }
        size++;
    }

    @Override
    /**
     * @author Nitya Krishna Kumar
     * @return the data stored in the removed node
     * @throws EmptyContainerException if the list is empty and there are no 
     * elements left to remove.
     * Removes the node found at the end of the list
     */
    public T remove() {
        if (this.front == null) {
            throw new EmptyContainerException();
        }
        T removed = back.data;
        if (this.back.prev == null) {
            this.back = null;
            this.front = null;
        } else {
            this.back = back.prev;
            this.back.next = null;
        }
        this.size--;
        return removed;
    }

    @Override
    /**
     * @author Nitya Krishna Kumar
     * @param int index
     * @return the value stored at that index
     * @throws IndexOutOfBoundsException if the index < 0 or index >= this.size()
     * Returns the item located at the given index.
     */
    public T get(int index) {
        if (index < 0 || index >= this.size) {
            throw new IndexOutOfBoundsException();
        }
        Node<T> curr = findNode(index);
        return curr.data;
    }

    @Override
    /**
     * @author Nitya Krishna Kumar
     * @param int index, T item
     * @throws IndexOutOfBoundsException if the index < 0 or index >= this.size()
     * Changes the element found at the given index with the new item.
     */
    public void set(int index, T item) {
        if (index < 0 || index >= this.size) {
            throw new IndexOutOfBoundsException();
        }
        
        if (index == 0) {
            Node<T> curr = this.front;
            this.front = new Node<T>(item, this.front.next);
            if (curr.next == null) {
                this.back = this.front;
            } else {
                curr = curr.next;
                curr.prev = this.front;
            }
        } else if (index == this.size - 1) {
            Node<T> curr = this.back;
            this.back = new Node<T>(this.back.prev, item, null);
            curr = curr.prev;
            curr.next = this.back;
        } else {
            Node<T> curr = findNode(index);
            Node<T> newNode = new Node<T>(curr.prev, item, curr.next);
            curr.prev.next = newNode;
            curr.next.prev = newNode;
        }
    }

    @Override
    /**
     * @author Nitya Krishna Kumar
     * @param int index, T item
     * @throws IndexOutOfBoundsException if index < 0 or index >= size of list + 1
     * Adds the given item to the list at the given index and moves all subsequent
     * elements located at that index and on, one index over
     */
    public void insert(int index, T item) {
        if (index < 0 || index >= this.size + 1) {
            throw new IndexOutOfBoundsException();
        }
        
        if (index == 0) {
            this.front = new Node<T>(item, this.front);
            if (this.size == 0) {
                this.back = this.front;
            }
        } else if (index == this.size) {
            this.back.next = new Node<T>(this.back, item, null);
            this.back = this.back.next;
        } else {
            Node<T> curr = findNode(index-1);
            Node<T> insertNode = new Node<T>(curr, item, curr.next); 
            curr.next = insertNode;
            insertNode.next.prev = insertNode;
        }
        
        this.size++;
    }
    
    /**
     * @author Nitya Krishna Kumar
     * @param int index
     * @return Node<T> curr
     * Finds and returns the node located at the given index 
     */
    private Node<T> findNode(int index) {
        Node<T> curr;
        if (index < this.size/2) {
            int count = 0;
            curr = this.front;
            while (count!=index) {
                curr = curr.next;
                count++;
            }
        } else {
            int count = this.size - 1;
            curr = this.back;
            while (count!=index) {
                curr = curr.prev;
                count--;
            }
        }
        
        return curr;
    }

    @Override
    /**
     * @author Nitya Krishna Kumar
     * @throws IndexOutOfBoundsException if index < 0 or index > size of list
     * @throws EmptyContainerException if index = size of list
     * Deletes the item at the given index and moves the subsequent elements
     * up one index
     */
    public T delete(int index) {
        if (index < 0 || index > this.size) {
            throw new IndexOutOfBoundsException();
        }
        if (index == 0 && this.size == 0) {
            throw new EmptyContainerException();
        }
        
        T deleted;
        if (index == 0) {
            deleted = this.front.data;
            this.front = this.front.next;
        } else if (index == this.size - 1) {
            deleted = this.back.data;
            this.back = this.back.prev;
            this.back.next = null;
        } else {
            Node<T> curr = findNode(index);
            deleted = curr.data;
            curr.prev.next = curr.next;
            curr.next.prev = curr.prev;
        }
        
        size--;
        return deleted;
    }

    @Override
    /**
     * @author Nitya Krishna Kumar
     * @param T item
     * @return the index at which the item is located 
     * Returns the index of the first occurrence of the given item.
     * Returns -1 if the item is not found in the list.
     */
    public int indexOf(T item) {
        int index = 0;
        Node<T> curr = this.front;
        while ((curr.data != null || item != null) && !curr.data.equals(item)) {
            if (index >= this.size - 1) {
                return -1;
            }
            curr = curr.next;
            index++;
        }
        
        return index;
    }

    @Override
    /**
     * @author Nitya Krishna Kumar
     * @return int size
     * Returns the number of elements in the container.
     */
    public int size() {
        return this.size;
    }

    @Override
    /**
     * @author Nitya Krishna Kumar
     * @param T other
     * @return 'true' if given element "other" is found in the list,
     *         'false' otherwise
     */
    public boolean contains(T other) {
        Node<T> curr = this.front;
        for (int i = 0; i < this.size; i++) {
            if (curr.data == null) {
                if (other == null) {
                    return true;  
                }
            } else {
                if (other != null) { 
                    if (curr.data.equals(other)) {
                        return true;
                    }
                }
            }
            curr = curr.next;  
            if (curr == null) {
                return false;
            }
        }
        
        return false; 
    }

    @Override
    public Iterator<T> iterator() {
        return new DoubleLinkedListIterator<>(this.front);
    }

    private static class Node<E> {
        public final E data;
        public Node<E> prev;
        public Node<E> next;

        public Node(Node<E> prev, E data, Node<E> next) {
            this.data = data;
            this.prev = prev;
            this.next = next;
        }

        public Node(E data) {
            this(null, data, null);
        }
        
        public Node(E data, Node<E> next) {
            this(null, data, next);
        }
    }

    private static class DoubleLinkedListIterator<T> implements Iterator<T> {
        private Node<T> current;

        public DoubleLinkedListIterator(Node<T> current) {
            this.current = current;
        }

        /**
         * @author Catherine Yoo
         * 
         * Returns 'true' if the iterator still has elements to look at;
         * returns 'false' otherwise.
         */
        public boolean hasNext() {
            return (this.current != null);
        }

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
            T item = this.current.data;
            this.current = this.current.next;
            return item;
        }
    }
}
