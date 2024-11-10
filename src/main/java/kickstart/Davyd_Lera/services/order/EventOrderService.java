package kickstart.Davyd_Lera.services.order;

import kickstart.Davyd_Lera.models.order.EventOrder;
import org.salespointframework.order.OrderManagement;
import org.springframework.stereotype.Service;

@Service
public class EventOrderService extends AbstractOrderService<EventOrder> {
	public EventOrderService(OrderManagement<EventOrder> orderManagement) {
		super(orderManagement);
	}
}
