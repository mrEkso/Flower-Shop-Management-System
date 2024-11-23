package flowershop.inventory;

import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.Pricing;
import flowershop.product.ProductService;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.*;
import java.util.stream.Collectors;

@Controller
public class InventoryController {

	private final List<DeletedProduct> deletedProducts = new ArrayList<>();
	private final List<Flower> selectedFlowersForBouquet = new ArrayList<>();

	private final ProductService productService;
	private List<Product> products;

	// Initialize products using the service
	public InventoryController(ProductService productService) {
		this.productService = productService;
	}

	private Optional<Product> findProductByName(String name) {
		return products.stream()
			.filter(product -> product.getName().equalsIgnoreCase(name))
			.findFirst();
	}

	private boolean updateStock(String productName, int quantityChange) {
		for (Product product : products) {
			if (product.getName().equalsIgnoreCase(productName)) {
				int newQuantity = getQuantity(product) + quantityChange;
				if (newQuantity >= 0) {
					if (product instanceof Flower){
						((Flower) product).setQuantity(newQuantity);
					}
					if (product instanceof Bouquet){
						((Bouquet) product).setQuantity(newQuantity);
					}
					return true;
				}
				break;
			}
		}
		return false;
	}

	@GetMapping("/inventory")
	public String inventoryMode(@RequestParam(required = false) String search,
								@RequestParam(required = false, defaultValue = "all") String filter,
								Model model) {
		this.products = productService.getAllProducts();
		List<Product> filteredProducts = new ArrayList<>(products);

		if (search != null && !search.isEmpty()) {
			filteredProducts = filteredProducts.stream()
				.filter(product -> product.getName().toLowerCase().contains(search.toLowerCase()))
				.collect(Collectors.toList());
		}

		if (!filter.equals("all")) {
			filteredProducts = filteredProducts.stream()
				.filter(product -> {
					if (filter.equalsIgnoreCase("Flower")) {
						return product instanceof Flower;
					} else if (filter.equalsIgnoreCase("Bouquet")) {
						return product instanceof Bouquet;
					}
					return false;
				})
				.collect(Collectors.toList());
		}

		// Add computed fields to the products
		List<Map<String, Object>> enrichedProducts = filteredProducts.stream()
			.map(this::enrichProductData)
			.collect(Collectors.toList());

		model.addAttribute("products", enrichedProducts);
		model.addAttribute("createBouquetMode", false);
		model.addAttribute("showModal", false);
		model.addAttribute("showDeletedModal", false);
		return "inventory";
	}

	// Helper method to enrich product data
	private Map<String, Object> enrichProductData(Product product) {
		Map<String, Object> data = new HashMap<>();
		data.put("name", product.getName());
		data.put("quantity", getQuantity(product));
		data.put("pricePerUnit", computePricePerUnit(product));
		data.put("type", determineType(product));
		data.put("id", product.getId());
		return data;
	}

	// Helper method to determine the type of the product
	private String determineType(Product product) {
		if (product instanceof Flower) {
			return "Flower";
		} else if (product instanceof Bouquet) {
			return "Bouquet";
		}
		return "Unknown";
	}

	// Helper method to compute the price per unit
	private double computePricePerUnit(Product product) {
		if (product instanceof Bouquet) {
			Bouquet bouquet = (Bouquet) product;
			return 5;
		}
		return product.getPrice().getNumber().doubleValue();
	}

	@GetMapping("/inventory/create-bouquet")
	public String createBouquetMode(Model model) {
		List<Flower> flowersOnly = products.stream()
			.filter(product -> product instanceof Flower)
			.map(product -> (Flower) product)
			.collect(Collectors.toList());

		model.addAttribute("products", flowersOnly);
		model.addAttribute("createBouquetMode", true);
		model.addAttribute("selectedFlowersForBouquet", selectedFlowersForBouquet);
		model.addAttribute("showModal", false);
		model.addAttribute("showDeletedModal", false);
		model.addAttribute("showChooseModal", false);
		return "inventory";
	}



	@GetMapping("/inventory/choose-flower")
	public String showChooseModal(@RequestParam UUID flowerID, Model model) {
		Optional<Product> selectedFlower = productService.getProductById(flowerID);

		selectedFlower.ifPresent(flower -> {
			model.addAttribute("selectedFlower", flower);
			model.addAttribute("showChooseModal", true);
		});

		model.addAttribute("createBouquetMode", true);
		model.addAttribute("products", productService.getAllProducts());
		return "inventory";
	}


