package erozic.optimisation.algorithms.metaheuristics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import erozic.optimisation.algorithms.OptimisationAlgorithm;
import erozic.optimisation.solutions.TSPSolution;
import erozic.optimisation.utils.TSPUtil;

/**
 * An implementation of the Any Colony System ACO algorithm applied to solving the TSP problem.
 * 
 * @author Eugen Rožić
 *
 */
public class AntColonySystemTSP extends OptimisationAlgorithm<TSPSolution> {

	// --------------------------------------------------
	// -- ALGORITHM PARAMETERS --------------------------
	// --------------------------------------------------
	/** The number of Ants to use */
	protected int colonySize = 50;
	/**
	 * The power to which to raise the pheromones when determining the
	 * probability for the next town
	 */
	protected double alpha = 1;
	/**
	 * The power to which to raise the the distance inverse when determining the
	 * probability for the next town
	 */
	protected double beta = 2;
	/** The proportion of pheromones to evaporate in each step */
	protected double rho = 0.5;
	// --------------------------------------------------
	// --------------------------------------------------

	protected List<TSPSolution> ants;

	/** The heuristic for each town: (1/distance)^beta */
	private double[][] heuristics;
	/** The current pheromones, which get updated each step */
	private double[][] pheromones;
	/**
	 * The (unnormalised) probabilities of going from a town to another town,
	 * updated each step according to new pheromones
	 */
	private double[][] probabilities;
	/**
	 * A convenience array of all town indices that gets shuffled and used to
	 * track remaining towns while constructing a new path
	 */
	private int[] available;

	protected TSPUtil tspUtil;
	protected int numTowns;

	/**
	 * Sets all the algorithm parameters and prepares it to run.
	 * 
	 * @param maxSteps {@link OptimisationAlgorithm#OptimisationAlgorithm(int)}
	 */
	public AntColonySystemTSP(TSPUtil tspUtil, int colonySize, double alpha, double beta, double rho, int maxSteps) {
		super(maxSteps);
		this.colonySize = colonySize;
		this.alpha = alpha;
		this.beta = beta;
		this.rho = rho;

		initialise(tspUtil);
	}
	
	/**
	 * Sets the maximum number of steps to Integer.MAX_VALUE and everything else the same as in
	 * {@link #AntColonySystemTSP(TSPUtil, int, double, double, double, int)}.
	 * 
	 * @see OptimisationAlgorithm#OptimisationAlgorithm()
	 */
	public AntColonySystemTSP(TSPUtil tspUtil, int colonySize, double alpha, double beta, double rho) {
		this(tspUtil, colonySize, alpha, beta, rho, Integer.MAX_VALUE);
	}

	private void initialise(TSPUtil tspUtil) {
		this.tspUtil = tspUtil;
		this.numTowns = tspUtil.getNumTowns();

		ants = new ArrayList<TSPSolution>(colonySize);
		for (int i = 0; i < colonySize; i++)
			ants.add(new TSPSolution(numTowns));
		
		currentBestSolution = new TSPSolution(numTowns);
		currentBestSolution.setPath(tspUtil.getGreedyPath());
		currentBestSolution.setPathLength(tspUtil.getGreedyPathLength());

		heuristics = new double[numTowns][numTowns];
		pheromones = new double[numTowns][numTowns];
		probabilities = new double[numTowns][numTowns];
		double initialPheromones = 1 / tspUtil.getGreedyPathLength();
		for (int i = 0; i < numTowns; i++) {
			heuristics[i][i] = pheromones[i][i] = probabilities[i][i] = 0;
			for (int j = i + 1; j < numTowns; j++) {
				pheromones[i][j] = pheromones[j][i] = initialPheromones;
				heuristics[i][j] = heuristics[j][i] = Math.pow((1.0 / tspUtil.getDistances()[i][j]), beta);
				probabilities[i][j] = probabilities[j][i] = calculateEdgeProbability(i, j);
			}
		}

		available = new int[numTowns];
		for (int i = 0; i < numTowns; i++)
			available[i] = i;
	}
	
	/**
	 * Does what it says + fires an update event
	 */
	private void findAndSaveBestSolution() {

		TSPSolution bestInColony = ants.get(0);
		for (int i = 1; i < colonySize; i++) {
			if (ants.get(i).compareTo(bestInColony) < 0) {
				bestInColony = ants.get(i); 
			}
		}

		if (currentBestSolution.compareTo(bestInColony) > 0) {
			currentBestSolution.makeACloneOf(bestInColony);
			fireCurrentBestSolutionUpdated(currentBestSolution);
		}
	}

