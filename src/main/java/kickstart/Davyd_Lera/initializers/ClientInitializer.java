package kickstart.Davyd_Lera.initializers;

import kickstart.Davyd_Lera.repositories.ClientRepository;
import kickstart.Davyd_Lera.models.Client;
import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Order(5)
public class ClientInitializer implements DataInitializer {

	private final ClientRepository clientRepository;

	public ClientInitializer(ClientRepository clientRepository) {
		Assert.notNull(clientRepository, "ClientRepository must not be null!");
		this.clientRepository = clientRepository;
	}

	@Override
	public void initialize() {
		if (clientRepository.count() > 0) return;

		clientRepository.save(new Client("John Doe", "123 Main St", "+123456789"));
		clientRepository.save(new Client("Jane Smith", "456 Elm St", "+987654321"));
		clientRepository.save(new Client("Alice Johnson", "789 Oak St", "+555123456"));
	}
}
