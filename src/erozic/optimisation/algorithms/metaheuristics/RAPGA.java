package erozic.optimisation.algorithms.metaheuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import erozic.optimisation.algorithms.OptimisationAlgorithm;
import erozic.optimisation.solutions.BinarySolution;
import erozic.optimisation.utils.BinaryUtil;

/**
 * A Relevant Alleles Preserving Genetic Algorithm implementation for finding a solution to
 * a problem defined in an implementation of the {@link BinaryUtil} using individuals
 * with a genotypic representation ({@link BinarySolution}.
 * 
 * @author Eugen Rožić
 *
 */
public class RAPGA extends OptimisationAlgorithm<BinarySolution> {

	// --------------------------------------------------
	// -- ALGORITHM PARAMETERS --------------------------
	// --------------------------------------------------
	/** Maximum size of the population */
	protected int maxPopulationSize;
	/** Minimum size of the population */
	protected int minPopulationSize = 2;
	/** The maximum number of children to create for the next generation */
	protected int maxEffort;
	/** How much better than its parent a child has to be */
	protected double compFactor = 0;
	/** The probability of a single-bit mutation */
	protected double mutationRate = 0.03;
	// --------------------------------------------------
	// --------------------------------------------------

	protected List<BinarySolution> activePopulation;
	protected List<BinarySolution> nextGeneration;

	/** Holds all necessary (input) information to handle the individuals */
	protected BinaryUtil decoderUtil;
	
	/**
	 * Calls the other constructor with maxSteps = Integer.MAX_Value
	 */
	public RAPGA(BinaryUtil decoderUtil, int maxPopulationSize, int minPopulationSize, int maxEffort,
			double compFactor, double mutationRate) {
		this(decoderUtil, maxPopulationSize, minPopulationSize, maxEffort, compFactor, mutationRate, Integer.MAX_VALUE);
	}

	/**
	 * Sets all the algorithm parameters and class variables.
	 * 
	 * @param decoderUtil
	 *            {@link #decoderUtil}
	 * @param maxPopulationSize
	 *            {@link #maxPopulationSize}
	 * @param minPopulationSize
	 *            {@link #minPopulationSize}
	 * @param maxEffort
	 *            {@link #maxEffort}
	 * @param compFactor
	 *            {@link #compFactor}
	 * @param mutationRate
	 *            {@link #mutationRate}
	 * @param maxSteps
	 *            see super
	 * 
	 * @see OptimisationAlgorithm
	 */
	public RAPGA(BinaryUtil decoderUtil, int maxPopulationSize, int minPopulationSize, int maxEffort, double compFactor, 
			double mutationRate, int maxSteps) {
		super(maxSteps);
		this.decoderUtil = decoderUtil;
		this.maxPopulationSize = maxPopulationSize;
		this.minPopulationSize = minPopulationSize;
		this.maxEffort = maxEffort;
		this.compFactor = compFactor;
		this.mutationRate = mutationRate;
		
		activePopulation = new ArrayList<BinarySolution>(maxPopulationSize / 2);
		currentBestSolution = new BinarySolution(decoderUtil.getNumBits());
	}
	
	/**
	 * Does what it says + fires an update event.
	 * The {@link #activePopulation} is sorted when this method finishes.
	 */
	private void findAndSaveBestSolution() {

		Collections.sort(activePopulation);

		if (currentBestSolution.compareTo(activePopulation.get(0)) > 0) {
			currentBestSolution.makeACloneOf(activePopulation.get(0));
			fireCurrentBestSolutionUpdated(currentBestSolution);
		}
	}
	
	@Override
	protected void algorithmStart() {
		
		System.out.println("[ " + Thread.currentThread().getName() + " ] " + this.getClass().getSimpleName()
				+ " started with parameters: " + " maxPopulationSize = " + maxPopulationSize + ", minPopulationSize = "
				+ minPopulationSize + ", maxEffort = " + maxEffort + ", compFactor = " + compFactor + ", mutationRate = "
				+ mutationRate);

		generateInitialPopulation();

		findAndSaveBestSolution();
	}
	
	@Override
	protected void algorithmStep() {
		
		int effort = 0;

		nextGeneration = new ArrayList<BinarySolution>(maxPopulationSize);

		// this is the implementation of elitism
		nextGeneration.add(activePopulation.get(0));

		// generate the next generation of individuals
		while ((nextGeneration.size() < maxPopulationSize) && (effort < maxEffort)) {

			BinarySolution[] parents = selection();
			
			assert parents[0].getFitness() > parents[1].getFitness();

			BinarySolution[] children = crossover(parents[0], parents[1]);

			mutate(children[0]);
			mutate(children[1]);
			
			decoderUtil.evaluate(children[0]);
			decoderUtil.evaluate(children[1]);

			// this is why the parents array has to be sorted
			double minimumFitness = parents[1].getFitness()
					+ (parents[0].getFitness() - parents[1].getFitness()) * compFactor;

			for (BinarySolution child : children) {
				if (child.getFitness() > minimumFitness && !activePopulation.contains(child)) {
					
					nextGeneration.add(child);
				}
			}
			effort += 2;
		}
		activePopulation = nextGeneration;

		adjustCompFactor();

		findAndSaveBestSolution();
		
		fireStateChanged(activePopulation);
		
		if (activePopulation.size() < minPopulationSize) {
			this.stop();
		}
	}
	
