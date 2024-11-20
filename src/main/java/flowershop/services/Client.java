package flowershop.services;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Client {

	@Id
	@GeneratedValue
	private Long id;

	private String name;
	private String phone;

	public Client(Long id, String name, String phone) {
		this.id = id;
		this.name = name;
		this.phone = phone;
	}

	public Client(String name, String phone) {
		this.name = name;
		this.phone = phone;
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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
}
