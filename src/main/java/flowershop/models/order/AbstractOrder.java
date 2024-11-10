package flowershop.models.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToOne;
import flowershop.models.Client;
import org.salespointframework.order.Order;

public abstract class AbstractOrder extends Order {
	@ManyToOne(cascade = CascadeType.ALL)
	private Client client;

	@SuppressWarnings({"unused", "deprecation"})
	public AbstractOrder(Client client) {
		super();
		this.client = client;
	}

	@SuppressWarnings({"unused", "deprecation"})
	public AbstractOrder() {
		super();
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}
}
