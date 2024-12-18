package flowershop.calendar;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class CalendarDayTests {
	@Test
	public void testConstructor() {
		CalendarDay calendarDay = new CalendarDay(LocalDate.now());
		assertNotNull(calendarDay);
		assertEquals(LocalDate.now(), calendarDay.getDate());
		assertTrue(calendarDay.getEvents().isEmpty());
	}
	@Test
	public void testAddEvent() {
		Event event = new Event(1L,"Test",LocalDate.now().atStartOfDay(),"Description");
		CalendarDay calendarDay = new CalendarDay(LocalDate.now());
		calendarDay.addEvent(event);
		assertEquals(1, calendarDay.getEvents().size());
		assertEquals(event, calendarDay.getEvents().getFirst());
	}
	@Test
	public void testState() {
		CalendarDay calendarDay = new CalendarDay(LocalDate.now());
		assertFalse(calendarDay.getState());
		calendarDay.setState(true);
		assertTrue(calendarDay.getState());
	}
}
