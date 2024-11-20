package flowershop.sales;

import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.ProductService;
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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;


@SessionAttributes({"buyBasket", "sellBasket"})
@Controller
public class SalesController {

	private final ProductService productService;
	private final SalesService salesService;
	private final WholesalerService wholesalerService;

	SalesController(ProductService productService, SalesService salesService, WholesalerService wholesalerService) {
		this.productService = productService;
		this.salesService = salesService;
		this.wholesalerService = wholesalerService;
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

		List<Flower> flowers = productService.findAllFlowers();
		List<Bouquet> bouquets = productService.findAllBouquets();

		//List<Product> products = productService.getAllProducts(); // -------------- Please use me <3

		// Filter by color
		if (filterItem != null && !filterItem.isEmpty()) {
			flowers = productService.findFlowersByColor(filterItem);
			bouquets = new ArrayList<>();
		}

		// Search by name
		if (searchInput != null && !searchInput.isEmpty()) {
			flowers = productService.findFlowersByName(searchInput);
			bouquets = new ArrayList<>();    // FIXME: allow search for bouquets!
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
		List<Flower> flowers = wholesalerService.findAllFlowers();

		// Only by color? Seems reasonable but who knows.
		if (filterItem != null && !filterItem.isEmpty()) {
			flowers = wholesalerService.findFlowersByColor(filterItem);
		}

		if (searchInput != null && !searchInput.isEmpty()) {
			flowers = wholesalerService.findFlowersByName(searchInput);
		}

		Set<String> colors = wholesalerService.findAllFlowerColors();

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
		@RequestParam String productName, // FIXME Use ID!
		@RequestParam(required = false) String redirectPage,
		@ModelAttribute("buyBasket") List<BasketItem> buyBasket
	) {
		addToBasket(productName, buyBasket);

		model.addAttribute("buyBasket", buyBasket);

		return "redirect:/" + redirectPage; // Reload the page
	}

	@PostMapping("/add-to-sellBasket")
	public String addToSellBasket(
		Model model,
		@RequestParam String productName,// FIXME Use ID!
		@RequestParam(required = false) String redirectPage,
		@ModelAttribute("sellBasket") List<BasketItem> sellBasket
	) {
		addToBasket(productName, sellBasket);

		model.addAttribute("sellBasket", sellBasket);

		return "redirect:/" + redirectPage; // Reload the page
	}

	private void addToBasket(String productName, List<BasketItem> basket) {
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
	}

	private void removeFromBasket(String productName, List<BasketItem> basket) {
		basket.removeIf(b -> b.getProduct().getName().equalsIgnoreCase(productName));
	}

	@PostMapping("/add-to-sellBasket-bouquet")
	public String addBouquetToBasket(
		Model model,
		@RequestParam String productName,    // FIXME Use ID!
		@RequestParam(required = false) String redirectPage,
		@ModelAttribute("sellBasket") List<BasketItem> sellBasket
	) {
		addToBasket(productName, sellBasket);

		model.addAttribute("sellBasket", sellBasket);

		return "redirect:/" + redirectPage;
	}

	@GetMapping("/")
	public String index() {
		return "redirect:/sell";
	}

	@PostMapping("/remove-from-sellBasket")
	public String removeFromSellBasket(
		@RequestParam String productName,// FIXME Use ID!
		@ModelAttribute("sellBasket") List<BasketItem> sellBasket,
		HttpServletRequest request
	) {
		String referer = request.getHeader("Referer").split("http://localhost:8080/")[1];

		removeFromBasket(productName, sellBasket);

		return "redirect:/" + (referer == null ? "sell" : referer);
	}

	@PostMapping("/remove-from-buyBasket")
	public String removeFromBuyBasket(
		@RequestParam String productName,// FIXME Use ID!
		@ModelAttribute("buyBasket") List<BasketItem> buyBasket,
		HttpServletRequest request
	) {
		String referer = request.getHeader("Referer").split("http://localhost:8080/")[1];
		removeFromBasket(productName, buyBasket);

		return "redirect:/" + (referer == null ? "sell" : referer);
	}

	/**
	 * Registers a {@link WholesalerOrder} instance based on the {@link BasketItem}s.
	 */
	@PostMapping("buy-from-buyBasket")
	public String buyFromBasket(
		@ModelAttribute("buyBasket") List<BasketItem> buyBasket,
		HttpServletRequest request,
		Model model
	) {
		if (buyBasket == null || buyBasket.isEmpty()) {
			model.addAttribute("message", "Your buyBasket is empty.");
			return "sales/buy";
		}

		salesService.buyProductsFromBasket(buyBasket, "Card");

		model.addAttribute("message", "Your order has been successfully placed.");
		String referer = request.getHeader("Referer").split("http://localhost:8080/")[1];
		return "redirect:" + (referer == null ? "sell" : referer);
	}

	/**
	 * Registers a {@link SimpleOrder} instance based on the {@link BasketItem}s.
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

		salesService.sellProductsFromBasket(sellBasket, "Cash");

		model.addAttribute("message", "Your order has been successfully placed.");
		String referer = request.getHeader("Referer").split("http://localhost:8080/")[1];
		return "redirect:" + (referer == null ? "sell" : referer);
	}

	@PostMapping("/increase-from-sellBasket")
	public String increaseFromSellBasket(
		@RequestParam String productName,
		@ModelAttribute("sellBasket") List<BasketItem> sellBasket
	) {

		sellBasket.stream()
			.filter(b -> b.getProduct().getName().equalsIgnoreCase(productName))
			.findFirst()
			.get()
			.increaseQuantity();

		return "redirect:/sell";
	}

	@PostMapping("/decrease-from-sellBasket")
	public String decreaseFromSellBasket(
		@RequestParam String productName,
		@ModelAttribute("sellBasket") List<BasketItem> sellBasket
	) {
		Boolean isLast = sellBasket.stream().filter(b -> b.getProduct().getName().equalsIgnoreCase(productName)).findFirst().get().tryDecreaseQuantity();
		if (!isLast) removeFromBasket(productName, sellBasket);

		return "redirect:/sell";
	}

	@PostMapping("/increase-from-buyBasket")
	public String increaseFromBuyBasket(
		@RequestParam String productName,
		@ModelAttribute("buyBasket") List<BasketItem> buyBasket
	) {

		buyBasket.stream()
			.filter(b -> b.getProduct().getName().equalsIgnoreCase(productName))
			.findFirst()
			.get()
			.increaseQuantity();

		return "redirect:/buy";
	}

	@PostMapping("/decrease-from-buyBasket")
	public String decreaseFromBuyBasket(
		@RequestParam String productName,
		@ModelAttribute("buyBasket") List<BasketItem> buyBasket
	) {
		Boolean isLast = buyBasket.stream().filter(b -> b.getProduct().getName().equalsIgnoreCase(productName)).findFirst().get().tryDecreaseQuantity();
		if (!isLast) removeFromBasket(productName, buyBasket);

		return "redirect:/buy";
	}
}
 