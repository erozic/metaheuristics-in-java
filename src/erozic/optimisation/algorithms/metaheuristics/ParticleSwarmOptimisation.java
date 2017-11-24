package erozic.optimisation.algorithms.metaheuristics;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import erozic.optimisation.algorithms.OptimisationAlgorithm;
import erozic.optimisation.solutions.VectorParticle;
import erozic.optimisation.solutions.VectorSolution;
import erozic.optimisation.utils.VectorUtil;

/**
 * An implementation of the Particle Swarm Optimisation (PSO) algorithm for finding the extremum
 * of a function.
 * 
 * The neighbourhood is modeled as a list with cyclic boundary contitions, i.e. a ring, and
 * velocities of particles are calculated with influences from the particle itself (its historically
 * best solution * c1 * U[0,1]), its neighbourhood (historically best solution in neighbourhood * c2 *
 * U[0,1]) and its "inertia" (w * current velocity) which changes (drops) with time (iterations), 
 * where U[0,1] means a uniformly random number from the [0,1] interval.
 * 
 * @author Eugen Rožić
 *
 */
public class ParticleSwarmOptimisation extends OptimisationAlgorithm<VectorParticle> {

	// --------------------------------------------------
	// -- ALGORITHM PARAMETERS --------------------------
	// --------------------------------------------------
	/** The number of particles in the swarm */
	private int swarmSize = 20;
	/** How far (in indices) two particles can be to be neighbours */
	private int neighbourhoodRadius = 5;
	/** How attractive is a particle's best historical value */
	private double c1 = 2;
	/** How attractive is the local (neighbourhood) historical best */
	private double c2 = 2;
	/**
	 * How much inertia the particle has at the start (fraction of current
	 * velocity)
	 */
	private double wStart = 0.9;
	/**
	 * How much inertia the particle has at the end (fraction of current
	 * velocity)
	 */
	private double wEnd = 0.4;
	/** After how many iterations wEnd is reached */
	private int wIterBound = 50;
	/**
	 * The biggest possible change in the velocity as a fraction of the range of
	 * possible values
	 */
	private double vMaxDiffFraction = 0.1;
	// --------------------------------------------------
	// --------------------------------------------------

	/** The biggest possible change in the velocity */
	private double vMaxDiff;

	private ArrayList<VectorParticle> particles = null;
	
	private double[][] velocities = null;
	
	private VectorUtil vectorUtil = null;
	private int dimensions;

	/**
	 * Sets all the algorithm parameters and prepares it to run.
	 * 
	 * @param maxSteps {@link OptimisationAlgorithm#OptimisationAlgorithm(int)}
	 */
	public ParticleSwarmOptimisation(VectorUtil vectorUtil, int swarmSize, int neighbourhoodSize, double c1, double c2,
			double wStart, double wEnd, int wIterBound, double vMaxDiffFraction, int maxSteps) {
		super(maxSteps);
		
		this.swarmSize = swarmSize;
		this.neighbourhoodRadius = neighbourhoodSize;
		this.c1 = c1;
		this.c2 = c2;
		this.wStart = wStart;
		this.wEnd = wEnd;
		this.wIterBound = wIterBound;
		this.vMaxDiffFraction = vMaxDiffFraction;

		initialise(vectorUtil);
	}
	
	/**
	 * Sets the maximum number of steps to Integer.MAX_VALUE and everything else the same as in
	 * {@link #ParticleSwarmOptimisation(VectorUtil, int, int, double, double, double, double, int, double, int)}.
	 * 
	 * @see OptimisationAlgorithm#OptimisationAlgorithm()
	 */
	public ParticleSwarmOptimisation(VectorUtil vectorUtil, int swarmSize, int neighbourhoodSize, double c1, double c2,
			double wStart, double wEnd, int wIterBound, double vMaxDiffFactor) {
		this(vectorUtil, swarmSize, neighbourhoodSize, c1, c2, wStart, wEnd, wIterBound, vMaxDiffFactor, Integer.MAX_VALUE);
	}

