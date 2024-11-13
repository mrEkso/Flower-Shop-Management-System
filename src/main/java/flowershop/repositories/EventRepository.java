package flowershop.repositories;

import flowershop.models.calendar.Event;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends CrudRepository<Event, Long> {

	@Query("SELECT e FROM Event e WHERE e.date BETWEEN :startDate AND :endDate")
	List<Event> findAllByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}