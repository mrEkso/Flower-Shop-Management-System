package flowershop.models.order;

import jakarta.persistence.Entity;
import org.salespointframework.order.Order;
import org.salespointframework.useraccount.UserAccount;

@Entity
public class SimpleOrder extends AbstractOrder {
	public SimpleOrder(UserAccount user) {
		super(user);
	}

	protected SimpleOrder() {

	}
}
