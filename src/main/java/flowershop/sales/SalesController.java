package flowershop.sales;

import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.ProductService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;

import org.salespointframework.catalog.Product;

import java.util.*;
import java.util.stream.Stream;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;


@SessionAttributes({"buyBasket", "sellBasket", "fullSellPrice", "fullBuyPrice"})
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

		// Filter products with quantity > 0
		flowers = productService.filterFlowersInStock(flowers);
		bouquets = productService.filterBouquetsInStock(bouquets);

		// Add both flowers and bouquets together.
		List<Product> products = new ArrayList<>();
		products.addAll(flowers);
		products.addAll(bouquets);

		Set<String> colors = productService.getAllFlowerColors();

		model.addAttribute("typeList", colors);
		model.addAttribute("filterItem", filterItem);
		model.addAttribute("searchInput", searchInput);
		model.addAttribute("products", products);
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
		model.addAttribute("fullBuyPrice", calculateFullBasketPrice(buyBasket));
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
		model.addAttribute("fullSellPrice", calculateFullBasketPrice(sellBasket));
		return "redirect:/sell";
	}

	@PostMapping("/remove-from-sellBasket")
	public String removeFromSellBasket(
		Model model,
		@RequestParam UUID productId, // Use UUID instead of product name
		@ModelAttribute("sellBasket") List<BasketItem> sellBasket,
		HttpServletRequest request
	) {
		basketService.removeFromBasket(sellBasket, productId);
		model.addAttribute("fullSellPrice", calculateFullBasketPrice(sellBasket));
		return "redirect:/sell";
	}

	@PostMapping("/remove-from-buyBasket")
	public String removeFromBuyBasket(
		Model model,
		@RequestParam UUID productId, // Use UUID instead of product name
		@ModelAttribute("buyBasket") List<BasketItem> buyBasket,
		HttpServletRequest request
	) {
		basketService.removeFromBasket(buyBasket, productId);
		model.addAttribute("fullBuyPrice", calculateFullBasketPrice(buyBasket));
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
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("sellBasket") List<BasketItem> sellBasket
	) {
		basketService.increaseQuantity(sellBasket, productId);
		model.addAttribute("fullSellPrice", calculateFullBasketPrice(sellBasket));
		return "redirect:/sell";
	}

	@PostMapping("/decrease-from-sellBasket")
	public String decreaseFromSellBasket(
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("sellBasket") List<BasketItem> sellBasket
	) {
		basketService.decreaseQuantity(sellBasket, productId);
		model.addAttribute("fullSellPrice", calculateFullBasketPrice(sellBasket));
		return "redirect:/sell";
	}

	@PostMapping("/increase-from-buyBasket")
	public String increaseFromBuyBasket(
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("buyBasket") List<BasketItem> buyBasket
	) {
		basketService.increaseQuantity(buyBasket, productId);
		model.addAttribute("fullBuyPrice", calculateFullBasketPrice(buyBasket));
		return "redirect:/buy";
	}

	@PostMapping("/decrease-from-buyBasket")
	public String decreaseFromBuyBasket(
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("buyBasket") List<BasketItem> buyBasket
	) {
		basketService.decreaseQuantity(buyBasket, productId);
		model.addAttribute("fullBuyPrice", calculateFullBasketPrice(buyBasket));
		return "redirect:/buy";
	}

	public double calculateFullBasketPrice(List<BasketItem> basket) {

		double fp = basket.stream()
		.mapToDouble(bi -> {
			if (bi.getProduct() instanceof Flower flower) {
				return flower.getPricing().getSellPrice().getNumber().doubleValue() * 1.0 * bi.getQuantityAsInteger() * 1.0;
			} else if (bi.getProduct() instanceof Bouquet bouquet) {
				return bouquet.getPrice().getNumber().doubleValue() * 1.0 * bi.getQuantityAsInteger() * 1.0;
			} else {
				return 0;
			}
		}).sum();
		System.out.println(fp);

		return fp;
	}
}
 