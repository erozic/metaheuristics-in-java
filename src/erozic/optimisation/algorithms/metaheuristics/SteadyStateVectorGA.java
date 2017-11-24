package erozic.optimisation.algorithms.metaheuristics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import erozic.optimisation.algorithms.OptimisationAlgorithm;
import erozic.optimisation.solutions.VectorSolution;
import erozic.optimisation.utils.BinaryVectorUtil;
import erozic.optimisation.utils.VectorUtil;

/**
 * A steady state genetic algorithm implementation for finding the minimum of a
 * function using individuals with a phenotypic representation (double[]).
 * 
 * @author Eugen Rožić
 *
 */
public class SteadyStateVectorGA extends OptimisationAlgorithm<VectorSolution> {

	// --------------------------------------------------
	// -- ALGORITHM PARAMETERS --------------------------
	// --------------------------------------------------
	/** Number of individuals in a population (generation) */
	protected int populationSize = 50;
	/**
	 * How many standard deviations (sigma) of a Gaussian fit in the
	 * (maxValue-minValue) range
	 */
	protected double mutationIntensity = 30;
	// --------------------------------------------------
	// --------------------------------------------------

	protected List<VectorSolution> population;
	
	protected VectorUtil vectorUtil;
	
	/**
	 * Sets the maximum number of steps (maxSteps) to Integer.MAX_VALUE.
	 * 
	 * @see GenerationEliteBinaryGA#GenerationEliteBinaryGA(BinaryVectorUtil,
	 *      int, double, double, int)
	 */
	public SteadyStateVectorGA(VectorUtil vectorUtil, int populationSize, double mutationIntensity) {
		this(vectorUtil, populationSize, mutationIntensity, Integer.MAX_VALUE);
	}

	/**
	 * Sets all the algorithm parameters and class variables.
	 * 
	 * @param vectorUtil Holds all the problem-specific information (function etc.)
	 * @param populationSize The number of individuals in a population
	 * @param mutationIntensity {@link #mutationIntensity}
	 * @param maxSteps {@link OptimisationAlgorithm#OptimisationAlgorithm(int)}
	 */
	public SteadyStateVectorGA(VectorUtil vectorUtil, int populationSize, double mutationIntensity, int maxSteps) {
		super(maxSteps);

		this.vectorUtil = vectorUtil;
		this.populationSize = populationSize;
		this.mutationIntensity = mutationIntensity;
		
		population = new ArrayList<VectorSolution>(populationSize);
		currentBestSolution = new VectorSolution(vectorUtil.getDimensions());
	}
	
	/**
	 * Does what it says in O(N) & fires an update event
	 */
	private void findAndSaveBestSolution() {
		
		VectorSolution bestInPopulation = population.get(0);
		for (VectorSolution individual : population) {
			if (individual.compareTo(bestInPopulation) < 0) {
				bestInPopulation = individual;
			}
		}
		
		checkIfBest(bestInPopulation);
	}
	
	private void checkIfBest(VectorSolution individual) {
		
		if (currentBestSolution.compareTo(individual) > 0) {
			currentBestSolution.makeACloneOf(individual);
			fireCurrentBestSolutionUpdated(currentBestSolution);
		}
	}
	
	@Override
	protected void algorithmStart() {
		
		System.out.println("[ " + Thread.currentThread().getName() + " ] " + this.getClass().getSimpleName()
				+ " started with parameters: " + "populationSize = " + populationSize
				+ ", mutationIntensity = " + mutationIntensity);
		
		generateInitialPopulation();

		findAndSaveBestSolution();
	}
	
	@Override
	protected void algorithmStep() {
		
		TreeSet<VectorSolution> potentialParents = selection();

		VectorSolution child = crossover(potentialParents.pollFirst(), potentialParents.pollFirst());

		mutatation(child);

		population.remove(potentialParents.pollFirst());
		population.add(vectorUtil.evaluate(child));
		checkIfBest(child);
		
		fireStateChanged(population);
	}
	
	@Override
	protected void algorithmEnd() {
		
		fireFinalSolutionFound(currentBestSolution);
		
		System.out.println("[ " + Thread.currentThread().getName() + " ] " + this.getClass().getSimpleName()
				+ " ended.");
	}

	/**
	 * Generates the initial (random) {@link #population} with their fitnesses
	 * calculated.
	 */
	private void generateInitialPopulation() {

		for (int i = 0; i < populationSize; i++) {
			population.add(vectorUtil.generateRandom());
		}
	}

	/**
	 * A modified 3-tournament selection operator - picks 3 different
	 * individuals at random and sorts them (a TreeSet is automatically sorted).
	 * <br>
	 * The first two should be used as parents and the third one removed from
	 * the population to make room for a child of the better two parents. <br>
	 * This implements selection pressure because better individuals will have a
	 * larger chance of reproducing.
	 */
	protected TreeSet<VectorSolution> selection() {
		
		Random random = ThreadLocalRandom.current();

		TreeSet<VectorSolution> parents = new TreeSet<VectorSolution>();

		while (parents.size() < 3) {
			parents.add(population.get(random.nextInt(population.size())));
		}
		return parents;
	}

	/**
	 * A simple numerical crossover operator implementation. It creates one
	 * child using for its values the arithmetic mean of the corresponding
	 * parent values.
	 * 
	 * @return a child
	 */
	protected VectorSolution crossover(VectorSolution parent1, VectorSolution parent2) {
		
		VectorSolution child = new VectorSolution(vectorUtil.getDimensions());

		for (int i = 0; i < vectorUtil.getDimensions(); i++) {
			child.getValues()[i] = (parent1.getValues()[i] + parent2.getValues()[i]) / 2;
		}

		return child;
	}

	/**
	 * The implementation of a mutation operator.
	 * 
	 * This is a bit tricky with phenotype-represented individuals. This
	 * implementation adds to every value in the individual's phenotype a small
	 * value sampled from a Gaussian, whose width is determined by the inverse
	 * of the {@link #mutationIntensity} parameter. <br>
	 * This way all values get mutated, and if the width is small enough this
	 * amounts to a stochastic local search.<br>
	 * <br>
	 * Another way to possibly do this would be to have a mutationRate parameter
	 * which would be small (like in the
	 * {@link optjava.basic_ga.algorithms.impl.GenerationEliteBinImpl}
	 * implementation) and would determine if a value would at all be mutated.
	 * If it would it could be assigned a uniformly random value from the
	 * (maxValue-minValue) interval.
	 * 
	 * @param individual
	 *            The individual to be mutated
	 */
	protected void mutatation(VectorSolution individual) {
		
		Random random = ThreadLocalRandom.current();

		double[] values = individual.getValues();
		double interval = vectorUtil.getMaxValue() - vectorUtil.getMinValue();

		for (int i = 0; i < values.length; i++) {
			individual.getValues()[i] += random.nextGaussian() * interval / this.mutationIntensity;
			if (individual.getValues()[i] > vectorUtil.getMaxValue()
					|| individual.getValues()[i] < vectorUtil.getMinValue()) {
				individual.getValues()[i] %= interval;
				individual.getValues()[i] += vectorUtil.getMinValue();
			}
		}
	}

}
