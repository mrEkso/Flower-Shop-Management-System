package kickstart.Davyd_Lera.services.order;

import kickstart.Davyd_Lera.models.orders.ContractOrder;
import kickstart.Davyd_Lera.repositories.orders.OrderFactoryRepository;
import org.salespointframework.order.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

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

	public Optional<ContractOrder> getById(Long id) {
		return orderFactoryRepository.getContractOrderRepository().findById(Order.OrderIdentifier.of(id.toString()));
	}

	public ContractOrder create(ContractOrder order) {
		return orderFactoryRepository.getContractOrderRepository().save(order);
	}

	public void delete(ContractOrder order) {
		orderFactoryRepository.getContractOrderRepository().delete(order);
	}
}
