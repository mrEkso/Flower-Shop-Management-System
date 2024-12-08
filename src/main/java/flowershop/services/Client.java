package flowershop.services;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * The `Client` class represents a client entity in the flower shop system.
 * It is annotated with `@Entity` to indicate that it is a JPA entity.
 */
@Entity
public class Client {

	@Id
	@GeneratedValue
	private Long id;

	private String name;
	@Column(unique = true)
	private String phone;

	/**
	 * Constructs a `Client` with the specified id, name, and phone.
	 *
	 * @param id    the unique identifier of the client
	 * @param name  the name of the client
	 * @param phone the phone number of the client
	 */
	public Client(Long id, String name, String phone) {
		this.id = id;
		this.name = name;
		this.phone = phone;
	}

	/**
	 * Constructs a `Client` with the specified name and phone.
	 *
	 * @param name  the name of the client
	 * @param phone the phone number of the client
	 */
	public Client(String name, String phone) {
		this.name = name;
		this.phone = phone;
	}

	/**
	 * Default constructor for `Client`.
	 * This constructor is primarily used by JPA and other frameworks.
	 */
	public Client() {
	}

	/**
	 * Returns the unique identifier of the client.
	 *
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Returns the name of the client.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the client.
	 *
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the phone number of the client.
	 *
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * Sets the phone number of the client.
	 *
	 * @param phone the phone number to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}
}
