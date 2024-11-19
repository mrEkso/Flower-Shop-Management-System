package flowershop.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
}
