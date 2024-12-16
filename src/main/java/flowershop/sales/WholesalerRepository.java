//package flowershop.sales;
//
//import flowershop.product.Flower;
//import jakarta.validation.constraints.NotNull;
//import org.javamoney.moneta.Money;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.repository.CrudRepository;
//
//import java.util.UUID;
//
//import org.springframework.data.repository.CrudRepository;
//import org.springframework.data.repository.PagingAndSortingRepository;
//import org.springframework.data.jpa.repository.Query;
//
//
//import java.util.List;
//
//public interface WholesalerRepository extends CrudRepository<Flower, Long>, PagingAndSortingRepository<Flower, Long> {
//
//	/**
//	 * Find all flowers in the wholesaler's catalog with pagination support.
//	 *
//	 * @param pageable The pagination information.
//	 * @return A page of flowers.
//	 */
//	@NotNull
//	@Query("select f from Flower f")
//	Page<Flower> findAll(@NotNull Pageable pageable);
//
//	/**
//	 * Find all flowers by a specific color.
//	 *
//	 * @param color The color of the flowers to find.
//	 * @return A list of flowers matching the color.
//	 */
//	List<Flower> findByColorIgnoreCase(String color);
//
//	/**
//	 * Find all flowers by a specific name (case-insensitive).
//	 *
//	 * @param name The name of the flowers to find.
//	 * @return A list of flowers matching the name.
//	 */
//	List<Flower> findByNameIgnoreCase(String name);
//}
