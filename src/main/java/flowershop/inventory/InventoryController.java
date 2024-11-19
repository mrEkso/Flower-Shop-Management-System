package flowershop.inventory;

import flowershop.product.Product;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class InventoryController {

	private final InventoryInitializer inventoryInitializer;
	private final List<Product> products;
	private final List<Product> bouquets = new ArrayList<>();
	private final List<ProductToDelete> deletedProducts = new ArrayList<>();
	private final List<Product> selectedFlowersForBouquet = new ArrayList<>(); // Store selected flowers for the bouquet

	public InventoryController(InventoryInitializer inventoryInitializer) {
		this.inventoryInitializer = inventoryInitializer;
		this.products = inventoryInitializer.initializeProducts();
	}

	private Optional<Product> findProductByName(String name) {
		return products.stream()
				.filter(product -> product.getName().equalsIgnoreCase(name))
				.findFirst();
	}

	private boolean updateStock(String productName, int quantityChange) {
		for (Product product : products) {
			if (product.getName().equalsIgnoreCase(productName)) {
				int newQuantity = product.getQuantity() + quantityChange;
				if (newQuantity >= 0) {
					product.setQuantity(newQuantity);
					return true;
				}
				break;
			}
		}
		return false;
	}


	@GetMapping("/inventory")
	public String inventoryMode(@RequestParam(required = false) String search,
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
		model.addAttribute("createBouquetMode", false);
		model.addAttribute("showModal", false);
		model.addAttribute("showDeletedModal", false);
		return "inventory";
	}

	@GetMapping("/inventory/create-bouquet")
	public String createBouquetMode(Model model) {
		List<Product> flowersOnly = products.stream()
				.filter(product -> product.getType().equalsIgnoreCase("Flower"))
				.collect(Collectors.toList());

		model.addAttribute("products", flowersOnly);
		model.addAttribute("createBouquetMode", true);
		model.addAttribute("showModal", false);
		model.addAttribute("showDeletedModal", false);
		return "inventory";
	}

	@PostMapping("/inventory/add-flower")
	public String addFlowerToBouquet(@RequestParam String flowerName, @RequestParam int chooseQuantity, Model model) {
		Product selectedFlower = products.stream()
				.filter(product -> product.getName().equalsIgnoreCase(flowerName))
				.findFirst()
				.orElse(null);

		if (selectedFlower != null && selectedFlower.getQuantity() >= chooseQuantity) {
			selectedFlower.setQuantity(selectedFlower.getQuantity() - chooseQuantity);
			selectedFlowersForBouquet.add(new Product(selectedFlower.getName(), selectedFlower.getType(), chooseQuantity, selectedFlower.getPricePerUnit()));
			model.addAttribute("success", "Flower added to bouquet.");
		} else {
			model.addAttribute("error", "Not enough stock or invalid quantity.");
		}

		model.addAttribute("createBouquetMode", true);
		model.addAttribute("products", products);
		model.addAttribute("selectedFlowersForBouquet", selectedFlowersForBouquet);
		return "inventory";
	}

	@PostMapping("/create-custom-bouquet")
	public String createCustomBouquet(@RequestParam String bouquetName, Model model) {
		if (!selectedFlowersForBouquet.isEmpty() && bouquetName != null && !bouquetName.isEmpty()) {
			double totalPrice = selectedFlowersForBouquet.stream()
					.mapToDouble(flower -> flower.getPricePerUnit() * flower.getQuantity())
					.sum();

			Product customBouquet = new Product(bouquetName, "Bouquet", 1, totalPrice);
			bouquets.add(customBouquet);
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



	@GetMapping("/inventory/choose-flower")
	public String showChooseModal(@RequestParam String flowerName, Model model) {
		Optional<Product> selectedFlower = findProductByName(flowerName);

		selectedFlower.ifPresent(flower -> {
			model.addAttribute("showChooseModal", true);
			model.addAttribute("selectedFlower", flower);
		});

		model.addAttribute("createBouquetMode", true);
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

	@PostMapping("/delete-product")
	public String deleteProduct(@RequestParam String productName, @RequestParam int deleteQuantity, Model model) {
		Optional<Product> selectedProductOpt = findProductByName(productName);

		if (selectedProductOpt.isPresent()) {
			Product product = selectedProductOpt.get();

			if (product.getQuantity() >= deleteQuantity) {
				updateStock(productName, -deleteQuantity);

				ProductToDelete deletedProduct = new ProductToDelete(
						product.getName(),
						product.getPricePerUnit(),
						deleteQuantity,
						product.getPricePerUnit() * deleteQuantity
				);
				deletedProducts.add(deletedProduct);
			} else {
				model.addAttribute("error", "Not enough stock available to delete.");
			}
		} else {
			model.addAttribute("error", "Product not found.");
		}
		return "redirect:/inventory";
	}

	@GetMapping("/inventory/deleted-products")
	public String showDeletedProducts(Model model) {
		double totalLossSum = deletedProducts.stream()
				.mapToDouble(ProductToDelete::getTotalLoss)
				.sum();


		model.addAttribute("deletedProducts", deletedProducts);
		model.addAttribute("totalLossSum", totalLossSum);
		model.addAttribute("showDeletedModal", !deletedProducts.isEmpty());

		model.addAttribute("createBouquetMode", false);
		model.addAttribute("showModal", false);
		model.addAttribute("products", products);
		return "inventory";
	}

}