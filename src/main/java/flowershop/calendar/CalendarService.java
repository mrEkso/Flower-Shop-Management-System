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
	public List<Event> findAll() {
		return (List<Event>) eventRepository.findAll();
	}

	public Event save(Event event) {
		return eventRepository.save(event);
	}

	public void delete(Long id) {
		eventRepository.deleteById(id);
	}
	public List<Event> findAllByDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
		return eventRepository.findAllByDateBetween(startDate, endDate);
	}
	public List<CalendarDay> generateCalendarDays(LocalDate firstDayOfMonth, List<Event> events) {
		List<CalendarDay> calendarDays = new ArrayList<>();

		LocalDate startOfGrid = firstDayOfMonth.withDayOfMonth(1).with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY));

		for (int i = 0; i < 31; i++) {
			LocalDate currentDay = startOfGrid.plusDays(i);
			CalendarDay calendarDay = new CalendarDay(currentDay);

			for (Event event : events) {
				if (event.getDate().toLocalDate().equals(currentDay)) {
					calendarDay.addEvent(event);
				}
			}

			calendarDays.add(calendarDay);
		}

		return calendarDays;
	}
}
