package erozic.optimisation.utils;

import erozic.optimisation.solutions.BinarySolution;

/**
 * A class that keeps all data necessary to create, evaluate and manage
 * {@link BinarySolution} objects with regard to solving the max-ones
 * problem given with a specific fitness function ({@link #evaluate(BinarySolution)}.
 *  
 * @author Eugen Rožić
 * @version 0.1
 *
 */
public class MaxOnesUtil extends BinaryUtil {
	
	public MaxOnesUtil(int numBits) {
		this.numBits = numBits;
	}
	
	/**
	 * Calculates and sets the fitness of the given individual
	 * 
	 * The fitness is equal to the number of ones 'k' divided by the number of
	 * bits 'n' when that ratio (k/n) is less then 0.8.<br>
	 * For k/n in [0.8, 0.9] the fitness is the same -> 0.8<br>
	 * For k/n in [0.9, 1.0] the fitness is (2k/n)-1<br>
	 * <br>
	 * This results in a problem for optimisation to overcome the "plateau" of
	 * values between 0.8 and 0.9.
	 * 
	 * @return a reference to the individual (for convenience)
	 */
	public BinarySolution evaluate(BinarySolution individual) {
		
		int k = 0;
		for (int i = 0; i < numBits; i++) {
			if (individual.getGenotype()[i] == 1) {
				k++;
			}
		}

		if (k <= (0.8 * numBits)) {
			individual.setFitness((double) k / numBits);
		} else if (k <= 0.9 * numBits) {
			individual.setFitness(0.8);
		} else {
			individual.setFitness(((2.0 * k) / numBits) - 1);
		}

		return individual;
	}

}
