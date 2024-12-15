package flowershop.calendar;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CalendarServiceTests {

	private final EventRepository eventRepository = Mockito.mock(EventRepository.class);
	private final CalendarService calendarService = new CalendarService(eventRepository);


	@Test
	public void testFindAll() {
		when(eventRepository.findAll()).thenReturn(List.of(new Event(1L, "Event 1", LocalDateTime.now(), "Description 1")));
		List<Event> events = calendarService.findAll();
		assertEquals(1, events.size());
	}

	@Test
	public void testFindById_Found() {
		Event event = new Event(1L, "Event 1", LocalDateTime.now(), "Description 1");
		when(eventRepository.findAll()).thenReturn(List.of(event));
		Event foundEvent = calendarService.findById(1L);
		assertNotNull(foundEvent);
		assertEquals(1L, foundEvent.getId());
	}

	@Test
	public void testFindById_NotFound() {
		when(eventRepository.findAll()).thenReturn(List.of());
		Event foundEvent = calendarService.findById(1L);
		assertNull(foundEvent);
	}

	@Test
	public void testSave() {
		Event event = new Event(1L, "Event 1", LocalDateTime.now(), "Description 1");
		when(eventRepository.save(event)).thenReturn(event);
		Event savedEvent = calendarService.save(event);
		assertNotNull(savedEvent);
		assertEquals(event, savedEvent);
	}

	@Test
	public void testDelete() {
		long eventId = 1L;
		calendarService.delete(eventId);
		verify(eventRepository, times(1)).deleteById(eventId);
	}


	@Test
	public void testFindAllByDateBetween() {
		LocalDateTime startDate = LocalDateTime.of(2023, 10, 1, 0, 0);
		LocalDateTime endDate = LocalDateTime.of(2023, 10, 31, 23, 59);
		when(eventRepository.findAllByDateBetween(startDate, endDate)).thenReturn(List.of());
		List<Event> events = calendarService.findAllByDateBetween(startDate, endDate);
		assertEquals(0, events.size());
		when(eventRepository.findAllByDateBetween(startDate, endDate)).thenReturn(List.of(new Event(1L, "Event 1", LocalDateTime.now(), "Description 1")));
		assertEquals(1, calendarService.findAllByDateBetween(startDate, endDate).size());
		events = calendarService.findAllByDateBetween(startDate, endDate);
		assertEquals(1, events.size());
	}
}