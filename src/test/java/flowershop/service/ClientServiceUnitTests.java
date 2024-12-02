package flowershop.service;

import flowershop.services.Client;
import flowershop.services.ClientRepository;
import flowershop.services.ClientService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ClientService}.
 */

public class ClientServiceUnitTests {
	@Test
	void retrievesExistingClientOrCreatesANewOne() {

		ClientRepository clientRepository = mock(ClientRepository.class);
		when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));

		ClientService clientService = new ClientService(clientRepository);

		String existingPhone = "123456789";
		Client existingClient = new Client("Alice", existingPhone);
		when(clientRepository.findByPhone(existingPhone)).thenReturn(Optional.of(existingClient));

		String newPhone = "987654321";
		when(clientRepository.findByPhone(newPhone)).thenReturn(Optional.empty());

		Client retrievedClient = clientService.getOrCreateClient("Alice", existingPhone);

		Client newClient = clientService.getOrCreateClient("Bob", newPhone);

		verify(clientRepository, times(1)).findByPhone(existingPhone);
		assertThat(retrievedClient).isEqualTo(existingClient);

		verify(clientRepository, times(1)).findByPhone(newPhone);
		verify(clientRepository, times(1)).save(any(Client.class));
		assertThat(newClient).isNotNull();
		assertThat(newClient.getName()).isEqualTo("Bob");
		assertThat(newClient.getPhone()).isEqualTo(newPhone);
	}
}
