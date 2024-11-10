package flowershop.services.order;

import flowershop.models.order.ReservationOrder;
import org.salespointframework.order.OrderManagement;
import org.springframework.stereotype.Service;

@Service
public class ReservationOrderService extends AbstractOrderService<ReservationOrder> {
	public ReservationOrderService(OrderManagement<ReservationOrder> orderManagement) {
		super(orderManagement);
	}
}
