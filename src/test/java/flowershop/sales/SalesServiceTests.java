package flowershop.sales;

import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.Pricing;
import flowershop.product.ProductService;
import flowershop.services.OrderFactory;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.OrderEvents;
import org.salespointframework.quantity.Quantity;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SalesServiceTests {
	private ProductService productService;
	private SimpleOrderService simpleOrderService;
	private OrderFactory orderFactory;
	private WholesalerOrderService wholesalerOrderService;
	private ApplicationEventPublisher eventPublisher;
	private SalesService salesService;

	@BeforeEach
	void setUp() {
		productService = mock(ProductService.class);
		simpleOrderService = mock(SimpleOrderService.class);
		orderFactory = mock(OrderFactory.class);
		wholesalerOrderService = mock(WholesalerOrderService.class);
		eventPublisher = mock(ApplicationEventPublisher.class);

		salesService = new SalesService(productService, simpleOrderService, orderFactory, wholesalerOrderService, eventPublisher);
	}

	@Test
	void sellProductsFromBasket_shouldSellProductsSuccessfully() {
		Cart cart = new Cart();
		Flower flower = new Flower("Rose", new Pricing(Money.of(20, "EUR"), Money.of(40, "EUR")), "Red", 10);

		cart.addOrUpdateItem(flower, 5);

		SimpleOrder simpleOrder = mock(SimpleOrder.class);
		when(orderFactory.createSimpleOrder()).thenReturn(simpleOrder);

		salesService.sellProductsFromBasket(cart, "Cash");

		verify(productService, times(1)).removeFlowers(flower, 5);
		verify(simpleOrder, times(1)).addOrderLine(flower, Quantity.of(5));
		verify(simpleOrderService, times(1)).create(simpleOrder);
		assertTrue(cart.isEmpty());
		verify(eventPublisher, times(1)).publishEvent(any(OrderEvents.OrderPaid.class));
	}

	@Test
	void sellProductsFromBasket_shouldThrowExceptionForEmptyBasket() {
		Cart cart = new Cart();

		assertThrows(
			IllegalArgumentException.class,
			() -> salesService.sellProductsFromBasket(cart, "CASH")
		);

	}

	@Test
	void buyProductsFromBasket_shouldBuyProductsSuccessfully() {
		Cart cart = new Cart();
		Flower flower = new Flower("Lily", new Pricing(Money.of(20, "EUR"), Money.of(40, "EUR")), "White", 10);
		Flower flower2 = new Flower("Rose", new Pricing(Money.of(10, "EUR"), Money.of(20, "EUR")), "Red", 15);

		cart.addOrUpdateItem(flower, 5);
		cart.addOrUpdateItem(flower2, 5);

		WholesalerOrder wholesalerOrder = mock(WholesalerOrder.class);
		when(orderFactory.createWholesalerOrder()).thenReturn(wholesalerOrder);

		salesService.buyProductsFromBasket(cart, "Card");

		assertTrue(cart.isEmpty());
	}

	@Test
	void buyBouquetFromBasket_ShouldThrowException() {
		// Arrange
		Cart cart = new Cart();
		Flower flower1 = new Flower("Lily", new Pricing(Money.of(20, "EUR"), Money.of(40, "EUR")), "White", 10);
		Flower flower2 = new Flower("Rose", new Pricing(Money.of(10, "EUR"), Money.of(20, "EUR")), "Red", 15);

		Map<Flower, Integer> bouquetFlowers = Map.of(
			flower1, 3,
			flower2, 2
		);
		Money additionalPrice = Money.of(0.5, "EUR");
		int quantity = 10;

		Bouquet bouquet = new Bouquet("Spring Bouquet", bouquetFlowers, additionalPrice, quantity);
		cart.addOrUpdateItem(bouquet, 2);

		assertThrows(IllegalArgumentException.class, () -> salesService.buyProductsFromBasket(cart, "Card"));
	}


	@Test
	void buyProductsFromBasket_shouldThrowExceptionForEmptyBasket() {
		Cart cart = new Cart();

		assertThrows(
			IllegalArgumentException.class,
			() -> salesService.buyProductsFromBasket(cart, "Card")
		);
	}

	@Test
	void buyProductsFromBasket_shouldThrowExceptionForBouquets() {
		Cart cart = new Cart();
		Bouquet bouquet = mock(Bouquet.class);

		assertThrows(
			IllegalArgumentException.class,
			() -> cart.addOrUpdateItem(bouquet, 2)
		);
		assertThrows(
			IllegalArgumentException.class,
			() -> salesService.buyProductsFromBasket(cart, "Card")
		);

	}
	@Test
	void calculateFullCartPrice_ShouldReturnZero_ForEmptyCart() {
		Cart cart = new Cart();

		double result = salesService.calculateFullCartPrice(null, cart, true);

		assertEquals(0.0, result);
	}
	@Test
	void calculateFullCartPrice_ShouldCalculateCorrectly_ForFlowersOnSellPage() {
		Cart cart = new Cart();

		Flower flower = new Flower("Rose", new Pricing(Money.of(10, "EUR"), Money.of(20, "EUR")), "Red", 10);
		cart.addOrUpdateItem(flower, Quantity.of(5));

		double result = salesService.calculateFullCartPrice(null, cart, true);

		assertEquals(100.0, result); // 20 (sell price) * 5 (quantity)
	}

	@Test
	void calculateFullCartPrice_ShouldCalculateCorrectly_ForFlowersOnBuyPage() {
		Cart cart = new Cart();

		Flower flower = new Flower("Lily", new Pricing(Money.of(15, "EUR"), Money.of(30, "EUR")), "White", 20);
		cart.addOrUpdateItem(flower, Quantity.of(2));

		double result = salesService.calculateFullCartPrice(null, cart, false);

		assertEquals(30.0, result); // 15 (buy price) * 2 (quantity)
	}

	@Test
	void calculateFullCartPrice_ShouldCalculateCorrectly_ForBouquetsOnSellPage() {
		Cart cart = new Cart();

		Flower flower1 = new Flower("Tulip", new Pricing(Money.of(5, "EUR"), Money.of(10, "EUR")), "Yellow", 50);
		Flower flower2 = new Flower("Daisy", new Pricing(Money.of(4, "EUR"), Money.of(8, "EUR")), "White", 30);
		Map<Flower, Integer> flowers = Map.of(flower1, 3, flower2, 2);
		Bouquet bouquet = new Bouquet("Sunny Bouquet", flowers, Money.of(2.5, "EUR"), 10);

		cart.addOrUpdateItem(bouquet, Quantity.of(3));

		double result = salesService.calculateFullCartPrice(null, cart, true);

		assertEquals(145.5, result);
	}

	@Test
	void calculateFullCartPrice_ShouldIgnoreBouquetsOnBuyPage() {
		Cart cart = new Cart();

		Flower flower1 = new Flower("Lily", new Pricing(Money.of(5, "EUR"), Money.of(10, "EUR")), "White", 20);
		Flower flower2 = new Flower("Rose", new Pricing(Money.of(4, "EUR"), Money.of(8, "EUR")), "Red", 15);
		Map<Flower, Integer> bouquetFlowers = Map.of(flower1, 2, flower2, 3);
		Bouquet bouquet = new Bouquet("Romantic Bouquet", bouquetFlowers, Money.of(6.5, "EUR"), 10);

		cart.addOrUpdateItem(bouquet, Quantity.of(2));

		double result = salesService.calculateFullCartPrice(null, cart, false);

		assertEquals(0.0, result); // Bouquets are ignored on the buy page
	}

	@Test
	void calculateFullCartPrice_ShouldCalculateCorrectly_ForMixedCartOnSellPage() {
		Cart cart = new Cart();

		Flower flower = new Flower("Rose", new Pricing(Money.of(10, "EUR"), Money.of(20, "EUR")), "Red", 15);
		cart.addOrUpdateItem(flower, Quantity.of(4));

		Flower flower1 = new Flower("Tulip", new Pricing(Money.of(5, "EUR"), Money.of(10, "EUR")), "Yellow", 50);
		Flower flower2 = new Flower("Daisy", new Pricing(Money.of(4, "EUR"), Money.of(8, "EUR")), "White", 30);
		Map<Flower, Integer> bouquetFlowers = Map.of(flower1, 3, flower2, 2);
		Bouquet bouquet = new Bouquet("Spring Bouquet", bouquetFlowers, Money.of(3.0, "EUR"), 10);
		cart.addOrUpdateItem(bouquet, Quantity.of(2));

		double result = salesService.calculateFullCartPrice(null, cart, true);

		assertEquals(178, result);
	}


}