	@Override
	protected void algorithmStart() {
		System.out.println("[ " + Thread.currentThread().getName() + " ] " + this.getClass().getSimpleName()
				+ " started with parameters: " + "colonySize = " + colonySize + ", alpha = " + alpha
				+ ", beta = " + beta + ", rho = " + rho);
		
		fireCurrentBestSolutionUpdated(currentBestSolution);
	}
	
	@Override
	protected void algorithmStep() {
		
		for (int index = 0; index < ants.size(); index++) {
			TSPSolution ant = ants.get(index);
			findAPath(ant);
			tspUtil.evaluate(ant);
		}

		depositPheromones();
		evaporatePheromones();
		updateProbabilities();
		
		findAndSaveBestSolution();
		
		fireStateChanged(ants);
	}
	
	@Override
	protected void algorithmEnd() {
		
		fireFinalSolutionFound(currentBestSolution);
		
		System.out.println("[ " + Thread.currentThread().getName() + " ] " + this.getClass().getSimpleName()
				+ " ended.");
	}
	
	/**
	 * Represents the given ant finding a path between towns, which gets
	 * reflected in its state. The finding of a the path is influenced by the
	 * heuristic and the pheromones.
	 */
	private void findAPath(TSPSolution ant) {
		
		Random random = ThreadLocalRandom.current();

		tspUtil.shuffleArray(available);
		ant.getPath()[0] = available[0];

		for (int step = 1; step < numTowns - 1; step++) {

			int lastTown = ant.getPath()[step - 1];

			double sum = 0;
			for (int i = step; i < numTowns; i++) {
				sum += probabilities[lastTown][available[i]];
			}

			// the multiplication by sum is the same as normalising probabilities
			double rand = random.nextDouble() * sum;
			sum = 0.;
			int nextTownIndex = -1;
			for (int i = step; i < numTowns; i++) {
				sum += probabilities[lastTown][available[i]];
				if (rand <= sum) {
					nextTownIndex = i;
					break;
				}
			}
			ant.getPath()[step] = available[nextTownIndex];

			if (step != nextTownIndex) {
				int temp = available[step];
				available[step] = available[nextTownIndex];
				available[nextTownIndex] = temp;
			}
		}
		ant.getPath()[numTowns - 1] = available[numTowns - 1];
	}

	/**
	 * Simulates ants depositing pheromones on their paths. The pheromone
	 * deposit is inverse proportional to the path length of an ant.
	 * 
	 * TODO a possible addition is to make only the best N ants deposit pheromones
	 */
	private void depositPheromones() {

		for (TSPSolution ant : ants) {

			double delta = 1 / ant.getPathLength();

			for (int i = 0; i < numTowns - 1; i++) {
				int currentTown = ant.getPath()[i];
				int nextTown = ant.getPath()[i + 1];
				pheromones[nextTown][currentTown] = pheromones[currentTown][nextTown] += delta;
			}
			pheromones[0][numTowns - 1] = pheromones[numTowns - 1][0] += delta;
		}
	}

	/**
	 * Simulates pheromone evaporation by a fraction of {@link #rho}.
	 */
	private void evaporatePheromones() {
		
		for (int i = 0; i < numTowns - 1; i++) {
			for (int j = i + 1; j < numTowns; j++) {
				pheromones[j][i] = pheromones[i][j] *= (1 - rho);
			}
		}
	}
	
	/**
	 * Updates probabilities with respect to new pheromone levels
	 */
	private void updateProbabilities() {
		
		for (int i = 0; i < numTowns - 1; i++) {
			for (int j = i + 1; j < numTowns; j++) {
				probabilities[i][j] = probabilities[j][i] = calculateEdgeProbability(i, j);
			}
		}
	}
	
	/**
	 * Calculates the (unnormalised) probability of the ant going from the
	 * current town (currentTownIndex) to the next town (nextTownIndex) as:<br>
	 * pheromone_trail^alpha * (1/distance)^beta.
	 * 
	 * This method should be overriden to implement a different way of using the
	 * heuristic and pheromone information while keeping the same algorithm structure.
	 */
	protected double calculateEdgeProbability(int currentTownIndex, int nextTownIndex) {
		return Math.pow(pheromones[currentTownIndex][nextTownIndex], alpha)
				* heuristics[currentTownIndex][nextTownIndex];
	}
}