package flowershop.sales;

import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.ProductService;
import flowershop.services.OrderFactory;
import org.salespointframework.order.OrderEvents;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.salespointframework.catalog.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;

@SessionAttributes({"buyBasket", "sellBasket"})
@Controller
public class SalesController {

	private final ProductService productService;
	private final OrderFactory orderFactory;
	private final SimpleOrderService simpleOrderService;
	private final WholesalerOrderService wholesalerOrderService;

	SalesController(ProductService productService, OrderFactory orderFactory, SimpleOrderService simpleOrderService, WholesalerOrderService wholesalerOrderService) {
		this.productService = productService;
		this.orderFactory = orderFactory;
		this.simpleOrderService = simpleOrderService;
		this.wholesalerOrderService = wholesalerOrderService;
	}

	@ModelAttribute("buyBasket")
	public List<BasketItem> createBuyBasket() {
		return new ArrayList<>();
	}

	@ModelAttribute("sellBasket")
	public List<BasketItem> createSellBasket() {
		return new ArrayList<>();
	}

	@GetMapping("/sell")
	public String sell(Model model,
					   @RequestParam(required = false) String filterItem,
					   @RequestParam(required = false) String searchInput,
					   @ModelAttribute("sellBasket") List<BasketItem> sellBasket) {

		List<Flower> flowers = productService.getAllFlowers();
		List<Bouquet> bouquets = productService.getAllBouquets();

		//List<Product> products = productService.getAllProducts(); // -------------- Please use me <3

		// Filter by color
		// TODO: filter by something else?
		if (filterItem != null && !filterItem.isEmpty()) {
			flowers = productService.findFlowersByColor(filterItem);
		}

		// Search by name
		if (searchInput != null && !searchInput.isEmpty()) {
			flowers = productService.findFlowersByName(searchInput);
		}

		Set<String> colors = productService.getAllFlowerColors();

		model.addAttribute("typeList", colors);
		model.addAttribute("filterItem", filterItem);
		model.addAttribute("searchInput", searchInput);

		model.addAttribute("flowers", flowers);
		model.addAttribute("bouquets", bouquets);
		model.addAttribute("sellBasket", sellBasket);

		return "sales/sell";
	}

	@GetMapping("/buy")
	public String buy(Model model,
					  @RequestParam(required = false) String filterItem,
					  @RequestParam(required = false) String searchInput,
					  @ModelAttribute("buyBasket") List<BasketItem> buyBasket) {

		// Shouldn't allow to work with bouquets because wholesalers sell only flowers.
		List<Flower> flowers = productService.getAllFlowers();

		// Only by color? Seems reasonable but who knows.
		if (filterItem != null && !filterItem.isEmpty()) {
			flowers = productService.findFlowersByColor(filterItem);
		}

		if (searchInput != null && !searchInput.isEmpty()) {
			flowers = productService.findFlowersByName(searchInput);
		}

		Set<String> colors = productService.getAllFlowerColors();

		model.addAttribute("typeList", colors);
		model.addAttribute("filterItem", filterItem);
		model.addAttribute("searchInput", searchInput);
		model.addAttribute("flowers", flowers);
		model.addAttribute("buyBasket", buyBasket);

		return "sales/buy";
	}

	@PostMapping("/add-to-buyBasket")
	public String addToBuyBasket(
		Model model,
		@RequestParam String productName,
		@RequestParam(required = false) String redirectPage,
		@ModelAttribute("buyBasket") List<BasketItem> buyBasket
	) {
		Product product = productService.findByName(productName);

		Optional<BasketItem> basketItem = buyBasket.stream()
			.filter(item -> item.getProduct().equals(product))
			.findFirst();

		if (product != null) {
			if (basketItem.isPresent()) {
				basketItem.get().increaseQuantity();
			} else {
				buyBasket.add(new BasketItem(product, 1));
			}
		}

		model.addAttribute("buyBasket", buyBasket);

		return "redirect:/" + redirectPage; // Reload the page
	}

	@PostMapping("/add-to-sellBasket")
	public String addToSellBasket(
		Model model,
		@RequestParam String productName,
		@RequestParam(required = false) String redirectPage,
		@ModelAttribute("sellBasket") List<BasketItem> sellBasket
	) {
		Product product = productService.findByName(productName);

		Optional<BasketItem> basketItem = sellBasket.stream()
			.filter(item -> item.getProduct().equals(product))
			.findFirst();

		if (product != null) {
			if (basketItem.isPresent()) {
				basketItem.get().increaseQuantity();
			} else {
				sellBasket.add(new BasketItem(product, 1));
			}
		}

		model.addAttribute("sellBasket", sellBasket);

		return "redirect:/" + redirectPage; // Reload the page
	}

