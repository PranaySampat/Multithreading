package q2;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

/**
 * Given a <code>LinkedList</code>, this class will find the maximum over a
 * subset of its <code>Integers</code>.
 */
public class ParallelMaximizerWorker extends Thread {

    protected LinkedList<Integer> list;
    protected LinkedList<Integer> unsortedList;
    private int size;

    public ParallelMaximizerWorker(int size) {
        this.list = new LinkedList<>();
        this.size = size;

    }

    /**
     * Update <code>partialMax</code> until the list is exhausted.
     */
    public void run() {
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            list.add(rand.nextInt(100));
        }
        unsortedList = list;
        Collections.sort(list);
    }

    public LinkedList<Integer> getList() {
        return list;
    }

    public LinkedList<Integer> getUnsortedList() {
        return unsortedList;
    }

}
