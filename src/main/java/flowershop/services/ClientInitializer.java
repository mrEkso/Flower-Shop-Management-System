package flowershop.services;

import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * The `ClientInitializer` class is responsible for initializing the client data in the flower shop system.
 * It implements the `DataInitializer` interface and is annotated with `@Component` and `@Order(5)`.
 */
@Component
@Order(5)
public class ClientInitializer implements DataInitializer {

	private final ClientRepository clientRepository;

	/**
	 * Constructs a `ClientInitializer` with the specified `ClientRepository`.
	 *
	 * @param clientRepository the repository used to manage client data
	 * @throws IllegalArgumentException if the `clientRepository` is null
	 */
	public ClientInitializer(ClientRepository clientRepository) {
		Assert.notNull(clientRepository, "ClientRepository must not be null!");
		this.clientRepository = clientRepository;
	}

	/**
	 * Initializes the client data.
	 * If the client repository already contains data, the method returns without making any changes.
	 * Otherwise, it saves a predefined list of clients to the repository.
	 */
	@Override
	public void initialize() {
		if (clientRepository.count() > 0) {
			return; // Return if clients were already initialized.
		}
		clientRepository.save(new Client("John Doe", "+491854581212"));
		clientRepository.save(new Client("Jane Smith", "+491854581213"));
		clientRepository.save(new Client("Alice Johnson", "+491854581214"));
	}
}
