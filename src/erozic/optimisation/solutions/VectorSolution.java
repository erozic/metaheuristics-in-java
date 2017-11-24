package erozic.optimisation.solutions;

/**
 * A representation of a solution (a vector of real numbers) as an array of
 * doubles.
 * 
 * @author Eugen Rožić
 *
 */
public class VectorSolution extends Solution {

	protected double[] values;
	
	/**
	 * Constructs an empty VectorSolution with "dimension" values.
	 */
	public VectorSolution(int dimension) {
		values = new double[dimension];
	}
	
	public double[] getValues() {
		return values;
	}
	
	/**
	 * Sets the values by VALUE, not by reference.
	 */
	public void setValues(double[] values) {
		System.arraycopy(values, 0, this.values, 0, values.length);
	}
	
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	
	/**
	 * Copies the values and sets the fitness from "other".
	 */
	public void makeACloneOf(Solution other) {
		
		if (!(other instanceof VectorSolution)) {
			throw new RuntimeException(other.getClass() + " is not a VectorSolution!");
		}
		
		setValues(((VectorSolution) other).values);
		setFitness(((VectorSolution) other).fitness);
	}

	/**
	 * Compares the individuals' vector number by number.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VectorSolution))
			return false;

		for (int i = 0; i < values.length; i++) {
			if (((VectorSolution) obj).values[i] != this.values[i])
				return false;
		}
		return true;
	}

	/**
	 * Returns the phenotype and fitness in a readable format.
	 * 
	 * {@inheritDoc}}
	 */
	@Override
	public String toString() {

		StringBuilder str = new StringBuilder("[ values: ");

		for (int i = 0; i < values.length - 1; i++) {
			str.append(values[i] + ", ");
		}
		str.append(values[values.length - 1] + " ; fitness: " + fitness + " ]");

		return str.toString();
	}
}
