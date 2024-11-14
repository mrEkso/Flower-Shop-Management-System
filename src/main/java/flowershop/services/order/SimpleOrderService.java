package flowershop.services.order;

import flowershop.models.orders.SimpleOrder;
import flowershop.repositories.orders.OrderFactoryRepository;
import org.salespointframework.order.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SimpleOrderService {

	private final OrderFactoryRepository orderFactoryRepository;

	public SimpleOrderService(OrderFactoryRepository orderFactoryRepository) {
		Assert.notNull(orderFactoryRepository, "OrderFactoryRepository must not be null!");
		this.orderFactoryRepository = orderFactoryRepository;
	}

	public List<SimpleOrder> findAll() {
		return orderFactoryRepository.getSimpleOrderRepository().findAll(Pageable.unpaged()).toList();
	}

	public Optional<SimpleOrder> getById(UUID id) {
		return orderFactoryRepository.getSimpleOrderRepository().findById(Order.OrderIdentifier.of(id.toString()));
	}

	public SimpleOrder create(SimpleOrder order) {
		return orderFactoryRepository.getSimpleOrderRepository().save(order);
	}

	public void delete(SimpleOrder order) {
		orderFactoryRepository.getSimpleOrderRepository().delete(order);
	}
}