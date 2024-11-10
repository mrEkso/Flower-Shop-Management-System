package kickstart.Davyd_Lera.models.order;

import jakarta.persistence.Entity;
import kickstart.Davyd_Lera.models.Client;
import org.jetbrains.annotations.NotNull;
import org.salespointframework.order.OrderStatus;

import java.time.LocalDate;

@Entity
public class ContractOrder extends AbstractOrder {

	private String frequency;
	private LocalDate startDate;
	private LocalDate endDate;
	private OrderStatus orderStatus;

	public ContractOrder(String frequency, LocalDate startDate, LocalDate endDate, OrderStatus orderStatus, Client client) {
		super(client);
		this.frequency = frequency;
		this.startDate = startDate;
		this.endDate = endDate;
		this.orderStatus = orderStatus;
	}

	public ContractOrder() {
		super();
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
}
