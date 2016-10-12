package q2;

//import java.util.LinkedList;
import java.util.*;

/**
 * This class runs <code>numThreads</code> instances of
 * <code>ParallelMaximizerWorker</code> in parallel to find the maximum
 * <code>Integer</code> in a <code>LinkedList</code>.
 */
public class ParallelMaximizer {
	
	int numThreads;
        int listSize;
        
	ArrayList<ParallelMaximizerWorker> workers; // = new ArrayList<ParallelMaximizerWorker>(numThreads);
        ArrayList<MergeLinkedList> mergeLinkedLists;
	public ParallelMaximizer(int numThreads, int size) {
		workers = new ArrayList<ParallelMaximizerWorker>(numThreads);
                this.listSize = size;
                this.numThreads = numThreads;
	}


	
	public static void main(String[] args) throws InterruptedException {
		int numThreads = 4; // number of threads for the maximizer
		int numElements = 10; // number of integers in the list
		
		ParallelMaximizer maximizer = new ParallelMaximizer(numThreads,numElements);
                HashMap<String,LinkedList<Integer>> map = maximizer.generateSortedList();
                System.out.println("Unsorted List");
                
                System.out.println(map.get("unsorted")); 
                System.out.println("Merged List");
                
                System.out.println(map.get("sorted")); 
		
		
	}
	
	/**
	 * Finds the maximum by using <code>numThreads</code> instances of
	 * <code>ParallelMaximizerWorker</code> to find partial maximums and then
	 * combining the results.
	 * @param list <code>LinkedList</code> containing <code>Integers</code>
	 * @return Maximum element in the <code>LinkedList</code>
	 * @throws InterruptedException
	 */
	public HashMap<String,LinkedList<Integer>> generateSortedList() throws InterruptedException{
            LinkedList<Integer> unsortedList = new LinkedList<>();
            for(int i = 0;i<numThreads;i++){
                workers.add(new ParallelMaximizerWorker(listSize));
                workers.get(i).start();
            }
            for(int i=0;i<numThreads;i++){
                workers.get(i).join();
            }
            Stack<LinkedList<Integer>> linkedListStack = new Stack();
            for(int i=0;i<numThreads;i++){
                
                linkedListStack.add(workers.get(i).getList());
                unsortedList.addAll(workers.get(i).getUnsortedList());
            }/*
            
            while(linkedListStack.size() != 0){
                System.out.println("Sorted List before merging");
                System.out.println(linkedListStack.pop());
            }*/
            while(linkedListStack.size() >1){
               mergeLinkedLists = new ArrayList<>();
               int numberofThreads =linkedListStack.size()/2;
               for(int i =0 ;i<numberofThreads;i++){
                   mergeLinkedLists.add(new MergeLinkedList((LinkedList<Integer>)linkedListStack.pop(),(LinkedList<Integer>) linkedListStack.pop()));
                   mergeLinkedLists.get(i).start();
               }
               for(int i=0;i<numberofThreads;i++){
                   mergeLinkedLists.get(i).join();
                   linkedListStack.push(mergeLinkedLists.get(i).getLinkedList());
               }
            }HashMap<String,LinkedList<Integer>> map= new HashMap<>();
            while(linkedListStack.size() !=0){
                  map.put("sorted",linkedListStack.pop() );
            }
            map.put("unsorted", unsortedList);
            return map;
            
        }
	
}
