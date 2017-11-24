package erozic.optimisation.extras;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * Contains a date, start time and end time.
 * 
 * @author Eugen Rožić
 * @version 0.1
 *
 */
public class Time {

	/** The date in the format: yyyy-MM-dd */
	private String date;
	/** start time in minutes of a 24 hour day */
	private int start;
	/** end time in minutes of a 24 hour day */
	private int end;

	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * @param date
	 *            Expected format: yyyy-MM-dd
	 * @param startTime
	 *            Expected format: HH:MM (24 hour day)
	 * @param endTime
	 *            Expected format: HH:MM (24 hour day)
	 * @throws IllegalArgumentException
	 *             if startTime is later than endTime
	 */
	public Time(String date, String startTime, String endTime) throws IllegalArgumentException {

		try {
			Date temp = dateFormat.parse(date);
			if (!date.equals(dateFormat.format(temp)))
				throw new ParseException("", 0);
		} catch (ParseException e) {
			throw new IllegalArgumentException(
					"Date (" + date + ") is given in the wrong format (should be yyyy-MM-dd).");
		}
		this.date = date;
		this.start = parseTime(startTime);
		this.end = parseTime(endTime);

		if (start > end)
			throw new IllegalArgumentException(
					"Start time (" + startTime + ") cannot be later than end time (" + endTime + ")!");
	}

	private int parseTime(String time) throws IllegalArgumentException {
		String[] parts = time.split(":");
		try {
			if (parts.length != 2)
				throw new Exception();
			int hours = Integer.parseInt(parts[0]);
			int minutes = Integer.parseInt(parts[1]);
			if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59)
				throw new Exception();
			return hours * 60 + minutes;
		} catch (Exception e) {
			throw new IllegalArgumentException("Time (" + time + ") is given in the wrong format (should be HH:MM).");
		}
	}

	/**
	 * Check if this time overlaps another time.
	 * 
	 * @return 0 if NOT, the overlap in minutes if YES.
	 */
	public int overlaps(Time other) {
		if (this.date.equals(other.date)) {
			if (this.start <= other.start) {
				if (this.end <= other.start)
					return 0;
				else
					return (this.end - other.start);
			} else {
				if (this.start >= other.end)
					return 0;
				else
					return (int) (other.end - this.start);
			}
		} else
			return 0;
	}

	/**
	 * Returns true all object fields are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Time))
			return false;

		Time time = (Time) obj;

		if (this.date.equals(time.date) && this.start == time.start && this.end == time.end)
			return true;
		else
			return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		return date + " | " + format(start) + " | " + format(end);
	}

	private String format(int timeInMinutes) {
		int hours = timeInMinutes / 60;
		int remainingMinutes = timeInMinutes % 60;
		return String.format("%02d:%02d", hours, remainingMinutes);
	}

}
