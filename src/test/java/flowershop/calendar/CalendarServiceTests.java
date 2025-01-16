package flowershop.calendar;

import flowershop.clock.ClockService;
import flowershop.services.ContractOrderService;
import flowershop.services.EventOrderRepository;
import flowershop.services.EventOrderService;
import flowershop.services.ReservationOrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CalendarServiceTests {

	private EventRepository eventRepository;
	private ContractOrderService contractOrderService;
	private ReservationOrderService reservationOrderService;
	private EventOrderService eventOrderService;
	private ClockService clockService;
	private CalendarService calendarService;

	@BeforeEach
	void setUp() {
		eventRepository = mock(EventRepository.class);
		contractOrderService = mock(ContractOrderService.class);
		reservationOrderService = mock(ReservationOrderService.class);
		eventOrderService = mock(EventOrderService.class);
		clockService = mock(ClockService.class);
		calendarService = new CalendarService(eventRepository, eventOrderService, contractOrderService, reservationOrderService, clockService);
	}



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

//	@Test
//	public void testSave() {
//		Event event = new Event(1L, "Event 1", LocalDateTime.now(), "Description 1");
//		when(eventRepository.save(event)).thenReturn(event);
//		Event savedEvent = calendarService.save(event);
//		assertNotNull(savedEvent);
//		assertEquals(event, savedEvent);
//	}
	@Test
	public void testUpdate() {
		Event event = new Event(1L, "Event 1", LocalDateTime.now(), "Description 1");
		when(eventRepository.save(event)).thenReturn(event);
		when(eventRepository.findAll()).thenReturn(List.of(event));

		assertEquals("Event 1", calendarService.findAll().getFirst().getName());
		event.setName("Updated Event");
		calendarService.update(event);
		assertEquals("Updated Event", calendarService.findAll().getFirst().getName());

		when(eventRepository.findAll()).thenReturn(List.of(event));
		assertEquals("Updated Event", calendarService.findAll().get(0).getName());
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