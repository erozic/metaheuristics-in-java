package erozic.optimisation.functions;

/**
 * 
 * An interface defining a function.
 * 
 * @author Eugen Rožić
 * @version 0.1
 *
 */
public interface Function {
	
	/** A constant to indicate that a minimum is to be found */
	public static final int MINIMISE = -1;
	/** A constant to indicate that a maximum is to be found */
	public static final int MAXIMISE = 1;

	/**
	 * A method to calculate the fitness.
	 * The intention is for it to be the value of the function or the
	 * negative value of the function, depending on whether the goal is
	 * to find the minimum or the maximum.
	 */
	public double fitness(double... input);
	
	/**
	 * A method to calculate the value of the function given the input vector.
	 */
	public double value(double... input);
	
}