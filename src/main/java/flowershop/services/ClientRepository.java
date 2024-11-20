package flowershop.services;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ClientRepository extends CrudRepository<Client, Long> {
	Optional<Client> findByName(String name);

	Optional<Client> findByPhone(String phone);
}