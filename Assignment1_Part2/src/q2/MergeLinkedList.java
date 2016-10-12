/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package q2;

import java.util.LinkedList;

/**
 *
 * @author pranaysampat
 */
public class MergeLinkedList extends Thread {
    protected LinkedList<Integer> linkedListA;
    protected LinkedList<Integer> linkedListB;
    public LinkedList<Integer> mergeList;
    
    public MergeLinkedList(LinkedList<Integer> list1, LinkedList<Integer> list2){
        this.linkedListA = list1;
        this.linkedListB = list2;
        this.mergeList = new LinkedList<>();
    }
    public void run(){
        mergeList(linkedListA,linkedListB);
    }
    public LinkedList<Integer> getLinkedList(){
        return mergeList;
    }
    public void mergeList(LinkedList<Integer> l1, LinkedList<Integer> l2){
        if(l1.isEmpty() && l2.isEmpty()){
            return;
        }
        if(l1.isEmpty()){
             mergeList.addAll(l2);
             return;
        }
        if(l2.isEmpty()){
             mergeList.addAll(l1);
             return;
        }
        if(l1.peekFirst() < l2.peekFirst()){
            mergeList.add(l1.removeFirst());
            this.mergeList(l1,l2);
        }else{
            mergeList.add(l2.removeFirst());
            this.mergeList(l1,l2);
        }
    }
}
