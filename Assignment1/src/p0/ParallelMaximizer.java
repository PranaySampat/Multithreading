package p0;

//import java.util.LinkedList;
import java.util.*;

/**
 * This class runs <code>numThreads</code> instances of
 * <code>ParallelMaximizerWorker</code> in parallel to find the maximum
 * <code>Integer</code> in a <code>LinkedList</code>.
 */
public class ParallelMaximizer {
	
	int numThreads;
	ArrayList<ParallelMaximizerWorker> workers; // = new ArrayList<ParallelMaximizerWorker>(numThreads);

	public ParallelMaximizer(int numThreads) {
		workers = new ArrayList<ParallelMaximizerWorker>(numThreads);
                this.numThreads = numThreads;
	}


	
	public static void main(String[] args) {
		int numThreads = 4; // number of threads for the maximizer
		int numElements = 10; // number of integers in the list
		
		ParallelMaximizer maximizer = new ParallelMaximizer(numThreads);
		LinkedList<Integer> list = new LinkedList<Integer>();
		Random r = new Random();
		// populate the list
		// TODO: change this implementation to test accordingly
		for (int i=0; i<numElements; i++) {
                    int rand = r.nextInt();
                    System.out.println(rand);
			list.add(rand);
                }
		// run the maximizer
		try {
			System.out.println(maximizer.max(list));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Finds the maximum by using <code>numThreads</code> instances of
	 * <code>ParallelMaximizerWorker</code> to find partial maximums and then
	 * combining the results.
	 * @param list <code>LinkedList</code> containing <code>Integers</code>
	 * @return Maximum element in the <code>LinkedList</code>
	 * @throws InterruptedException
	 */
	public int max(LinkedList<Integer> list) throws InterruptedException {
		int max = Integer.MIN_VALUE; // initialize max as lowest value
		
		System.out.println(workers.size());
		// run numThreads instances of ParallelMaximizerWorker
		for (int i=0; i < this.numThreads; i++) {
			workers.add(new ParallelMaximizerWorker(list));
			workers.get(i).run();
		}
		// wait for threads to finish
		for (int i=0; i<this.numThreads; i++)
			workers.get(i).join();
		
		// take the highest of the partial maximums
		// TODO: IMPLEMENT CODE HERE
                for(int i=0;i<this.numThreads;i++){
                    int num = workers.get(i).getPartialMax();
                    if(num > max){
                        max = num;
                    }
                }
		
		return max;
	}
	
}
