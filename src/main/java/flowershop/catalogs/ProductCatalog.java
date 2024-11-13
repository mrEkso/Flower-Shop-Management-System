package flowershop.catalogs;

import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.Product;
import org.springframework.stereotype.Component;

@Component
public interface ProductCatalog extends Catalog<Product> {
}
