package flowershop.sales;

import flowershop.services.OrderRepositoryFactory;
import org.salespointframework.order.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WholesalerOrderService {
	private final OrderRepositoryFactory orderFactoryRepository;

	public WholesalerOrderService(OrderRepositoryFactory orderFactoryRepository) {
		this.orderFactoryRepository = orderFactoryRepository;
	}

	public List<WholesalerOrder> findAll() {
		return orderFactoryRepository.getWholesalerOrderRepository().findAll(Pageable.unpaged()).toList();
	}

	public Optional<WholesalerOrder> getById(UUID id) {
		return orderFactoryRepository.getWholesalerOrderRepository().findById(Order.OrderIdentifier.of(id.toString()));
	}

	public WholesalerOrder create(WholesalerOrder order) {
		return orderFactoryRepository.getWholesalerOrderRepository().save(order);
	}

	public void delete(WholesalerOrder order) {
		orderFactoryRepository.getWholesalerOrderRepository().delete(order);
	}
}

