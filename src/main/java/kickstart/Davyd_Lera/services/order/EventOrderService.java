package kickstart.Davyd_Lera.services.order;

import kickstart.Davyd_Lera.models.orders.EventOrder;
import kickstart.Davyd_Lera.repositories.OrdersRepository;
import org.salespointframework.order.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
public class EventOrderService {
	private final OrdersRepository<EventOrder> eventOrderRepository;

	public EventOrderService(OrdersRepository<EventOrder> eventOrderRepository) {
		Assert.notNull(eventOrderRepository, "OrderRepository must not be null!");
		this.eventOrderRepository = eventOrderRepository;
	}

	public List<EventOrder> findAll() {
		return eventOrderRepository.findAll(Pageable.unpaged()).toList();
	}

	public Optional<EventOrder> getById(Long id) {
		return eventOrderRepository.findById(Order.OrderIdentifier.of(id.toString()));
	}

	public EventOrder create(EventOrder order) {
		return eventOrderRepository.save(order);
	}

	public void delete(EventOrder order) {
		eventOrderRepository.delete(order);
	}
}
