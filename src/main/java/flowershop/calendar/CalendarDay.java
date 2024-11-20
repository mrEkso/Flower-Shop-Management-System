package flowershop.calendar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarDay {

	private LocalDate date;
	private List<Event> events;
	private boolean belongsToCurrentMonth;

	public CalendarDay(LocalDate date) {
		this.date = date;
		this.events = new ArrayList<>();
	}

	public void addEvent(Event event) {
		events.add(event);
	}

	public LocalDate getDate() {
		return date;
	}

	public List<Event> getEvents() {
		return events;
	}

	public boolean getState() {
		return belongsToCurrentMonth;
	}
	public void setState(boolean belongsToCurrentMonth) {
		this.belongsToCurrentMonth = belongsToCurrentMonth;
	}
}
