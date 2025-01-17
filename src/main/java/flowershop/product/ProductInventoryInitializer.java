package flowershop.product;

import org.javamoney.moneta.Money;
import org.salespointframework.core.DataInitializer;
import org.salespointframework.inventory.UniqueInventory;
import org.salespointframework.inventory.UniqueInventoryItem;
import org.salespointframework.quantity.Quantity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

import static org.salespointframework.core.Currencies.EURO;

@Component
@Order(15)
public class ProductInventoryInitializer implements DataInitializer {

	private final UniqueInventory<UniqueInventoryItem> inventory;
	private final ProductCatalog productCatalog;
	private final ProductService productService;
	private final GiftCardRepository giftCardRepository;

	public ProductInventoryInitializer(UniqueInventory<UniqueInventoryItem> inventory,
									   ProductCatalog productCatalog, ProductService productService, GiftCardRepository giftCardRepository) {
		Assert.notNull(inventory, "Inventory must not be null!");
		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
		Assert.notNull(productService, "ProductService must not be null!");
		this.inventory = inventory;
		this.productCatalog = productCatalog;
		this.productService = productService;
		this.giftCardRepository = giftCardRepository;
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
		Flower bluebell = new Flower("Bluebell", new Pricing(Money.of(5.0, EURO), Money.of(10.0, EURO)), "Blue", 1);
		Flower hibiscus = new Flower("Hibiscus", new Pricing(Money.of(7.0, EURO), Money.of(14.0, EURO)), "Crimson", 15);
		Flower lavender = new Flower("Lavender", new Pricing(Money.of(6.0, EURO), Money.of(12.0, EURO)), "Purple", 20);

		// All of those should be out of stock:
		Flower camellia = new Flower("Camellia", new Pricing(Money.of(8.0, EURO), Money.of(16.0, EURO)), "Pink", 0);
		Flower jasmine = new Flower("Jasmine", new Pricing(Money.of(3.0, EURO), Money.of(6.0, EURO)), "White", 0);
		Flower peony = new Flower("Peony", new Pricing(Money.of(12.0, EURO), Money.of(24.0, EURO)), "Red", 0);
		Flower dahlia = new Flower("Dahlia", new Pricing(Money.of(10.0, EURO), Money.of(20.0, EURO)), "Burgundy", 0);
		Flower gladiolus = new Flower("Gladiolus", new Pricing(Money.of(6.5, EURO), Money.of(13.0, EURO)), "Orange", 0);

		// Pre-defined Gift Cards
		GiftCard giftCard1 = new GiftCard(Money.of(10, "EUR"), "20");
		GiftCard giftCard2 = new GiftCard(Money.of(20, "EUR"), "20");

		productCatalog.save(rose);
		productCatalog.save(sunflower);
		productCatalog.save(lily);
		productCatalog.save(bluebell);
		productCatalog.save(hibiscus);
		productCatalog.save(lavender);

		productCatalog.save(camellia);
		productCatalog.save(jasmine);
		productCatalog.save(peony);
		productCatalog.save(dahlia);
		productCatalog.save(gladiolus);

		giftCardRepository.save(giftCard1);
		giftCardRepository.save(giftCard2);

		Map<Flower, Integer> bouquetFlowersMap1 = new HashMap<>();
		bouquetFlowersMap1.put(rose, 3);
		bouquetFlowersMap1.put(lily, 2);
		Bouquet roseLilyBouquet = new Bouquet(
			"Rose and Lily Bouquet",
			bouquetFlowersMap1,
			Money.of(5.0, EURO),
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

		productCatalog.save(roseLilyBouquet);
		productCatalog.save(roseLilyBouquet2);

		// Initialize inventory with 10 items of each product
		productCatalog.findAll().forEach(flower -> {
			int q = flower instanceof Flower ?
				((Flower) flower).getQuantity()
				: ((Bouquet) flower).getQuantity();
			if (inventory.findByProduct(flower).isEmpty()) {
				inventory.save(new UniqueInventoryItem(flower, Quantity.of(q)));
			}
		});
	}
}