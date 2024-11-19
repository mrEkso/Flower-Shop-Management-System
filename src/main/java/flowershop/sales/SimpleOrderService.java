package flowershop.sales;

import org.salespointframework.order.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SimpleOrderService {

	private final SimpleOrderRepository simpleOrderRepository;

	public SimpleOrderService(SimpleOrderRepository simpleOrderRepository) {
		Assert.notNull(simpleOrderRepository, "SimpleOrderRepository must not be null!");
		this.simpleOrderRepository = simpleOrderRepository;
	}

	public List<SimpleOrder> findAll() {
		return simpleOrderRepository.findAll(Pageable.unpaged()).toList();
	}

	public Optional<SimpleOrder> getById(UUID id) {
		return simpleOrderRepository.findById(Order.OrderIdentifier.of(id.toString()));
	}

	public SimpleOrder create(SimpleOrder order) {
		return simpleOrderRepository.save(order);
	}

	public void delete(SimpleOrder order) {
		simpleOrderRepository.delete(order);
	}
}