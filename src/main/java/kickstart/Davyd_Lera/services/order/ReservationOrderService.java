package kickstart.Davyd_Lera.services.order;

import kickstart.Davyd_Lera.models.orders.EventOrder;
import kickstart.Davyd_Lera.models.orders.ReservationOrder;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationOrderService {
	private final OrderManagement<ReservationOrder> orderManagement;

	public ReservationOrderService(OrderManagement<ReservationOrder> orderManagement) {
		Assert.notNull(orderManagement, "OrderManagement must not be null!");
		this.orderManagement = orderManagement;
	}

	public List<ReservationOrder> findAll() {
		return orderManagement.findAll(Pageable.unpaged()).toList();
	}

	public Optional<ReservationOrder> getById(Long id) {
		return orderManagement.get(Order.OrderIdentifier.of(id.toString()));
	}

	public ReservationOrder create(ReservationOrder order) {
		return orderManagement.save(order);
	}

	public void delete(ReservationOrder order) {
		orderManagement.delete(order);
	}
}
