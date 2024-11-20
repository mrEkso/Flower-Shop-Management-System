package flowershop.inventory;

import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.ProductService;
import org.salespointframework.catalog.Product;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InventoryService {

	private final DeletedProductService deletedProductService;
	private final ProductService productService;

	public InventoryService(DeletedProductService deletedProductService, ProductService productService) {
		this.deletedProductService = deletedProductService;
		this.productService = productService;
	}

	public void deleteProduct(UUID productID, List<DeletedProduct> deletedProductsList) {

		Optional<Product> selectedProductOpt = productService.getProductById(productID);
		Product product = selectedProductOpt.get();

		if (product instanceof Bouquet bouquet) {
			productService.removeBouquet(bouquet, 1); // TODO: enable deleting more than 1 instance at once.
			DeletedProduct deleted = deletedProductService.deleteProduct(bouquet, 1);
			deletedProductsList.add(deleted);
		} else if (product instanceof Flower flower) {
			productService.removeFlowers(flower, 1); // TODO: enable deleting more than 1 instance at once.
			DeletedProduct deleted = deletedProductService.deleteProduct(flower, 1);
			deletedProductsList.add(deleted);
		} else {
			throw new RuntimeException("Unsupported Product type!");
		}
	}

	// Use this if needed:
	public boolean updateStock(UUID productID, int quantityChange) {
		for (Product product : productService.getAllProducts()) {
			var existingProduct = productService.getProductById(productID).get();
			if (!product.equals(existingProduct)) {
				return false;
			}

			if (existingProduct instanceof Bouquet bouquet) {
				int newQuantity = bouquet.getQuantity() + quantityChange;
				if (newQuantity >= 0) {
					bouquet.setQuantity(newQuantity);
					return true;
				}
			} else if (existingProduct instanceof Flower flower) {
				int newQuantity = flower.getQuantity() + quantityChange;
				if (newQuantity >= 0) {
					flower.setQuantity(newQuantity);
					return true;
				}
			} else {
				throw new IllegalArgumentException("Invalid product type");
			}

		}
		return false;
	}
}
