package flowershop.sales;

import org.salespointframework.catalog.Catalog;
import org.salespointframework.catalog.Product;
import org.springframework.stereotype.Component;

// FIXME: Use this in /buy page! (after prototype phase is over)
@Component
public interface WholesalerCatalog extends Catalog<Product> {
}

