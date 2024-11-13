package flowershop.services.order;

import flowershop.models.orders.EventOrder;
import flowershop.repositories.orders.OrderFactoryRepository;

import org.salespointframework.order.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EventOrderService {
	private final OrderFactoryRepository orderFactoryRepository;

	public EventOrderService(OrderFactoryRepository orderFactoryRepository) {
		Assert.notNull(orderFactoryRepository, "OrderFactoryRepository must not be null!");
		this.orderFactoryRepository = orderFactoryRepository;
	}

	public List<EventOrder> findAll() {
		return orderFactoryRepository.getEventOrderRepository().findAll(Pageable.unpaged()).toList();
	}

	public Optional<EventOrder> getById(UUID id) {
		return orderFactoryRepository.getEventOrderRepository().findById(Order.OrderIdentifier.of(id.toString()));
	}

	public EventOrder create(EventOrder order) {
		return orderFactoryRepository.getEventOrderRepository().save(order);
	}

	public void delete(EventOrder order) {
		orderFactoryRepository.getEventOrderRepository().delete(order);
	}
}
