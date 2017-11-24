package erozic.optimisation.extras;

/**
 * Represents a term, meaning a Time and a Location.
 * 
 * @author Eugen Rožić
 * @version 0.1
 *
 */
public class Term {

	private Time time;
	private Room location;

	/**
	 * Just sets the object parameters {@link #time} and {@link #location}.
	 */
	public Term(Time time, Room location) {
		this.time = time;
		this.location = location;
	}

	public Time getTime() {
		return time;
	}

	public Room getLocation() {
		return location;
	}

	/**
	 * Returns true if all fields are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Term))
			return false;

		Term term = (Term) obj;

		if (this.location.equals(term.location) && this.time.equals(term.time))
			return true;
		else
			return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return time + " | " + location;
	}
}
