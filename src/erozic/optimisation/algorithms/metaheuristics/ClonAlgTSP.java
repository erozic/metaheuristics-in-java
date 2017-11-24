package erozic.optimisation.algorithms.metaheuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import erozic.optimisation.algorithms.OptimisationAlgorithm;
import erozic.optimisation.solutions.TSPSolution;
import erozic.optimisation.utils.TSPUtil;

/**
 * An implementation of the CLONALG AIS algorithm applied to solving the TSP problem.
 * 
 * @author Eugen Rožić
 *
 */
public class ClonAlgTSP extends OptimisationAlgorithm<TSPSolution> {

	// --------------------------------------------------
	// -- ALGORITHM PARAMETERS --------------------------
	// --------------------------------------------------
	/** The number of antibodies in each step of the algorithm */
	protected int populationSize = 200;
	/** The fraction of current antibodies that will be cloned */
	protected double selectFraction = 1;
	/** The fraction of completely new, random antibodies in every population */
	protected double birthFraction = 0.2;
	/** The cloning factor; clones of i-th antibody = (beta*populationSize)/i */ 
	protected double beta = 10;
	/** The hypermutation parameter, {@link #hyperMutation(TSPSolution, int)} */
	protected double rho = 0.4;
	// --------------------------------------------------
	// --------------------------------------------------
	
	/** The total number of new antibodies generated in every iteration */
	protected int numberOfClones;
	/** A secondary parameter for hypermutation: (1 - popSize)/ln(1-rho) */
	protected double tau;
	
	protected List<TSPSolution> antibodies;
	protected List<TSPSolution> clones;

	protected TSPUtil tspUtil;
	protected int numTowns;
	
	/**
	 * Sets all the algorithm parameters and prepares it to run.
	 * 
	 * @param maxSteps {@link OptimisationAlgorithm#OptimisationAlgorithm(int)}
	 */
	public ClonAlgTSP(TSPUtil tspUtil, int populationSize, double selectFraction, double birthFraction, double beta,
			double rho, int maxSteps) {
		super(maxSteps);
		this.populationSize = populationSize;
		this.selectFraction = 1;
		this.birthFraction = 0.2;
		this.beta = 10;
		this.rho = 0.4;

		tau = (1 - populationSize) / Math.log(1 - rho);
		numberOfClones = 0;
		int numSelected = (int) (selectFraction * populationSize);
		for (int i = 1; i <= numSelected; i++) {
			numberOfClones += (int) ((beta * populationSize) / i);
		}

		initialise(tspUtil);
	}
	
	/**
	 * Sets the maximum number of steps to Integer.MAX_VALUE and everything else the same as in
	 * {@link #ClonAlgTSP(TSPUtil, int, double, double, double, double, int)}.
	 * 
	 * @see OptimisationAlgorithm#OptimisationAlgorithm()
	 */
	public ClonAlgTSP(TSPUtil tspUtil, int populationSize, double selectFraction, double birthFraction, double beta,
			double rho) {
		this(tspUtil, populationSize, selectFraction, birthFraction, beta, rho, Integer.MAX_VALUE);
	}
	
	private void initialise(TSPUtil tspUtil) {
		this.tspUtil = tspUtil;
		this.numTowns = tspUtil.getNumTowns();
		
		TSPSolution greedy = new TSPSolution(numTowns);
		greedy.setPath(tspUtil.getGreedyPath());
		greedy.setPathLength(tspUtil.getGreedyPathLength());

		antibodies = new ArrayList<TSPSolution>(populationSize);
		antibodies.add(greedy); // add the greedy to start with
		for (int i = 1; i < populationSize; i++) {
			antibodies.add(tspUtil.generateRandom());
		}
		
		clones = new ArrayList<TSPSolution>(numberOfClones);
		for (int i = 0; i < numberOfClones; i++) {
			clones.add(new TSPSolution(numTowns));
		}
		
		currentBestSolution = new TSPSolution(numTowns);
	}
	
