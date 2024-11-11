package kickstart.Davyd_Lera.controllers;

import kickstart.Davyd_Lera.models.products.Bouquet;
import kickstart.Davyd_Lera.models.products.Flower;
import kickstart.Davyd_Lera.services.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
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

	// Endpoint to add a new flower
	@PostMapping("/flowers")
	public ResponseEntity<Flower> addFlower(@RequestBody Flower flower) {
		Flower savedFlower = productService.addFlower(flower);
		return new ResponseEntity<>(savedFlower, HttpStatus.CREATED);
	}

	// Endpoint to add a new bouquet
	@PostMapping("/bouquets")
	public ResponseEntity<Bouquet> addBouquet(@RequestBody Bouquet bouquet) {
		Bouquet savedBouquet = productService.addBouquet(bouquet);
		return new ResponseEntity<>(savedBouquet, HttpStatus.CREATED);
	}

	// Endpoint to delete a product by ID
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProductById(@PathVariable Long id) {
		productService.deleteProductById(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
