package erozic.optimisation.applications;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;

import erozic.optimisation.algorithms.OptimisationAlgorithm.OptimisationAlgorithmListener;
import erozic.optimisation.algorithms.metaheuristics.RAPGA;
import erozic.optimisation.solutions.BinarySolution;
import erozic.optimisation.utils.MaxOnesUtil;

/**
 * A console application for solving the max ones problem using a RAPGA genetic algorithm implementation.
 * 
 * @author Eugen Rožić
 * @version 0.1
 *
 */
public class RAPGA_maxones_solver {

	/**
	 * Starts an instance of the RAPGA algorithm for solving the "max ones"
	 * problem.
	 * 
	 * @param args TODO
	 */
	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println(usage());
			System.exit(-1);
		}
		
		// TODO check for other console arguments...

		int numBits = 0;

		try {
			numBits = Integer.parseInt(args[0]);
			if (numBits < 1)
				throw new NumberFormatException("The number of bits 'n' has to be a positive integer!");
		} catch (NumberFormatException e) {
			System.out.println(usage());
			System.err.println("ERROR: Number Format Exception " + e.getMessage());
			System.exit(-1);
		} catch (Exception e) {
			System.out.println(usage());
			System.err.println("ERROR: Unexpected exception type: " + e.getClass().getName() + ": " + e.getMessage());
			System.exit(-1);
		}
		
		final MaxOnesUtil util = new MaxOnesUtil(numBits);

		RAPGA algorithm = new RAPGA(util, numBits, 2, 100*numBits, 0, 0.03);
		
		algorithm.addAlgorithmListener(new OptimisationAlgorithmListener<BinarySolution>() {
			@Override
			public void stateChanged(Collection<BinarySolution> state, int currentStep) {
				
				System.out.println(currentStep + ". step; active population size = " + state.size() + ", compFactor = "
						+ algorithm.getCompFactor());
				
				if (currentStep % (util.getNumBits() / 10) == 0) {
					
					System.out.println("current population:\n");
					for (BinarySolution individual : state) {
						System.out.println(individual);
					}
					user_pause();
				}
				System.out.println();
			}
			@Override
			public void currentBestSolutionUpdated(BinarySolution solution, int currentStep) {
				
				System.out.println(currentStep + ". step, new best solution: " + solution + "\n");
			}
			@Override
			public void finalSolutionFound(BinarySolution solution, int currentStep) {
				
				System.out.println("final solution: " + solution);
			}
		});
		
		algorithm.run();
	}

	/**
	 * @return a String containing the description of proper usage
	 */
	public static String usage() {

		StringBuilder sb = new StringBuilder();

		sb.append(
				"\nThis program uses the RAPGA genetic algorithm to find solution to the 'max ones' problem with 'n' bits.\n\n");
		sb.append("The program expects only 1 argument: the number of bits 'n'\n");

		return sb.toString();
	}
	
	/**
	 * Pauses until the user presses [enter].
	 */
	private static void user_pause() {
		System.out.println("Press [enter] to continue");
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		try {
			input.readLine();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}