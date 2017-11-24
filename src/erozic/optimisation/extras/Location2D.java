package erozic.optimisation.extras;

/**
 * Represents a town in the TSP problem on a 2D flat plane.
 * 
 * @author Eugen Rožić
 *
 */
public class Location2D {

	/** The identificator (name) of the town. */
	private String id;
	/** The absolute x coordinate of the town. */
	private double x;
	/** The absolute y coordinate of the town. */
	private double y;

	/**
	 * The x coordinate relative to the minimum x coordinate of all towns, and
	 * in units of the maximum x-distance among all towns (problem dependable -
	 * for drawing)
	 */
	private double xRel = Double.NaN;
	/**
	 * The y coordinate relative to the minimum y coordinate of all towns, and
	 * in units of the maximum y-distance among all towns (problem dependable -
	 * for drawing)
	 */
	private double yRel = Double.NaN;

	/**
	 * @param x
	 *            {@link #x}
	 * @param y
	 *            {@link #y}
	 */
	public Location2D(String id, double x, double y) {
		this.id = id;
		this.x = x;
		this.y = y;
	}
	
	public String getId() {
		return id;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getxRel() {
		return xRel;
	}

	public double getyRel() {
		return yRel;
	}

	/**
	 * {@link #xRel}
	 * 
	 * @param xMin Minimum x-coordinate among all towns of a TSP problem instance.
	 * @param xMax Maximum x-coordinate among all towns of a TSP problem instance.
	 */
	public void setxRel(double xMin, double xMax) {
		this.xRel = (this.x - xMin) / (xMax - xMin);
	}
	
	/**
	 * {@link #yRel}
	 * 
	 * @param yMin Minimum y-coordinate among all towns of a TSP problem instance.
	 * @param yMax Maximum y-coordinate among all towns of a TSP problem instance.
	 */
	public void setyRel(double yMin, double yMax) {
		this.yRel = (this.y - yMin) / (yMax - yMin);
	}
	
	public double distanceTo(Location2D other) {
		return Math.sqrt((this.x-other.x)*(this.x-other.x) + (this.y-other.y)*(this.y-other.y));
	}

	@Override
	public String toString() {

		return id+" (" + x + "," + y + ")";
	}

}