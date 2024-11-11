package kickstart.Davyd_Lera.repositories;

import kickstart.Davyd_Lera.models.Client;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ClientRepository extends CrudRepository<Client, Long> {
	Optional<Client> findByName(String name);
}
