package flowershop.repositories.orders;

import kickstart.Davyd_Lera.models.orders.EventOrder;
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

public interface EventOrderRepository extends CrudRepository<EventOrder, Order.OrderIdentifier>, PagingAndSortingRepository<EventOrder, Order.OrderIdentifier> {
	@NotNull
	@Query("select o from #{#entityName} o")
	Page<EventOrder> findAll(@NotNull Pageable pageable);

	Streamable<EventOrder> findByDateCreatedBetween(LocalDateTime from, LocalDateTime to);

	Streamable<EventOrder> findByOrderStatus(OrderStatus orderStatus);

	Streamable<EventOrder> findByUserAccountIdentifier(UserAccount.UserAccountIdentifier userAccountIdentifier);

	Streamable<EventOrder> findByUserAccountIdentifierAndDateCreatedBetween(UserAccount.UserAccountIdentifier userAccountIdentifier, LocalDateTime from, LocalDateTime to);
}
