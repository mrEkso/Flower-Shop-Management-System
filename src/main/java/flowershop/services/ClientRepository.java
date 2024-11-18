package flowershop.services;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface ClientRepository extends CrudRepository<Client, Long> {
	Optional<Client> findByName(String name);

	Optional<Client> findByPhone(String phone);
}