package flowershop.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalendarService {

	@Autowired
	private EventRepository eventRepository;
	public CalendarService(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}
	public List<Event> findAll() {
		return (List<Event>) eventRepository.findAll();
	}

	public Event save(Event event) {
		return eventRepository.save(event);
	}
	public Event findById(Long id) {
		for(Event event : eventRepository.findAll()){
			if(event.getId().equals(id)){
				return event;
			}
		}
		return null;
	}
	public void update(Event event) {

		eventRepository.save(event);
	}
	public void delete(Long id) {
		eventRepository.deleteById(id);
	}
	public List<Event> findAllByDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
		return eventRepository.findAllByDateBetween(startDate, endDate);
	}
	public List<CalendarDay> generateCalendarDays(LocalDate firstDayOfMonth, List<Event> events) {

		List<CalendarDay> calendarDays = new ArrayList<>();

		LocalDate startOfGrid = firstDayOfMonth.withDayOfMonth(1)
			.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY));

		LocalDate endOfGrid = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth())
			.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SATURDAY));

		LocalDate currentDay = startOfGrid;

		while (!currentDay.isAfter(endOfGrid)) {
			CalendarDay calendarDay = new CalendarDay(currentDay);
			for (Event event : events) {
				if (event.getDate().toLocalDate().equals(currentDay)) {
					calendarDay.addEvent(event);
				}
			}
			calendarDay.setState(currentDay.getMonth() == firstDayOfMonth.getMonth());
			calendarDays.add(calendarDay);
			currentDay = currentDay.plusDays(1);
		}

		return calendarDays;
	}
}