	@PostMapping("/add-to-sellBasket-bouqet")
	public String addBouquetToBasket(
		Model model,
		@RequestParam String productName,
		@RequestParam(required = false) String redirectPage,
		@ModelAttribute("sellBasket") List<BasketItem> sellBasket
	) {
		Product product = productService.findByName(productName);

		Optional<BasketItem> basketItem = sellBasket.stream()
			.filter(item -> item.getProduct().equals(product))
			.findFirst();

		if (product != null) {
			if (basketItem.isPresent()) {
				basketItem.get().increaseQuantity();
			} else {
				sellBasket.add(new BasketItem(product, 1));
			}
		}

		model.addAttribute("sellBasket", sellBasket);

		return "redirect:/" + redirectPage;
	}

	@GetMapping("/")
	public String index() {
		return "redirect:/sell";
	}

	@PostMapping("/remove-from-sellBasket")
	public String removeFromSellBasket(
		@RequestParam String productName,
		@ModelAttribute("sellBasket") List<BasketItem> sellBasket,
		HttpServletRequest request
	) {
		String referer = request.getHeader("Referer").split("http://localhost:8080/")[1];
		sellBasket.removeIf(b -> b.getProduct().getName().equalsIgnoreCase(productName));

		return "redirect:/" + (referer == null ? "sell" : referer);
	}

	@PostMapping("/remove-from-buyBasket")
	public String removeFromBuyBasket(
		@RequestParam String productName,
		@ModelAttribute("buyBasket") List<BasketItem> buyBasket,
		HttpServletRequest request
	) {
		String referer = request.getHeader("Referer").split("http://localhost:8080/")[1];
		buyBasket.removeIf(b -> b.getProduct().getName().equalsIgnoreCase(productName));

		return "redirect:/" + (referer == null ? "sell" : referer);
	}

	/**
	 * Registers a {@link WholesalerOrder} instance based on the {@link BasketItem}s.
	 *
	 * @param basket
	 * @param request
	 * @param model
	 * @return
	 */
	@PostMapping("/buy-from-basket")
	public String buyFromBasket(
		@ModelAttribute("buyBasket") List<BasketItem> buyBasket,
		HttpServletRequest request,
		Model model
	) {
		if (buyBasket == null || buyBasket.isEmpty()) {
			model.addAttribute("message", "Your buyBasket is empty.");
			return "sales/buy";
		}

		WholesalerOrder wholesalerOrder = orderFactory.createWholesalerOrder();

		for (BasketItem basketItem : buyBasket) {
			Product product = basketItem.getProduct();

			if (product instanceof Flower) {
				productService.addFlowers((Flower) product, basketItem.getQuantityAsInteger());
				wholesalerOrder.addOrderLine(product, basketItem.getQuantity());
			} else if (product instanceof Bouquet) {
				model.addAttribute("message", "Cannot buy bouquets from Wholesaler.");
				return "sales/buy";
				//productService.addBouquets((Bouquet) product, basketItem.getQuantityAsInteger());
			}
		}
		wholesalerOrder.setPaymentMethod("Card");

		wholesalerOrderService.create(wholesalerOrder);
		var orderPaid = OrderEvents.OrderPaid.of(wholesalerOrder); // TODO: hide this logic somewhere maybe

		buyBasket.clear();

		model.addAttribute("message", "Your order has been successfully placed.");
		String referer = request.getHeader("Referer").split("http://localhost:8080/")[1];
		return "redirect:" + (referer == null ? "sell" : referer);
	}

	/**
	 * Registers a {@link SimpleOrder} instance based on the {@link BasketItem}s.
	 *
	 * @param basket
	 * @param request
	 * @param model
	 * @return
	 */
	@PostMapping("/sell-from-basket")
	public String sellFromBasket(
		@ModelAttribute("sellBasket") List<BasketItem> sellBasket,
		HttpServletRequest request,
		Model model
	) {
		if (sellBasket == null || sellBasket.isEmpty()) {
			model.addAttribute("message", "Your basket is empty.");
			return "sell";
		}

		SimpleOrder simpleOrder = orderFactory.createSimpleOrder();
		for (BasketItem basketItem : sellBasket) {
			Product product = basketItem.getProduct();

			if (product instanceof Flower) {
				productService.removeFlowers((Flower) product, basketItem.getQuantityAsInteger());
			} else if (product instanceof Bouquet) {
				//productService.removeBouquet((Bouquet) product, basketItem.getQuantity());
				productService.removeBouquet((Bouquet) product);
			}
			simpleOrder.addOrderLine(product, basketItem.getQuantity());

		}
		simpleOrder.setPaymentMethod("Cash");

		simpleOrderService.create(simpleOrder);
		var orderPaid = OrderEvents.OrderPaid.of(simpleOrder); // TODO: hide this logic somewhere maybe

		sellBasket.clear();

		model.addAttribute("message", "Your order has been successfully placed.");
		String referer = request.getHeader("Referer").split("http://localhost:8080/")[1];
		return "redirect:" + (referer == null ? "sell" : referer);
	}

}
 