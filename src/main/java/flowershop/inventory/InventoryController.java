package flowershop.inventory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class InventoryController {

	private List<ProductToDelete> products = new ArrayList<>();
	private List<Map<String, Object>> deletedProducts = new ArrayList<>();
	private List<ProductToDelete> bouquets = new ArrayList<>();

	public InventoryController() {
		// Initializing products
		products.add(new ProductToDelete("Rose", "Flower", 50, 2.00));
		products.add(new ProductToDelete("Sunflower", "Flower", 30, 1.50));
		products.add(new ProductToDelete("Tulip", "Flower", 40, 2.50));
		products.add(new ProductToDelete("Hibiscus", "Flower", 20, 3.00));
		products.add(new ProductToDelete("Daisy", "Flower", 60, 1.20));
		products.add(new ProductToDelete("Spring Bouquet", "Bouquet", 15, 12.00));
		products.add(new ProductToDelete("Romantic Bouquet", "Bouquet", 25, 15.00));
		products.add(new ProductToDelete("Tulip Mix", "Bouquet", 10, 18.00));
		products.add(new ProductToDelete("Sunshine Bouquet", "Bouquet", 12, 20.00));
		products.add(new ProductToDelete("Mixed Flowers", "Bouquet", 18, 10.00));
	}

	@GetMapping("/inventory")
	public String inventoryMode(@RequestParam(required = false) String search,
								@RequestParam(required = false, defaultValue = "all") String filter, Model model) {
		List<ProductToDelete> filteredProducts = products;

		// Filter by search
		if (search != null && !search.isEmpty()) {
			filteredProducts = filteredProducts.stream()
				.filter(product -> product.getName().toLowerCase().contains(search.toLowerCase()))
				.collect(Collectors.toList());
		}

		// Filter by type
		if (!filter.equals("all")) {
			filteredProducts = filteredProducts.stream()
				.filter(product -> product.getType().equalsIgnoreCase(filter))
				.collect(Collectors.toList());
		}

		// Add attributes for inventory view
		model.addAttribute("products", filteredProducts);
		model.addAttribute("createBouquetMode", false); // Inventory mode
		model.addAttribute("showModal", false);
		model.addAttribute("showDeletedModal", false);
		return "inventory";
	}

	@GetMapping("/inventory/create-bouquet")
	public String createBouquetMode(Model model) {
		// Filter only flower products
		List<ProductToDelete> flowersOnly = products.stream()
			.filter(product -> product.getType().equalsIgnoreCase("Flower"))
			.collect(Collectors.toList());

		model.addAttribute("products", flowersOnly);
		model.addAttribute("createBouquetMode", true); // Bouquet creation mode
		model.addAttribute("showModal", false);
		model.addAttribute("showDeletedModal", false);
		return "inventory";
	}


	@GetMapping("/inventory/choose-flower")
	public String showChooseModal(@RequestParam String flowerName, Model model) {
		ProductToDelete selectedFlower = products.stream()
			.filter(product -> product.getName().equalsIgnoreCase(flowerName))
			.findFirst()
			.orElse(null);

		if (selectedFlower != null) {
			model.addAttribute("showChooseModal", true); // Modal will be shown
			model.addAttribute("selectedFlower", selectedFlower);
		}

		model.addAttribute("createBouquetMode", true);  // Ensure bouquet creation mode is still active
		model.addAttribute("showModal", false);
		model.addAttribute("showDeletedModal", false);
		model.addAttribute("products", products);
		return "inventory"; // Render the page with modal visible
	}



	@PostMapping("/create-bouquet")
	public String createBouquet(@RequestParam String flowerName, @RequestParam int chooseQuantity, Model model) {
		ProductToDelete selectedFlower = products.stream()
			.filter(product -> product.getName().equalsIgnoreCase(flowerName))
			.findFirst()
			.orElse(null);

		if (selectedFlower != null && selectedFlower.getQuantity() >= chooseQuantity && chooseQuantity >= 2) {
			selectedFlower.setQuantity(selectedFlower.getQuantity() - chooseQuantity);

			// Create the bouquet product
			ProductToDelete bouquet = new ProductToDelete(selectedFlower.getName() + " Bouquet", "Bouquet", chooseQuantity, selectedFlower.getPricePerUnit() * chooseQuantity);
			bouquets.add(bouquet);  // Add the bouquet to the bouquet list

			// Add success message
			model.addAttribute("success", "Bouquet created successfully!");
		} else {
			model.addAttribute("error", "Not enough stock or invalid quantity.");
		}

		model.addAttribute("createBouquetMode", true); // Keep bouquet creation mode
		model.addAttribute("showModal", false);
		model.addAttribute("showDeletedModal", false);
		model.addAttribute("products", products);
		return "inventory"; // Return to inventory page
	}




	@GetMapping("/inventory/delete")
	public String showDeleteModal(@RequestParam String productName, Model model) {
		ProductToDelete selectedProduct = products.stream()
			.filter(product -> product.getName().equalsIgnoreCase(productName))
			.findFirst()
			.orElse(null);

		if (selectedProduct != null) {
			model.addAttribute("showModal", true);
			model.addAttribute("selectedProduct", selectedProduct);
		}

		model.addAttribute("createBouquetMode", false);
		model.addAttribute("showDeletedModal", false);
		model.addAttribute("products", products); // Ensure products are displayed
		return "inventory";
	}

	@PostMapping("/delete-product")
	public String deleteProduct(@RequestParam String productName, @RequestParam int deleteQuantity, Model model) {
		for (ProductToDelete product : products) {
			if (product.getName().equalsIgnoreCase(productName)) {
				if (product.getQuantity() >= deleteQuantity) {
					product.setQuantity(product.getQuantity() - deleteQuantity);

					Map<String, Object> deletedProductData = new HashMap<>();
					deletedProductData.put("name", product.getName());
					deletedProductData.put("pricePerUnit", product.getPricePerUnit());
					deletedProductData.put("quantityDeleted", deleteQuantity);
					deletedProductData.put("totalLoss", product.getPricePerUnit() * deleteQuantity);

					deletedProducts.add(deletedProductData);
				} else {
					model.addAttribute("error", "Not enough stock available to delete.");
				}
				break;
			}
		}
		return "redirect:/inventory";
	}

	@GetMapping("/inventory/deleted-products")
	public String showDeletedProducts(Model model) {
		double totalLossSum = deletedProducts.stream()
			.mapToDouble(product -> (double) product.get("totalLoss"))
			.sum();

		model.addAttribute("deletedProducts", deletedProducts);
		model.addAttribute("totalLossSum", totalLossSum);
		model.addAttribute("showDeletedModal", true);

		model.addAttribute("createBouquetMode", false);
		model.addAttribute("showModal", false);
		model.addAttribute("products", products);
		return "inventory";
	}
}
