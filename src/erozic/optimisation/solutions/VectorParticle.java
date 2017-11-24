package erozic.optimisation.solutions;

/**
 * A representation of a "particle" for the PSO algorithm. It is represented as an evolving
 * solution to the problem, holding its current, last and historical best "states".
 * 
 * @author Eugen Rožić
 *
 */
public class VectorParticle extends VectorSolution {

	/** Best recorded solution for this particle. */
	private VectorSolution best;
	/** The previous state of this particle. */
	private VectorSolution last;
	
	/**
	 * Creates a new instance whose current and best states are the given VectorSolution
	 * (by value) and last state is empty.
	 */
	public VectorParticle(VectorSolution solution) {
		super(solution.values.length);
		this.last = new VectorSolution(values.length);
		this.best = new VectorSolution(values.length);
		this.makeACloneOf(solution);
	}
	
	/**
	 * Sets the new fitness and sets this particle's best if it is the best
	 * fitness so far.
	 */
	@Override
	public void setFitness(double fitness) {
		super.setFitness(fitness);
		if (fitness > best.fitness) {
			best.makeACloneOf(this);
		}
	}

	public VectorSolution getBest() {
		return best;
	}

	public VectorSolution getLast() {
		return last;
	}
	
	/**
	 * Saves the current state of this particle (by value).
	 */
	public void saveLast() {
		last.makeACloneOf(this);
	}
}
