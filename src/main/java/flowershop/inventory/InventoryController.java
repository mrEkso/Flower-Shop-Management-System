package flowershop.inventory;

import flowershop.clock.ClockService;
import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.Pricing;
import flowershop.product.ProductService;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.*;
import java.util.stream.Collectors;

@Controller
public class InventoryController {
	/**
	 * List to keep track of deleted products along with their details.
	 */
	public final List<DeletedProduct> deletedProducts = new ArrayList<>();
	/**
	 * List to store flowers selected for bouquet creation.
	 */
	public final List<Flower> selectedFlowersForBouquet = new ArrayList<>();
	/**
	 * Service layer dependency for product-related operations.
	 */
	public final ProductService productService;
	private final ClockService clockService;

	/**
	 * Constructor to initialize the InventoryController with a ProductService instance.
	 *
	 * @param productService the service for managing products
	 */
	public InventoryController(ProductService productService, ClockService clockService) {
		this.productService = productService;
		this.clockService = clockService;
	}

	/**
	 * Displays the inventory page with optional search and filter parameters.
	 *
	 * @param search search term to filter products by name
	 * @param filter filter to show specific product types (e.g., Flower, Bouquet)
	 * @param quantityProblemLabel appear when the user try to over delete quantity product
	 * @param model  the model to pass data to the view
	 * @return the name of the inventory view
	 */
	@GetMapping("/inventory")
	@PreAuthorize("hasRole('BOSS')")
	public String inventoryMode(
		@RequestParam(required = false) String search,
		@RequestParam(required = false, defaultValue = "all") String filter,
		@RequestParam(required = false) Boolean quantityProblemLabel,
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
		model.addAttribute("showChangePriceModal", false);

		if (quantityProblemLabel != null && quantityProblemLabel) {
			model.addAttribute("quantityProblemLabel", true);
		}

		return "inventory";
	}


	/**
	 * Enriches a product with additional display data.
	 *
	 * @param product the product to enrich
	 * @return a map containing enriched product data
	 */
	private Map<String, Object> enrichProductData(Product product) {
		Map<String, Object> data = new HashMap<>();
		data.put("name", product.getName());
		data.put("quantity", getQuantity(product));
		data.put("pricePerUnit", computePricePerUnit(product));
		data.put("type", determineType(product));
		data.put("id", product.getId());
		return data;
	}

	/**
	 * Determines the type of a product.
	 *
	 * @param product the product to check
	 * @return the type of the product as a string
	 */
	private String determineType(Product product) {
		if (product instanceof Flower) {
			return "Flower";
		} else if (product instanceof Bouquet) {
			return "Bouquet";
		}
		return "Unknown";
	}

	/**
	 * Computes the price per unit for a product.
	 *
	 * @param product the product to calculate price for
	 * @return the price per unit as a double
	 */
	private double computePricePerUnit(Product product) {
		if (product instanceof Bouquet) {
			return ((Bouquet)product).getPrice().getNumber().doubleValue();
		}

		return product.getPrice().getNumber().doubleValue();
	}

	/**
	 * Enables bouquet creation mode by filtering the product list to only include flowers.
	 *
	 * @param model the model to pass data to the view
	 * @return the name of the inventory view
	 */
	@GetMapping("/inventory/create-bouquet")
	@PreAuthorize("hasRole('BOSS')")
	public String createBouquetMode(Model model) {
		List<Map<String, Object>> flowersOnly = productService.getAllProducts().stream()
			.filter(product -> product instanceof Flower) // Filter only Flower products
			.map(this::enrichProductData) // Enrich only Flower products
			.collect(Collectors.toList());


		model.addAttribute("products", flowersOnly);
		model.addAttribute("createBouquetMode", true);
		model.addAttribute("showModal", false);
		model.addAttribute("showDeletedModal", false);
		model.addAttribute("selectedFlower", productService.findAllFlowers().getFirst());
		//model.addAttribute("showChooseModal", true);
		return "inventory";
	}


