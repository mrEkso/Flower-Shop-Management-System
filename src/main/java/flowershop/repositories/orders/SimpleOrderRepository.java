package flowershop.repositories.orders;

import flowershop.models.orders.ContractOrder;
import flowershop.models.orders.SimpleOrder;
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

public interface SimpleOrderRepository extends CrudRepository<SimpleOrder, Order.OrderIdentifier>, PagingAndSortingRepository<SimpleOrder, Order.OrderIdentifier> {
	@NotNull
	@Query("select o from #{#entityName} o")
	Page<SimpleOrder> findAll(@NotNull Pageable pageable);

	Streamable<SimpleOrder> findByDateCreatedBetween(LocalDateTime from, LocalDateTime to);

	Streamable<SimpleOrder> findByOrderStatus(OrderStatus orderStatus);

	Streamable<SimpleOrder> findByUserAccountIdentifier(UserAccount.UserAccountIdentifier userAccountIdentifier);

	Streamable<SimpleOrder> findByUserAccountIdentifierAndDateCreatedBetween(UserAccount.UserAccountIdentifier userAccountIdentifier, LocalDateTime from, LocalDateTime to);
}