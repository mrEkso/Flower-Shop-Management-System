package kickstart.Davyd_Lera.services.order;

import kickstart.Davyd_Lera.models.orders.ReservationOrder;
import kickstart.Davyd_Lera.repositories.OrdersRepository;
import org.salespointframework.order.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationOrderService {
	private final OrdersRepository<ReservationOrder> reservationOrderRepository;

	public ReservationOrderService(OrdersRepository<ReservationOrder> reservationOrderRepository) {
		Assert.notNull(reservationOrderRepository, "OrderRepository must not be null!");
		this.reservationOrderRepository = reservationOrderRepository;
	}

	public List<ReservationOrder> findAll() {
		return reservationOrderRepository.findAll(Pageable.unpaged()).toList();
	}

	public Optional<ReservationOrder> getById(Long id) {
		return reservationOrderRepository.findById(Order.OrderIdentifier.of(id.toString()));
	}

	public ReservationOrder create(ReservationOrder order) {
		return reservationOrderRepository.save(order);
	}

	public void delete(ReservationOrder order) {
		reservationOrderRepository.delete(order);
	}
}
