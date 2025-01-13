package flowershop.sales;

import flowershop.clock.ClockService;
import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.GiftCard;
import flowershop.product.GiftCardRepository;
import flowershop.product.ProductService;
import flowershop.services.ReservationOrder;
import flowershop.services.ReservationOrderService;

import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.catalog.Product.ProductIdentifier;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.OrderLine;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


@SessionAttributes({"fullSellPrice", "fullBuyPrice",
	"buyCart", "sellCart"})
@Controller
public class SalesController {

	private final ProductService productService;
	private final GiftCardRepository giftCardRepository;
	private final SalesService salesService;
	private final ClockService clockService;
	private final ReservationOrderService reservationOrderService;

	SalesController(ProductService productService, SalesService salesService,
					ClockService clockService, ReservationOrderService reservationOrderService, GiftCardRepository giftCardRepository) {
		this.productService = productService;
		this.salesService = salesService;
		this.clockService = clockService;
		this.reservationOrderService = reservationOrderService;
		this.giftCardRepository = giftCardRepository;
	}

	@ModelAttribute("buyCart")
	Cart initializeBuyCart() {
		return new Cart();
	}

	@ModelAttribute("sellCart")
	Cart initializeSellCart() {
		return new Cart();
	}

	/**
	 * Redirects to the selling catalog, i.e. it is defined as the default page.
	 *
	 * @return the redirect URL to the selling catalog
	 */
	@GetMapping("/")
	public String index() {
		return "redirect:/calendar";
	}

	/**
	 * Displays the sell catalog with optional filters and search.
	 * Shows only those products that are in stock, which makes it different from buy catalog.
	 *
	 * @param model       the model to hold attributes for the view
	 * @param filterItem  the filter for product color (optional)
	 * @param searchInput the search input for product name (optional)
	 * @return the view name for the selling catalog
	 */
	@GetMapping("/sell")
	@PreAuthorize("hasRole('BOSS')")
	public String sellCatalog(Model model,
							  @RequestParam(required = false) String filterItem,
							  @RequestParam(required = false) String searchInput) {

		List<Flower> flowers = productService.findAllFlowers();
		List<Bouquet> bouquets = productService.findAllBouquets();

		//List<Product> products = productService.getAllProducts(); // -------------- Please use me <3

		// Search by name
		if (searchInput != null && !searchInput.isEmpty()) {
			flowers = productService.findFlowersByName(searchInput);
			bouquets = productService.findBouquetsByName(searchInput);
		}

		// Filter by color
		if (filterItem != null && !filterItem.isEmpty()) {
			flowers = productService.findFlowersByColor(filterItem, flowers);
			bouquets = new ArrayList<>();
		}

		// Filter products with quantity > 0
		flowers = productService.filterFlowersInStock(flowers);
		bouquets = productService.filterBouquetsInStock(bouquets);

		// Create a Map with adjusted quantities
		Map<ProductIdentifier, Integer> productQuantities = new HashMap<>();
		for (Flower flower : flowers) {
			int adjustedQuantity = flower.getQuantity() - getReservedQuantity(flower.getName());
			productQuantities.put(flower.getId(), Math.max(adjustedQuantity, 0));
		}

		for (Bouquet bouquet : bouquets) {
			int adjustedQuantity = bouquet.getQuantity() - getReservedQuantity(bouquet.getName());
			productQuantities.put(bouquet.getId(), Math.max(adjustedQuantity, 0));
		}

		// Add both flowers and bouquets together.
		List<Product> products = new ArrayList<>();
		products.addAll(flowers);
		products.addAll(bouquets);

		Set<String> colors = productService.getAllFlowerColors();

		model.addAttribute("typeList", colors);
		model.addAttribute("filterItem", filterItem);
		model.addAttribute("searchInput", searchInput);
		model.addAttribute("products", products);
		model.addAttribute("quantities", productQuantities);

		return "sales/sell";
	}

	/**
	 * Displays the buy catalog with optional filters and search.
	 *
	 * @param model       the model to hold attributes for the view
	 * @param filterItem  the filter for product color (optional)
	 * @param searchInput the search input for product name (optional)
	 * @return the view name for the buying catalog
	 */
	@GetMapping("/buy")
	@PreAuthorize("hasRole('BOSS')")
	public String buyCatalog(Model model,
							 @RequestParam(required = false) String filterItem,
							 @RequestParam(required = false) String searchInput) {

		// Shouldn't allow to work with bouquets because wholesalers sell only flowers.
		List<Flower> flowers = productService.findAllFlowers();

		if (searchInput != null && !searchInput.isEmpty()) {
			flowers = productService.findFlowersByName(searchInput);
		}

		// Only by color? Seems reasonable but who knows.
		if (filterItem != null && !filterItem.isEmpty()) {
			flowers = productService.findFlowersByColor(filterItem, flowers);
		}

		Set<String> colors = productService.getAllFlowerColors();

		model.addAttribute("typeList", colors);
		model.addAttribute("filterItem", filterItem);
		model.addAttribute("searchInput", searchInput);
		model.addAttribute("flowers", flowers);

		return "sales/buy";
	}

