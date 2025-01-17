package flowershop.calendar;

import flowershop.clock.ClockService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CalendarControllerIntegrationTests {

	private final ClockService clockService = Mockito.mock(ClockService.class);
	private final CalendarService service = Mockito.mock(CalendarService.class);
	private final CalendarController controller = new CalendarController(service, clockService);


	@Test
	public void testShowCalendar_NoMonthYear() {
		Model model = mock(Model.class);
		when(service.findAllByDateBetween(any(), any())).thenReturn(List.of());
		LocalDate today = LocalDate.now();

		String viewName = controller.showCalendar(model, null, null);
		assertEquals("calendar/calendar", viewName);

		verify(model).addAttribute(eq("currentMonth"), eq(today.getMonthValue()));
		verify(model).addAttribute(eq("currentYear"), eq(today.getYear()));
	}

	@Test
	public void testShowCalendar_WithMonthYear() {
		Model model = mock(Model.class);
		when(service.findAllByDateBetween(any(), any())).thenReturn(List.of());
		String viewName = controller.showCalendar(model, 10, 2023);

		assertEquals("calendar/calendar", viewName);
		verify(model).addAttribute(eq("currentMonth"), eq(10));
		verify(model).addAttribute(eq("currentYear"), eq(2023));
	}

	@Test
	public void testNextMonth() {
		String redirect = controller.nextMonth(null, 12, 2023);
		assertEquals("redirect:/calendar?month=1&year=2024", redirect);
	}

	@Test
	public void testPreviousMonth() {
		String redirect = controller.previousMonth(null, 1, 2024);
		assertEquals("redirect:/calendar?month=12&year=2023", redirect);
	}

	@Test
	public void testAddEvent() {
		Event event = new Event(1L, "New Event", LocalDate.now().atStartOfDay(), "Description");
		String redirect = controller.addEvent(event, mock(RedirectAttributes.class));
		assertEquals("redirect:/calendar", redirect);
		verify(service).save(event);
	}

	@Test
	public void testDeleteEvent() {
		String redirect = controller.deleteEvent(1L);
		assertEquals("redirect:/calendar", redirect);
		verify(service).delete(1L);
	}

	@Test
	public void testEditEvent() {
		Model model = mock(Model.class);
		Event event = new Event(1L, "EditEvent", LocalDate.now().atStartOfDay(), "Description");
		when(service.findById(1L)).thenReturn(event);

		String viewName = controller.editEvent(model, 1L);
		assertEquals("calendar/edit_event", viewName);
		verify(model).addAttribute(eq("event"), eq(event));
	}

	@Test
	public void testUpdateEvent() {
		Event event = new Event(1L, "Updated Event", LocalDate.now().atStartOfDay(), "Updated Description");
		String redirect = controller.updateEvent(event, mock(RedirectAttributes.class));
		assertEquals("redirect:/calendar", redirect);
		verify(service).update(event);
	}
}