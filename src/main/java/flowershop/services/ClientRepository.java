package flowershop.services;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * The `ClientRepository` interface provides CRUD operations for `Client` entities.
 * It extends the `CrudRepository` interface provided by Spring Data.
 */
public interface ClientRepository extends CrudRepository<Client, Long> {
	/**
	 * Finds a client by their name.
	 *
	 * @param name the name of the client
	 * @return an `Optional` containing the client if found, or empty if not found
	 */
	Optional<Client> findByName(String name);

	/**
	 * Finds a client by their phone number.
	 *
	 * @param phone the phone number of the client
	 * @return an `Optional` containing the client if found, or empty if not found
	 */
	Optional<Client> findByPhone(String phone);
}