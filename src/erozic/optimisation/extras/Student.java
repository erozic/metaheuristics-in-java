package erozic.optimisation.extras;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a student by his id and holds a list of times he/she is
 * unavailable.
 * 
 * @author Eugen Rožić
 * @version 0.1
 *
 */
public class Student {

	private String id;
	/** A list of times the student is unavailable */
	private List<Time> busyTimes;

	/**
	 * Sets the objects {@link #id} field and constructs a new, empty list of
	 * {@link #busyTimes}.
	 */
	public Student(String id) {
		this.id = id;
		this.busyTimes = new ArrayList<Time>();
	}

	public String getId() {
		return id;
	}

	public List<Time> getBusyTimes() {
		return busyTimes;
	}

	/**
	 * Returns true if {@link #id}'s are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Student))
			return false;

		Student student = (Student) obj;

		if (this.id == student.id)
			return true;
		else
			return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return id;
	}
}
