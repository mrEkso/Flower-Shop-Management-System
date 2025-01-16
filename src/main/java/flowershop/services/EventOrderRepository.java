package flowershop.services;

import org.jetbrains.annotations.NotNull;
import org.salespointframework.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * The `EventOrderRepository` interface provides CRUD operations and pagination for `EventOrder` entities.
 * It extends both `CrudRepository` and `PagingAndSortingRepository` interfaces provided by Spring Data.
 *
 * <p>This class serves as a replacement for orderManagement, as the logic currently faces a limitation
 * of the built-in OrderManagement.
 * Issue: All custom orders are saved without additional fields. They are converted to a generic Order class.
 * As a result, when retrieved from the database they are treated as regular Order objects from the Salespointframework
 * and cannot be converted to subclasses of custom orders.</p>
 *
 * <p>Possible solution for future improvements:
 * <ul>
 * <li>Change the persistence logic so that custom order subclasses retain their fields during database operations.</li>
 * <li>Implement a custom repository or mapper to save and retrieve custom order objects.</li>
 * <li>Adjust to the logic of built-in solutions and use only built-in tools.</li>
 * </ul>
 * </p>
 *
 * <p>At this point, the priority is to make the current implementation work.</p>
 */
public interface EventOrderRepository extends CrudRepository<EventOrder, Order.OrderIdentifier>,
	PagingAndSortingRepository<EventOrder, Order.OrderIdentifier> {

	/**
	 * Retrieves all `EventOrder` entities with pagination support.
	 *
	 * @param pageable the pagination information
	 * @return a page of `EventOrder` entities
	 */
	@NotNull
	@Query("select o from #{#entityName} o")
	Page<EventOrder> findAll(@NotNull Pageable pageable);
}
