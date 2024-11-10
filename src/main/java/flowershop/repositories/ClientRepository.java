package flowershop.repositories;

import flowershop.models.Client;
import org.springframework.data.repository.CrudRepository;

public interface ClientRepository extends CrudRepository<Client, Long> {
	// Hier kannst du zusätzliche benutzerdefinierte Abfragen definieren, wenn nötig.
}