package flowershop.calendar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code CalendarDay} class represents a specific day in the calendar.
 * <p>
 * It stores information about the date itself, the events associated with that date,
 * and whether the day belongs to the current month being viewed in a calendar.
 * </p>
 */
public class CalendarDay {

	/**
	 * The specific date that this {@code CalendarDay} represents.
	 */
	private LocalDate date;

	/**
	 * A list of {@link Event} objects representing the events taking place on this day.
	 */
	private List<Event> events;

	/**
	 * A flag indicating whether this day belongs to the current month being viewed.
	 * This is useful for visual calendars where days from other months may be shown.
	 */
	private boolean belongsToCurrentMonth;

	/**
	 * Constructs a {@code CalendarDay} for the given date.
	 * <p>
	 * The associated {@code events} list is initialized empty by default.
	 * </p>
	 *
	 * @param date The specific date this {@code CalendarDay} represents.
	 */
	public CalendarDay(LocalDate date) {
		this.date = date;
		this.events = new ArrayList<>();
	}

	/**
	 * Adds an {@link Event} to the list of events associated with this day.
	 *
	 * @param event The {@link Event} to be added.
	 */
	public void addEvent(Event event) {
		events.add(event);
	}

	/**
	 * Returns the date represented by this {@code CalendarDay}.
	 *
	 * @return The {@code LocalDate} of this calendar day.
	 */
	public LocalDate getDate() {
		return date;
	}

	/**
	 * Returns the list of events associated with this day.
	 *
	 * @return A {@code List} of {@link Event} objects.
	 */
	public List<Event> getEvents() {
		return events;
	}

	/**
	 * Returns whether this day belongs to the current month.
	 *
	 * @return {@code true} if the day belongs to the current month, {@code false} otherwise.
	 */
	public boolean getState() {
		return belongsToCurrentMonth;
	}

	/**
	 * Sets whether this day belongs to the current month.
	 *
	 * @param belongsToCurrentMonth {@code true} if the day belongs to the current month,
	 *                              {@code false} otherwise.
	 */
	public void setState(boolean belongsToCurrentMonth) {
		this.belongsToCurrentMonth = belongsToCurrentMonth;
	}
}