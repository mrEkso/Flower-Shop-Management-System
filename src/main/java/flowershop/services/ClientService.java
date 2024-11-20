package flowershop.services;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientService {
	private final ClientRepository clientRepository;

	public ClientService(ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}

	public Client getOrCreateClient(String name, String phone) {
		Optional<Client> existingClient = clientRepository.findByPhone(phone);
		if (existingClient.isPresent()) return existingClient.get();
		Client newClient = new Client(name, phone);
		return clientRepository.save(newClient);
	}
}
