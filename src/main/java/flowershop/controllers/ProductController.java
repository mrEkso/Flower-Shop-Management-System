package flowershop.controllers;

import flowershop.models.product.Bouquet;
import flowershop.models.product.Flower;
import flowershop.models.embedded.Pricing;
import flowershop.services.ProductService;
import org.javamoney.moneta.Money;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.salespointframework.core.Currencies.EURO;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	// Endpoint to add a new flower
	@PostMapping("/flowers")
	public ResponseEntity<Flower> addFlower(@RequestParam String name,
											@RequestParam double buyPrice,
											@RequestParam double sellPrice,
											@RequestParam String color) {
		Pricing pricing = new Pricing(Money.of(buyPrice, EURO), Money.of(sellPrice, EURO));
		Flower flower = productService.addFlower(name, pricing, color);
		return new ResponseEntity<>(flower, HttpStatus.CREATED);
	}

	// Endpoint to add a new bouquet
	@PostMapping("/bouquets")
	public ResponseEntity<Bouquet> addBouquet(@RequestParam String name,
											  @RequestParam double buyPrice,
											  @RequestParam double sellPrice,
											  @RequestParam List<Flower> flowers,
											  @RequestParam double additionalPrice) {
		Pricing pricing = new Pricing(Money.of(buyPrice, EURO), Money.of(sellPrice, EURO));
		Bouquet bouquet = productService.addBouquet(name, pricing, flowers, Money.of(additionalPrice, EURO));
		return new ResponseEntity<>(bouquet, HttpStatus.CREATED);
	}

	// Endpoint to get all products
	@GetMapping
	public ResponseEntity<Iterable<?>> getAllProducts() {
		return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.OK);
	}

	// Endpoint to get a product by ID
	@GetMapping("/{id}")
	public ResponseEntity<?> getProductById(@PathVariable Long id) {
		Optional<?> product = productService.getProductById(id);
		return product.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
			.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Endpoint to delete a product by ID
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProductById(@PathVariable Long id) {
		productService.deleteProductById(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
