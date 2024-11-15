package flowershop.models.orders;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import flowershop.models.Client;
import org.salespointframework.order.Order;
import org.salespointframework.useraccount.UserAccount;

@Entity
public abstract class AbstractOrder extends Order {
	@ManyToOne(cascade = CascadeType.ALL)
	private Client client;

	private String notes;

	/*
	The order is always built on the worker who took the order and
	the client to whom the order is given. Depending on the type of order,
	various other fields will be added.
	 */
	public AbstractOrder(UserAccount orderProcessingEmployee, Client client, String notes) {
		super(Objects.requireNonNull(orderProcessingEmployee.getId()));
		this.client = client;
		this.notes = notes;
	}

	public AbstractOrder(UserAccount orderProcessingEmployee, Client client) {
		super(Objects.requireNonNull(orderProcessingEmployee.getId()));
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

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
