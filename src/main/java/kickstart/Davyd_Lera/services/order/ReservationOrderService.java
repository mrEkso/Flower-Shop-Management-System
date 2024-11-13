package kickstart.Davyd_Lera.services.order;

import kickstart.Davyd_Lera.models.orders.ReservationOrder;
import kickstart.Davyd_Lera.repositories.orders.OrderFactoryRepository;
import org.salespointframework.order.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationOrderService {
	private final OrderFactoryRepository orderFactoryRepository;

	public ReservationOrderService(OrderFactoryRepository orderFactoryRepository) {
		Assert.notNull(orderFactoryRepository, "OrderFactoryRepository must not be null!");
		this.orderFactoryRepository = orderFactoryRepository;
	}

	public List<ReservationOrder> findAll() {
		return orderFactoryRepository.getReservationOrderRepository().findAll(Pageable.unpaged()).toList();
	}

	public Optional<ReservationOrder> getById(Long id) {
		return orderFactoryRepository.getReservationOrderRepository().findById(Order.OrderIdentifier.of(id.toString()));
	}

	public ReservationOrder create(ReservationOrder order) {
		return orderFactoryRepository.getReservationOrderRepository().save(order);
	}

	public void delete(ReservationOrder order) {
		orderFactoryRepository.getReservationOrderRepository().delete(order);
	}
}
