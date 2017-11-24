package erozic.optimisation.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import erozic.optimisation.functions.Function;
import erozic.optimisation.solutions.VectorSolution;

/**
 * A class that keeps all data necessary to create, evaluate and manage
 * {@link VectorSolution} objects with regard to finding an extremum of
 * a function.
 * 
 * @author Eugen Rožić
 *
 */
public class VectorUtil implements SolutionUtil<VectorSolution> {
	
	private Function function;
	private int dimensions;
	private double minValue;
	private double maxValue;
	
	/**
	 * Sets the object fields.
	 * 
	 * @param function
	 *            The function to find the extremum of
	 * @param dimensions
	 *            The dimensionality of the domain
	 * @param minValue
	 *            The lower bound in all dimensions of the domain
	 * @param maxValue
	 *            The upper bound in all dimensions of the domain
	 */
	public VectorUtil(Function function, int dimensions, double minValue, double maxValue) {
		this.function = function;
		this.dimensions = dimensions;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	/**
	 * @return the dimensionality of the domain of the function.
	 */
	public int getDimensions() {
		return dimensions;
	}
	
	/**
	 * @return the minimum value of the domain in every dimension
	 */
	public double getMinValue() {
		return minValue;
	}
	
	/**
	 * @return the maximum value of the domain in every dimension
	 */
	public double getMaxValue() {
		return maxValue;
	}
	
	/**
	 * Evaluates the given VectorSolution.
	 * 
	 * @return A reference to the given object, for convenience.
	 */
	public VectorSolution evaluate(VectorSolution solution) {
		
		solution.setFitness(function.fitness(solution.getValues()));
		
		return solution;
	}
	
	/**
	 * @return A new, random and evaluated VectorSolution.
	 */
	public VectorSolution generateRandom() {
		
		Random random = ThreadLocalRandom.current();
		
		VectorSolution newRandSol = new VectorSolution(dimensions);
		
		for (int d = 0; d < dimensions; d++) {
			newRandSol.getValues()[d] = random.nextDouble() * (maxValue - minValue) + minValue;
		}
		return evaluate(newRandSol);
	}
	
	/**
	 * Adds the values in "delta" to the values in the "solution", like vector addition.
	 * If the new values of the VectorSolution fall outside the [{@link #minValue}, 
	 * {@link #maxValue}] interval they are "wrapped" back inside as if the inerval has
	 * cyclic boundary conditions.
	 * 
	 * @param solution The VectorSolution to update
	 * @param delta The values to add to the VectorSolution's values
	 */
	public void addToValues(VectorSolution solution, double[] delta) {
		
		assert delta.length == solution.getValues().length;
		
		for (int i = 0; i < dimensions; i++) {
			
			solution.getValues()[i] += delta[i];
			
			if (solution.getValues()[i] > maxValue) {
				solution.getValues()[i] -= (maxValue - minValue);
			} else if (solution.getValues()[i] < minValue) {
				solution.getValues()[i] += (maxValue - minValue);
			}
		}
	}

}
