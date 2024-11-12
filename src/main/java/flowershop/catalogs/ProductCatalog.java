package flowershop.catalogs;

import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.Product;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;

// import flowershop.models.product.ProductType;

@Component
public interface ProductCatalog extends Catalog<Product> {

  //   static final Sort DEFAULT_SORT = Sort.sort(Product.class).by(Product::getId).descending();

  //   Streamable<Product> findByType(ProductType type, Sort sort);

  //   default Streamable<Product> findByType(ProductType type) {
	// 	return findByType(type, DEFAULT_SORT);
	// }
}
