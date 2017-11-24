package erozic.optimisation.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import erozic.optimisation.extras.Room;
import erozic.optimisation.extras.Student;
import erozic.optimisation.extras.Team;
import erozic.optimisation.extras.Term;
import erozic.optimisation.extras.Time;
import erozic.optimisation.solutions.Schedule;

/**
 * Provides the methods neccessary to generate and evaluate
 * {@link optjava.os_ga.models.Schedule} individuals for the algorithm.<br>
 * <br>
 * Also holds references to all the neccessary files and keeps all the relevant
 * data extracted from those files in appropriate collections.
 * 
 * @author Eugen Rožić
 * @version 0.1
 *
 */
public class ScheduleUtil implements SolutionUtil<Schedule> {

	/**
	 * An exception to throw when there is something wrong with a file, for
	 * example its contents are not in the expected format.
	 */
	public class FileException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public FileException(String message) {
			super(message);
		}
	}

	/** The file holding information about the teams */
	private File teamsFile;
	/** The file holding information about possible terms */
	private File termsFile;
	/** The file holding information about student unavailability */
	private File student_unavailabilityFile;

	private Map<String, Student> students;
	private Map<String, Room> locations;
	private List<Team> teams;
	private List<Term> terms;

	/**
	 * Finds all the neccessary files, reads all the data from them and stores
	 * them in the object variables for later use.
	 */
	public ScheduleUtil(File teamsFile, File termsFile, File student_unavailabilityFile) throws FileException {

		this.teamsFile = teamsFile;
		this.termsFile = termsFile;
		this.student_unavailabilityFile = student_unavailabilityFile;

		try {
			readStudentData();
			readTeamData();
			readTermData();
		} catch (IOException e) {
			// this should not happen since I've checked the files exist and are
			// readable
			System.err.println("Unexpected error occured: " + e.getMessage());
			System.err.println("Exiting...");
			System.exit(-1);
		}
	}

	/**
	 * Reads the student data from the 'student_unavailability' file and parses
	 * it into the {@link #students} collection.
	 */
	private void readStudentData() throws IOException {

		System.out.println(
				"Parsing the students' unavailability info file (" + student_unavailabilityFile.getPath() + ") ...");
		BufferedReader input = new BufferedReader(new FileReader(student_unavailabilityFile));

		students = new HashMap<String, Student>();
		String line;

		input.readLine();
		input.readLine(); // burn first two "header" lines
		while ((line = input.readLine()) != null) {
			String[] lineParts = line.split(" \\| ");
			if (lineParts.length != 4) {
				input.close();
				throw new FileException(
						"Wrong format! Expected format is:\n<student ID> | <date as YYYY-MM-DD> | <start time as HH:MM> | <end time as HH:MM>");
			}
			String studentID = lineParts[0].trim();
			Student student = students.get(studentID);
			if (student == null) {
				student = new Student(studentID);
				students.put(studentID, student);
			}
			student.getBusyTimes().add(new Time(lineParts[1].trim(), lineParts[2].trim(), lineParts[3].trim()));
		}
		input.close();
	}

	/**
	 * Reads the team data from the 'teams' file and parses it into the
	 * {@link #teams} collection.
	 */
	private void readTeamData() throws IOException {

		System.out.println("Parsing the teams info file (" + teamsFile.getPath() + ") ...");
		BufferedReader input = new BufferedReader(new FileReader(teamsFile));

		teams = new ArrayList<Team>();
		String line;

		input.readLine();
		input.readLine(); // burn first two "header" lines
		while ((line = input.readLine()) != null) {
			String[] lineParts = line.split(" \\| ");
			if (lineParts.length != 3) {
				input.close();
				throw new FileException(
						"Wrong format! Expected format is:\n<team id> | <assistant name> | <space separated list of student id's>");
			}
			String[] studentIDs = lineParts[2].split(" ");
			List<Student> teamMembers = new ArrayList<Student>(studentIDs.length);
			for (String studentID : studentIDs) {
				studentID = studentID.trim();
				Student student = students.get(studentID);
				if (student == null) {
					student = new Student(studentID);
					students.put(studentID, student);
				}
				teamMembers.add(student);
			}
			teams.add(new Team(lineParts[0].trim(), lineParts[1].trim(), teamMembers));
		}
		input.close();
	}

	/**
	 * Reads the term data from the 'terms' file and parses it into the
	 * {@link #terms} and {@link #locations} collections.
	 */
	private void readTermData() throws IOException {

		System.out.println("Parsing the terms info file (" + termsFile.getPath() + ") ...");
		BufferedReader input = new BufferedReader(new FileReader(termsFile));

		terms = new ArrayList<Term>();
		locations = new HashMap<String, Room>();
		String line;

		input.readLine();
		input.readLine(); // burn first two "header" lines
		while ((line = input.readLine()) != null) {
			String[] lineParts = line.split(" \\| ");
			if (lineParts.length < 5) {
				input.close();
				throw new FileException(
						"Wrong format! Expected format is:\n<date as YYYY-MM-DD> | <start time as HH:MM> | <end time as HH:MM> | "
								+ "<location1 id> | <location1 capacity> | ...");
			}
			Time time = new Time(lineParts[0].trim(), lineParts[1].trim(), lineParts[2].trim());
			for (int i = 3; i < lineParts.length; i += 2) {
				int capacity;
				try {
					capacity = Integer.parseInt(lineParts[i + 1]);
				} catch (NumberFormatException e) {
					input.close();
					throw new FileException(
							"Capacity of a location (" + lineParts[i + 1] + ")has to be a (positive) integer!");
				}
				String locationID = lineParts[i].trim();
				Room location = locations.get(locationID);
				if (location == null) {
					location = new Room(lineParts[i].trim(), capacity);
					locations.put(locationID, location);
				}
				terms.add(new Term(time, location));
			}
		}
		input.close();
	}

	public Map<String, Room> getLocations() {
		return locations;
	}

	public Map<String, Student> getStudents() {
		return students;
	}

	public List<Term> getTerms() {
		return terms;
	}

	public List<Team> getTeams() {
		return teams;
	}

	/**
	 * Prints out all collections: the map of students and locations and the
	 * lists of teams and terms.
	 */
	public void printAllCollections() {

		for (Student s : students.values())
			System.out.println(s);
		for (Room s : locations.values())
			System.out.println(s);
		for (Team t : teams)
			System.out.println(t);
		for (Term t : terms)
			System.out.println(t);
	}

	@Override
	public Schedule generateRandom() {
		
		Random random = ThreadLocalRandom.current();

		Map<Team, Term> newIndividual = new HashMap<Team, Term>();

		for (Team team : teams)
			newIndividual.put(team, terms.get(random.nextInt(terms.size())));

		return evaluate(new Schedule(newIndividual));
	}

	/**
	 * The fitness is calculated as the negative of the total sum of overlapping
	 * time (in minutes) of students' obligations. This number is multiplied by
	 * the times a hard constraint has been broken, which are the overshooting
	 * of location capacities and the physical abilities of assitants (dealing
	 * with two teams at the same time).
	 */
	@Override
	public Schedule evaluate(Schedule individual) {

		int sum = 0;
		int multiplier = 1;

		Map<Term, Integer> termOccupancy = new HashMap<Term, Integer>();
		Map<String, List<Term>> assistantObligations = new HashMap<String, List<Term>>();

		for (Map.Entry<Team, Term> entry : individual.getPhenotype().entrySet()) {

			Team team = entry.getKey();
			Term term = entry.getValue();

			// check and add assitants' obligations
			if (!assistantObligations.containsKey(team.getAssistant())) {

				List<Term> newAssitantObligations = new ArrayList<Term>();
				assistantObligations.put(team.getAssistant(), newAssitantObligations);
				newAssitantObligations.add(term);

			} else {
				for (Term obligation : assistantObligations.get(team.getAssistant()))
					if (obligation.getTime().overlaps(term.getTime()) > 0)
						multiplier++;
				assistantObligations.get(team.getAssistant()).add(term);
			}

			// check and add term occupancy
			if (!termOccupancy.containsKey(term)) {
				termOccupancy.put(term, team.getMembers().size());
			} else {
				termOccupancy.put(term, termOccupancy.get(term) + team.getMembers().size());
			}
			if (termOccupancy.get(term) > term.getLocation().getCapacity())
				multiplier++;

			// count the number of overlapping minutes
			for (Student student : team.getMembers()) {
				for (Time unavailable : student.getBusyTimes()) {
					sum += unavailable.overlaps(term.getTime());
				}
			}
		}

		individual.setFitness(-(sum * multiplier));
		individual.setOverlapingMinutes(sum);

		return individual;
	}

}
