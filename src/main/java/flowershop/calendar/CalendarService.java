package flowershop.calendar;

import flowershop.services.ContractOrderService;
import flowershop.services.EventOrderService;
import flowershop.services.ReservationOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CalendarService {

	/**
	 * Set the desired dependencies
	 *for {@link Event}
	 */
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private ContractOrderService contractOrderService;
	@Autowired
	private ReservationOrderService reservationOrderService;
	@Autowired
	private EventOrderService eventOrderService;

	public CalendarService(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}
	/**
	 * Returns all events stored in the repository.
	 */
	public List<Event> findAll() {
		return (List<Event>) eventRepository.findAll();
	}
	/**
	 * Saves an {@link Event} to the repository.
	 * @param event The {@link Event} to be saved.
	 * @return The saved {@link Event}.
	 */
	public Event save(Event event) {
		return eventRepository.save(event);
	}
	/**
	 * Finds and retrieves an {@link Event} by its ID.
	 * @param id The ID of the event to be retrieved.
	 * @return The {@link Event} with the specified ID, or {null} if not found.
	 */
	public Event findById(Long id) {
		for(Event event : eventRepository.findAll()){
			if(event.getId().equals(id)){
				return event;
			}
		}
		return null;
	}
	/**
	 * Updates an existing {@link Event} in the repository.
	 * If the {@link Event} with the same ID exists, it will be updated.
	 * If not, the {@link Event} will be added as a new entry.
	 * @param event The {@link Event} to be updated.
	 */
	public void update(Event event) {
		eventRepository.save(event);
	}
	/**
	 * Deletes an {@link Event} from the repository by its ID.
	 * @param id The ID of the {@link Event} to be deleted.
	 */
	public void delete(Long id) {
		eventRepository.deleteById(id);
	}

	/**
	 * Finds all {@link Event} instances that take place between 2 given dates
	 * @param startDate starting poing
	 * @param endDate end point
	 * @return a {@code List} of {@link Event} in the given date range
	 */
	public List<Event> findAllByDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
		if(startDate.isAfter(endDate)){
			return new ArrayList<>();
		}
		return eventRepository.findAllByDateBetween(startDate, endDate);
	}

	/**
	 * Generates a list of {@link CalendarDay} objects representing a calendar grid
	 * The grid starts on a Sunday before or on the first day of the month and
	 * ends on a Saturday after or on the last day of the month.
	 * @param firstDayOfMonth firt day of the month
	 * @param events all the events to be added to the calendar
	 * @return {@code List} of {@link CalendarDay} containing the given events
	 */
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

					switch (event.getType()) {
						case "event":
							event.setName(eventOrderService.getById(event.getOrderId()).get().getClient().getName() + "'s Event");
							calendarDay.addEvent(event);
							break;
						case "contract":
							event.setName(contractOrderService.getById(event.getOrderId()).get().getClient().getName() + "'s Contract");
							calendarDay.addEvent(event);
							break;
						case "reservation":
							event.setName(reservationOrderService.getById(event.getOrderId()).get().getClient().getName() + "'s Reservation");
							calendarDay.addEvent(event);
							break;
						default:
							calendarDay.addEvent(event);
							break;
					}

				}
			}
			calendarDay.setState(currentDay.getMonth() == firstDayOfMonth.getMonth());
			calendarDays.add(calendarDay);
			currentDay = currentDay.plusDays(1);
		}

		return calendarDays;
	}
		public void createReccuringEvent(String name, LocalDateTime startDate, LocalDateTime endDate, String description, String frequency, String type, UUID orderId) {
		LocalDateTime current = startDate;
		while (!current.isAfter(endDate)) {
			Event event = new Event(name, current, description, type, orderId);
			save(event);
			current = current.plusDays(7);

		}
	}
	/**
	 * Removes all {@link Event} instances with the specified UUID.
	 * @param orderId The UUID of the events to be removed.
	 */
	public void removeReccuringEvent(UUID orderId) {
		for(Event event : eventRepository.findAll()){
			if(event.getOrderId().equals(orderId)){
				delete(event.getId());
			}
		}
	}
	/**
	 * Removes all {@link Event} instances with the specified UUID.
	 * @param orderId The UUID of the events to be removed.
	 */
	public void removeEvent(UUID orderId) {
		for(Event event : eventRepository.findAll()){
			if(event.getOrderId().equals(orderId)){
				delete(event.getId());
			}
		}
	}

	/**
	 * Finds and retrieves an {@link Event} by its UUID.
	 * @param orderId The UUID of the event to be retrieved.
	 * @return The {@link Event} with the specified UUID, or {null} if not found.
	 */

	public Event findEventByUUID(UUID orderId) {
		for(Event event : eventRepository.findAll()){
			if(event.getOrderId().equals(orderId)){
				return event;
			}
		}
		return null;
	}
}
