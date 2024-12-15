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

	public final List<DeletedProduct> deletedProducts = new ArrayList<>();
	public final List<Flower> selectedFlowersForBouquet = new ArrayList<>();

	public final ProductService productService;

	public InventoryController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping("/inventory")
	public String inventoryMode(@RequestParam(required = false) String search,
								@RequestParam(required = false, defaultValue = "all") String filter,
								Model model) {
		List<Product> products = productService.getAllProducts();
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

		List<Map<String, Object>> enrichedProducts = filteredProducts.stream()
			.map(this::enrichProductData)
			.collect(Collectors.toList());

		model.addAttribute("products", enrichedProducts);
		model.addAttribute("createBouquetMode", false);
		model.addAttribute("selectedProduct", productService.findAllFlowers().getFirst());
		model.addAttribute("showModal", true);
		model.addAttribute("showDeletedModal", false);
		return "inventory";
	}

	private Map<String, Object> enrichProductData(Product product) {
		Map<String, Object> data = new HashMap<>();
		data.put("name", product.getName());
		data.put("quantity", getQuantity(product));
		data.put("pricePerUnit", computePricePerUnit(product));
		data.put("type", determineType(product));
		data.put("id", product.getId());
		return data;
	}

	private String determineType(Product product) {
		if (product instanceof Flower) {
			return "Flower";
		} else if (product instanceof Bouquet) {
			return "Bouquet";
		}
		return "Unknown";
	}

	private double computePricePerUnit(Product product) {
		if (product instanceof Bouquet) {
			// System.out.println( );
			return ((Bouquet)product).getPrice().getNumber().doubleValue();
		}

		return product.getPrice().getNumber().doubleValue();
	}

	@GetMapping("/inventory/create-bouquet")
	public String createBouquetMode(Model model) {
		List<Map<String, Object>> flowersOnly = productService.getAllProducts().stream()
			.filter(product -> product instanceof Flower) // Filter only Flower products
			.map(this::enrichProductData) // Enrich only Flower products
			.collect(Collectors.toList());


		model.addAttribute("products", flowersOnly);
		model.addAttribute("createBouquetMode", true);
		//model.addAttribute("selectedFlowersForBouquet", selectedFlowersForBouquet);
		model.addAttribute("showModal", false);
		model.addAttribute("showDeletedModal", false);
		model.addAttribute("selectedFlower", productService.findAllFlowers().getFirst());
		model.addAttribute("showChooseModal", true);
		return "inventory";
	}


	@GetMapping("/inventory/choose-flower")
	public String showChooseModal(@RequestParam UUID flowerID, Model model) {
		Optional<Product> selectedFlowerOpt = productService.getProductById(flowerID);

		List<Map<String, Object>> enrichedProducts = productService.getAllProducts().stream()
			.map(this::enrichProductData)
			.collect(Collectors.toList());

		if (selectedFlowerOpt.isPresent()) {
			Product product = selectedFlowerOpt.get();
			if (product instanceof Flower) {
				model.addAttribute("selectedFlower", (Flower) product);
				model.addAttribute("showChooseModal", true);
			} else {
				model.addAttribute("error", "Selected product is not a flower.");
			}
		} else {
			model.addAttribute("error", "Product not found.");
		}

		model.addAttribute("createBouquetMode", true);
		model.addAttribute("products", enrichedProducts);

		return "inventory";
	}




	@PostMapping("/inventory/add-flower")
	public String addFlowerToBouquet(@RequestParam UUID flowerID,
									 @RequestParam int chooseQuantity,
									 Model model) {

		Optional<Product> productOpt = productService.getProductById(flowerID);

		if (productOpt.isPresent()) {
			Product product = productOpt.get();

			if (product instanceof Flower selectedFlower) {

				if (selectedFlower.getQuantity() >= chooseQuantity) {
					selectedFlower.setDeletedQuantity(chooseQuantity);
					if (!selectedFlowersForBouquet.contains(selectedFlower)) {
						selectedFlowersForBouquet.add(selectedFlower);
					}
					//productService.removeFlowers(selectedFlower, chooseQuantity);
				}
			}
		}


		List<Map<String, Object>> enrichedProducts = productService.getAllProducts().stream()
			.map(this::enrichProductData)
			.collect(Collectors.toList());

		model.addAttribute("createBouquetMode", true);
		model.addAttribute("selectedFlower", productService.findAllFlowers().getFirst());
		model.addAttribute("showChooseModal", true);
		model.addAttribute("products", enrichedProducts);
		model.addAttribute("selectedFlowersForBouquet", selectedFlowersForBouquet);
		return "inventory";
	}


	@PostMapping("/create-custom-bouquet")
	public String createCustomBouquet(@RequestParam String bouquetName, Model model) {
		if (!selectedFlowersForBouquet.isEmpty() && bouquetName != null && !bouquetName.isEmpty()) {
			if (selectedFlowersForBouquet.size() > 1 || selectedFlowersForBouquet.getFirst().getDeletedQuantity()> 1) {
				Map<Flower, Integer> flowerMap = selectedFlowersForBouquet.stream()
					.filter(Objects::nonNull)
					.collect(Collectors.toMap(
						flower -> (Flower) flower,
						flower -> ((Flower) flower).getDeletedQuantity()
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

				productService.addBouquet(customBouquet);
			}
		}
		selectedFlowersForBouquet.clear();
		List<Map<String, Object>> enrichedProducts = productService.getAllProducts().stream()
			.map(this::enrichProductData)
			.collect(Collectors.toList());

		model.addAttribute("createBouquetMode", false);
		model.addAttribute("products", enrichedProducts);
		model.addAttribute("selectedProduct", productService.findAllFlowers().getFirst());
		model.addAttribute("showModal", true);
		return "inventory";
	}

	@GetMapping("/inventory/deleted-products")
	public String showDeletedProducts(Model model) {
		double totalLossSum = 0.0;
		for (DeletedProduct deletedProduct : deletedProducts) {
			totalLossSum += deletedProduct.getTotalLoss();
		}

		List<Map<String, Object>> enrichedProducts = productService.getAllProducts().stream()
			.map(this::enrichProductData)
			.collect(Collectors.toList());

		model.addAttribute("deletedProducts", deletedProducts);
		model.addAttribute("totalLossSum", totalLossSum);
		model.addAttribute("showDeletedModal", !deletedProducts.isEmpty());

		model.addAttribute("createBouquetMode", false);
		model.addAttribute("selectedProduct", productService.findAllFlowers().getFirst());
		model.addAttribute("showModal", true);
		model.addAttribute("products", enrichedProducts);
		return "inventory";
	}



	@GetMapping("/inventory/delete")
	public String showDeleteModal(@RequestParam("productID") UUID productID, Model model) {
		Optional<Product> selectedProductOpt = productService.getProductById(productID);

		selectedProductOpt.ifPresent(product -> model.addAttribute("selectedProduct", product));

		List<Map<String, Object>> enrichedProducts = productService.getAllProducts().stream()
			.map(this::enrichProductData)
			.collect(Collectors.toList());

		model.addAttribute("deletedProducts", deletedProducts);
		model.addAttribute("showModal", true);
		model.addAttribute("createBouquetMode", false);
		model.addAttribute("products", enrichedProducts);

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
	public String deleteProduct(@RequestParam String productName, @RequestParam int deleteQuantity) {
		List<Flower> flowers = productService.findAllFlowers();
		List<Bouquet> bouquets = productService.findAllBouquets();
		for (Flower flower : flowers) {
			if (flower.getName().equals(productName)) {
				if (flower.getQuantity() >= deleteQuantity) {
					productService.removeFlowers(flower, deleteQuantity);
					DeletedProduct deletedProduct = new DeletedProduct(
						flower.getName(),
						flower.getPrice().getNumber().doubleValue(),
						deleteQuantity,
						flower.getPrice().getNumber().doubleValue() * deleteQuantity
					);
					deletedProducts.add(deletedProduct);
					return "redirect:/inventory";
				}
			}

		}
		for (Bouquet bouquet : bouquets) {
			if (bouquet.getName().equals(productName)) {
				if (bouquet.getQuantity() >= deleteQuantity) {
					productService.removeBouquet(bouquet, deleteQuantity);
					DeletedProduct deletedProduct = new DeletedProduct(
						bouquet.getName(),
						bouquet.getPrice().getNumber().doubleValue(),
						deleteQuantity,
						bouquet.getPrice().getNumber().doubleValue() * deleteQuantity
					);
					deletedProducts.add(deletedProduct);
					return "redirect:/inventory";
				}
			}
		}

		return "redirect:/inventory";
	}
}
