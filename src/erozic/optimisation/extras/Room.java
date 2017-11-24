package erozic.optimisation.extras;

/**
 * Represents a Location through its name and capacity.
 * 
 * @author Eugen Rožić
 * @version 0.1
 *
 */
public class Room {

	private String id;
	private int capacity;

	/**
	 * Just sets the objects {@link #id} and {@link #capacity}.
	 */
	public Room(String id, int capacity) {
		this.id = id;
		this.capacity = capacity;
	}

	public int getCapacity() {
		return capacity;
	}

	public String getId() {
		return id;
	}

	/**
	 * Returns true if {@link #id}'s are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Room))
			return false;

		Room location = (Room) obj;

		if (this.id.equals(location.id))
			return true;
		else
			return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		return id + " | " + capacity;
	}

}
