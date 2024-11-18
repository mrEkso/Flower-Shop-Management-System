package flowershop.sales;

import flowershop.services.AbstractOrder;
import jakarta.persistence.Entity;
import org.salespointframework.order.Order;
import org.salespointframework.useraccount.UserAccount;

import java.util.Objects;

/**
 * Represents a basic order created when a client makes a direct purchase in the shop.
 * This type of order is used for straightforward transactions where no special delivery
 * or reservation arrangements are required.
 *
 * <p>Use cases include walk-in clients who complete purchases without requiring
 * reservations, scheduling, or delivery services.</p>
 */
@Entity
public class SimpleOrder extends AbstractOrder {
	public SimpleOrder(UserAccount user) {
		super(user);
	}

	protected SimpleOrder() {

	}
}