	/**
	 * @param productName the name of the product
	 * @return the reserved quantity
	 */
	private int getReservedQuantity(String productName) {

		Map<Product, Integer> productQuantities = new HashMap<>();

		List<ReservationOrder> orders = reservationOrderService.findAll();

		for (ReservationOrder order : orders) {
			for (OrderLine line : order.getOrderLines()) {
				Product product = productService.getProductById(line.getProductIdentifier())
					.orElseThrow(() -> new IllegalArgumentException("Product not found: " + line.getProductIdentifier()));

				int quantity = line.getQuantity().getAmount().intValue();
				productQuantities.merge(product, quantity, Integer::sum);
			}
		}

		return productQuantities.entrySet().stream()
			.filter(entry -> entry.getKey().getName().equalsIgnoreCase(productName))
			.map(Map.Entry::getValue)
			.findFirst()
			.orElse(0);
	}

	/**
	 * Processes the sale of products from the sell cart.
	 *
	 * @param sellCart the cart containing products to sell
	 * @param model    the model to hold attributes for the view
	 * @return the redirect URL to the selling catalog
	 */
	@PostMapping("/sell-from-cart")
	public String sellFromCart(
		@ModelAttribute("sellCart") Cart sellCart,
		Model model,
		@RequestParam(required = false) String paymentMethod,
		@RequestParam(required = false) String giftCardId,
		RedirectAttributes redirAttrs
	) throws InsufficientFundsException {

		// Alert when shop is closed
		if (!clockService.isOpen()) {
			sellCart.clear();
			redirAttrs.addFlashAttribute("error", "The day is closed, go home");
			return "redirect:sell";
		}

		boolean isInvalid = sellCart.get().anyMatch(ci -> {
			if (productService.findProductsByName(ci.getProduct().getName()).get(0) instanceof Flower) {
				return !(((Flower) productService.findProductsByName(ci.getProduct().getName()).get(0)).getQuantity()
					- getReservedQuantity(ci.getProduct().getName()) >= ci.getQuantity().getAmount().intValue());
			} else {
				return !(((Bouquet) productService.findProductsByName(ci.getProduct().getName()).get(0)).getQuantity()
					- getReservedQuantity(ci.getProduct().getName()) >= ci.getQuantity().getAmount().intValue());
			}
		});

		if (isInvalid) {
			sellCart.clear();
			redirAttrs.addFlashAttribute("error", "There are not enough products in the storage!");
			return "redirect:sell";
		}

		if (sellCart.isEmpty()) {
			model.addAttribute("message", "Your basket is empty.");
			return "redirect:sell";
		}

		if (paymentMethod.equals("GiftCard")) {
			try {
				UUID cardID = UUID.fromString(giftCardId);
				salesService.sellProductsFromBasket(sellCart, paymentMethod, cardID);
			} catch (InsufficientFundsException e) {
				redirAttrs.addFlashAttribute("error", e.getMessage());
				return "redirect:sell";
			} catch (IllegalArgumentException e) {
				redirAttrs.addFlashAttribute("error", "Enter correct GiftCard ID!");
				return "redirect:sell";
			}

		} else {
			salesService.sellProductsFromBasket(sellCart, paymentMethod, null);
		}

		double fp = salesService.calculateFullCartPrice(model, sellCart, true);
		model.addAttribute("fullSellPrice", fp);

		redirAttrs.addFlashAttribute("success", "Done");
		model.addAttribute("message", "Your order has been successfully placed.");
		return "redirect:sell";
	}

	/**
	 * Processes the purchase of products from the buy cart.
	 *
	 * @param buyCart the cart containing products to buy
	 * @param model   the model to hold attributes for the view
	 * @return the redirect URL to the buying catalog
	 */
	@PostMapping("/buy-from-cart")
	public String buyFromCart(
		@ModelAttribute("buyCart") Cart buyCart,
		Model model,
		RedirectAttributes redirAttrs
	) {
		// Alert when shop is closed
		if (!clockService.isOpen()) {
			buyCart.clear();
			redirAttrs.addFlashAttribute("error", "The day is closed, go home");
			return "redirect:buy";
		}

		if (buyCart == null || buyCart.isEmpty()) {
			model.addAttribute("message", "Your basket is empty.");
			return "redirect:buy";
		}
		salesService.buyProductsFromBasket(buyCart, "Card");

		double fp = salesService.calculateFullCartPrice(model, buyCart, false);
		model.addAttribute("fullBuyPrice", fp);

		redirAttrs.addFlashAttribute("success", "Done");
		model.addAttribute("message", "Your order has been successfully placed.");
		return "redirect:buy";
	}

	/**
	 * Adds a product to the sell cart.
	 *
	 * @param model     the model to hold attributes for the view
	 * @param productId the ID of the product to add
	 * @param sellCart  the cart to which the product is added
	 * @return the redirect URL to the selling catalog
	 */
	@PostMapping("add-to-sell-cart")
	public String addToSellCart(
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("sellCart") Cart sellCart
	) {
		Product product = productService.getProductById(productId).get();

		if (sellCart.getQuantity(product).getAmount().intValue() <
			(product instanceof Flower ? ((Flower) product).getQuantity() :
				((Bouquet) product).getQuantity())) {
			sellCart.addOrUpdateItem(product, 1);

			double fp = salesService.calculateFullCartPrice(model, sellCart, true);
			model.addAttribute("fullSellPrice", fp);
		}

		return "redirect:/sell";
	}

