package erozic.optimisation.algorithms.metaheuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import erozic.optimisation.algorithms.OptimisationAlgorithm;
import erozic.optimisation.solutions.BinarySolution;
import erozic.optimisation.utils.BinaryUtil;
import erozic.optimisation.utils.BinaryVectorUtil;

/**
 * A generation elite genetic algorithm implementation for finding a solution to
 * a problem defined in an implementation of the {@link BinaryUtil} using individuals
 * with a genotypic representation ({@link BinarySolution}.
 * 
 * @author Eugen Rožić
 *
 */
public class GenerationEliteBinaryGA extends OptimisationAlgorithm<BinarySolution> {

	// --------------------------------------------------
	// -- ALGORITHM PARAMETERS --------------------------
	// --------------------------------------------------
	/** Number of individuals in a population (generation) */
	protected int populationSize = 50;
	/** How many children will there be in the {@link #nextGeneration} */
	protected double procreationFactor = 2;
	/** The probability of a single-bit mutation */
	protected double mutationRate = 0.03;
	// --------------------------------------------------
	// --------------------------------------------------
	
	private int nextGenerationSize;

	protected List<BinarySolution> activePopulation;
	private List<BinarySolution> nextGeneration;

	/** Holds all necessary (input) information to handle the individuals */
	protected BinaryUtil decoderUtil;

	/**
	 * Sets the maximum number of steps (maxSteps) to Integer.MAX_VALUE.
	 * 
	 * @see GenerationEliteBinaryGA#GenerationEliteBinaryGA(BinaryVectorUtil,
	 *      int, double, double, int)
	 */
	public GenerationEliteBinaryGA(BinaryUtil decoderUtil, int populationSize, double procreationFactor,
			double mutationRate) {
		this(decoderUtil, populationSize, procreationFactor, mutationRate, Integer.MAX_VALUE);
	}

	/**
	 * Sets all the algorithm parameters and class variables.
	 * 
	 * @param decoderUtil
	 *            Holds the problem-specific information (function etc.)
	 * @param populationSize
	 *            The number of individuals in a population
	 * @param procreationFactor
	 *            The number of individuals to generate in an iteration
	 * @param mutationRate
	 *            The probability of a single-bit mutation
	 * @param maxSteps
	 *            {@link OptimisationAlgorithm#OptimisationAlgorithm(int)}
	 */
	public GenerationEliteBinaryGA(BinaryUtil decoderUtil, int populationSize, double procreationFactor,
			double mutationRate, int maxSteps) {
		super(maxSteps);

		this.decoderUtil = decoderUtil;
		this.populationSize = populationSize;
		this.procreationFactor = procreationFactor;
		this.mutationRate = mutationRate;

		nextGenerationSize = (int) (procreationFactor * populationSize);

		activePopulation = new ArrayList<BinarySolution>(populationSize);
		currentBestSolution = new BinarySolution(decoderUtil.getNumBits());
	}

	/**
	 * Does what it says + fires an update event
	 */
	private void findAndSaveBestSolution() {

		BinarySolution bestInPopulation = activePopulation.get(0);
		for (BinarySolution individual : activePopulation) {
			if (individual.compareTo(bestInPopulation) < 0) {
				bestInPopulation = individual;
			}
		}

		if (currentBestSolution.compareTo(bestInPopulation) > 0) {
			currentBestSolution.makeACloneOf(bestInPopulation);
			fireCurrentBestSolutionUpdated(currentBestSolution);
		}
	}
	
	@Override
	protected void algorithmStart() {
		
		System.out.println("[ " + Thread.currentThread().getName() + " ] " + this.getClass().getSimpleName()
				+ " started with parameters: " + "populationSize = " + populationSize + ", procreationFactor = "
				+ procreationFactor + ", mutationRate = " + mutationRate);
		
		generateInitialPopulation();

		findAndSaveBestSolution();
	}
	
	@Override
	protected void algorithmStep() {
		
		reproduction();
		
		makeNewPopulation();
		
		findAndSaveBestSolution();
		
		fireStateChanged(activePopulation);
	}
	
