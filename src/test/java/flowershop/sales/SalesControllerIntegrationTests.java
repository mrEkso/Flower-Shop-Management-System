package flowershop.sales;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import java.util.UUID;

import flowershop.product.ProductService;
import org.junit.jupiter.api.Test;
import org.salespointframework.catalog.Product;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.salespointframework.order.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import flowershop.AbstractIntegrationTests;
import flowershop.product.ProductService;

@WithMockUser(username = "boss", roles = {"BOSS", "USER"})
public class SalesControllerIntegrationTests extends AbstractIntegrationTests {

	@Mock
	private SalesService salesService;

	@Autowired
	@InjectMocks
	private SalesController controller;

	@Autowired
	private ProductService productService;

	// @Autowired
	// ProductService productService;


	@Test
	public void testSellCatalog_DefaultView() {
		Model model = new ExtendedModelMap();

		String sellView = controller.sellCatalog(model, null, null);
		assertThat(sellView).isEqualTo("sales/sell");

		Iterable<Object> sellProducts = (Iterable<Object>) model.asMap().get("products");
		assertThat(sellProducts).isNotNull().hasSize(8);
	}

	// Test filtering "sell" catalog by color
	@Test
	public void testSellCatalog_FilterByColor() {
		Model model = new ExtendedModelMap();

		String sellView = controller.sellCatalog(model, "Red", null);
		assertThat(sellView).isEqualTo("sales/sell");

		Iterable<Object> sellProducts = (Iterable<Object>) model.asMap().get("products");
		assertThat(sellProducts).isNotNull().hasSize(1);
	}

	// Test searching "sell" catalog by name
	@Test
	public void testSellCatalog_SearchByName() {
		Model model = new ExtendedModelMap();

		String sellView = controller.sellCatalog(model, null, "Rose");
		assertThat(sellView).isEqualTo("sales/sell");

		Iterable<Object> sellProducts = (Iterable<Object>) model.asMap().get("products");
		assertThat(sellProducts).isNotNull().hasSize(2);
	}

	// Test "buy" catalog view
	@Test
	public void testBuyCatalog_DefaultView() {
		Model model = new ExtendedModelMap();

		String buyView = controller.buyCatalog(model, null, null);
		assertThat(buyView).isEqualTo("sales/buy");

		Iterable<Object> buyFlowers = (Iterable<Object>) model.asMap().get("flowers");
		assertThat(buyFlowers).isNotNull().hasSize(11);
	}

	// Test filtering "buy" catalog by color
	@Test
	public void testBuyCatalog_FilterByColor() {
		Model model = new ExtendedModelMap();

		String buyView = controller.buyCatalog(model, "Yellow", null);
		assertThat(buyView).isEqualTo("sales/buy");

		Iterable<Object> buyFlowers = (Iterable<Object>) model.asMap().get("flowers");
		assertThat(buyFlowers).isNotNull().hasSize(1);
	}

	// Test searching "buy" catalog by name
	@Test
	public void testBuyCatalog_SearchByName() {
		Model model = new ExtendedModelMap();

		String buyView = controller.buyCatalog(model, null, "Lily");
		assertThat(buyView).isEqualTo("sales/buy");

		Iterable<Object> buyFlowers = (Iterable<Object>) model.asMap().get("flowers");
		assertThat(buyFlowers).isNotNull().hasSize(1);
	}

	// Test the case when the filter and search input are empty in "sell" catalog
	@Test
	public void testSellCatalog_EmptyFilters() {
		Model model = new ExtendedModelMap();

		String sellView = controller.sellCatalog(model, "", "");
		assertThat(sellView).isEqualTo("sales/sell");

		Iterable<Object> sellProducts = (Iterable<Object>) model.asMap().get("products");
		assertThat(sellProducts).isNotNull().hasSize(8);
	}

	// Test the case when the filter and search input are empty in "buy" catalog
	@Test
	public void testBuyCatalog_EmptyFilters() {
		Model model = new ExtendedModelMap();

		String buyView = controller.buyCatalog(model, "", "");
		assertThat(buyView).isEqualTo("sales/buy");

		Iterable<Object> buyFlowers = (Iterable<Object>) model.asMap().get("flowers");
		assertThat(buyFlowers).isNotNull().hasSize(11);
	}

	// Test session attribute initialization for buy cart
	@Test
	public void testInitializeBuyCart() {
		Cart buyCart = controller.initializeBuyCart();
		assertThat(buyCart).isNotNull();
	}

	// Test session attribute initialization for sell cart
	@Test
	public void testInitializeSellCart() {
		Cart sellCart = controller.initializeSellCart();
		assertThat(sellCart).isNotNull();
	}

