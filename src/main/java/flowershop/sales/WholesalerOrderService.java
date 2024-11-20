package flowershop.sales;

import org.salespointframework.order.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WholesalerOrderService {
	private final WholesalerOrderRepository wholesalerOrderRepository;

	public WholesalerOrderService(WholesalerOrderRepository wholesalerOrderRepository) {
		this.wholesalerOrderRepository = wholesalerOrderRepository;
	}

	public List<WholesalerOrder> findAll() {
		return wholesalerOrderRepository.findAll(Pageable.unpaged()).toList();
	}

	public Optional<WholesalerOrder> getById(UUID id) {
		return wholesalerOrderRepository.findById(Order.OrderIdentifier.of(id.toString()));
	}

	public WholesalerOrder create(WholesalerOrder order) {
		return wholesalerOrderRepository.save(order);
	}

	public void delete(WholesalerOrder order) {
		wholesalerOrderRepository.delete(order);
	}
}

