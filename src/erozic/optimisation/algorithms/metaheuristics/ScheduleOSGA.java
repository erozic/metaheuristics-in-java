package erozic.optimisation.algorithms.metaheuristics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import erozic.optimisation.algorithms.OptimisationAlgorithm;
import erozic.optimisation.extras.Team;
import erozic.optimisation.extras.Term;
import erozic.optimisation.solutions.Schedule;
import erozic.optimisation.utils.ScheduleUtil;

/**
 * An abstract genetic algorithm for handling
 * {@link optjava.os_ga.models.Schedule}s as individuals in the search for the
 * optimal schedule.
 * 
 * @author Eugen Rožić
 * @version 0.1
 *
 */
public class ScheduleOSGA extends OptimisationAlgorithm<Schedule> {

	// --------------------------------------------------
	// -- ALGORITHM PARAMETERS --------------------------
	// --------------------------------------------------
	/** Number of individuals in a population (generation) */
	protected int populationSize = 100;
	/**
	 * how many times more children do have to be created for the algorithm to
	 * stop
	 */
	protected double maxSelPressure = 25;
	/** How much better does a child have to be than his parents to be good */
	protected double compFactor = 0;
	/** How much of the next generation have to be 'good children' */
	protected double successRatio = 0.6;
	/** How many individuals compete in a selection tournament */
	protected int kTour = 3;
	/** The probability of a single-bit mutation */
	protected double mutationRate = 0.05;
	// --------------------------------------------------
	// --------------------------------------------------
	
	protected double currSelPressure; 

	protected List<Schedule> activePopulation;
	protected List<Schedule> nextGeneration;
	protected List<Schedule> badChildrenPool;

	/** Holds all necessary (input) information to handle the individuals */
	protected ScheduleUtil scheduleHandler;
	
	/**
	 * Calls the other constructor with maxSteps = Integer.MAX_Value.
	 */
	public ScheduleOSGA(ScheduleUtil scheduleHandler, int populationSize, int maxSelPressure, double compFactor,
			double successRatio, int kTour, double mutationRate) {
		this(scheduleHandler, populationSize, maxSelPressure, compFactor, successRatio, kTour, mutationRate,
				Integer.MAX_VALUE);
	}

	/**
	 * Sets the algorithm parameters.
	 * 
	 * @param scheduleHandler
	 *            {@link #scheduleHandler}
	 * @param populationSize
	 *            {@link #populationSize}
	 * @param kTour
	 *            {@link #kTour}
	 * @param mutationRate
	 *            {@link #mutationRate}
	 *            
	 * @see OptimisationAlgorithm
	 */
	public ScheduleOSGA(ScheduleUtil scheduleHandler, int populationSize, int maxSelPressure, double compFactor,
			double successRatio, int kTour, double mutationRate, int maxSteps) {
		super(maxSteps);
		this.scheduleHandler = scheduleHandler;
		this.populationSize = populationSize;
		this.maxSelPressure = maxSelPressure;
		this.compFactor = compFactor;
		this.successRatio = successRatio;
		this.kTour = kTour;
		this.mutationRate = mutationRate;
		
		activePopulation = new ArrayList<Schedule>(populationSize);
		currentBestSolution = new Schedule(null);
	}

	/**
	 * Generates the initial (random) {@link #activePopulation} with their
	 * fitnesses calculated.
	 */
	protected void generateInitialPopulation() {

		for (int i = 0; i < populationSize; i++) {
			activePopulation.add(scheduleHandler.generateRandom());
		}
	}
	
	/**
	 * Does what it says in O(N) & fires an update event
	 */
	protected void findAndSaveBestSolution() {
		
		Schedule bestInPopulation = activePopulation.get(0);
		for (Schedule individual : activePopulation) {
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
				+ " started with parameters:" + " populationSize = " + populationSize + ", maxSelPressure = "
				+ maxSelPressure + ", compFactor = " + compFactor + ", successRatio = " + successRatio + ", kTour = "
				+ kTour + ", mutationRate = " + mutationRate);

		generateInitialPopulation();

		findAndSaveBestSolution();
		
		currSelPressure = 1;
	}
	
	@Override
	protected void algorithmStep() {
		
		Random random = ThreadLocalRandom.current();
		
		nextGeneration = new ArrayList<Schedule>(populationSize);
		badChildrenPool = new ArrayList<Schedule>(populationSize);

		reproduction();

		currSelPressure = (double) (nextGeneration.size() + badChildrenPool.size()) / populationSize;
		adjustCompFactor();

		// populate the remainder of the next generation with 'bad' children
		while (nextGeneration.size() < populationSize) {
			nextGeneration.add(badChildrenPool.get(random.nextInt(badChildrenPool.size())));
		}

		activePopulation = nextGeneration;
		
		findAndSaveBestSolution();
		
		fireStateChanged(activePopulation);
		
		if (currSelPressure >= maxSelPressure) {
			this.stop();
		}
	}
	
	/**
	 * Creates children to fill the {@link #nextGeneration} and
	 * {@link #badChildrenPool} collections.
	 */
	private void reproduction() {

		// while no enough 'good' children made or too much effort has been
		// put into it
		while (nextGeneration.size() < (activePopulation.size() * successRatio)
				&& (nextGeneration.size() + badChildrenPool.size()) < (maxSelPressure * populationSize)) {

			Schedule[] parents = selection();
			Schedule[] children = crossover(parents[0], parents[1]);

			mutation(children[0]);
			mutation(children[1]);

			scheduleHandler.evaluate(children[0]);
			scheduleHandler.evaluate(children[1]);

			// this is why the parents array has to be sorted
			double minimumFitness = parents[1].getFitness()
					+ (parents[0].getFitness() - parents[1].getFitness()) * compFactor;

			for (Schedule child : children) {
				if (child.getFitness() > minimumFitness)
					nextGeneration.add(child);
				else
					badChildrenPool.add(child);
			}
		}
	}
	
