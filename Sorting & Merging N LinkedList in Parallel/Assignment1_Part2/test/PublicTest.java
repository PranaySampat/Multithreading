

import q2.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import java.util.*;


import org.junit.Test;

import q2.ParallelMaximizer;

public class PublicTest {
        HashMap<String,LinkedList<Integer>> map;
	private int	threadCount = 10; // number of threads to run
        private int     sizeList =10;
	private ParallelMaximizer maximizer = new ParallelMaximizer(threadCount,sizeList);
	
	@Test
	public void compareSorting() {
		
		LinkedList<Integer> list = new LinkedList<Integer>();
		
		
		// try to find parallelMax
		try {
			map = maximizer.generateSortedList();
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("The test failed because the max procedure was interrupted unexpectedly.");
		} catch (Exception e) {
			e.printStackTrace();
			fail("The test failed because the max procedure encountered a runtime error: " + e.getMessage());
		}
		LinkedList<Integer> serialSorted = map.get("unsorted");
                Collections.sort(serialSorted);
                assertThat(serialSorted, is(map.get("sorted")));
        }
}
