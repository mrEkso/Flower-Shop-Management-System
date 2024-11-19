package flowershop.product;

import org.salespointframework.catalog.Product;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

	private final ProductCatalog productCatalog;

	public ProductService(ProductCatalog productCatalog) {
		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
		this.productCatalog = productCatalog;
	}

	public Flower addFlowers(Flower flower, int quantity) {
		Flower existingFlower = (Flower) productCatalog.findById(flower.getId()).orElse(null);

		// If the flower exists, update its quantity
		if (existingFlower != null) {
			existingFlower.setQuantity(existingFlower.getQuantity() + quantity);
			return productCatalog.save(existingFlower);
		} else {
			// If the flower doesn't exist, save it as a new flower
			return productCatalog.save(flower);
		}
	}

	// Method to add a new flower
	public Flower addFlower(Flower flower) {
		return addFlowers(flower, flower.getQuantity());
	}

	public Bouquet addBouquet(Bouquet bouquet) {
		return addBouquets(bouquet, bouquet.getQuantity());
	}

	public Bouquet addBouquets(Bouquet bouquet, int quantity) {
		// Check if sufficient stock of each flower is available for the requested quantity of bouquets
		for (Flower flower : bouquet.getFlowers()) {
			Flower existingFlower = (Flower) productCatalog.findById(flower.getId()).orElse(null);

			if (existingFlower == null) {
				throw new IllegalStateException("Flower with id " + flower.getId() + " not found!");
			} else {
				// Calculate the total quantity required for the bouquets
				int totalRequired = flower.getQuantity() * quantity;
				if (existingFlower.getQuantity() < totalRequired) {
					throw new IllegalStateException(
						"Not enough stock for flower with id " + flower.getId() +
							". Required: " + totalRequired + ", Available: " + existingFlower.getQuantity()
					);
				}
			}
		}

		// Deduct the required quantities from the stock
		for (Flower flower : bouquet.getFlowers()) {
			Flower existingFlower = (Flower) productCatalog.findById(flower.getId()).orElse(null);
			int totalRequired = flower.getQuantity() * quantity;
			existingFlower.setQuantity(existingFlower.getQuantity() - totalRequired);
			productCatalog.save(existingFlower);
		}

		// Save the bouquet with the specified quantity
		bouquet.setQuantity(quantity);
		return productCatalog.save(bouquet);
	}

	public void removeFlowers(Flower flower, int quantity) {
		// Find the existing flower
		Flower existingFlower = (Flower) productCatalog.findById(flower.getId()).orElse(null);

		if (existingFlower == null) {
			throw new IllegalStateException("Flower not found in the catalog.");
		} else {
			// Calculate the new quantity
			int newQuantity = existingFlower.getQuantity() - quantity;

			if (newQuantity >= 0) {
				// Update the quantity if still positive or zero
				existingFlower.setQuantity(newQuantity);
				productCatalog.save(existingFlower);
			} else {
				throw new IllegalStateException("Insufficient stock to remove the specified quantity of flowers.");
			}
		}
	}


	public void removeBouquet(Bouquet bouquet) {
		// Find the existing bouquet in the catalog
		Bouquet existingBouquet = (Bouquet) productCatalog.findById(bouquet.getId()).orElse(null);

		if (existingBouquet != null) {
			// Calculate the new quantity
			int newQuantity = existingBouquet.getQuantity() - bouquet.getQuantity();

			if (newQuantity > 0) {
				// Update the quantity if it's still positive
				existingBouquet.setQuantity(newQuantity);
				productCatalog.save(existingBouquet);
			} else if (newQuantity == 0) {
				// Remove the bouquet completely if the quantity becomes zero
				productCatalog.delete(existingBouquet);
			} else {
				// Handle the case where there's an attempt to remove more than available
				throw new IllegalStateException("Insufficient stock to remove the specified quantity of bouquets.");
			}
		} else {
			// Handle the case where the bouquet doesn't exist in the catalog
			throw new IllegalStateException("Bouquet not found in the catalog.");
		}
	}


	public Iterable<Product> getAllProducts() {
		return productCatalog.findAll();
	}

	public List<Flower> getAllFlowers() {
		return productCatalog.findAll()
			.filter(product -> product instanceof Flower)
			.map(product -> (Flower) product)
			.toList();
	}

	public List<Bouquet> getAllBouquets() {
		return productCatalog.findAll()
			.filter(product -> product instanceof Bouquet)
			.map(product -> (Bouquet) product)
			.toList();
	}

	public Set<String> getAllFlowerColors() {
		return getAllFlowers()
			.stream()
			.map(Flower::getColor)
			.collect(Collectors.toSet());
	}

	public Product findByName(String productName) {
		return productCatalog.findAll()
			.filter(product -> product.getName().equalsIgnoreCase(productName))
			.stream()
			.findFirst()
			.orElse(null);
	}

	public List<Flower> findFlowersByName(String productName) {
		return productCatalog.findAll()
			.stream()
			.filter(product -> product instanceof Flower)
			.map(product -> (Flower) product)
			.filter(flower -> flower.getName().equalsIgnoreCase(productName))
			.collect(Collectors.toList());
	}

	public List<Flower> findFlowersByColor(String color) {
		return getAllFlowers()
			.stream()
			.filter(flower -> flower.getColor().equalsIgnoreCase(color))
			.toList();
	}

	public Optional<Product> getProductById(UUID id) {
		return productCatalog.findById(getProductId(id));
	}

	public void deleteProductById(UUID id) {
		productCatalog.findById(getProductId(id)).ifPresent(productCatalog::delete);
	}

	private Product.ProductIdentifier getProductId(UUID id) {
		return Product.ProductIdentifier.of(id.toString());
	}


}
