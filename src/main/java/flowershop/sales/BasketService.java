package flowershop.sales;

import flowershop.product.ProductCatalog;
import flowershop.product.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BasketService {
	private final ProductService productService;

	public BasketService(ProductService productService) {
		this.productService = productService;
	}

	public boolean getIsLastItem(List<BasketItem> basket, UUID productID) {
		var existingProductOptional = productService.getProductById(productID);
		var existingProduct = existingProductOptional.get();

		return basket.stream()
			.filter(b -> b.getProduct().equals(existingProduct))
			.findFirst()
			.get()
			.tryDecreaseQuantity();
	}

	public void increaseQuantity(List<BasketItem> basket, UUID productID) {
		var existingFlowerOptional = productService.getProductById(productID);
		var existingFlower = existingFlowerOptional.get();

		basket.stream()
			.filter(b -> b.getProduct().equals((existingFlower)))
			.findFirst()
			.get()
			.increaseQuantity();
	}

	public void decreaseQuantity(List<BasketItem> basket, UUID productID) {
		var existingProductOptional = productService.getProductById(productID);
		var existingProduct = existingProductOptional.get();

		boolean isLast = getIsLastItem(basket, productID);
		if (!isLast)
			removeFromBasket(basket, productID);
	}

	public void removeFromBasket(List<BasketItem> basket, UUID productID) {
		var existingProductOptional = productService.getProductById(productID);
		var existingProduct = existingProductOptional.get();

		basket.removeIf(b -> b.getProduct().equals(existingProduct));
	}

	public void addToBasket(List<BasketItem> basket, UUID productID) {
		var existingProductOptional = productService.getProductById(productID);

		if (existingProductOptional.isPresent()) {
			Optional<BasketItem> basketItem = basket.stream()
				.filter(item -> item.getProduct().equals(existingProductOptional.get()))
				.findFirst();

			if (basketItem.isPresent()) {
				basketItem.get().increaseQuantity();
			} else {
				basket.add(new BasketItem(existingProductOptional.get(), 1));
			}
		}
	}
}
