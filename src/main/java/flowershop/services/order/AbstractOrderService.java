package flowershop.services.order;

import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;

@Service
public abstract class AbstractOrderService<T extends Order> {

	private final OrderManagement<T> orderManagement;

	public AbstractOrderService(OrderManagement<T> orderManagement) {
		Assert.notNull(orderManagement, "OrderManagement must not be null!");
		this.orderManagement = orderManagement;
	}

	public Optional<T> getById(Long id) {
		return orderManagement.get(Order.OrderIdentifier.of(id.toString()));
	}

	public T create(T order) {
		return orderManagement.save(order);
	}

	public void delete(T order) {
		orderManagement.delete(order);
	}
}
