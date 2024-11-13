package flowershop.services;

import flowershop.catalogs.ProductCatalog;
import flowershop.models.product.Bouquet;
import flowershop.models.product.Flower;
import flowershop.models.embedded.Pricing;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

	private final ProductCatalog productCatalog;

	public ProductService(ProductCatalog productCatalog) {
		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
		this.productCatalog = productCatalog;
	}

	// Method to add a new flower
	public Flower addFlower(String name, Pricing pricing, String color) {
		Flower flower = new Flower(name, pricing, color);
		return productCatalog.save(flower);
	}

	// Method to add a new bouquet
	public Bouquet addBouquet(String name, Pricing pricing, List<Flower> flowers, Money additionalPrice) {
		Bouquet bouquet = new Bouquet(name, pricing, flowers, additionalPrice);
		return productCatalog.save(bouquet);
	}

	// Method to find all products
	public Iterable<Product> getAllProducts() {
		return productCatalog.findAll();
	}

	// Method to find a product by ID
	public Optional<Product> getProductById(Long id) {
		return productCatalog.findById(getProductId(id));
	}

	// Method to delete a product by ID
	public void deleteProductById(Long id) {
		productCatalog.findById(getProductId(id)).ifPresent(productCatalog::delete);
	}

	private Product.ProductIdentifier getProductId(Long id) {
		return Product.ProductIdentifier.of(id.toString());
	}
}
