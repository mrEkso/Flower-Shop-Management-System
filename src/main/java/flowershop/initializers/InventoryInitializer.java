package flowershop.initializers;

import flowershop.models.products.Bouquet;
import flowershop.models.products.Flower;
import flowershop.models.products.Product;
import flowershop.models.embedded.Pricing;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryInitializer {

	public List<Product> initializeProducts() {
		List<Product> products = new ArrayList<>();

		Pricing rosePricing = new Pricing(Money.of(1.50, "USD"), Money.of(2.00, "USD"));
		Pricing sunflowerPricing = new Pricing(Money.of(1.00, "USD"), Money.of(1.50, "USD"));
		Pricing tulipPricing = new Pricing(Money.of(2.00, "USD"), Money.of(2.50, "USD"));
		Pricing hibiscusPricing = new Pricing(Money.of(2.50, "USD"), Money.of(3.00, "USD"));
		Pricing daisyPricing = new Pricing(Money.of(0.80, "USD"), Money.of(1.20, "USD"));

		// Initialize flowers
		Flower rose = new Flower("Rose", rosePricing, "Red", 50);
		Flower sunflower = new Flower("Sunflower", sunflowerPricing, "Yellow", 30);
		Flower tulip = new Flower("Tulip", tulipPricing, "Pink", 40);
		Flower hibiscus = new Flower("Hibiscus", hibiscusPricing, "Orange", 20);
		Flower daisy = new Flower("Daisy", daisyPricing, "White", 60);

		products.add(new Product(rose));
		products.add(new Product(sunflower));
		products.add(new Product(tulip));
		products.add(new Product(hibiscus));
		products.add(new Product(daisy));

		// Initialize bouquets
		List<Flower> springFlowers = List.of(
			new Flower("Tulip", tulipPricing, "Pink", 5),
			new Flower("Daisy", daisyPricing, "White", 5)
		);
		Bouquet springBouquet = new Bouquet("Spring Bouquet", springFlowers, Money.of(5.00, "USD"));

		List<Flower> romanticFlowers = List.of(
			new Flower("Rose", rosePricing, "Red", 10),
			new Flower("Tulip", tulipPricing, "Pink", 5)
		);
		Bouquet romanticBouquet = new Bouquet("Romantic Bouquet", romanticFlowers, Money.of(7.00, "USD"));

		products.add(new Product(springBouquet));
		products.add(new Product(romanticBouquet));

		return products;
	}
}
