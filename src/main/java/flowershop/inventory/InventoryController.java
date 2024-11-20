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
	private final DeletedProductService deletedProductService;
	private final InventoryService inventoryService;
	private List<Product> products;

	// Initialize products using the service
	public InventoryController(ProductService productService,
							   DeletedProductService deletedProductService,
							   InventoryService inventoryService) {
		this.productService = productService;
		this.deletedProductService = deletedProductService;
		this.inventoryService = inventoryService;

		// Use the product service to initialize the products list
		this.products = productService.getAllProducts();
		
		// System.out.println("the products are" + products);
		// System.out.println(productService.findAllFlowers());
	}

	@GetMapping("/inventory")
	public String inventoryMode(@RequestParam(required = false) String search,
								@RequestParam(required = false, defaultValue = "all") String filter, Model model) {
		// System.out.println("-------------inventory--------------");
		// System.out.println(productService.getAllProducts());
		// System.out.println(productService.findAllFlowers());
		this.products = productService.getAllProducts();
							
		List<Product> filteredProducts = new ArrayList<>(products);
							
		if (search != null && !search.isEmpty()) {
			filteredProducts = filteredProducts.stream()
				.filter(product -> product.getName().toLowerCase().contains(search.toLowerCase()))
				.collect(Collectors.toList());
		}

		if (!filter.equals("all")) {
			if (filter.equalsIgnoreCase("Flower")) {
				filteredProducts = filteredProducts.stream()
					.filter(product -> product instanceof Flower)
					.collect(Collectors.toList());
			} else if (filter.equalsIgnoreCase("Bouquet")) {
				filteredProducts = filteredProducts.stream()
					.filter(product -> product instanceof Bouquet)
					.collect(Collectors.toList());
			}
		}

		model.addAttribute("products", filteredProducts);
		model.addAttribute("createBouquetMode", false);
		model.addAttribute("showModal", false);
		model.addAttribute("showDeletedModal", false);
		return "inventory";
	}


	@GetMapping("/inventory/create-bouquet")
	public String createBouquetMode(Model model) {
		List<Flower> flowersOnly = products.stream()
			.filter(product -> product instanceof Flower)
			.map(product -> (Flower) product)
			.collect(Collectors.toList());

		model.addAttribute("products", flowersOnly);
		model.addAttribute("createBouquetMode", true);
		model.addAttribute("showModal", false);
		model.addAttribute("showDeletedModal", false);
		return "inventory";
	}
	@PostMapping("/inventory/add-flower")
	public String addFlowerToBouquet(@RequestParam UUID flowerID,
									 @RequestParam int chooseQuantity,
									 Model model) {

		Optional<Flower> flowerOpt = productService.getFlowerById(flowerID);

		if (flowerOpt.isPresent()) {
			Flower selectedFlower = flowerOpt.get();

			if (selectedFlower.getQuantity() >= chooseQuantity) {
				selectedFlower.setQuantity(selectedFlower.getQuantity() - chooseQuantity);
				selectedFlowersForBouquet.add(selectedFlower);
				model.addAttribute("success", "Flower added to bouquet.");
			} else {
				model.addAttribute("error", "Not enough stock or invalid quantity.");
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
		return "inventory";
	}
}
