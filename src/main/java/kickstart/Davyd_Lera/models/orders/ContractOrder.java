package kickstart.Davyd_Lera.models.orders;

import jakarta.persistence.Entity;
import kickstart.Davyd_Lera.models.Client;
import org.salespointframework.useraccount.UserAccount;

import java.time.LocalDate;

@Entity
public class ContractOrder extends AbstractOrder {

	private String frequency;
	private LocalDate startDate;
	private LocalDate endDate;

	public ContractOrder(String frequency, LocalDate startDate, LocalDate endDate, UserAccount orderProcessingEmployee, Client client) {
		super(orderProcessingEmployee, client);
		this.frequency = frequency;
		this.startDate = startDate;
		this.endDate = endDate;
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
}
