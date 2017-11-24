package erozic.optimisation.solutions;

import erozic.optimisation.utils.BinaryVectorUtil;

/**
 * A representation of a solution (e.g. a vector of real numbers) by a byte array (a
 * genotype). The information of what exactly the byte array encodes and how to
 * decode it is contained in the
 * {@link BinaryVectorUtil.basic_ga.models.utils.BinaryIndividualDecoder} class.
 * 
 * @author Eugen Rožić
 * @version 0.1
 *
 */
public class BinarySolution extends Solution {

	private byte[] genotype;
	
	/**
	 * Instantiates a genotype with numBits elements.
	 */
	public BinarySolution(int numBits) {
		this.genotype = new byte[numBits];
	}
	
	/**
	 * Sets the genotype by reference.
	 */
	public BinarySolution(byte[] genotype) {
		this.genotype = genotype;
	}

	public byte[] getGenotype() {
		return genotype;
	}
	
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	
	/**
	 * Copies the values and sets the fitness from "other".
	 */
	public void makeACloneOf(Solution other) {
		
		if (!(other instanceof BinarySolution)) {
			throw new RuntimeException(other.getClass() + " is not a BinarySolution!");
		}
		
		System.arraycopy(((BinarySolution) other).genotype, 0, genotype, 0, genotype.length);
		this.fitness = ((BinarySolution) other).fitness;
	} 

	/**
	 * Compares the individuals' genotype byte by byte.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BinarySolution))
			return false;

		// TODO this is highly non-optimal, should make a better representation
		// of the byte array and store it in setGenotype, then use that for
		// comparison.

		for (int i = 0; i < genotype.length; i++) {
			if (((BinarySolution) obj).genotype[i] != this.genotype[i])
				return false;
		}
		return true;
	}
	
	/**
	 * Returns the genotype and fitness in a readable format.
	 * 
	 * {@inheritDoc}}
	 */
	@Override
	public String toString() {

		StringBuilder str = new StringBuilder("[ genotype: ");

		for (int i = 0; i < genotype.length; i++)
			str.append(genotype[i]);

		str.append(" ; fitness: " + fitness + " ]");

		return str.toString();
	}
}
