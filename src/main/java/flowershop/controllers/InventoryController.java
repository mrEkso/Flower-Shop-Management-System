package flowershop.controllers;

import flowershop.models.products.Product;
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

	private List<Product> products = new ArrayList<>();
	private List<Map<String, Object>> deletedProducts = new ArrayList<>();

	public InventoryController() {
		// Initializing products
		products.add(new Product("Rose", "Flower", 50, 2.00));
		products.add(new Product("Sunflower", "Flower", 30, 1.50));
		products.add(new Product("Tulip", "Flower", 40, 2.50));
		products.add(new Product("Hibiscus", "Flower", 20, 3.00));
		products.add(new Product("Daisy", "Flower", 60, 1.20));
		products.add(new Product("Spring Bouquet", "Bouquet", 15, 12.00));
		products.add(new Product("Romantic Bouquet", "Bouquet", 25, 15.00));
		products.add(new Product("Tulip Mix", "Bouquet", 10, 18.00));
		products.add(new Product("Sunshine Bouquet", "Bouquet", 12, 20.00));
		products.add(new Product("Mixed Flowers", "Bouquet", 18, 10.00));
	}

	@GetMapping("/inventory")
	public String inventoryPage(@RequestParam(required = false) String search,
								@RequestParam(required = false, defaultValue = "all") String filter, Model model) {
		List<Product> filteredProducts = products;

		if (search != null && !search.isEmpty()) {
			filteredProducts = filteredProducts.stream()
				.filter(product -> product.getName().toLowerCase().contains(search.toLowerCase()))
				.collect(Collectors.toList());
		}
		if (!filter.equals("all")) {
			filteredProducts = filteredProducts.stream()
				.filter(product -> product.getType().equalsIgnoreCase(filter))
				.collect(Collectors.toList());
		}
		model.addAttribute("products", filteredProducts);
		return "inventory";
	}

	@GetMapping("/inventory/delete")
	public String showDeleteModal(@RequestParam String productName, Model model) {
		Product selectedProduct = products.stream()
			.filter(product -> product.getName().equalsIgnoreCase(productName))
			.findFirst()
			.orElse(null);
		if (selectedProduct != null) {
			model.addAttribute("showModal", true);
			model.addAttribute("selectedProduct", selectedProduct);
		}
		return "inventory";
	}

	@PostMapping("/delete-product")
	public String deleteProduct(@RequestParam String productName, @RequestParam int deleteQuantity, Model model) {
		for (Product product : products) {
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
		return "inventory";
	}

}