	/**
	 * Does what it says + fires an update event
	 */
	private void findAndSaveBestSolution() {

		TSPSolution bestInPopulation = antibodies.get(0);
		for (int i = 1; i < populationSize; i++) {
			if (antibodies.get(i).compareTo(bestInPopulation) < 0) {
				bestInPopulation = antibodies.get(i); 
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
				+ " started with parameters: " + "populationSize = " + populationSize + ", selectFration = "
				+ selectFraction + ", birthFraction = " + birthFraction + ", beta = " + beta + ", rho = " + rho);
		
		findAndSaveBestSolution();
	}
	
	@Override
	protected void algorithmStep() {
					
		cloningAndHyperMutation();

		newPopulation();
		
		findAndSaveBestSolution();
		
		fireStateChanged(antibodies);
	}
	
	@Override
	protected void algorithmEnd() {
		
		fireFinalSolutionFound(currentBestSolution);
		
		System.out.println("[ " + Thread.currentThread().getName() + " ] " + this.getClass().getSimpleName()
				+ " ended.");
	}
	
	/**
	 * Makes the clones, mutates and evaluates them.
	 */
	protected void cloningAndHyperMutation() {
		
		Collections.sort(antibodies);
		
		int cloneIndex = 0;
		int numSelected = (int)(selectFraction * populationSize);

		for (int i = 0; i < numSelected; i++) {

			TSPSolution original = antibodies.get(i);

			int clonesToMake = (int)((beta * populationSize) / (i + 1));
			
			for (int j = 0; j < clonesToMake; j++) {
				
				TSPSolution clone = clones.get(cloneIndex);
				clone.setPath(original.getPath());
				
				if (cloneIndex > 0) {
					hyperMutation(clone, i+1);
				}
				tspUtil.evaluate(clone);
				cloneIndex++;
			}
		}
	}
	
	/**
	 * Hypermutates the given antibody (clone) by making a number of mutations that is determined
	 * by the following formula:<br>
	 *   1 + cloneSize*rho*(1 - 1/e^(index/tau))<br><br>
	 *   
	 * The mutation is done by randomly performing a {@link #segmentFlip(Antibody, int, int)} or a
	 * {@link #simpleSwitch(TSPSolution, int, int)} on two randomly chosen indices.
	 */
	protected void hyperMutation(TSPSolution antibody, int index) {
		
		Random random = ThreadLocalRandom.current();
		
		int numberOfMutations = (int)(1 + numTowns*rho*(1 - Math.exp(-index/tau)));
		
		for (int mutation = 0; mutation < numberOfMutations; mutation++) {
			
			int firstIndex = random.nextInt(numTowns);
			int secondIndex = random.nextInt(numTowns);
			while (secondIndex == firstIndex) {
				secondIndex = random.nextInt(numTowns);
			}
			
			if (firstIndex > secondIndex){
				int temp = firstIndex; 
				firstIndex = secondIndex; 
				secondIndex = temp;
			}
			
			if (random.nextBoolean()) {
				simpleSwitch(antibody, firstIndex, secondIndex);
			} else {
				segmentFlip(antibody, firstIndex, secondIndex);
			}
		}
	}
	
	/**
	 * Performs a simple switch of the elements on the given indices in the given antibody. 
	 */
	protected void simpleSwitch(TSPSolution antibody, int firstIndex, int secondIndex) {
		
		assert (firstIndex < secondIndex);
		
		int temp = antibody.getPath()[firstIndex]; 
		antibody.getPath()[firstIndex] = antibody.getPath()[secondIndex]; 
		antibody.getPath()[secondIndex] = temp;	
	}
	
	/**
	 * Performs a reversal of elements between the given indices of the given antibody.
	 */
	protected void segmentFlip(TSPSolution antibody, int firstIndex, int secondIndex) {
		
		assert (firstIndex < secondIndex);
		
		int numSwitches = (secondIndex - firstIndex + 1) / 2; 
		
		for (int i = 0; i < numSwitches; i++){
			int temp = antibody.getPath()[firstIndex+i]; 
			antibody.getPath()[firstIndex+i] = antibody.getPath()[secondIndex-i]; 
			antibody.getPath()[secondIndex-i] = temp;
		}
	}

	/**
	 * Creates a new population by taking the best from the newly created antibodies and
	 * adding a fraction ({@link #birthFraction}) of freshly created, new, random ones.
	 */
	private void newPopulation() {
		
		Collections.sort(clones);
		
		int newOnes = (int)(populationSize * birthFraction);
		
		for (int i = 0; i < newOnes; i++) {
			antibodies.get(i).makeACloneOf(clones.get(i));
		}
		
		for (int i = newOnes; i < populationSize; i++) {
			tspUtil.shuffleArray(antibodies.get(i).getPath());
			tspUtil.evaluate(antibodies.get(i));
		}
		
	}

}