	@Override
	protected void algorithmEnd() {
		
		fireFinalSolutionFound(currentBestSolution);
		
		System.out.println("[ " + Thread.currentThread().getName() + " ] " + this.getClass().getSimpleName()
				+ " ended.");
	}

	/**
	 * An assistance method to return 2 sorted parents. Real selection goes on
	 * in {@link #kTournamentSelection(int, Random)}
	 * 
	 * @return A sorted pair of Schedule individuals (fittest first)
	 */
	protected Schedule[] selection() {

		Schedule parent1 = kTournamentSelection(kTour);
		Schedule parent2 = kTournamentSelection(kTour);

		if (parent2.getFitness() > parent1.getFitness()) {
			Schedule temp = parent2;
			parent2 = parent1;
			parent1 = temp;
		}

		return new Schedule[] { parent1, parent2 };
	}

	/**
	 * A k-tournament selection operator - picks k individuals from
	 * the {@link #activePopulation} at random and returns the fittest one.
	 * 
	 * This implements selection pressure because better individuals will have a
	 * larger chance of reproducing.
	 */
	protected Schedule kTournamentSelection(int k) {
		
		Random random = ThreadLocalRandom.current();
		
		Schedule[] candidates = new Schedule[k];

		for (int i = 0; i < k; i++) {
			candidates[i] = activePopulation.get(random.nextInt(populationSize));
		}
		Arrays.sort(candidates);
		return candidates[0];
	}

	/**
	 * A crossover that chooses randomly between two different standard
	 * implementations of the crossover operator: a
	 * {@link #singlePointCrossover(Schedule, Schedule, Random)} and a
	 * {@link #uniformCrossover(Schedule, Schedule, Random)}.
	 */
	protected Schedule[] crossover(Schedule parent1, Schedule parent2) {
		
		Random random = ThreadLocalRandom.current();

		boolean coinFlip = random.nextBoolean();
		if (coinFlip)
			return singlePointCrossover(parent1, parent2);
		else
			return uniformCrossover(parent1, parent2);
	}

	/**
	 * A uniform crossover operator implementation - a child is constructed by
	 * taking a <Team,Term> entry from a parent at random for each team (since
	 * there NEEDS to be exactly one team per schedule).
	 * 
	 * @return A couple of children individuals
	 */
	protected Schedule[] uniformCrossover(Schedule parent1, Schedule parent2) {
		
		Random random = ThreadLocalRandom.current();

		Map<Team, Term> child1 = new HashMap<Team, Term>();
		Map<Team, Term> child2 = new HashMap<Team, Term>();

		for (Map.Entry<Team, Term> entry : parent1.getPhenotype().entrySet()) {
			if (random.nextBoolean())
				child1.put(entry.getKey(), entry.getValue());
			else
				child2.put(entry.getKey(), entry.getValue());
		}
		for (Map.Entry<Team, Term> entry : parent2.getPhenotype().entrySet()) {
			if (!child1.containsKey(entry.getKey()))
				child1.put(entry.getKey(), entry.getValue());
			else
				child2.put(entry.getKey(), entry.getValue());
		}

		return new Schedule[] { new Schedule(child1), new Schedule(child2) };
	}

	/**
	 * A single-point crossover operator implementation - a child is constructed
	 * by taking the first 'k' <Team,Term> entries from one parent and the
	 * remaining ones (since there NEEDS to be exactly one team per schedule)
	 * from the other, where 'k' is chosen randomly.
	 * 
	 * @return A couple of children individuals
	 */
	protected Schedule[] singlePointCrossover(Schedule parent1, Schedule parent2) {
		
		Random random = ThreadLocalRandom.current();

		Map<Team, Term> child1 = new HashMap<Team, Term>();
		Map<Team, Term> child2 = new HashMap<Team, Term>();

		int k = random.nextInt(parent1.getPhenotype().size());
		int i = 0;
		for (Map.Entry<Team, Term> entry : parent1.getPhenotype().entrySet()) {
			if (i < k)
				child1.put(entry.getKey(), entry.getValue());
			else
				child2.put(entry.getKey(), entry.getValue());
			i++;
		}
		for (Map.Entry<Team, Term> entry : parent2.getPhenotype().entrySet()) {
			if (!child1.containsKey(entry.getKey()))
				child1.put(entry.getKey(), entry.getValue());
			else
				child2.put(entry.getKey(), entry.getValue());
		}

		return new Schedule[] { new Schedule(child1), new Schedule(child2) };
	}

	/**
	 * A mutation operator implementation - it gives a random term to a team
	 * with the probability equal to {@link #mutationRate}.
	 */
	protected void mutation(Schedule individual) {
		
		Random random = ThreadLocalRandom.current();

		for (Team team : individual.getPhenotype().keySet()) {
			if (random.nextDouble() > mutationRate)
				continue;
			individual.getPhenotype().put(team,
					scheduleHandler.getTerms().get(random.nextInt(scheduleHandler.getTerms().size())));
		}
	}
	
	/**
	 * Adjusts the {@link #compFactor} so children are always expected to be
	 * better and better. However, a child will never be requested to be better
	 * than both its parents.
	 */
	protected void adjustCompFactor() {
		if (compFactor < 1)
			compFactor = compFactor + 1 / (currSelPressure * populationSize);
		if (compFactor > 1)
			compFactor = 1;
	}
	
	public double getCurrSelPressure() {
		return currSelPressure;
	}
	
	public double getCompFactor() {
		return compFactor;
	}
}
