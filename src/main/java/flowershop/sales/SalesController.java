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
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;

@SessionAttributes("basket")
@Controller
public class SalesController {

	private final ProductService productService;

	SalesController(ProductService productService) {
		this.productService = productService;
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

		List<Flower> flowers = productService.getAllFlowers();

		System.out.println(basket);

		if (filterItem != null && !filterItem.isEmpty()) {
			flowers = flowers.stream()
				.filter(flower -> flower.getColor().equalsIgnoreCase(filterItem))
				.toList();
		}

		if (searchInput != null && !searchInput.isEmpty()) {
			flowers = flowers.stream()
				.filter(flower -> flower.getName().toLowerCase().contains(searchInput.toLowerCase()))
				.toList();
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

		List<Flower> flowers = productService.getAllFlowers();

		System.out.println(basket);

		if (filterItem != null && !filterItem.isEmpty()) {
			flowers = flowers.stream()
				.filter(flower -> flower.getColor().equalsIgnoreCase(filterItem))
				.toList();
		}

		if (searchInput != null && !searchInput.isEmpty()) {
			flowers = flowers.stream()
				.filter(flower -> flower.getName().toLowerCase().contains(searchInput.toLowerCase()))
				.toList();
		}

		Set<String> colors = productService.getAllFlowers()
			.stream()
			.map(Flower::getColor)
			.collect(Collectors.toSet());

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
		return "redirect:sales/sell";
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

		for (BasketItem basketItem : basket) {
			Product product = basketItem.getProduct();

			if (product instanceof Flower) {
				productService.addFlowers((Flower) product, basketItem.getQuantity());
			} else if (product instanceof Bouquet) {
				productService.addBouquets((Bouquet) product, basketItem.getQuantity());
			}
		}

		basket.clear();

		// Optionally, you could create an order or transaction record in the database here

		model.addAttribute("message", "Your order has been successfully placed.");
		String referer = request.getHeader("Referer").split("http://localhost:8080/")[1];
		return "redirect:" + (referer == null ? "sell" : referer);

	}

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

		for (BasketItem basketItem : basket) {
			Product product = basketItem.getProduct();

			if (product instanceof Flower) {
				productService.removeFlowers((Flower) product, basketItem.getQuantity());
			} else if (product instanceof Bouquet) {
				//productService.removeBouquet((Bouquet) product, basketItem.getQuantity());
				productService.removeBouquet((Bouquet) product);
			}
		}
		basket.clear();

		// Optionally, you could create an order or transaction record in the database here

		model.addAttribute("message", "Your order has been successfully placed.");
		String referer = request.getHeader("Referer").split("http://localhost:8080/")[1];
		return "redirect:" + (referer == null ? "sell" : referer);
	}
}
 