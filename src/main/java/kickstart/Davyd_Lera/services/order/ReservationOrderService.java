package kickstart.Davyd_Lera.services.order;

import kickstart.Davyd_Lera.models.order.ReservationOrder;
import org.salespointframework.order.OrderManagement;
import org.springframework.stereotype.Service;

@Service
public class ReservationOrderService extends AbstractOrderService<ReservationOrder> {
	public ReservationOrderService(OrderManagement<ReservationOrder> orderManagement) {
		super(orderManagement);
	}
}
