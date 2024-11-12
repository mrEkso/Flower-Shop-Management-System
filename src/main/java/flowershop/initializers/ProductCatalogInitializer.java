package flowershop.initializers;

import flowershop.catalogs.ProductCatalog;
import flowershop.models.product.Bouquet;
import flowershop.models.product.Flower;
import flowershop.models.embedded.Pricing;
import org.javamoney.moneta.Money;
import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Arrays;
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
		if (productCatalog.findAll().iterator().hasNext()) {
			return; // Skip initialization if products already exist
		}

		// Creating specific flowers with Pricing and color
		Flower rose = new Flower("Rose", new Pricing(Money.of(10.0, EURO), Money.of(20.0, EURO)), "Red");
		Flower sunflower = new Flower("Sunflower", new Pricing(Money.of(2.5, EURO), Money.of(5.0, EURO)), "Yellow");
		Flower lily = new Flower("Lily", new Pricing(Money.of(9.0, EURO), Money.of(18.0, EURO)), "White");
		Flower lily2 = new Flower("Lily2", new Pricing(Money.of(10.0, EURO), Money.of(19.0, EURO)), "White");
		Flower lily3 = new Flower("Lily3", new Pricing(Money.of(11.0, EURO), Money.of(20.0, EURO)), "Purple");

		// Saving flowers to the catalog
		productCatalog.save(rose);
		productCatalog.save(sunflower);
		productCatalog.save(lily);
		productCatalog.save(lily2);
		productCatalog.save(lily3);

		// Creating a bouquet with a list of flowers and additional price
		List<Flower> bouquetFlowers = Arrays.asList(rose, lily);
		Bouquet roseLilyBouquet = new Bouquet(
			"Rose and Lily Bouquet",
			new Pricing(Money.of(15.0, EURO), Money.of(30.0, EURO)),
			bouquetFlowers,
			Money.of(5.0, EURO) // Additional price for the bouquet
		);

		// Saving bouquet to the catalog
		productCatalog.save(roseLilyBouquet);
	}
}