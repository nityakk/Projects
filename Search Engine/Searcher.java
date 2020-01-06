package misc;

import datastructures.concrete.ArrayHeap;
import datastructures.concrete.DoubleLinkedList;
import datastructures.interfaces.IList;
import datastructures.interfaces.IPriorityQueue;

public class Searcher {
    /**
     * @author Nitya Krishna Kumar
     * This method takes the input list and returns the top k elements
     * in sorted order.
     *
     * So, the first element in the output list should be the "smallest"
     * element; the last element should be the "biggest".
     *
     * If the input list contains fewer then 'k' elements, return
     * a list containing all input.length elements in sorted order.
     *
     * This method must not modify the input list.
     * @throws IllegalArgumentException  if k < 0
     */
    public static <T extends Comparable<T>> IList<T> topKSort(int k, IList<T> input) {
        if (input == null) {
            throw new IllegalArgumentException();
        }
        if (k < 0) {
            throw new IllegalArgumentException();
        }
        int startingSize = input.size();
        
        if (k >= startingSize) {
            k = startingSize;
        }
        
        IPriorityQueue<T> inputHeap = new ArrayHeap<T>();
        IList<T> outputList = new DoubleLinkedList<T>();
        
        if (k > 0) {
            int numNodes = 0;
            for (T value: input) {
                if (numNodes > 0 && numNodes >= k) {
                    T minVal = inputHeap.peekMin();
                    if (numNodes == k) {
                        if (minVal.compareTo(value) < 0) {
                            inputHeap.insert(value);
                            inputHeap.removeMin();
                        }
                    }
                    
                }else {
                    inputHeap.insert(value);
                    numNodes++;
                }
            }
        }
        
        while (inputHeap.size() > 0) {
            T topValue = inputHeap.removeMin();
            outputList.add(topValue);
        }
        
        return outputList;
    }
}
