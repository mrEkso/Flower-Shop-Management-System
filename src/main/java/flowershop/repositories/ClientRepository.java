package flowershop.repositories;

import flowershop.models.Client;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface ClientRepository extends CrudRepository<Client, Long> {
	Optional<Client> findByName(String name);
}