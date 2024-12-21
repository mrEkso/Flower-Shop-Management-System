
package flowershop.services;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import org.salespointframework.useraccount.UserAccount;

import java.time.LocalDateTime;

/**
 * This type of order is intended for clients who receive regular flower deliveries over a
 * specified period, such as weekly or monthly deliveries.
 * <p>
 * Example use cases include corporate clients who require regular flower arrangements
 * for office spaces, or individual clients who want fresh flowers delivered to their homes
 * periodically.
 */
@Entity
public class ContractOrder extends AbstractOrder {


	private String contractType;
	private String frequency;
	private Integer customFrequency;
	private String customUnit;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private String address;
	@ManyToOne
	private Client client;

	/**
	 * Constructs a `ContractOrder` with the specified user, contract type, start date, end date, address, client, and notes.
	 *
	 * @param user         the user account associated with the order
	 * @param contractType the type of contract (e.g., weekly, monthly)
	 * @param startDate   the start date of the contract
	 * @param endDate      the end date of the contract
	 * @param address      the delivery address
	 * @param client       the client associated with the order
	 * @param notes        additional notes for the order
	 */
	public ContractOrder(UserAccount user, String contractType, String frequency, LocalDateTime startDate, LocalDateTime endDate, String address, Client client, String notes) {
		super(user, notes);
		this.client = client;
		this.contractType = contractType;
		this.frequency = frequency;
		this.startDate = startDate;
		this.endDate = endDate;
		this.address = address;
	}

	/**
	 * Default constructor for `ContractOrder`.
	 * This constructor is primarily used by JPA and other frameworks.
	 */
	public ContractOrder() {
		super();
	}

	/**
	 * Returns the type of contract.
	 *
	 * @return the contract type
	 */
	public String getContractType() {
		return contractType;
	}

	/**
	 * Sets the type of contract.
	 *
	 * @param contractType the contract type to set
	 */
	public void setContractType(String contractType) {
		this.contractType = contractType;
	}

	/**
	 * Returns the start date of the contract.
	 *
	 * @return the start date
	 */
	public LocalDateTime getStartDate() {
		return startDate;
	}

	/**
	 * Sets the start date of the contract.
	 *
	 * @param startDate the start date to set
	 */
	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	/**
	 * Returns the end date of the contract.
	 *
	 * @return the end date
	 */
	public LocalDateTime getEndDate() {
		return endDate;
	}

	/**
	 * Sets the end date of the contract.
	 *
	 * @param endDate the end date to set
	 */
	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	/**
	 * Returns the delivery address.
	 *
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Sets the delivery address.
	 *
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}


	/**
	 * Returns the frequency of deliveries.
	 *
	 * @return the frequency
	 */
	public String getFrequency() {
		return frequency;
	}

	/**
	 * Sets the frequency of deliveries.
	 *
	 * @param frequency the frequency to set
	 */
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	/**
	 * Returns the custom frequency of deliveries.
	 *
	 * @return the custom frequency
	 */
	public Integer getCustomFrequency() {
		return customFrequency;
	}

	/**
	 * Sets the custom frequency of deliveries.
	 *
	 * @param customFrequency the custom frequency to set
	 */
	public void setCustomFrequency(Integer customFrequency) {
		this.customFrequency = customFrequency;
	}

	/**
	 * Returns the custom unit of frequency.
	 *
	 * @return the custom unit
	 */
	public String getCustomUnit() {
		return customUnit;
	}

	/**
	 * Sets the custom unit of frequency.
	 *
	 * @param customUnit the custom unit to set
	 */
	public void setCustomUnit(String customUnit) {
		this.customUnit = customUnit;
	}

	/**
	 * Returns the client associated with the order.
	 *
	 * @return the client
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * Sets the client associated with the order.
	 *
	 * @param client the client to set
	 */
	public void setClient(Client client) {
		this.client = client;
	}
}
