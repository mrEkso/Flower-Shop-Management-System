package kickstart.Davyd_Lera.catalogs;

import kickstart.Davyd_Lera.models.Client;
import org.salespointframework.inventory.Inventory;
import org.springframework.data.repository.CrudRepository;

public class ClientCatalog extends CrudRepository<Client, CustomerIdentifier> {
}
