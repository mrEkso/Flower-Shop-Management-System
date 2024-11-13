package flowershop.catalogs;


import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.Product;
import org.springframework.stereotype.Component;

// Maybe we don't need this. I don't know.
@Component
public interface WholesalerCatalog extends Catalog<Product> {
}

