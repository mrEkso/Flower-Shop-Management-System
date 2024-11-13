package flowershop.repositories.orders;

import kickstart.Davyd_Lera.models.orders.ReservationOrder;
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

public interface ReservationOrderRepository extends CrudRepository<ReservationOrder, Order.OrderIdentifier>, PagingAndSortingRepository<ReservationOrder, Order.OrderIdentifier> {
	@NotNull
	@Query("select o from #{#entityName} o")
	Page<ReservationOrder> findAll(@NotNull Pageable pageable);

	Streamable<ReservationOrder> findByDateCreatedBetween(LocalDateTime from, LocalDateTime to);

	Streamable<ReservationOrder> findByOrderStatus(OrderStatus orderStatus);

	Streamable<ReservationOrder> findByUserAccountIdentifier(UserAccount.UserAccountIdentifier userAccountIdentifier);

	Streamable<ReservationOrder> findByUserAccountIdentifierAndDateCreatedBetween(UserAccount.UserAccountIdentifier userAccountIdentifier, LocalDateTime from, LocalDateTime to);
}
