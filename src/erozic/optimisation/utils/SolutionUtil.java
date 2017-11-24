package erozic.optimisation.utils;

import erozic.optimisation.solutions.Solution;

/**
 * A SolutionUtil object basically defines the optimisation problem being solved.
 * It should contain the information about the structure of a solution and all
 * the necessary information about the problem to handle the solution (e.g. generate,
 * evaluate etc.)
 * 
 * @author Eugen Rožić
 * 
 */
public interface SolutionUtil<T extends Solution> {
	
	/**
	 * Calculates and sets the fitness of the given individual.
	 * 
	 * @return The reference to the given individual, for convenience
	 */
	public T evaluate(T individual);
	
	/**
	 * @return A new, random and evaluated Solution.
	 */
	public T generateRandom();
}
