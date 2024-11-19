package flowershop.sales;

import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.ProductService;
import flowershop.services.OrderFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.OrderEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SessionAttributes("basket")
@Controller
public class SalesController {

	private final ProductService productService;
	private final OrderFactory orderFactory;
	private final SimpleOrderService simpleOrderService;
	private final WholesalerOrderService wholesalerOrderService;
	@Autowired
	private ApplicationEventPublisher eventPublisher;

	SalesController(ProductService productService, OrderFactory orderFactory, SimpleOrderService simpleOrderService, WholesalerOrderService wholesalerOrderService) {
		this.productService = productService;
		this.orderFactory = orderFactory;
		this.simpleOrderService = simpleOrderService;
		this.wholesalerOrderService = wholesalerOrderService;
	}

	@ModelAttribute("basket")
	public List<BasketItem> createBasket() {
		return new ArrayList<>();
	}

	@GetMapping("/sell")
	public String sell(Model model,
					   @RequestParam(required = false) String filterItem,
					   @RequestParam(required = false) String searchInput,
					   @ModelAttribute("basket") List<BasketItem> basket) {

		// FIXME: does not allow to work with bouquets!
		List<Flower> flowers = productService.getAllFlowers();

		//List<Product> products = productService.getAllProducts(); // -------------- Please use me <3

		// Filter by color
		// TODO: filter by something else?
		if (filterItem != null && !filterItem.isEmpty()) {
			flowers = productService.findFlowersByColor(filterItem);
		}

		// Search by name
		// TODO: search by something else?
		if (searchInput != null && !searchInput.isEmpty()) {
			flowers = productService.findFlowersByName(searchInput);
		}

		Set<String> colors = productService.getAllFlowerColors();

		model.addAttribute("typeList", colors);
		model.addAttribute("filterItem", filterItem);
		model.addAttribute("searchInput", searchInput);
		model.addAttribute("flowers", flowers);
		model.addAttribute("basket", basket);

		return "sales/sell";
	}

	@GetMapping("/buy")
	public String buy(Model model,
					  @RequestParam(required = false) String filterItem,
					  @RequestParam(required = false) String searchInput,
					  @ModelAttribute("basket") List<BasketItem> basket) {

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
		model.addAttribute("basket", basket);

		return "sales/buy";
	}

	@PostMapping("/add-to-basket")
	public String addToBasket(
		Model model,
		@RequestParam String productName,
		@RequestParam(required = false) String redirectPage,
		@ModelAttribute("basket") List<BasketItem> basket
	) {
		Product product = productService.findByName(productName);

		Optional<BasketItem> basketItem = basket.stream()
			.filter(item -> item.getProduct().equals(product))
			.findFirst();

		if (product != null) {
			if (basketItem.isPresent()) {
				basketItem.get().increaseQuantity();
			} else {
				basket.add(new BasketItem(product, 1));
			}
		}

		model.addAttribute("basket", basket);

		return "redirect:/" + redirectPage; // Reload the page
	}

	@GetMapping("/")
	public String index() {
		return "redirect:/sell";
	}

	@PostMapping("/remove-from-basket")
	public String removeFromBasket(
		@RequestParam String productName,
		@ModelAttribute("basket") List<BasketItem> basket,
		HttpServletRequest request
	) {
		String referer = request.getHeader("Referer").split("http://localhost:8080/")[1];
		basket.removeIf(b -> b.getProduct().getName().equalsIgnoreCase(productName));

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
		@ModelAttribute("basket") List<BasketItem> basket,
		HttpServletRequest request,
		Model model
	) {
		if (basket == null || basket.isEmpty()) {
			model.addAttribute("message", "Your basket is empty.");
			return "sales/buy";
		}

		WholesalerOrder wholesalerOrder = orderFactory.createWholesalerOrder();

		for (BasketItem basketItem : basket) {
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
		eventPublisher.publishEvent(orderPaid);
		basket.clear();

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
		@ModelAttribute("basket") List<BasketItem> basket,
		HttpServletRequest request,
		Model model
	) {
		if (basket == null || basket.isEmpty()) {
			model.addAttribute("message", "Your basket is empty.");
			return "sell";
		}

		SimpleOrder simpleOrder = orderFactory.createSimpleOrder();
		for (BasketItem basketItem : basket) {
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
		eventPublisher.publishEvent(orderPaid);

		basket.clear();

		model.addAttribute("message", "Your order has been successfully placed.");
		String referer = request.getHeader("Referer").split("http://localhost:8080/")[1];
		return "redirect:" + (referer == null ? "sell" : referer);
	}
}
 