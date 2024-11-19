package flowershop.sales;

import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.Pricing;
import flowershop.product.ProductCatalog;
import org.javamoney.moneta.Money;
import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

import static org.salespointframework.core.Currencies.EURO;

@Component
@Order(16)
public class WholesalerCatalogInitializer implements DataInitializer {
	private final WholesalerRepository wholesalerRepository;

	public WholesalerCatalogInitializer(WholesalerRepository wholesalerRepository) {
		Assert.notNull(wholesalerRepository, "wholesalerRepository must not be null!");
		this.wholesalerRepository = wholesalerRepository;
	}

	@Override
	public void initialize() {
//		if (wholesalerCatalog.findAll().iterator().hasNext()) {
//			return; // Skip initialization if products already exist
//		}

		// Creating specific flowers
//		Flower rose = new Flower("Rose", new Pricing(Money.of(10.0, EURO), Money.of(20.0, EURO)), "Red", 10);
//		Flower sunflower = new Flower("Sunflower", new Pricing(Money.of(2.5, EURO), Money.of(5.0, EURO)), "Yellow", 20);
//		Flower lily = new Flower("Lily", new Pricing(Money.of(9.0, EURO), Money.of(18.0, EURO)), "White", 11);
//		Flower orchid = new Flower("Orchid", new Pricing(Money.of(15.0, EURO), Money.of(30.0, EURO)), "Pink", 5);
//		Flower tulip = new Flower("Tulip", new Pricing(Money.of(3.5, EURO), Money.of(7.0, EURO)), "Orange", 25);
//		Flower daffodil = new Flower("Daffodil", new Pricing(Money.of(4.0, EURO), Money.of(8.0, EURO)), "Yellow", 15);
//		Flower marigold = new Flower("Marigold", new Pricing(Money.of(2.0, EURO), Money.of(4.0, EURO)), "Gold", 30);
		Flower bluebell = new Flower("Bluebell", new Pricing(Money.of(5.0, EURO), Money.of(10.0, EURO)), "Blue", 12);
		Flower hibiscus = new Flower("Hibiscus", new Pricing(Money.of(7.0, EURO), Money.of(14.0, EURO)), "Crimson", 8);
		Flower lavender = new Flower("Lavender", new Pricing(Money.of(6.0, EURO), Money.of(12.0, EURO)), "Purple", 18);
		Flower camellia = new Flower("Camellia", new Pricing(Money.of(8.0, EURO), Money.of(16.0, EURO)), "Pink", 10);
		Flower jasmine = new Flower("Jasmine", new Pricing(Money.of(3.0, EURO), Money.of(6.0, EURO)), "White", 40);
		Flower peony = new Flower("Peony", new Pricing(Money.of(12.0, EURO), Money.of(24.0, EURO)), "Red", 7);
		Flower dahlia = new Flower("Dahlia", new Pricing(Money.of(10.0, EURO), Money.of(20.0, EURO)), "Burgundy", 13);
		Flower gladiolus = new Flower("Gladiolus", new Pricing(Money.of(6.5, EURO), Money.of(13.0, EURO)), "Orange", 9);

		// Saving all of them
//		wholesalerRepository.save(rose);
//		wholesalerRepository.save(sunflower);
//		wholesalerRepository.save(lily);
//		wholesalerRepository.save(orchid);
//		wholesalerRepository.save(tulip);
//		wholesalerRepository.save(daffodil);
//		wholesalerRepository.save(marigold);
		wholesalerRepository.save(bluebell);
		wholesalerRepository.save(hibiscus);
		wholesalerRepository.save(lavender);
		wholesalerRepository.save(camellia);
		wholesalerRepository.save(jasmine);
		wholesalerRepository.save(peony);
		wholesalerRepository.save(dahlia);
		wholesalerRepository.save(gladiolus);
	}
}
