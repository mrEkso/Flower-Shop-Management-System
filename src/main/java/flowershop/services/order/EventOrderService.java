package flowershop.services.order;

import flowershop.models.order.EventOrder;
import org.salespointframework.order.OrderManagement;
import org.springframework.stereotype.Service;

@Service
public class EventOrderService extends AbstractOrderService<EventOrder> {
	public EventOrderService(OrderManagement<EventOrder> orderManagement) {
		super(orderManagement);
	}
}
