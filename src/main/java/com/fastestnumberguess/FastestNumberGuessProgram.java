package com.fastestnumberguess;

import java.math.BigInteger;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * This program finds the number between the range 1 to maximum (999999999999999) and prints number of total iterations.
 * Also It prints the total elapsed time took to find it.
 * 
 * The program's work is divided among one to 10000 threads.
 */
public class FastestNumberGuessProgram {
   /**
    * The upper limit of the range of integers that is to be tested.
    */
   private final static BigInteger MAX = new BigInteger("999999999999999");
   
   /**
    * The total number of iterations found to guess the number.  (Note: This is
    * volatile since it is referenced in unsynchronized code in the
    * countIterationsWithThreads() method.)
    */
   private volatile static int totalIterationCount = 0;
   
   
   /**
    * Boolean volatile variable to stop the thread and indicate that thread is now stopped.
    */
   private volatile static boolean isTerminated = false;
   
   
   /**
    * This method is called by a thread when it has completed its iterations,
    * to report the count.  
    * The information is used to update the totalIterationCount.
    * 
    * @param iterationCountPerThread count of iteration
    */
   synchronized private static void report(int iterationCountPerThread) {
	   totalIterationCount += iterationCountPerThread;
   }
   
   /**
    * A thread belonging to this class counts the number of iterations to find the integer in an assigned range of integers.  
    * The range is specified in the constructor.  
    * At the end of its iterations, the thread reports its answer by calling the report() method.
    */
	private static class CountIterationsThread extends Thread {

		BigInteger min, max, number;

		public CountIterationsThread(BigInteger min, BigInteger max, BigInteger numberToGuess) {
			this.min = min;
			this.max = max;
			this.number = numberToGuess;
			//this.startTime = System.currentTimeMillis();
		}

		public void run() {
			
			if (isTerminated) {
				//Already one thread find the number.
				return;
			}
			int iterationCount = 0;
			BigInteger bigI = min;
			while (bigI.compareTo(max) <= 0
					&& number.compareTo(max) <= 0) {

				iterationCount++;
				if (bigI.compareTo(number) == 0) {
					System.out.println("\n"+
							currentThread().getName() + " find the number with " + iterationCount + " iterations.");
					this.terminate();
					break;
				}  else {
					bigI = bigI.add(new BigInteger("1"));
				}
			}
			/*
			 * long elapsedTime = System.currentTimeMillis() - startTime;
			 * System.out.println("Thread " + currentThread().getName() + " used " +
			 * (elapsedTime/1000.0) + " seconds."); //testing the thread execution
			 */
			report(iterationCount);
		}
		
		public void terminate(){
	        isTerminated = true;
	    }
	}
   
   /**
    * Function to find the number between the range 1 to maximum (999999999999999) with counting the iterations, 
    * and dividing the work among a specified number of threads.
    */
	private static void countIterationsWithThreads(BigInteger numberToGuess) {

		int numberOfThreads = 10000;
		System.out.println("\nCounting iteration and time to find " + numberToGuess + " using " + +numberOfThreads
				+ " threads...");

		long startTime = System.currentTimeMillis();
		CountIterationsThread[] worker = new CountIterationsThread[numberOfThreads];
		BigInteger integersPerThread = MAX.divide(BigInteger.valueOf(numberOfThreads));
		BigInteger start = new BigInteger("0");
		BigInteger end = integersPerThread; // End point of the range of integers.

		for (int i = 0; i < numberOfThreads; i++) {
			if (start.compareTo(numberToGuess) == 1) {
				numberOfThreads = i;
				break;
			}
			if (i == numberOfThreads - 1) {
				end = MAX; // Make sure that the last thread's range goes all the way up to MAX
			}
			worker[i] = new CountIterationsThread(start, end, numberToGuess);
			start = end.add(new BigInteger("1")); // Determine the range of ints for the NEXT thread.
			end = start.add(integersPerThread).subtract(new BigInteger("1"));
		}

		totalIterationCount = 0;
		for (int i = 0; i < numberOfThreads; i++)
			worker[i].start();
		for (int i = 0; i < numberOfThreads; i++) {
			// Wait for each worker thread to report the answer and die.
			while (worker[i].isAlive() && !isTerminated) {
				try {
					worker[i].join();
				} catch (InterruptedException e) {
				}
			}
		}
		if (isTerminated) {
			System.out.println("\nThe number of total threads created to find the number is " + numberOfThreads);
			long elapsedTime = System.currentTimeMillis() - startTime;
			System.out.println("\nThe number of iterations to find the number " + " between 0 and " + MAX + " is "
					+ totalIterationCount);
			System.out.println("Total elapsed time:  " + (elapsedTime / 1000.0) + " seconds.\n");
		}
	}
   
   /**
    * Gets the number from user for the computer to guess.
    */
   public static void main(String[] args) {
	  Scanner in = new Scanner(System.in);
      BigInteger numberToGuess = new BigInteger("0");
      boolean isCorrectInput = false;
      
      while (!isCorrectInput) {
    	  System.out.print("Please input a number between 0 and 999999999999999 :  ");
    	  try {
    		  numberToGuess = in.nextBigInteger(10);
    	  } catch (InputMismatchException e) {
    		  System.out.print("invalid input!. Please starts the program again.");
    		  return;
    	  }
    	  int comparevalueByMax = numberToGuess.compareTo(MAX); 
          int comparevalueByMin = new BigInteger("0").compareTo(numberToGuess); 
          if (comparevalueByMin == 1 || comparevalueByMax == 1) {
        	  System.out.print("Please input a number between 0 and 999999999999999! ");
          } else {
        	  isCorrectInput = true;
          }
      }
      countIterationsWithThreads(numberToGuess);
   }
   
}