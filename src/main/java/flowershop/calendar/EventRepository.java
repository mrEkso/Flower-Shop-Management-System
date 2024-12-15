package flowershop.calendar;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The {@code EventRepository} interface provides methods to interact with
 * the database for {@link Event} entities.
 * It extends the {@code CrudRepository} to inherit standard CRUD operations.
 */
public interface EventRepository extends CrudRepository<Event, Long> {

	/**
	 * Retrieves a list of {@link Event} entities occurring within the specified
	 * date and time range.
	 *
	 * @param startDate The start of the date and time range (inclusive).
	 * @param endDate   The end of the date and time range (inclusive).
	 * @return A {@code List} of events occurring between the specified dates.
	 */
	@Query("SELECT e FROM Event e WHERE e.date BETWEEN :startDate AND :endDate")
	List<Event> findAllByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}