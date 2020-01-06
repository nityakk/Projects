package datastructures.concrete;

import java.util.Arrays;

import datastructures.interfaces.IPriorityQueue;
import misc.exceptions.EmptyContainerException;

public class ArrayHeap<T extends Comparable<T>> implements IPriorityQueue<T> {
    private static final int NUM_CHILDREN = 4;
    private T[] heap;
    private int heapSize;

    public ArrayHeap() {
        this.heap = makeArrayOfT(8);
        this.heapSize = 0;
    }

    /**
     * This method will return a new, empty array of the given size
     * that can contain elements of type T.
     *
     * Note that each element in the array will initially be null.
     */
    @SuppressWarnings("unchecked")
    private T[] makeArrayOfT(int size) {
        return (T[]) (new Comparable[size]);
    }

    @Override
    /**
     * @author Catherine Yoo
     * 
     * Removes and returns the smallest element in the queue.
     *
     * @throws EmptyContainerException  if the queue is empty
     */
    public T removeMin() {
        if (isEmpty()) {
            throw new EmptyContainerException();
        }
                        
        T min = heap[0];
        heap[0] = heap[this.heapSize - 1]; 
        this.heapSize--;
        percolateDown(0);
        return min;
    }
    
    /**
     * @author Catherine Yoo
     * 
     * Percolates down after removeMin is called
     * to make sure that the parent node is smaller than its 
     * children nodes. If not, swap the parent and the smaller child.
     * Update the index to the smaller child and keep percolating 
     * until min-heap qualities are met. 
     * 
     * */
    private void percolateDown(int index) {
        while (kthChild(index, 0) < this.heapSize) {
            int smallerChild = index;
            
            for (int k = 1; k <= NUM_CHILDREN; k++) {
                if (kthChild(index, k) < this.heapSize 
                        && heap[kthChild(index, k)].compareTo(heap[smallerChild]) < 0) {
                    smallerChild = kthChild(index, k);
                }
            }
            
            if (heap[smallerChild].compareTo(heap[index]) < 0) {
                T temp = heap[index];
                heap[index] = heap[smallerChild];
                heap[smallerChild] = temp;   
                index = smallerChild;
            } else {
                break;
            }
        }  
    }    
 
    @Override
    /**
     * @author Catherine Yoo
     * 
     * Returns, but does not remove, the smallest element in the queue.
     *
     * @throws EmptyContainerException if the queue is empty
    */
    public T peekMin() {
        if (isEmpty()) {
            throw new EmptyContainerException();
        }
        
        return heap[0];
    }

    @Override
    /**
     * @author Catherine Yoo
     * 
     * Inserts the given item into the queue.
     *
     * @throws IllegalArgumentException  if the item is null
    */
    public void insert(T item) {
        if (item == null) {
            throw new IllegalArgumentException();
        }
        
        if (this.heapSize + 1 > heap.length) {
            heap = Arrays.copyOf(heap, heap.length * 2);
        }

        heap[this.heapSize] = item;
        percolateUp(this.heapSize);
        this.heapSize++;
    }

    /**
     * @author Catherine Yoo
     * 
     * After given item is inserted, percolate up so that 
     * the parent node is smaller than its children node. 
     * If not, swap children and parent and keep percolating until
     * min-heap property is satisfied. 
     * 
     * */
    private void percolateUp(int index) {
        while (index > 0 && heap[index].compareTo(heap[parentIndex(index)]) < 0) {
            T temp = heap[parentIndex(index)];
            heap[parentIndex(index)] = heap[index];
            heap[index] = temp;
            index = parentIndex(index);
        }   
    }

    private int kthChild(int index, int k) {
        return index * NUM_CHILDREN + k;
    }

    /**
     * @author Catherine Yoo
     * 
     * Returns the parent index of the given index. 
     * */
    private int parentIndex(int i) {
        return (i - 1) / 4;
    }

    @Override
    /**
     * @author Catherine Yoo 
     * 
     * Returns the number of elements in this queue.
     * 
     */
    public int size() {
        return this.heapSize;
    }
}
