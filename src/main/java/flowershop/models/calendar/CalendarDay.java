package flowershop.models.calendar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarDay {

	private LocalDate date;
	private List<Event> events;

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
}
