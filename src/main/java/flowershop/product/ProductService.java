package flowershop.product;

import flowershop.inventory.DeletedProduct;
import org.jetbrains.annotations.NotNull;
import org.salespointframework.catalog.Product;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class ProductService {

	public final List<DeletedProduct> deletedProducts = new ArrayList<>();

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

	public Bouquet addBouquet(@NotNull Bouquet bouquet) throws IllegalStateException {
		var bouquetQuantity = bouquet.getQuantity();

		for (int n = 0; n < bouquetQuantity; n++) {
			// remove for every single bouquet
			for (Map.Entry<Flower, Integer> entry : bouquet.getFlowers().entrySet()) {
				Flower flower = entry.getKey();
				Integer quant = entry.getValue();

				removeFlowers(flower, quant);
			}
		}

		bouquet.setQuantity(bouquetQuantity);
		return productCatalog.save(bouquet);
	}

	public void removeFlowers(@NotNull Flower flower, int quantity) throws IllegalStateException {
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
				throw new IllegalStateException(
					"Insufficient stock for flower: " + existingFlower.getName() +
						" [ID: " + existingFlower.getId() + "]. Required: " + quantity +
						", Available: " + existingFlower.getQuantity());
			}
		}
	}


	public void removeBouquet(Bouquet bouquet, int quantity) throws IllegalStateException {
		Bouquet existingBouquet = (Bouquet) productCatalog.findById(bouquet.getId()).orElse(null);

		if (existingBouquet == null) {
			throw new IllegalStateException("Bouquet not found in the catalog.");
		} else {
			int newQuantity = existingBouquet.getQuantity() - quantity;

			if (newQuantity >= 0) {
				// Update the quantity if it's still positive
				existingBouquet.setQuantity(newQuantity);
				productCatalog.save(existingBouquet);
//			} else if (newQuantity == 0) {
//				// Remove the bouquet completely if the quantity becomes zero
//				//productCatalog.delete(existingBouquet); 	// <----------- This will cause database errors
			} else {
				// Handle the case where there's an attempt to remove more than available
				throw new IllegalStateException("Insufficient stock to remove the specified quantity of bouquets.");
			}
		}
	}


	public List<Product> getAllProducts() {
		return productCatalog.findAll().toList();
	}

	public List<Flower> findAllFlowers() {
		return productCatalog.findAll()
			.filter(product -> product instanceof Flower)
			.map(product -> (Flower) product)
			.toList();
	}

	public List<Bouquet> findAllBouquets() {
		return productCatalog.findAll()
			.filter(product -> product instanceof Bouquet)
			.map(product -> (Bouquet) product)
			.toList();
	}

	public Set<String> getAllFlowerColors() {
		return findAllFlowers()
			.stream()
			.map(Flower::getColor)
			.collect(Collectors.toSet());
	}


	public List<Bouquet> findBouquetsByName(String subString) {
		return productCatalog.findAll()
			.stream()
			.filter(product -> product instanceof Bouquet)
			.map(product -> (Bouquet) product)
			.filter(bouquet -> bouquet.getName().toLowerCase().contains(subString.toLowerCase()))
			.collect(Collectors.toList());
	}

	public List<Flower> findFlowersByName(String subString) {
		return productCatalog.findAll()
			.stream()
			.filter(product -> product instanceof Flower)
			.map(product -> (Flower) product)
			.filter(flower -> flower.getName().toLowerCase().contains(subString.toLowerCase()))
			.collect(Collectors.toList());
	}

	public List<Flower> findFlowersByColor(String color, List<Flower> givenFlowers) {
		return givenFlowers
			.stream()
			.filter(flower -> flower.getColor().equalsIgnoreCase(color))
			.toList();
	}

	public Optional<Product> getProductById(UUID id) {
		return productCatalog.findById(getProductId(id));
	}

	public Optional<Product> getProductById(Product.ProductIdentifier id) {
		return productCatalog.findById(id);
	}

	public List<Product> findProductsByName(String name) {
		return productCatalog.findAll()
			.stream()
			.filter(product -> product.getName().toLowerCase().contains(name.toLowerCase()))
			.collect(Collectors.toList());
	}

	public Optional<Flower> getFlowerById(UUID id) {
		return getProductById(id)
			.filter(product -> product instanceof Flower) // Ensure the product is a Flower
			.map(product -> (Flower) product); // Safely cast to Flower
	}


	public void deleteProductById(UUID id) {
		productCatalog.findById(getProductId(id)).ifPresent(productCatalog::delete);
	}

	private Product.ProductIdentifier getProductId(UUID id) {
		return Product.ProductIdentifier.of(id.toString());
	}

	/**
	 * @param flowers
	 * @return Returns all flowers that have Quantity > 0, i.e. that are in stock.
	 */
	public List<Flower> filterFlowersInStock(List<Flower> flowers) {
		return flowers.stream()
			.filter(flower -> flower.getQuantity() > 0)
			.collect(toList());
	}
	/**
	 * @param bouquets
	 * @return Returns all bouquets that have Quantity > 0, i.e. that are in stock.
	 */
	public List<Bouquet> filterBouquetsInStock(List<Bouquet> bouquets) {
		return bouquets.stream()
			.filter(bouquet -> bouquet.getQuantity() > 0)
			.collect(toList());
	}

	public List<DeletedProduct> getDeletedProducts(){
		return deletedProducts;
	}

	public void addDeletedProduct(DeletedProduct deletedProduct){
		deletedProducts.add(deletedProduct);
	}

	public void addDeliveredFlowersFromWholesaler(Map<Flower, Integer> flowersBought) {
		for (Map.Entry<Flower, Integer> flowerBought : flowersBought.entrySet()) {
			this.addFlowers(flowerBought.getKey(), flowerBought.getValue());
		}
	}

}
