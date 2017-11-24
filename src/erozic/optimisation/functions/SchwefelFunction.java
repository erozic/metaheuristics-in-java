package erozic.optimisation.functions;

/**
 * An implementation of the Schwefel function over a vector of real numbers of arbitrary dimension.
 * 
 * @author Eugen Rožić
 * @version 0.1
 *
 */
public class SchwefelFunction implements Function {
	
	private int minmax;
	
	/**
	 * @param minmax indicates whether the goal is to find the minimum or the maximum of
	 * the function. It should be given as either {@link Function#MINIMISE} or
	 * {@link Function#MAXIMISE}.
	 */
	public SchwefelFunction(int minmax) {
		if (minmax!=MINIMISE && minmax!=MAXIMISE)
			throw new IllegalArgumentException("Argument has to be either "+MINIMISE+" or "+MAXIMISE);
		this.minmax=minmax;
	}
	
	@Override
	public double fitness(double... input) {
		return minmax*value(input);
	}

	/**
	 * Calculates the Schwefel function with the given input vector.
	 */
	@Override
	public double value(double... input) {
		
		double value = 0;
		for (int i=0; i<input.length; i++){
			value += (-input[i])*Math.sin(Math.sqrt(Math.abs(input[i])));
		}
		value /= input.length;
		
		return value;
	}
}
