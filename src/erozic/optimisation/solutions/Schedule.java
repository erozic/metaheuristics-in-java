package erozic.optimisation.solutions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import erozic.optimisation.extras.Team;
import erozic.optimisation.extras.Term;

/**
 * Represents an individual, a solution to the scheduling problem.
 * 
 * @author Eugen Rožić
 * @version 0.1
 *
 */
public class Schedule extends Solution {

	private Map<Team, Term> phenotype;

	private int overlapingMinutes = -1;

	/**
	 * Sets the passed phenotype.
	 */
	public Schedule(Map<Team, Term> phenotype) {
		this.phenotype = phenotype;
	}

	public Map<Team, Term> getPhenotype() {
		return phenotype;
	}

	public void setFitness(int fitness) {
		this.fitness = (double) fitness;
	}

	public int getOverlapingMinutes() {
		return overlapingMinutes;
	}

	public void setOverlapingMinutes(int overlapingMinutes) {
		this.overlapingMinutes = overlapingMinutes;
	}
	
	/**
	 * Creates a new, empty map and copies the contents of the given
	 * "other" Schedule to "this" Schedule.
	 */
	@Override
	public void makeACloneOf(Solution other) {
		
		if (!(other instanceof Schedule)) {
			throw new RuntimeException(other.getClass() + " is not a Schedule!");
		}
		
		Schedule otherCast = (Schedule) other;
		
		this.phenotype = new HashMap<Team, Term>(otherCast.phenotype.size());
		this.phenotype.putAll(otherCast.phenotype);
		this.overlapingMinutes = otherCast.overlapingMinutes;
		this.fitness = otherCast.fitness;
	}

	/**
	 * Prints just the overlaping minutes and the fitness.
	 * 
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return "overlaping minutes: " + overlapingMinutes + ", fitness: " + fitness;
	}

	/**
	 * Prints the full schedule in a human-readable format.
	 */
	public String fullOutput() {

		Map<Term, List<Team>> inverse = new HashMap<Term, List<Team>>(phenotype.size());

		for (Map.Entry<Team, Term> entry : phenotype.entrySet()) {
			List<Team> teams = inverse.get(entry.getValue());
			if (teams == null) {
				teams = new ArrayList<Team>(3);
				inverse.put(entry.getValue(), teams);
			}
			teams.add(entry.getKey());
		}

		StringBuilder str = new StringBuilder();

		for (Map.Entry<Term, List<Team>> entry : inverse.entrySet()) {
			str.append(entry.getKey() + " | teams: ");
			for (Team team : entry.getValue()) {
				str.append(team.getId() + ", ");
			}
			str.append("\n");
		}
		str.append("\n" + this.toString());

		return str.toString();
	}

}