	@Override
	protected void algorithmEnd() {
		
		fireFinalSolutionFound(currentBestSolution);
		
		System.out.println("[ " + Thread.currentThread().getName() + " ] " + this.getClass().getSimpleName()
				+ " ended.");
	}

	/**
	 * Generates the initial (random) {@link #activePopulation} with their
	 * fitnesses calculated. The size of the initial population is half the
	 * {@link #maxPopulationSize}.
	 */
	private void generateInitialPopulation() {

		for (int i = 0; i < maxPopulationSize / 2; i++) {
			activePopulation.add(decoderUtil.generateRandom());
		}
	}

	/**
	 * An implementation of the simplest possible selection of two parents -
	 * they are just completely randomly selected from the
	 * {@link #activePopulation}, there is no aditional selection pressure
	 * implemented.<br>
	 * <br>
	 * The reason for this is because evolutionary pressure is implemented with
	 * the {@link #compFactor} determining how much better a child has to be in
	 * relation to its parents. But this does not prohibit implementing
	 * additional selection pressure if one wishes...
	 * 
	 * @return A pair of individuals ordered by their fitness in descending
	 *         order
	 */
	protected BinarySolution[] selection() {
		
		Random random = ThreadLocalRandom.current();

		BinarySolution parent1 = activePopulation.get(random.nextInt(activePopulation.size()));
		BinarySolution parent2 = activePopulation.get(random.nextInt(activePopulation.size()));

		if (parent2.getFitness() > parent1.getFitness()) {
			BinarySolution temp = parent2;
			parent2 = parent1;
			parent1 = temp;
		}

		return new BinarySolution[] { parent1, parent2 };
	}

	/**
	 * A crossover that chooses randomly between two different standard
	 * implementations of the crossover operator: a
	 * {@link #singlePointCrossover(BinarySolution, BinarySolution)} and a
	 * {@link #uniformCrossover(BinarySolution, BinarySolution)}.
	 */
	protected BinarySolution[] crossover(BinarySolution parent1, BinarySolution parent2) {
		
		Random random = ThreadLocalRandom.current();

		boolean coinFlip = random.nextBoolean();
		if (coinFlip)
			return singlePointCrossover(parent1, parent2);
		else
			return uniformCrossover(parent1, parent2);
	}

	/**
	 * A basic, single-point crossover operator implementation.
	 * 
	 * @return A couple of children individuals
	 */
	protected BinarySolution[] singlePointCrossover(BinarySolution parent1, BinarySolution parent2) {
		
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
	 * A uniform crossover operator implementation, takes each byte from a
	 * randomly chosen parent.
	 */
	protected BinarySolution[] uniformCrossover(BinarySolution parent1, BinarySolution parent2) {
		
		Random random = ThreadLocalRandom.current();

		BinarySolution[] children = new BinarySolution[2];

		byte[] child1 = new byte[decoderUtil.getNumBits()];
		byte[] child2 = new byte[decoderUtil.getNumBits()];

		for (int i = 0; i < decoderUtil.getNumBits(); i++) {
			boolean coinFlip = random.nextBoolean();
			if (coinFlip) {
				child1[i] = parent1.getGenotype()[i];
				child2[i] = parent2.getGenotype()[i];
			} else {
				child1[i] = parent2.getGenotype()[i];
				child2[i] = parent1.getGenotype()[i];
			}
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
	protected BinarySolution mutate(BinarySolution individual) {
		
		Random random = ThreadLocalRandom.current();

		for (int i = 0; i < decoderUtil.getNumBits(); i++) {
			if (random.nextDouble() < mutationRate) {
				individual.getGenotype()[i] ^= (byte) 1;
			}
		}
		return individual;
	}

	/**
	 * Adjusts the {@link #compFactor} so children are always expected to be
	 * better and better. However, a child will never be requested to be better
	 * than both its parents.
	 */
	private void adjustCompFactor() {
		if (compFactor < 1)
			compFactor = compFactor + (1.0 / maxPopulationSize);
		if (compFactor > 1)
			compFactor = 1;
	}
	
	public double getCompFactor() {
		return compFactor;
	}
}