	/**
	 * Shows a modal for choosing a flower to add to a bouquet.
	 *
	 * @param flowerID the ID of the selected flower
	 * @param model    the model to pass data to the view
	 * @return the name of the inventory view
	 */
	@GetMapping("/inventory/choose-flower")
	@PreAuthorize("hasRole('BOSS')")
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



	/**
	 * Adds a flower to the list for bouquet creation.
	 *
	 * @param flowerID       the ID of the flower to be added
	 * @param chooseQuantity the quantity of the flower to be added
	 * @param model          the model to hold attributes for the view
	 * @return the inventory view name
	 */
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

	/**
	 * Creates a custom bouquet using the selected flowers.
	 *
	 * @param bouquetName the name of the custom bouquet
	 * @param model       the model to hold attributes for the view
	 * @return the inventory view name
	 */
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

	/**
	 * Displays deleted products and calculates the total monetary loss.
	 *
	 * @param model the model to hold attributes for the view
	 * @return the inventory view name
	 */
	@GetMapping("/inventory/deleted-products")
	@PreAuthorize("hasRole('BOSS')")
	public String showDeletedProducts(Model model) {
		double totalLossSum = 0.0;
		for (DeletedProduct deletedProduct : productService.getDeletedProducts()) {
			totalLossSum += deletedProduct.getTotalLoss().getNumber().doubleValue();
		}

		List<Map<String, Object>> enrichedProducts = productService.getAllProducts().stream()
			.map(this::enrichProductData)
			.collect(Collectors.toList());

		model.addAttribute("deletedProducts", productService.getDeletedProducts());
		model.addAttribute("totalLossSum", totalLossSum);
		model.addAttribute("showDeletedModal", !productService.getDeletedProducts().isEmpty());

		model.addAttribute("createBouquetMode", false);
		model.addAttribute("selectedProduct", productService.findAllFlowers().getFirst());
		model.addAttribute("showModal", true);
		model.addAttribute("products", enrichedProducts);
		return "inventory";
	}


	/**
	 * Displays a modal to confirm deletion of a product.
	 *
	 * @param productID the ID of the product to delete
	 * @param model     the model to hold attributes for the view
	 * @return the inventory view name
	 */
	@GetMapping("/inventory/delete")
	@PreAuthorize("hasRole('BOSS')")
	public String showDeleteModal(@RequestParam("productID") UUID productID, Model model) {
		Optional<Product> selectedProductOpt = productService.getProductById(productID);

		selectedProductOpt.ifPresent(product -> model.addAttribute("selectedProduct", product));

		List<Map<String, Object>> enrichedProducts = productService.getAllProducts().stream()
			.map(this::enrichProductData)
			.collect(Collectors.toList());

		model.addAttribute("deletedProducts", productService.getDeletedProducts());
		model.addAttribute("showModal", true);
		model.addAttribute("createBouquetMode", false);
		model.addAttribute("showChangePriceModal", false);
		model.addAttribute("products", enrichedProducts);

		return "inventory";
	}

	/**
	 * Retrieves the quantity of a product.
	 *
	 * @param product the product whose quantity is retrieved
	 * @return the quantity of the product
	 */
	public int getQuantity(Product product) {
		if (product instanceof Flower) {
			return ((Flower) product).getQuantity();
		}
		if (product instanceof Bouquet) {
			return ((Bouquet) product).getQuantity();
		}

		return 0;
	}

