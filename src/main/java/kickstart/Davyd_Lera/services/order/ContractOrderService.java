package kickstart.Davyd_Lera.services.order;

import kickstart.Davyd_Lera.models.orders.ContractOrder;
import kickstart.Davyd_Lera.repositories.OrdersRepository;
import org.salespointframework.order.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
public class ContractOrderService {
	private final OrdersRepository<ContractOrder> contractOrderRepository;

	public ContractOrderService(OrdersRepository<ContractOrder> contractOrderRepository) {
		Assert.notNull(contractOrderRepository, "OrderRepository must not be null!");
		this.contractOrderRepository = contractOrderRepository;
	}

	public List<ContractOrder> findAll() {
		return contractOrderRepository.findAll(Pageable.unpaged()).toList();
	}

	public Optional<ContractOrder> getById(Long id) {
		return contractOrderRepository.findById(Order.OrderIdentifier.of(id.toString()));
	}

	public ContractOrder create(ContractOrder order) {
		return contractOrderRepository.save(order);
	}

	public void delete(ContractOrder order) {
		contractOrderRepository.delete(order);
	}
}
