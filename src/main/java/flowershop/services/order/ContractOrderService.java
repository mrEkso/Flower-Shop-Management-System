package flowershop.services.order;

import flowershop.models.orders.ContractOrder;
import flowershop.repositories.orders.OrderFactoryRepository;

import org.salespointframework.order.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ContractOrderService {
	private final OrderFactoryRepository orderFactoryRepository;

	public ContractOrderService(OrderFactoryRepository orderFactoryRepository) {
		Assert.notNull(orderFactoryRepository, "OrderFactoryRepository must not be null!");
		this.orderFactoryRepository = orderFactoryRepository;
	}

	public List<ContractOrder> findAll() {
		return orderFactoryRepository.getContractOrderRepository().findAll(Pageable.unpaged()).toList();
	}

	public Optional<ContractOrder> getById(UUID id) {
		return orderFactoryRepository.getContractOrderRepository().findById(Order.OrderIdentifier.of(id.toString()));
	}

	public ContractOrder create(ContractOrder order) {
		return orderFactoryRepository.getContractOrderRepository().save(order);
	}

	public void delete(ContractOrder order) {
		orderFactoryRepository.getContractOrderRepository().delete(order);
	}
}
