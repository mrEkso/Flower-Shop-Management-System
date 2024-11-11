package kickstart.Davyd_Lera.services.order;

import kickstart.Davyd_Lera.models.orders.ContractOrder;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
public class ContractOrderService {
	private final OrderManagement<ContractOrder> orderManagement;

	public ContractOrderService(OrderManagement<ContractOrder> orderManagement) {
		Assert.notNull(orderManagement, "OrderManagement must not be null!");
		this.orderManagement = orderManagement;
	}

	public List<ContractOrder> findAll() {
		return orderManagement.findAll(Pageable.unpaged()).toList();
	}

	public Optional<ContractOrder> getById(Long id) {
		return orderManagement.get(Order.OrderIdentifier.of(id.toString()));
	}

	public ContractOrder create(ContractOrder order) {
		return orderManagement.save(order);
	}

	public void delete(ContractOrder order) {
		orderManagement.delete(order);
	}
}
