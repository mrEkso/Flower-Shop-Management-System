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

import java.util.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;


@SessionAttributes({"buyBasket", "sellBasket"})
@Controller
public class SalesController {

	private final ProductService productService;
	private final SalesService salesService;
	private final WholesalerService wholesalerService;
	private final BasketService basketService;

	SalesController(ProductService productService, SalesService salesService, WholesalerService wholesalerService, BasketService basketService) {
		this.productService = productService;
		this.salesService = salesService;
		this.wholesalerService = wholesalerService;
		this.basketService = basketService;
	}

	@ModelAttribute("buyBasket")
	public List<BasketItem> createBuyBasket() {
		return new ArrayList<>();
	}

	@ModelAttribute("sellBasket")
	public List<BasketItem> createSellBasket() {
		return new ArrayList<>();
	}

	@GetMapping("/")
	public String index() {
		return "redirect:/sell";
	}

	@GetMapping("/sell")
	public String sellCatalog(Model model,
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
			bouquets = productService.findBouquetsByName(searchInput);
		}

		List<Product> products = new ArrayList<>();
		products.addAll(flowers);
		products.addAll(bouquets);
		Set<String> colors = productService.getAllFlowerColors();

		model.addAttribute("typeList", colors);
		model.addAttribute("filterItem", filterItem);
		model.addAttribute("searchInput", searchInput);
		model.addAttribute("products", products);
		model.addAttribute("flowers", flowers);
		model.addAttribute("bouquets", bouquets);
		model.addAttribute("sellBasket", sellBasket);

		return "sales/sell";
	}

	@GetMapping("/buy")
	public String buyCatalog(Model model,
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
		@RequestParam UUID productId, // Use UUID instead of product name
		@ModelAttribute("buyBasket") List<BasketItem> buyBasket
	) {

		basketService.addToBasket(buyBasket, productId);

		//model.addAttribute("buyBasket", buyBasket);

		return "redirect:/buy";
	}

	@PostMapping("/add-to-sellBasket")
	public String addToSellBasket(
		Model model,
		@RequestParam UUID productId, // Use UUID instead of product name
		@RequestParam(required = false) String redirectPage,
		@ModelAttribute("sellBasket") List<BasketItem> sellBasket
	) {
		basketService.addToBasket(sellBasket, productId);

		//model.addAttribute("sellBasket", sellBasket);

		return "redirect:/sell";
	}

	@PostMapping("/remove-from-sellBasket")
	public String removeFromSellBasket(
		@RequestParam UUID productId, // Use UUID instead of product name
		@ModelAttribute("sellBasket") List<BasketItem> sellBasket,
		HttpServletRequest request
	) {
		basketService.removeFromBasket(sellBasket, productId);
		return "redirect:/sell";
	}

	@PostMapping("/remove-from-buyBasket")
	public String removeFromBuyBasket(
		@RequestParam UUID productId, // Use UUID instead of product name
		@ModelAttribute("buyBasket") List<BasketItem> buyBasket,
		HttpServletRequest request
	) {
		basketService.removeFromBasket(buyBasket, productId);
		return "redirect:/buy";
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
			return "redirect:/buy";
		}

		salesService.buyProductsFromBasket(buyBasket, "Card");

		model.addAttribute("message", "Your order has been successfully placed.");
		return "redirect:/buy";
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
			return "redirect:sell";
		}
		salesService.sellProductsFromBasket(sellBasket, "Cash");

		model.addAttribute("message", "Your order has been successfully placed.");
		return "redirect:sell";
	}

	@PostMapping("/increase-from-sellBasket")
	public String increaseFromSellBasket(
		@RequestParam UUID productId,
		@ModelAttribute("sellBasket") List<BasketItem> sellBasket
	) {
		basketService.increaseQuantity(sellBasket, productId);
		return "redirect:/sell";
	}

	@PostMapping("/decrease-from-sellBasket")
	public String decreaseFromSellBasket(
		@RequestParam UUID productId,
		@ModelAttribute("sellBasket") List<BasketItem> sellBasket
	) {
		basketService.decreaseQuantity(sellBasket, productId);
		return "redirect:/sell";
	}

	@PostMapping("/increase-from-buyBasket")
	public String increaseFromBuyBasket(
		@RequestParam UUID productId,
		@ModelAttribute("buyBasket") List<BasketItem> buyBasket
	) {
		basketService.increaseQuantity(buyBasket, productId);
		return "redirect:/buy";
	}

	@PostMapping("/decrease-from-buyBasket")
	public String decreaseFromBuyBasket(
		@RequestParam UUID productId,
		@ModelAttribute("buyBasket") List<BasketItem> buyBasket
	) {
		basketService.decreaseQuantity(buyBasket, productId);
		return "redirect:/buy";
	}
}
 