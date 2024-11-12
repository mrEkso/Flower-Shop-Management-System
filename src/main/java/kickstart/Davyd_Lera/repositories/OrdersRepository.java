package kickstart.Davyd_Lera.repositories;

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

public interface OrdersRepository<T extends Order> extends CrudRepository<T, Order.OrderIdentifier>, PagingAndSortingRepository<T, Order.OrderIdentifier> {
	@NotNull
	@Query("select o from #{#entityName} o")
	Page<T> findAll(@NotNull Pageable pageable);

	Streamable<T> findByDateCreatedBetween(LocalDateTime from, LocalDateTime to);

	Streamable<T> findByOrderStatus(OrderStatus orderStatus);

	Streamable<T> findByUserAccountIdentifier(UserAccount.UserAccountIdentifier userAccountIdentifier);

	Streamable<T> findByUserAccountIdentifierAndDateCreatedBetween(UserAccount.UserAccountIdentifier userAccountIdentifier, LocalDateTime from, LocalDateTime to);
}
