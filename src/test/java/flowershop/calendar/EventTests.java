package flowershop.calendar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

public class EventTests {

	@Test
	public void testConstructor() {
		Event e = new Event(1L,"Event", LocalDate.now().atStartOfDay(), "Description");
		assertNotNull(e);
		assertEquals(1L, e.getId());
		assertEquals(e.getDate(), LocalDate.now().atStartOfDay());
		assertEquals("Description", e.getDescription());
		assertEquals("Event", e.getName());
		e.setId(2L);
		assertEquals(2L, e.getId());
		e.setName("New Event");
		assertEquals("New Event", e.getName());
		e.setDescription("New Description");
		assertEquals("New Description", e.getDescription());
		e.setDate(LocalDate.now().atStartOfDay().plusDays(1));
		assertEquals(LocalDate.now().atStartOfDay().plusDays(1), e.getDate());


	}
}
