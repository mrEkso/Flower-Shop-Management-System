package kickstart.Davyd_Lera.services.order;

import kickstart.Davyd_Lera.models.orders.EventOrder;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
public class EventOrderService {
	private final OrderManagement<EventOrder> orderManagement;

	public EventOrderService(OrderManagement<EventOrder> orderManagement) {
		Assert.notNull(orderManagement, "OrderManagement must not be null!");
		this.orderManagement = orderManagement;
	}

	public List<EventOrder> findAll() {
		return orderManagement.findAll(Pageable.unpaged()).toList();
	}

	public Optional<EventOrder> getById(Long id) {
		return orderManagement.get(Order.OrderIdentifier.of(id.toString()));
	}

	public EventOrder create(EventOrder order) {
		return orderManagement.save(order);
	}

	public void delete(EventOrder order) {
		orderManagement.delete(order);
	}
}
