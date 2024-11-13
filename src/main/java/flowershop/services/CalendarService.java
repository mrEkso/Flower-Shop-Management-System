package flowershop.services;

import flowershop.models.calendar.Event;
import flowershop.repositories.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.time.LocalDateTime;

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
}