	@PostMapping("/inventory/add-flower")
	public String addFlowerToBouquet(@RequestParam UUID flowerID,
									 @RequestParam int chooseQuantity,
									 Model model) {

		Optional<Product> productOpt = productService.getProductById(flowerID); // Adjust method to return Product

		if (productOpt.isPresent()) {
			Product product = productOpt.get();

			if (product instanceof Flower) { // Ensure it is a Flower
				Flower selectedFlower = (Flower) product;

				if (selectedFlower.getQuantity() >= chooseQuantity) {
					selectedFlower.setQuantity(selectedFlower.getQuantity() - chooseQuantity);
					selectedFlowersForBouquet.add(selectedFlower);
					model.addAttribute("success", "Flower added to bouquet.");
				} else {
					model.addAttribute("error", "Not enough stock or invalid quantity.");
				}
			} else {
				model.addAttribute("error", "Selected product is not a flower.");
			}
		} else {
			model.addAttribute("error", "Flower not found.");
		}

		model.addAttribute("createBouquetMode", true);
		model.addAttribute("products", productService.getAllProducts());
		model.addAttribute("selectedFlowersForBouquet", selectedFlowersForBouquet);
		return "inventory";
	}


	@PostMapping("/create-custom-bouquet")
	public String createCustomBouquet(@RequestParam String bouquetName, Model model) {
		if (!selectedFlowersForBouquet.isEmpty() && bouquetName != null && !bouquetName.isEmpty()) {
			// Ensure the selected items are Flowers and retrieve their quantities
			Map<Flower, Integer> flowerMap = selectedFlowersForBouquet.stream()
				.filter(product -> product instanceof Flower) // Ensure only Flower objects are processed
				.collect(Collectors.toMap(
					flower -> (Flower) flower,
					flower -> ((Flower) flower).getQuantity()
				));

			Money additionalPrice = Money.of(5, "EUR");
			Pricing bouquetPricing = Bouquet.calculateTotalPricing(flowerMap, additionalPrice);

			Bouquet customBouquet = new Bouquet(
				bouquetName,
				flowerMap,
				additionalPrice,
				1
			);
			customBouquet.setPricing(bouquetPricing);

			products.add(customBouquet);
			selectedFlowersForBouquet.clear();
			model.addAttribute("success", "Custom bouquet created successfully.");
		} else {
			model.addAttribute("error", "Bouquet name is required, and at least one flower must be added.");
		}

		model.addAttribute("createBouquetMode", false);
		model.addAttribute("products", products);
		return "inventory";
	}

	@GetMapping("/inventory/deleted-products")
	public String showDeletedProducts(Model model) {
		double totalLossSum = deletedProducts.stream()
			.mapToDouble(DeletedProduct::getTotalLoss)
			.sum();

		model.addAttribute("deletedProducts", deletedProducts);
		model.addAttribute("totalLossSum", totalLossSum);
		model.addAttribute("showDeletedModal", !deletedProducts.isEmpty());

		model.addAttribute("createBouquetMode", false);
		model.addAttribute("showModal", false);
		model.addAttribute("products", products);
		return "inventory";
	}



	@GetMapping("/inventory/delete")
	public String showDeleteModal(@RequestParam String productName, Model model) {
		Optional<Product> selectedProduct = findProductByName(productName);

		selectedProduct.ifPresent(product -> model.addAttribute("selectedProduct", product));

		model.addAttribute("showModal", true);
		model.addAttribute("createBouquetMode", false);
		model.addAttribute("products", products);
		return "inventory";
	}



	public int getQuantity(Product product) {
		if (product instanceof Flower) {
			return ((Flower) product).getQuantity();
		}
		if (product instanceof Bouquet) {
			return ((Bouquet) product).getQuantity();
		}

		return 0;
	}

	@PostMapping("/delete-product")
	public String deleteProduct(@RequestParam String productName, @RequestParam int deleteQuantity, Model model) {
		Optional<Product> selectedProductOpt = findProductByName(productName);

		if (selectedProductOpt.isPresent()) {
			Product product = selectedProductOpt.get();

			if (getQuantity(product) >= deleteQuantity) {
				updateStock(productName, -deleteQuantity);
				if (product instanceof Flower) {
					DeletedProduct deletedProduct = new DeletedProduct(
						product.getName(),
						((Flower) product).getPricing().getSellPrice().getNumber().doubleValue(),
						deleteQuantity,
						((Flower) product).getPricing().getSellPrice().multiply(deleteQuantity).getNumber().doubleValue()
					);
					deletedProducts.add(deletedProduct);
				}
				if (product instanceof Bouquet) {
					DeletedProduct deletedProduct = new DeletedProduct(
						product.getName(),
						((Bouquet) product).getPricing().getSellPrice().getNumber().doubleValue(),
						deleteQuantity,
						((Bouquet) product).getPricing().getSellPrice().multiply(deleteQuantity).getNumber().doubleValue()
					);
					deletedProducts.add(deletedProduct);
				}
			} else {
				model.addAttribute("error", "Not enough stock available to delete.");
			}
		} else {
			model.addAttribute("error", "Product not found.");
		}
		return "redirect:/inventory";
	}
}
