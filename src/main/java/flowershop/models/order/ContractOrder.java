package flowershop.models.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import flowershop.models.Client;
import jakarta.persistence.ManyToOne;
import org.jetbrains.annotations.NotNull;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.useraccount.UserAccount;

import java.time.LocalDate;

@Entity
public class ContractOrder extends AbstractOrder {

	private String frequency;
	private LocalDate startDate;
	private LocalDate endDate;
	private OrderStatus orderStatus;

	@ManyToOne(cascade = CascadeType.ALL)
	private Client client;

	public ContractOrder(UserAccount userAccount, String frequency, LocalDate startDate, LocalDate endDate, OrderStatus orderStatus, Client client) {
		super(userAccount);
		this.frequency = frequency;
		this.startDate = startDate;
		this.endDate = endDate;
		this.orderStatus = orderStatus;
		this.client = client;
	}

	public ContractOrder(UserAccount user) {
		super(user);
	}

	protected ContractOrder() {

	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	@NotNull
	@Override
	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public Client getClient() {
		return client;
	}
}
