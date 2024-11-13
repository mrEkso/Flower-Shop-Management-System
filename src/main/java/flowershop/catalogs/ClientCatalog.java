//package flowershop.catalogs;
//
//import flowershop.models.Client;
//import org.salespointframework.inventory.Inventory;
//import org.springframework.data.repository.CrudRepository;
//
//import java.util.Optional;
//
//public class ClientCatalog extends CrudRepository<Client, CustomerIdentifier> {
//	@Override
//	public <S extends Client> S save(S entity) {
//		return null;
//	}
//
//	@Override
//	public <S extends Client> Iterable<S> saveAll(Iterable<S> entities) {
//		return null;
//	}
//
//	@Override
//	public Optional<Client> findById(CustomerIdentifier customerIdentifier) {
//		return Optional.empty();
//	}
//
//	@Override
//	public boolean existsById(CustomerIdentifier customerIdentifier) {
//		return false;
//	}
//
//	@Override
//	public Iterable<Client> findAll() {
//		return null;
//	}
//
//	@Override
//	public Iterable<Client> findAllById(Iterable<CustomerIdentifier> customerIdentifiers) {
//		return null;
//	}
//
//	@Override
//	public long count() {
//		return 0;
//	}
//
//	@Override
//	public void deleteById(CustomerIdentifier customerIdentifier) {
//
//	}
//
//	@Override
//	public void delete(Client entity) {
//
//	}
//
//	@Override
//	public void deleteAllById(Iterable<? extends CustomerIdentifier> customerIdentifiers) {
//
//	}
//
//	@Override
//	public void deleteAll(Iterable<? extends Client> entities) {
//
//	}
//
//	@Override
//	public void deleteAll() {
//
//	}
//}
