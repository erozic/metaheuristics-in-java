package erozic.optimisation.solutions;

/**
 * A solution to an optimisation problem.
 * 
 * @author Eugen Rožić
 *
 */
public abstract class Solution implements Comparable<Solution> {
	
	/** The fitness, set at instantiation to -oo */
	protected double fitness = Double.NEGATIVE_INFINITY;
	
	public double getFitness() {
		return fitness;
	}
	
	/**
	 * Copies the values and sets the fitness from "other".
	 */
	abstract public void makeACloneOf(Solution other);
	
	/**
	 * Compares two Solutions by their fitnesses. The ordering is
	 * descending, meaning higher fitnesses first, also meaning it is the
	 * oposite of what the documentation tells it should be. <br>
	 * NOTE: this class has a natural ordering that is inconsistent with
	 * equals.<br>
	 */
	@Override
	public int compareTo(Solution o) {
		if (this.fitness > o.fitness)
			return -1;
		else if (this.fitness < o.fitness)
			return 1;
		else
			return 0;
	}
}