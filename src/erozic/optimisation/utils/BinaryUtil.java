package erozic.optimisation.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import erozic.optimisation.solutions.BinarySolution;

/**
 * A class that keeps all data necessary to create, evaluate and manage
 * {@link BinarySolution} objects.
 * 
 * @author Eugen Rožić
 * @version 0.1
 *
 */
public abstract class BinaryUtil implements SolutionUtil<BinarySolution> {

	protected int numBits;

	public int getNumBits() {
		return numBits;
	}
	
	@Override
	public BinarySolution generateRandom() {
		
		Random random = ThreadLocalRandom.current();
		
		BinarySolution newSolution = new BinarySolution(numBits);
		
		for (int i = 0; i < numBits; i++) {
			if (random.nextBoolean()) {
				newSolution.getGenotype()[i] = 1;
			} else {
				newSolution.getGenotype()[i] = 0;
			}
		}
		return evaluate(newSolution);
	}

}