	private void initialise(VectorUtil vectorUtil) {
		
		Random random = ThreadLocalRandom.current();
		
		this.vectorUtil = vectorUtil;
		
		this.dimensions = vectorUtil.getDimensions();
		this.vMaxDiff = ((vectorUtil.getMaxValue() - vectorUtil.getMinValue()) * vMaxDiffFraction);
		
		this.particles = new ArrayList<VectorParticle>(swarmSize);
		this.velocities = new double[swarmSize][dimensions];

		for (int i = 0; i < swarmSize; i++) {
			
			particles.add(new VectorParticle(vectorUtil.generateRandom()));
			
			for (int d = 0; d < dimensions; d++) {
				velocities[i][d] = (2 * random.nextDouble() * vMaxDiff) - vMaxDiff;
			}
		}
		currentBestSolution = new VectorParticle(particles.get(0));
	}

	/**
	 * Does what it says + fires an update event
	 */
	private void findAndSaveBestSolution() {

		VectorSolution bestInPopulation = particles.get(0).getBest();
		for (VectorParticle particle : particles) {
			if (particle.getBest().compareTo(bestInPopulation) < 0) {
				bestInPopulation = particle.getBest();
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
				+ " started with parameters: " + "swarmSize = " + swarmSize + ", neighbourhoodSize = "
				+ neighbourhoodRadius + ", c1 = " + c1 + ", c2 = " + c2 + ", wStart = " + wStart + ", wEnd = " + wEnd
				+ ", wIterBound = " + wIterBound + ", wMaxDiffFactor = " + vMaxDiffFraction);

		findAndSaveBestSolution();
	}

	@Override
	protected void algorithmStep() {

		updatePositions();
		
		evaluateParticles();

		findAndSaveBestSolution();
		
		fireStateChanged(particles);
	}
	
	@Override
	protected void algorithmEnd() {
		
		fireFinalSolutionFound(currentBestSolution);
		
		System.out.println("[ " + Thread.currentThread().getName() + " ] " + this.getClass().getSimpleName()
				+ " ended.");
	}

	/**
	 * Calculates new velocities and new positions (circular boundary
	 * conditions).
	 */
	private void updatePositions() {
		
		Random random = ThreadLocalRandom.current();

		double w;
		// TODO pogledaj jel ovo dobro, kako ide stvarno algoritam
		if (currentStep > wIterBound) {
			w = wEnd;
		} else {
			w = wStart + ((wEnd - wStart) * (currentStep - 1)) / wIterBound;
		}

		for (int i = 0; i < swarmSize; i++) {

			VectorParticle particle = particles.get(i);
			particle.saveLast();
			
			VectorSolution localBest = findLocalBest(i);

			for (int d = 0; d < dimensions; d++) {

				velocities[i][d] = w * velocities[i][d]
						+ c1 * random.nextDouble() * (particle.getBest().getValues()[d] - particle.getValues()[d])
						+ c2 * random.nextDouble() * (localBest.getValues()[d] - particle.getValues()[d]);

				if (velocities[i][d] < -vMaxDiff) {
					velocities[i][d] = -vMaxDiff;
				} else if (velocities[i][d] > vMaxDiff) {
					velocities[i][d] = vMaxDiff;
				}
				
				vectorUtil.addToValues(particle, velocities[i]);
			}
		}
	}
	
	/**
	 * Finds the best values in the neighbourhood (which is modeled as a ring)
	 */
	private VectorSolution findLocalBest(int particleIndex) {  

		int start = particleIndex - neighbourhoodRadius;
		if (start < 0) {
			start += swarmSize;
		}
		
		VectorSolution localBest = particles.get(start).getBest();
		
		int currentIndex = start + 1;
		for (int j = 1; j <= neighbourhoodRadius * 2; j++) {

			if (currentIndex >= swarmSize) {
				currentIndex -= swarmSize;
			}
			if (particles.get(currentIndex).getBest().compareTo(localBest) < 0) {
				localBest = particles.get(currentIndex).getBest();
			}
			currentIndex++;
		}
		return localBest;
	}
	
	/**
	 * Sets the fitnesses of particles and updates their historical best.
	 */
	public void evaluateParticles() {

		for (VectorParticle particle : particles) {
			// evaluate calls setFitness, which is overriden to set the new best if necessary
			vectorUtil.evaluate(particle);
		}
	}

}
