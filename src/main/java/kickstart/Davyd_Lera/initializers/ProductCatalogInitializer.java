package kickstart.Davyd_Lera.initializers;

import kickstart.Davyd_Lera.models.embedded.Pricing;
import kickstart.Davyd_Lera.models.products.Bouquet;
import kickstart.Davyd_Lera.models.products.Flower;
import kickstart.Davyd_Lera.repositories.ProductCatalog;
import org.javamoney.moneta.Money;
import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

import static org.salespointframework.core.Currencies.EURO;

@Component
@Order(10)
public class ProductCatalogInitializer implements DataInitializer {

	private final ProductCatalog productCatalog;

	public ProductCatalogInitializer(ProductCatalog productCatalog) {
		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
		this.productCatalog = productCatalog;
	}

	@Override
	public void initialize() {
		if (productCatalog.findAll().iterator().hasNext()) return; // Skip initialization if products already exist

		// Creating specific flowers with Pricing and color
		Flower rose1 = new Flower("Rose", new Pricing(Money.of(10.0, EURO), Money.of(20.0, EURO)), "Red");
		Flower rose2 = new Flower("Rose", new Pricing(Money.of(10.0, EURO), Money.of(20.0, EURO)), "Red");
		Flower sunflower = new Flower("Sunflower", new Pricing(Money.of(2.5, EURO), Money.of(5.0, EURO)), "Yellow");
		Flower lily = new Flower("Lily", new Pricing(Money.of(9.0, EURO), Money.of(18.0, EURO)), "White");

		// Saving flowers to the catalog
		productCatalog.save(rose1);
		productCatalog.save(rose2);
		productCatalog.save(sunflower);
		productCatalog.save(lily);

		// Creating a bouquet with a list of flowers and additional price
		Bouquet roseLilyBouquet = new Bouquet(
			"Rose and Lily Bouquet",
			List.of(rose1, lily),
			Money.of(5.0, EURO) // Additional price for the bouquet
		);
		Bouquet roseBoquet = new Bouquet(
			"Rose Bouquet",
			List.of(rose2),
			Money.of(5.0, EURO) // Additional price for the bouquet
		);

		// Saving bouquet to the catalog
		productCatalog.save(roseLilyBouquet);
		productCatalog.save(roseBoquet);
	}
}