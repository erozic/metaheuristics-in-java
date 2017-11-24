package erozic.optimisation.solutions;

import erozic.optimisation.functions.Function;
import erozic.optimisation.functions.RastriginFunction;
import erozic.optimisation.solutions.VectorParticle;
import erozic.optimisation.solutions.VectorSolution;
import erozic.optimisation.utils.VectorUtil;

/**
 * Tests for the VectorSolution and VectorParticle classes.
 * 
 * @author Eugen Rožić
 *
 */
public class VectorSolutionTest {
	
	
	public static void main(String[] args) {
		
		VectorUtil vectorUtil = new VectorUtil(new RastriginFunction(Function.MINIMISE),
				2, -10, 10);
		
		VectorSolution vector1 = vectorUtil.generateRandom();
		VectorSolution vector2 = vectorUtil.generateRandom();
		
		if (vector1.compareTo(vector2) > 0) {
			VectorSolution temp = vector1;
			vector1 = vector2;
			vector2 = temp;
		}
		
		// checks if compareTo works as expected
		assert vector1.getFitness() > vector2.getFitness();
		
		VectorParticle particle = new VectorParticle(vector2);
		
		// check constructor works as expected
		assert arrayCompare(particle.getValues(), vector2.getValues());
		assert particle.getFitness() == vector2.getFitness();
		assert arrayCompare(particle.getBest().getValues(), vector2.getValues());
		assert particle.getBest().getFitness() == vector2.getFitness();
		
		// check makeACloneOf copies array by value
		vector2.getValues()[0] = -vector2.getValues()[0];
		assert !arrayCompare(particle.getValues(), vector2.getValues());
		
		// check makeACloneOf calls the overridden setFitness of the VectorParticle
		particle.makeACloneOf(vector1);
		assert arrayCompare(particle.getValues(), vector1.getValues());
		assert particle.getFitness() == vector1.getFitness();
		assert arrayCompare(particle.getBest().getValues(), vector1.getValues());
		assert particle.getBest().getFitness() == vector1.getFitness();
		
		
		vector2 = new VectorParticle(vector2);
		particle = (VectorParticle) vector2;
		
		// check overridden setFitness is called when cast to inherited type
		assert particle.getBest().getFitness() == vector2.getFitness();
		vector2.setFitness(vector1.getFitness());
		assert vector2.getFitness() == vector1.getFitness();
		assert particle.getBest().getFitness() == vector1.getFitness(); 
	}
	
	private static boolean arrayCompare(double[] array1, double[] array2) {
		
		if (array1.length != array2.length)
			return false;
		
		for (int i = 0; i < array1.length; i++) {
			if (array1[i] != array2[i])
				return false;
		}
		
		return true;
	}

}
