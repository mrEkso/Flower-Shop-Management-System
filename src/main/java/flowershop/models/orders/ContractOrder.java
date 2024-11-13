
 package flowershop.models.orders;

 import jakarta.persistence.Entity;
 import flowershop.models.Client;
 import org.salespointframework.useraccount.UserAccount;
 
 import java.time.LocalDate;

/**
 * This type of order is intended for clients who receive regular flower deliveries over a
 * specified period, such as weekly or monthly deliveries.
 *
 * <p>Example use cases include corporate clients who require regular flower arrangements
 *  * for office spaces, or individual clients who want fresh flowers delivered to their homes
 *  * periodically.</p>
 */
 
 @Entity
 public class ContractOrder extends AbstractOrder {
 
	 private String frequency;
	 private LocalDate startDate;
	 private LocalDate endDate;
	 private String address;
 
	 public ContractOrder(String frequency, LocalDate startDate, LocalDate endDate, String address, UserAccount orderProcessingEmployee, Client client) {
		 super(orderProcessingEmployee, client);
		 this.frequency = frequency;
		 this.startDate = startDate;
		 this.endDate = endDate;
		 this.address = address;
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
 
	 public String getAddress() {
		 return address;
	 }
 
	 public void setAddress(String address) {
		 this.address = address;
	 }
 }
 