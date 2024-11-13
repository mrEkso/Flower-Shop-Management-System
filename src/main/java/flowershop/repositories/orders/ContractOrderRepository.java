package flowershop.repositories.orders;

import flowershop.models.orders.ContractOrder;
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

public interface ContractOrderRepository extends CrudRepository<ContractOrder, Order.OrderIdentifier>, PagingAndSortingRepository<ContractOrder, Order.OrderIdentifier> {
	@NotNull
	@Query("select o from #{#entityName} o")
	Page<ContractOrder> findAll(@NotNull Pageable pageable);

	Streamable<ContractOrder> findByDateCreatedBetween(LocalDateTime from, LocalDateTime to);

	Streamable<ContractOrder> findByOrderStatus(OrderStatus orderStatus);

	Streamable<ContractOrder> findByUserAccountIdentifier(UserAccount.UserAccountIdentifier userAccountIdentifier);

	Streamable<ContractOrder> findByUserAccountIdentifierAndDateCreatedBetween(UserAccount.UserAccountIdentifier userAccountIdentifier, LocalDateTime from, LocalDateTime to);
}
