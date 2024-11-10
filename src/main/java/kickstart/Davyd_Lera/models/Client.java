package kickstart.Davyd_Lera.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Client {

	@Id
	@GeneratedValue
	private Long id;

	private String name;
	private String address;
	private String contactPhoneNumber;

	public Client(Long id, String name, String address, String contactPhoneNumber) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.contactPhoneNumber = contactPhoneNumber;
	}

	public Client(String name, String address, String contactPhoneNumber) {
		this.name = name;
		this.address = address;
		this.contactPhoneNumber = contactPhoneNumber;
	}

	public Client() {
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getContactPhoneNumber() {
		return contactPhoneNumber;
	}

	public void setContactPhoneNumber(String contactPhoneNumber) {
		this.contactPhoneNumber = contactPhoneNumber;
	}
}
