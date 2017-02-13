package p3;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;



public class StudentMTMazeSolver extends SkippingMazeSolver
{

	public ExecutorService pool;

	public StudentMTMazeSolver(Maze maze)
	{
		super(maze);
	}

	public List<Direction> solve() 
	{
		// TODO: Implement your code here
		LinkedList<DFSSolver> taskList = new LinkedList<DFSSolver>();
		List<Future<List<Direction>>> directionChoiceList = new LinkedList<Future<List<Direction>>>();
		List<Direction> resultList = null;
		int num = Runtime.getRuntime().availableProcessors();
		pool = Executors.newFixedThreadPool(num);
		try{
			Choice start = firstChoice(maze.getStart());
			
			int size = start.choices.size();
			for(int index = 0; index < size; index++){
				Choice currChoice = follow(start.at, start.choices.peek());
				
				taskList.add(new DFSSolver(currChoice, start.choices.pop()));
				
			}
		}catch (SolutionFound e){
			System.out.println("caught");
		}
		try {
			directionChoiceList = pool.invokeAll(taskList);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		pool.shutdown();
		for(Future<List<Direction>> ans : directionChoiceList){
			try {
				
				if(ans.get() != null){
					resultList = ans.get();
					
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return resultList;
	}

	private class DFSSolver implements Callable<List<Direction>>{
		Choice head;
		Direction choiceDir;
		public DFSSolver(Choice head, Direction choiceDir){
			this.head = head;
			this.choiceDir = choiceDir;
			
		}

		@Override
		public List<Direction> call() {
			// TODO Auto-generated method stub
			LinkedList<Choice> choiceStack = new LinkedList<Choice>();
			Choice currChoice;

			try{
				choiceStack.push(this.head);
				
				while(!choiceStack.isEmpty()){
					currChoice = choiceStack.peek();

					if(currChoice.isDeadend()){
						//backtrack
						choiceStack.pop();
						if (!choiceStack.isEmpty()) choiceStack.peek().choices.pop();
						continue;
					}
					choiceStack.push(follow(currChoice.at, currChoice.choices.peek()));
				}
				return null;
			}catch (SolutionFound e){
				Iterator<Choice> iter = choiceStack.iterator();
	            LinkedList<Direction> solutionPath = new LinkedList<Direction>();
	        
	           
	            while (iter.hasNext())
	            {
	            	currChoice = iter.next();
	                solutionPath.push(currChoice.choices.peek());
	            }
	            solutionPath.push(choiceDir);
	            if (maze.display != null) maze.display.updateDisplay();
	            
	            return pathToFullPath(solutionPath);
			}

		}

	}
}