	/**
	 * Deletes a product and records the loss caused by the deletion.
	 *
	 * @param productName    the name of the product to delete
	 * @param deleteQuantity the quantity of the product to delete
	 * @return the redirect path to the inventory view
	 */
	@PostMapping("/delete-product")
	public String deleteProduct(@RequestParam String productName, @RequestParam int deleteQuantity, Model model) {
		List<Flower> flowers = productService.findAllFlowers();
		List<Bouquet> bouquets = productService.findAllBouquets();
		for (Flower flower : flowers) {
			if (flower.getName().equals(productName)) {
				if (flower.getQuantity() >= deleteQuantity) {
					productService.removeFlowers(flower, deleteQuantity);
					DeletedProduct deletedProduct = new DeletedProduct(
						flower.getName(),
						flower.getPrice(),
						deleteQuantity,
						flower.getPrice().multiply(deleteQuantity),
						clockService.getCurrentDate()
					);
					//deletedProducts.add(deletedProduct);
					productService.addDeletedProduct(deletedProduct);
					return "redirect:/inventory";
				}
				else {
					model.addAttribute("quantityProblemLabel", true);
					return "redirect:/inventory?quantityProblemLabel=true";
				}
			}

		}
		for (Bouquet bouquet : bouquets) {
			if (bouquet.getName().equals(productName)) {
				if (bouquet.getQuantity() >= deleteQuantity) {
					productService.removeBouquet(bouquet, deleteQuantity);
					DeletedProduct deletedProduct = new DeletedProduct(
						bouquet.getName(),
						bouquet.getPrice(),
						deleteQuantity,
						bouquet.getPrice().multiply(deleteQuantity),
						clockService.getCurrentDate()
					);
					//deletedProducts.add(deletedProduct);
					productService.addDeletedProduct(deletedProduct);
					return "redirect:/inventory";
				}
				else {
					model.addAttribute("quantityProblemLabel", true);
					return "redirect:/inventory?quantityProblemLabel=true";
				}
			}
		}

		return "redirect:/inventory";
	}

	/**
	 * Updates the price of a product.
	 *
	 * @param productID   the id of the product to update
	 * @param model       the model to hold attributes for the view
	 * @return the redirect path to the inventory view
	 */
	@PostMapping("/inventory/update-price")
	@PreAuthorize("hasRole('BOSS')")
	public String updateProductPrice(
		@RequestParam("productID") UUID productID, // Use productID
		@RequestParam("newSellPrice") double newSellPrice,
		Model model
	) {
		if (newSellPrice <= 0) {
			model.addAttribute("error", "Price must be greater than zero.");
			return "redirect:/inventory";
		}

		Optional<Product> productOpt = productService.getProductById(productID);

		if (productOpt.isPresent()) {
			Product product = productOpt.get();
			if (product instanceof Flower) {
				((Flower) product).getPricing().setSellPrice(Money.of(newSellPrice, "EUR"));
				System.out.println("------------------- change the sell price to "+ newSellPrice);
			} else if (product instanceof Bouquet) {
				((Bouquet) product).getPricing().setSellPrice(Money.of(newSellPrice, "EUR"));
			}
		} else {
			model.addAttribute("error", "Product not found.");
		}

		return "redirect:/inventory";
	}



	@GetMapping("/inventory/change-price")
	@PreAuthorize("hasRole('BOSS')")
	public String showChangePriceModal(@RequestParam("productID") UUID productID, Model model) {
		Optional<Product> selectedProductOpt = productService.getProductById(productID);

		selectedProductOpt.ifPresent(product -> model.addAttribute("selectedProduct", product));

		List<Map<String, Object>> enrichedProducts = productService.getAllProducts().stream()
			.map(this::enrichProductData)
			.collect(Collectors.toList());

		model.addAttribute("deletedProducts", productService.getDeletedProducts());
		model.addAttribute("showModal", true);
		model.addAttribute("createBouquetMode", false);
		model.addAttribute("showChangePriceModal", true);
		model.addAttribute("products", enrichedProducts);

		return "inventory";
	}
	/*
	public void addDeliveredFlowersFromWholesaler(Map<Flower, Integer> flowersBought) {
		for (Map.Entry<Flower, Integer> flowerBought : flowersBought.entrySet()) {
		  productService.addFlowers(flowerBought.getKey(), flowerBought.getValue());
		}
	  }

	 */
}