	// Test decreasing quantity from "buy" cart
	@Test
	public void testDecreaseFromBuyCart() {
		Model model = new ExtendedModelMap();
		Cart buyCart = controller.initializeBuyCart();

		Product product = productService.findAllFlowers().iterator().next();
		buyCart.addOrUpdateItem(product, 3);

		String buyView = controller.decreaseFromBuyCart(model, UUID.fromString(product.getId().toString()), buyCart);
		assertThat(buyView).isEqualTo("redirect:/buy");

		assertThat(buyCart.getQuantity(product).getAmount().intValue()).isEqualTo(2);

		double fullPrice = (double) model.asMap().get("fullBuyPrice");
		assertThat(fullPrice).isGreaterThan(0);
	}

	// Test decreasing quantity from "sell" cart
	@Test
	public void testDecreaseFromSellCart() {
		Model model = new ExtendedModelMap();
		Cart sellCart = controller.initializeSellCart();

		Product product = productService.findAllFlowers().iterator().next();
		sellCart.addOrUpdateItem(product, 3);

		String sellView = controller.decreaseFromSellCart(model, UUID.fromString(product.getId().toString()), sellCart);
		assertThat(sellView).isEqualTo("redirect:/sell");

		assertThat(sellCart.getQuantity(product).getAmount().intValue()).isEqualTo(2);

		double fullPrice = (double) model.asMap().get("fullSellPrice");
		assertThat(fullPrice).isGreaterThan(0);
	}

	// Test removing product from "buy" cart
	@Test
	public void testRemoveFromBuyCart() {
		Model model = new ExtendedModelMap();
		Cart buyCart = controller.initializeBuyCart();

		Product product = productService.findAllFlowers().iterator().next();
		buyCart.addOrUpdateItem(product, 3);

		assertThat(buyCart.getQuantity(product).getAmount().intValue()).isEqualTo(3);

		String buyView = controller.removeFromBuyCart(model, UUID.fromString(product.getId().toString()), buyCart);
		assertThat(buyView).isEqualTo("redirect:/buy");

		assertThat(buyCart.getQuantity(product).getAmount().intValue()).isEqualTo(0);

		double fullPrice = (double) model.asMap().get("fullBuyPrice");
		assertThat(fullPrice).isEqualTo(0);
	}

	// Test removing product from "sell" cart
	@Test
	public void testRemoveFromSellCart() {
		Model model = new ExtendedModelMap();
		Cart sellCart = controller.initializeSellCart();

		Product product = productService.findAllFlowers().iterator().next();
		sellCart.addOrUpdateItem(product, 2);

		assertThat(sellCart.getQuantity(product).getAmount().intValue()).isEqualTo(2);

		String sellView = controller.removeFromSellCart(model, UUID.fromString(product.getId().toString()), sellCart);
		assertThat(sellView).isEqualTo("redirect:/sell");

		assertThat(sellCart.getQuantity(product).getAmount().intValue()).isEqualTo(0);

		double fullPrice = (double) model.asMap().get("fullSellPrice");
		assertThat(fullPrice).isEqualTo(0);
	}

	// Test adding product to "sell" cart
	@Test
	public void testAddToSellCart() {
		Model model = new ExtendedModelMap();
		Cart sellCart = controller.initializeSellCart();

		Product product = productService.findAllFlowers().iterator().next();

		String sellView = controller.addToSellCart(model, UUID.fromString(product.getId().toString()), sellCart);
		assertThat(sellView).isEqualTo("redirect:/sell");

		assertThat(sellCart.getQuantity(product).getAmount().intValue()).isEqualTo(1);

		double fullPrice = (double) model.asMap().get("fullSellPrice");
		assertThat(fullPrice).isGreaterThan(0);
	}

	// Test adding product to "buy" cart
	@Test
	public void testAddToBuyCart() {
		Model model = new ExtendedModelMap();
		Cart buyCart = controller.initializeBuyCart();

		Product product = productService.findAllFlowers().iterator().next();

		String buyView = controller.addToBuyCart(model, UUID.fromString(product.getId().toString()), buyCart);
		assertThat(buyView).isEqualTo("redirect:/buy");

		assertThat(buyCart.getQuantity(product).getAmount().intValue()).isEqualTo(1);

		double fullPrice = (double) model.asMap().get("fullBuyPrice");
		assertThat(fullPrice).isGreaterThan(0);
	}

	@Test
	public void testSellFromCart_EmptyCart() throws InsufficientFundsException {
		Model model = new ExtendedModelMap();
		Cart emptyCart = new Cart();  // Empty cart scenario
		String paymentMethod = "Cash";
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

		String viewName = controller.sellFromCart(emptyCart, model, paymentMethod, null, redirectAttributes);

		assertThat(viewName).isEqualTo("redirect:sell");
		assertThat(model.asMap().get("message")).isEqualTo("Your basket is empty.");
	}

	@Test
	public void testBuyFromCart_EmptyCart() {
		Model model = new ExtendedModelMap();
		Cart emptyCart = new Cart();  // Empty cart scenario
		RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

		String viewName = controller.buyFromCart(emptyCart, model, redirectAttributes);

		assertThat(viewName).isEqualTo("redirect:buy");
		assertThat(model.asMap().get("message")).isEqualTo("Your basket is empty.");
	}


}

