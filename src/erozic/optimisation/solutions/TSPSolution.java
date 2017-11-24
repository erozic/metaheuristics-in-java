package erozic.optimisation.solutions;

import java.util.Arrays;

/**
 * A representation of a solution to a TSP problem.
 * 
 * The fitness ({@link Solution}) is set to the negative of the path length.
 * 
 * @author Eugen Rožić
 *
 */
public class TSPSolution extends Solution {

	/** Town indices in the sequence they are to be traversed. */
	private int[] path;
	/** The total length of the path through all "towns". */
	private double pathLength;

	/**
	 * Sets the object fields.
	 */
	public TSPSolution(int[] path, double pathLength) {
		this.path = path;
		this.pathLength = pathLength;
		this.fitness = -pathLength;
	}

	/**
	 * Creates an empty (i.e. all zeros) {@link #path} with the given number of
	 * towns and pathLength of +oo (meaning fitness of -oo).
	 */
	public TSPSolution(int numberOfTowns) {
		this(new int[numberOfTowns], Double.POSITIVE_INFINITY);
	}

	public int[] getPath() {
		return path;
	}

	/**
	 * Sets the path of this Ant to the given one BY VALUE not by reference.
	 */
	public void setPath(int[] path) {
		if (path.length != this.path.length)
			throw new IllegalArgumentException("The number of towns should be equal for all ants!");
		System.arraycopy(path, 0, this.path, 0, path.length);
	}

	public double getPathLength() {
		return pathLength;
	}

	/**
	 * Sets the path length to the given value and the fitness to the negative
	 * of the given value.
	 */
	public void setPathLength(double pathLength) {
		this.pathLength = pathLength;
		this.fitness = -pathLength;
	}
	
	/**
	 * Copies the given TSPSolution, i.e. sets all of this' fields to the other's
	 * fields by VALUE, not by reference.
	 */
	public void makeACloneOf(Solution other) {
		
		if (!(other instanceof TSPSolution)) {
			throw new RuntimeException(other.getClass() + " is not a TSPSolution!");
		}
		
		setPath(((TSPSolution) other).getPath());
		setPathLength(((TSPSolution) other).getPathLength());
	}

	@Override
	public String toString() {
		return "Town sequence: " + Arrays.toString(path) + "\nTotal length = " + pathLength;
	}
}