	/**
	 * Removes a product entirely from the sell cart.
	 *
	 * @param model     the model to hold attributes for the view
	 * @param productId the ID of the product to remove
	 * @param sellCart  the cart from which the product is removed
	 * @return the redirect URL to the selling catalog
	 */
	@PostMapping("remove-from-sell-cart")
	public String removeFromSellCart(
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("sellCart") Cart sellCart
	) {
		Product product = productService.getProductById(productId).get();

		sellCart.addOrUpdateItem(product, -1.0 * sellCart.getQuantity(product).getAmount().doubleValue());

		double fp = salesService.calculateFullCartPrice(model, sellCart, true);
		model.addAttribute("fullSellPrice", fp);

		return "redirect:/sell";
	}


	/**
	 * Decreases a product quantity by one in the sell cart.
	 *
	 * @param model     the model to hold attributes for the view
	 * @param productId the ID of the product to remove
	 * @param sellCart  the cart for which the product quantity is changed
	 * @return the redirect URL to the selling catalog
	 */
	@PostMapping("decrease-from-sell-cart")
	public String decreaseFromSellCart(
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("sellCart") Cart sellCart
	) {
		Product product = productService.getProductById(productId).get();

		sellCart.addOrUpdateItem(product, -1);

		double fp = salesService.calculateFullCartPrice(model, sellCart, true);
		model.addAttribute("fullSellPrice", fp);

		return "redirect:/sell";
	}

	/**
	 * Adds a product to the buy cart.
	 *
	 * @param model     the model to hold attributes for the view
	 * @param productId the ID of the product to add
	 * @param buyCart   the cart to which the product is added
	 * @return the redirect URL to the selling catalog
	 */
	@PostMapping("add-to-buy-cart")
	public String addToBuyCart(Model model,
							   @RequestParam UUID productId,
							   @ModelAttribute("buyCart") Cart buyCart) {
		Product product = productService.getProductById(productId).get();
		buyCart.addOrUpdateItem(product, 1);

		double fp = salesService.calculateFullCartPrice(model, buyCart, false);
		model.addAttribute("fullBuyPrice", fp);

		return "redirect:/buy";
	}

	/**
	 * Removes a product entirely from the buy cart.
	 *
	 * @param model     the model to hold attributes for the view
	 * @param productId the ID of the product to remove
	 * @param buyCart   the cart from which the product is removed
	 * @return the redirect URL to the buy catalog
	 */
	@PostMapping("remove-from-buy-cart")
	public String removeFromBuyCart(
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("buyCart") Cart buyCart
	) {
		Product product = productService.getProductById(productId).get();

		buyCart.addOrUpdateItem(product, -1.0 * buyCart.getQuantity(product).getAmount().doubleValue());

		double fp = salesService.calculateFullCartPrice(model, buyCart, false);
		model.addAttribute("fullBuyPrice", fp);
		return "redirect:/buy";
	}

	/**
	 * Decreases a product quantity by one in the buy cart.
	 *
	 * @param model     the model to hold attributes for the view
	 * @param productId the ID of the product to remove
	 * @param buyCart   the cart for which the product quantity is changed
	 * @return the redirect URL to the selling catalog
	 */
	@PostMapping("decrease-from-buy-cart")
	public String decreaseFromBuyCart(
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("buyCart") Cart buyCart
	) {
		Product product = productService.getProductById(productId).get();

		buyCart.addOrUpdateItem(product, -1);

		double fp = salesService.calculateFullCartPrice(model, buyCart, false);
		model.addAttribute("fullBuyPrice", fp);
		return "redirect:/buy";
	}

	@GetMapping("/giftcard")
	@PreAuthorize("hasRole('BOSS')")
	public String giftCard(Model model) {
		return "sales/giftcard";
	}

	@PostMapping("create-giftcard")
	@PreAuthorize("hasRole('BOSS')")
	public String createGiftCard(Model model,
								 @RequestParam(required = true) Integer amount) {
		GiftCard giftCard = new GiftCard(Money.of(amount, "EUR"), amount.toString());
		giftCardRepository.save(giftCard);

		model.addAttribute("giftCardId", giftCard.getId());
		return "sales/giftcard";
	}

	@GetMapping("/check-balance")
	@PreAuthorize("hasRole('BOSS')")
	public String checkGiftCardBalance(Model model,
									   @RequestParam String giftCardId
	) {
		giftCardRepository.findAll()
			.stream()
			.filter(giftCard -> giftCard.getId().equals(giftCardId.trim()))
			.findFirst()
			.ifPresentOrElse(
				giftCard -> model.addAttribute("giftCardBalance", giftCard.getBalance()),
				() -> model.addAttribute("giftCardBalance", "THERE IS NO CARD WITH THIS ID")
			);

		return "sales/giftcard";
	}
}
 