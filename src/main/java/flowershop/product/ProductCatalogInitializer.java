package flowershop.product;

import org.javamoney.moneta.Money;
import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

import static org.salespointframework.core.Currencies.EURO;

@Component
@Order(15)
public class ProductCatalogInitializer implements DataInitializer {

	private final ProductCatalog productCatalog;
	private final ProductService productService;

	public ProductCatalogInitializer(ProductCatalog productCatalog, ProductService productService) {
		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
		this.productCatalog = productCatalog;
		this.productService = productService;
	}

	@Override
	public void initialize() {
		if (productCatalog.findAll().iterator().hasNext()) {
			return; // Skip initialization if products already exist
		}

		// Creating specific flowers with Pricing and color
		Flower rose = new Flower("Rose", new Pricing(Money.of(10.0, EURO), Money.of(20.0, EURO)), "Red", 50);
		Flower sunflower = new Flower("Sunflower", new Pricing(Money.of(2.5, EURO), Money.of(5.0, EURO)), "Yellow", 20);
		Flower lily = new Flower("Lily", new Pricing(Money.of(9.0, EURO), Money.of(18.0, EURO)), "White", 20);
		Flower lily2 = new Flower("Lily2", new Pricing(Money.of(10.0, EURO), Money.of(19.0, EURO)), "White", 30);
		Flower lily3 = new Flower("Lily3", new Pricing(Money.of(11.0, EURO), Money.of(20.0, EURO)), "Purple", 73);

		// Saving flowers to the catalog
		productCatalog.save(rose);
		productCatalog.save(sunflower);
		productCatalog.save(lily);
		productCatalog.save(lily2);
		productCatalog.save(lily3);

		// Creating a bouquet with a Map of flowers and their quantities
		Map<Flower, Integer> bouquetFlowersMap1 = new HashMap<>();
		bouquetFlowersMap1.put(rose, 3);  // 3 roses
		bouquetFlowersMap1.put(lily, 2);  // 2 lilies
		Bouquet roseLilyBouquet = new Bouquet(
			"Rose and Lily Bouquet",
			bouquetFlowersMap1,   // Pass the Map to the Bouquet constructor
			Money.of(5.0, EURO),   // Additional price for the bouquet
			5                      // Quantity of the bouquet
		);

		Map<Flower, Integer> bouquetFlowersMap2 = new HashMap<>();
		bouquetFlowersMap2.put(sunflower, 5);
		bouquetFlowersMap2.put(rose, 7);
		Bouquet roseLilyBouquet2 = new Bouquet(
			"Perfect Spring Bouquet",
			bouquetFlowersMap2,
			Money.of(8.0, EURO),
			2
		);

		productService.addBouquet(roseLilyBouquet);
		productService.addBouquet(roseLilyBouquet2);
		// Saving bouquets to the catalog
		productCatalog.save(roseLilyBouquet);
		productCatalog.save(roseLilyBouquet2);
	}

}