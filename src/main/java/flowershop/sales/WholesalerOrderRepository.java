package flowershop.sales;

import org.jetbrains.annotations.NotNull;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.util.Streamable;

import java.time.LocalDateTime;

/**
 * Repository for storing {@link WholesalerOrder}s.
 */
public interface WholesalerOrderRepository extends CrudRepository<WholesalerOrder, Order.OrderIdentifier>,
	PagingAndSortingRepository<WholesalerOrder, Order.OrderIdentifier> {
	@NotNull
	@Query("select o from #{#entityName} o")
	Page<WholesalerOrder> findAll(@NotNull Pageable pageable);

	Streamable<WholesalerOrder> findByDateCreatedBetween(LocalDateTime from, LocalDateTime to);

	Streamable<WholesalerOrder> findByOrderStatus(OrderStatus orderStatus);

	Streamable<WholesalerOrder> findByUserAccountIdentifier(UserAccount.UserAccountIdentifier userAccountIdentifier);

	Streamable<WholesalerOrder> findByUserAccountIdentifierAndDateCreatedBetween(
		UserAccount.UserAccountIdentifier userAccountIdentifier,
		LocalDateTime from, LocalDateTime to);
}
