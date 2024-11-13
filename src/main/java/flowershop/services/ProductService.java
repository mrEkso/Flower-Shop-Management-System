package flowershop.services;

import flowershop.catalogs.ProductCatalog;
import flowershop.models.product.Bouquet;
import flowershop.models.product.Flower;
import flowershop.models.embedded.Pricing;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;

@Service
public class ProductService {

	private final ProductCatalog productCatalog;

	public ProductService(ProductCatalog productCatalog) {
		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
		this.productCatalog = productCatalog;
	}

	// Method to add a new flower
	public Flower addFlower(String name, Pricing pricing, String color, Integer quantity) {
		Flower flower = new Flower(name, pricing, color, quantity);
		return productCatalog.save(flower);
	}

	// Method to add a new bouquet
	public Bouquet addBouquet(String name, Pricing pricing, List<Flower> flowers, Money additionalPrice) {
		Bouquet bouquet = new Bouquet(name, pricing, flowers, additionalPrice);

		// If flowers are used to create a bouquet, these have to become unavailable (i.e. deleted)
		productCatalog.deleteAll(flowers);

		return productCatalog.save(bouquet);
	}

	public Iterable<Product> getAllProducts() {
		return productCatalog.findAll();
	}

	public Optional<Product> getProductById(Long id) {
		return productCatalog.findById(getProductId(id));
	}

	public void deleteProductById(Long id) {
		productCatalog.findById(getProductId(id)).ifPresent(productCatalog::delete);
	}

	private Product.ProductIdentifier getProductId(Long id) {
		return Product.ProductIdentifier.of(id.toString());
	}
}
