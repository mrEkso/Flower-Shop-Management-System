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
}
