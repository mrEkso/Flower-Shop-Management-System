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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;



@SessionAttributes({"fullSellPrice", "fullBuyPrice",
"buyCart", "sellCart"})
@Controller
public class SalesController {

	private final ProductService productService;
	private final SalesService salesService;
	private final WholesalerService wholesalerService;
	// private final BasketService basketService;
	
	SalesController(ProductService productService, SalesService salesService, WholesalerService wholesalerService/*, BasketService basketService*/) {
		this.productService = productService;
		this.salesService = salesService;
		this.wholesalerService = wholesalerService;
		// this.basketService = basketService;
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
		return "redirect:/sell";
	}

	@GetMapping("/sell")
	public String sellCatalog(Model model,
					   @RequestParam(required = false) String filterItem,
					   @RequestParam(required = false) String searchInput) {

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

		return "sales/sell";
	}

	@GetMapping("/buy")
	public String buyCatalog(Model model,
					  @RequestParam(required = false) String filterItem,
					  @RequestParam(required = false) String searchInput) {

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

		return "sales/buy";
	}

	/**
	 * Registers a {@link SimpleOrder} instance based on the {@link BasketItem}s.
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
		
		calculateFullCartPrice(model, sellCart, true);

		model.addAttribute("message", "Your order has been successfully placed.");
		return "redirect:sell";
	}

	
	/**
	 * Registers a {@link SimpleOrder} instance based on the {@link BasketItem}s.
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
		
		calculateFullCartPrice(model, buyCart, false);

		model.addAttribute("message", "Your order has been successfully placed.");
		return "redirect:buy";
	}

	public double calculateFullCartPrice(Model model, Cart cart, Boolean isSellPage) {

		double fp = cart.get()
		.mapToDouble(bi -> {
			if (bi.getProduct() instanceof Flower flower) {
				return flower.getPricing().getSellPrice().getNumber().doubleValue() * 1.0 * bi.getQuantity().getAmount().doubleValue() * 1.0;
			} else if (bi.getProduct() instanceof Bouquet bouquet) {
				return bouquet.getPrice().getNumber().doubleValue() * 1.0 * bi.getQuantity().getAmount().doubleValue() * 1.0;
			} else {
				return 0;
			}
		}).sum();
		System.out.println(fp);

		model.addAttribute(isSellPage?"fullSellPrice":"fullBuyPrice", fp);

		return fp;
	}

	@PostMapping("add-to-sell-cart")
	public String addToSellCart(
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("sellCart") Cart sellCart
	){
		// if(sellCart.getItem(
		// 	product.getId().toString()).isPresent()){}
		Product product = productService.getProductById(productId).get();
		sellCart.addOrUpdateItem(product, 1);

		System.out.println("--------------ATSC--------ADD-----");
		System.out.println(sellCart.size());
		
		calculateFullCartPrice(model, sellCart, true);

		return "redirect:/sell";
	}

	@PostMapping("remove-from-sell-cart")
	public String removeFromSellCart(
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("sellCart") Cart sellCart
	){
		// if(sellCart.getItem(
		// 	product.getId().toString()).isPresent()){}
		
		Product product = productService.getProductById(productId).get();

		sellCart.addOrUpdateItem(product, -1.0 * sellCart.getQuantity(product).getAmount().doubleValue());
		
		System.out.println("--------------ATSC-------REMOVE------");
		System.out.println(sellCart.size());
		
				calculateFullCartPrice(model, sellCart, true);;

		return "redirect:/sell";
	}

	@PostMapping("decrease-from-sell-cart")
	public String decreaseFromSellCart(
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("sellCart") Cart sellCart
	){
		// if(sellCart.getItem(
		// 	product.getId().toString()).isPresent()){}
		
		Product product = productService.getProductById(productId).get();

		sellCart.addOrUpdateItem(product, -1);
		
		System.out.println("--------------ATSC------DECRE-------");
		System.out.println(sellCart.size());
		
				calculateFullCartPrice(model, sellCart, true);;

		return "redirect:/sell";
	}

	@PostMapping("add-to-buy-cart")
	public String addToBuyCart(
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("buyCart") Cart buyCart
	){
		// if(sellCart.getItem(
		// 	product.getId().toString()).isPresent()){}
		Product product = productService.getProductById(productId).get();
		buyCart.addOrUpdateItem(product, 1);

		System.out.println("--------------ATBC--------ADD-----");
		System.out.println(buyCart.size());
		
			calculateFullCartPrice(model, buyCart, false);;

		return "redirect:/buy";
	}

	@PostMapping("remove-from-buy-cart")
	public String removeFromBuyCart(
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("buyCart") Cart buyCart
	){
		// if(sellCart.getItem(
		// 	product.getId().toString()).isPresent()){}
		
		Product product = productService.getProductById(productId).get();

		buyCart.addOrUpdateItem(product, -1.0 * buyCart.getQuantity(product).getAmount().doubleValue());
		
		System.out.println("--------------ATSC-------REMOVE------");
		System.out.println(buyCart.size());
		
				calculateFullCartPrice(model, buyCart, false);;

		return "redirect:/buy";
	}

	@PostMapping("decrease-from-buy-cart")
	public String decreaseFromBuyCart(
		Model model,
		@RequestParam UUID productId,
		@ModelAttribute("buyCart") Cart buyCart
	){
		// if(sellCart.getItem(
		// 	product.getId().toString()).isPresent()){}
		
		Product product = productService.getProductById(productId).get();

		buyCart.addOrUpdateItem(product, -1);
		
		System.out.println("--------------ATSC------DECRE-------");
		System.out.println(buyCart.size());

				calculateFullCartPrice(model, buyCart, false);;

		return "redirect:/buy";
	}
}
 