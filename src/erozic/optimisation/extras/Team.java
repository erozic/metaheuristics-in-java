package erozic.optimisation.extras;

import java.util.List;

/**
 * Represents a team by its id, assistant name and a list of student members.
 *
 */
public class Team {

	private String id;
	private String assistant;
	private List<Student> members;

	/**
	 * Just sets the objects {@link #id}, {@link #assistant} and
	 * {@link Team#members}.
	 */
	public Team(String id, String assistant, List<Student> members) {
		this.id = id;
		this.assistant = assistant;
		this.members = members;
	}

	public String getId() {
		return id;
	}

	public String getAssistant() {
		return assistant;
	}

	public List<Student> getMembers() {
		return members;
	}

	/**
	 * Returns true if {@link #id}'s are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Team))
			return false;

		Team team = (Team) obj;

		if (this.id.equals(team.id))
			return true;
		else
			return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		StringBuilder str = new StringBuilder(this.id + " (" + this.assistant + ": ");
		for (Student student : this.members) {
			str.append(student.getId() + " ");
		}
		str.append(")");
		return str.toString();
	}
}
