package flowershop.sales;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.salespointframework.order.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import flowershop.AbstractIntegrationTests;

import java.util.Set;

public class SalesControllerIntegrationTests extends AbstractIntegrationTests {
	@Autowired
	SalesController controller;


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

}

