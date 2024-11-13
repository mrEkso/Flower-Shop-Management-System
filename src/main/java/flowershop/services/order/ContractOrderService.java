package flowershop.services.order;

import flowershop.models.order.ContractOrder;
import org.salespointframework.order.OrderManagement;
import org.springframework.stereotype.Service;

@Service
public class ContractOrderService extends AbstractOrderService<ContractOrder> {
	public ContractOrderService(OrderManagement<ContractOrder> orderManagement) {
		super(orderManagement);
	}
}
