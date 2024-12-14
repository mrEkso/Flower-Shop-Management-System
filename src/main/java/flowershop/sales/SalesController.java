package flowershop.sales;

import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.ProductService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.salespointframework.order.Cart;

import org.salespointframework.catalog.Product;

import java.util.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;


@SessionAttributes({"fullSellPrice", "fullBuyPrice",
	"buyCart", "sellCart"})
@Controller
public class SalesController {

	private final ProductService productService;
	private final SalesService salesService;

	SalesController(ProductService productService, SalesService salesService) {
		this.productService = productService;
		this.salesService = salesService;
	}

	@ModelAttribute("buyCart")
	Cart initializeBuyCart() {
		return new Cart();
	}

	@ModelAttribute("sellCart")
	Cart initializeSellCart() {
		return new Cart();
	}

	@GetMapping("/")
	public String index() {
		return "redirect:/calendar";
	}

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

		// Add both flowers and bouquets together.
		List<Product> products = new ArrayList<>();
		products.addAll(flowers);
		products.addAll(bouquets);

		Set<String> colors = productService.getAllFlowerColors();

		model.addAttribute("typeList", colors);
		model.addAttribute("filterItem", filterItem);
		model.addAttribute("searchInput", searchInput);
		model.addAttribute("products", products);

		return "sales/sell";
	}

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
	 * Registers a {@link SimpleOrder} instance based on the {@link Cart}.
	 */
	@PostMapping("/sell-from-cart")
	public String sellFromCart(
		@ModelAttribute("sellCart") Cart sellCart, Model model
	) {

		if (sellCart == null || sellCart.isEmpty()) {
			model.addAttribute("message", "Your basket is empty.");
			return "redirect:sell";
		}
		salesService.sellProductsFromBasket(sellCart, "Cash");

		double fp = salesService.calculateFullCartPrice(model, sellCart, true);
		model.addAttribute("fullSellPrice", fp);

		model.addAttribute("message", "Your order has been successfully placed.");
		return "redirect:sell";
	}


	/**
	 * Registers a {@link SimpleOrder} instance based on the {@link Cart}.
	 */
	@PostMapping("/buy-from-cart")
	public String buyFromCart(
		@ModelAttribute("buyCart") Cart buyCart,
		Model model
	) {
		if (buyCart == null || buyCart.isEmpty()) {
			model.addAttribute("message", "Your basket is empty.");
			return "redirect:buy";
		}
		salesService.buyProductsFromBasket(buyCart, "Cash");

		double fp = salesService.calculateFullCartPrice(model, buyCart, false);
		model.addAttribute("fullBuyPrice", fp);

		model.addAttribute("message", "Your order has been successfully placed.");
		return "redirect:buy";
	}


	@PostMapping("add-to-sell-cart")
	public String addToSellCart(
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("sellCart") Cart sellCart
	) {
		Product product = productService.getProductById(productId).get();

		if(sellCart.getQuantity(product).getAmount().intValue() <
			(product instanceof Flower ? ((Flower)product).getQuantity().intValue() : 
				((Bouquet)product).getQuantity())){
			sellCart.addOrUpdateItem(product, 1);

			double fp = salesService.calculateFullCartPrice(model, sellCart, true);
			model.addAttribute("fullSellPrice", fp);
		}

		return "redirect:/sell";
	}

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

	@PostMapping("add-to-buy-cart")
	public String addToBuyCart(
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("buyCart") Cart buyCart
	) {
		Product product = productService.getProductById(productId).get();
		buyCart.addOrUpdateItem(product, 1);

		double fp = salesService.calculateFullCartPrice(model, buyCart, false);
		model.addAttribute("fullBuyPrice", fp);

		return "redirect:/buy";
	}

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
}
 