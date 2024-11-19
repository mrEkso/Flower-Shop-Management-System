package flowershop.services;

import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Order(5) // Устанавливаем порядок выполнения, если нужно, чтобы клиенты были добавлены раньше заказов
public class ClientInitializer implements DataInitializer {

	private final ClientRepository clientRepository;

	public ClientInitializer(ClientRepository clientRepository) {
		Assert.notNull(clientRepository, "ClientRepository must not be null!");
		this.clientRepository = clientRepository;
	}

	@Override
	public void initialize() {
		if (clientRepository.count() > 0) {
			return; // Return if clients were already initialized.
		}

		// Добавляем несколько клиентов
		clientRepository.save(new Client("John Doe", "+123456789"));
		clientRepository.save(new Client("Jane Smith", "+987654321"));
		clientRepository.save(new Client("Alice Johnson", "+555123456"));
	}
}
