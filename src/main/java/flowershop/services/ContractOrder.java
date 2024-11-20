
package flowershop.services;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import org.salespointframework.useraccount.UserAccount;

import java.time.LocalDate;

/**
 * This type of order is intended for clients who receive regular flower deliveries over a
 * specified period, such as weekly or monthly deliveries.
 *
 * <p>Example use cases include corporate clients who require regular flower arrangements
 * * for office spaces, or individual clients who want fresh flowers delivered to their homes
 * * periodically.</p>
 */

@Entity
public class ContractOrder extends AbstractOrder {


	private String contractType;
	private String frequency;
	private Integer customFrequency;
	private String customUnit;
	private LocalDate startDate;
	private LocalDate endDate;
	private String address;
	@ManyToOne
	private Client client;

	public ContractOrder(UserAccount user, String contractType, LocalDate startDate, LocalDate endDate, String address, Client client, String notes) {
		super(user, notes);
		this.client = client;
		this.contractType = contractType;
		this.startDate = startDate;
		this.endDate = endDate;
		this.address = address;
	}


	public ContractOrder() {
		super();
	}

	public String getContractType() {
		return contractType;
	}

	public void setContractType(String contractType) {
		this.contractType = contractType;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public Integer getCustomFrequency() {
		return customFrequency;
	}

	public void setCustomFrequency(Integer customFrequency) {
		this.customFrequency = customFrequency;
	}

	public String getCustomUnit() {
		return customUnit;
	}

	public void setCustomUnit(String customUnit) {
		this.customUnit = customUnit;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}
}
 