	@Override
	protected void algorithmEnd() {
		
		fireFinalSolutionFound(currentBestSolution);
		
		System.out.println("[ " + Thread.currentThread().getName() + " ] " + this.getClass().getSimpleName()
				+ " ended.");
	}

	/**
	 * Generates the initial (random) {@link #activePopulation} with their
	 * fitnesses calculated.
	 */
	private void generateInitialPopulation() {

		for (int i = 0; i < populationSize; i++) {
			activePopulation.add(decoderUtil.generateRandom());
		}
	}

	/**
	 * Generates the individuals of the {@link #nextGeneration}, i.e. the
	 * children, by successive {@link #selection()} of two parents, their
	 * {@link #crossover(BinaryIndividual, BinaryIndividual)} to create two
	 * children and the {@link #mutation(BinaryIndividual)} and
	 * {@link #evaluation(BinaryIndividual)} of those new individuals.
	 */
	private void reproduction() {

		nextGeneration = new ArrayList<BinarySolution>(nextGenerationSize);

		while (nextGeneration.size() < nextGenerationSize) {

			BinarySolution parent1 = selection();
			BinarySolution parent2 = selection();

			BinarySolution[] children = crossover(parent1, parent2);

			mutation(children[0]);
			mutation(children[1]);

			nextGeneration.add(decoderUtil.evaluate(children[0]));
			nextGeneration.add(decoderUtil.evaluate(children[1]));
		}
	}

	/**
	 * Merges the current {@link #activePopulation} and {@link #nextGeneration}
	 * and makes a new {@link #activePopulation} from the fittest
	 * {@link #populationSize} of them.
	 * 
	 * This is what makes the algorithm elitist, because the best are kept in
	 * the "gene pool" and can't be lost.
	 */
	private void makeNewPopulation() {

		activePopulation.addAll(nextGeneration);
		Collections.sort(activePopulation);
		// This seems the fastest way. Sublist view is no good cause it would accumulate
		// through iterations.
		activePopulation = new ArrayList<BinarySolution>(activePopulation.subList(0, populationSize));
	}

	/**
	 * A 2-tournament selection operator - just picks 2 individuals from the
	 * {@link #activePopulation} at random and returns the better one.
	 * 
	 * This implements selection pressure because better individuals will have a
	 * larger chance of reproducing.
	 */
	protected BinarySolution selection() {
		
		Random random = ThreadLocalRandom.current();

		BinarySolution first = activePopulation.get(random.nextInt(populationSize));
		BinarySolution second = activePopulation.get(random.nextInt(populationSize));

		if (first.getFitness() > second.getFitness())
			return first;
		else
			return second;
	}

	/**
	 * A basic, single-point crossover operator implementation.
	 * 
	 * @return A couple of children individuals
	 */
	protected BinarySolution[] crossover(BinarySolution parent1, BinarySolution parent2) {
		
		Random random = ThreadLocalRandom.current();

		BinarySolution[] children = new BinarySolution[2];

		byte[] child1 = new byte[decoderUtil.getNumBits()];
		byte[] child2 = new byte[decoderUtil.getNumBits()];

		int crossoverPoint = random.nextInt(decoderUtil.getNumBits() - 1) + 1;
		for (int i = 0; i < crossoverPoint; i++) {
			child1[i] = parent1.getGenotype()[i];
			child2[i] = parent2.getGenotype()[i];
		}
		for (int i = crossoverPoint; i < decoderUtil.getNumBits(); i++) {
			child1[i] = parent2.getGenotype()[i];
			child2[i] = parent1.getGenotype()[i];
		}

		children[0] = new BinarySolution(child1);
		children[1] = new BinarySolution(child2);

		return children;
	}

	/**
	 * A basic mutation operator implementation - just flips each bit of the
	 * given individual's genotype with {@link #mutationRate} probability.
	 * 
	 * TODO A problem is a lot of calls to the random generator...
	 * 
	 * @return a reference to the passed individual, just for convenience
	 */
	protected BinarySolution mutation(BinarySolution individual) {
		
		Random random = ThreadLocalRandom.current();

		for (int i = 0; i < decoderUtil.getNumBits(); i++)
			if (random.nextDouble() < mutationRate)
				(individual.getGenotype())[i] ^= (byte) 1;
		return individual;
	}
	
}
