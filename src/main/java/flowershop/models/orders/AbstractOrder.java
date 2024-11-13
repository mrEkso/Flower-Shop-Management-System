package flowershop.models.orders;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import flowershop.models.Client;
import org.salespointframework.order.Order;
import org.salespointframework.useraccount.UserAccount;

@Entity
public abstract class AbstractOrder extends Order {
	@SuppressWarnings({"unused"})
	public AbstractOrder(UserAccount user) {
		super(user.getId());
	}

	protected AbstractOrder() {
	}
}