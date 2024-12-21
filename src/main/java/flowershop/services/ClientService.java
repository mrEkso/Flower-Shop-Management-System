package flowershop.services;

import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * The `ClientService` class provides services related to client management in the flower shop system.
 * It is annotated with `@Service` to indicate that it is a Spring service component.
 */
@Service
public class ClientService {
	private final ClientRepository clientRepository;

	/**
	 * Constructs a `ClientService` with the specified `ClientRepository`.
	 *
	 * @param clientRepository the repository used to manage client data
	 */
	public ClientService(ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}

	/**
	 * Retrieves an existing client by phone number or creates a new client if none exists.
	 *
	 * @param name  the name of the client
	 * @param phone the phone number of the client
	 * @return the existing or newly created client
	 */
	public Client getOrCreateClient(String name, String phone) {
		Optional<Client> existingClient = clientRepository.findByPhone(phone);
		if (existingClient.isPresent()) {
			if (!existingClient.get().getName().equals(name)) {
				existingClient.get().setName(name);
				clientRepository.save(existingClient.get());
			}
			return existingClient.get();
		}
		Client newClient = new Client(name, phone);
		return clientRepository.save(newClient);
	}
}
