package erozic.optimisation.utils;

import erozic.optimisation.functions.Function;
import erozic.optimisation.solutions.BinarySolution;

/**
 * A class that keeps all data necessary to create, evaluate and manage
 * {@link BinarySolution} objects with regard to finding an extremum of
 * a function.
 * 
 * @author Eugen Rožić
 * @version 0.1
 *
 */
public class BinaryVectorUtil extends BinaryUtil {

	/** The function to find the extremum of */
	private Function function;
	/** The dimensionality of the domain */
	private int dimensions;
	/** The lower bound in all dimensions of the domain */
	private double minValue;
	/** The upper bound in all dimensions of the domain */
	private double maxValue;

	/**
	 * The number of bits necessary to encode a number between
	 * {@link BinaryVectorUtil#minValue} and
	 * {@link BinaryVectorUtil#maxValue} with the desired
	 * {@link BinaryVectorUtil#precision}.
	 */
	private int n;
	
	/**
	 * Instantiates a {@link #BinaryVectorUtil(Function, int, double, double, double)} with
	 * precision = 10^-4.
	 */
	public BinaryVectorUtil(Function function, int dimensions, double minValue, double maxValue) {
		this(function, dimensions, minValue, maxValue, Math.pow(10, -4));
	}

	/**
	 * @param function
	 *            The Function to find the extremum of
	 * @param dimensions
	 *            The dimensionality of the domain
	 * @param minValue
	 *            The lower bound of the domain in all dimensions
	 * @param maxValue
	 *            The upper bound of the domain in all dimensions
	 * @param precision
	 *            The absolute value of discretisation of the domain
	 */
	public BinaryVectorUtil(Function function, int dimensions, double minValue, double maxValue, double precision) {
		this.function = function;
		this.dimensions = dimensions;
		this.minValue = minValue;
		this.maxValue = maxValue;

		this.n = (int) Math.ceil(Math.log((maxValue - minValue) / precision) / Math.log(2));

		this.numBits = n * dimensions;
	}
	
	public int getDimensions() {
		return dimensions;
	}

	/**
	 * Decodes the genotype of a {@link BinarySolution}
	 * to an array of doubles with {@link BinaryVectorUtil#dimensions} elements.
	 */
	public double[] decode(BinarySolution individual) {

		double[] values = new double[dimensions];

		for (int i = 0; i < dimensions; i++) {
			for (int j = 0; j < n; j++) {
				values[i] += individual.getGenotype()[j + i * n] * Math.pow(2, n - 1 - j);
			}
			values[i] = minValue + (values[i] / (Math.pow(2, n) - 1)) * (maxValue - minValue);
		}
		return values;
	}
	
	/**
	 * Claculates and sets the fitness of the given individual with respect to
	 * finding the extremum of the {@link #function}.
	 * 
	 * @return The reference to the given individual, for convenience
	 */
	public BinarySolution evaluate(BinarySolution individual) {
		
		individual.setFitness(function.fitness(decode(individual)));
		
		return individual;
	}
}
