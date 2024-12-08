package flowershop.sales;

import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.Pricing;
import flowershop.product.ProductService;
import flowershop.services.OrderFactory;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.salespointframework.order.OrderEvents;
import org.salespointframework.quantity.Quantity;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;

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
		List<BasketItem> basket = new ArrayList<>();
		Flower flower = new Flower("Rose", new Pricing(Money.of(20, "EUR"), Money.of(40, "EUR")), "Red", 10);

		BasketItem item = new BasketItem(flower, 5);
		basket.add(item);

		SimpleOrder simpleOrder = mock(SimpleOrder.class);
		when(orderFactory.createSimpleOrder()).thenReturn(simpleOrder);

		salesService.sellProductsFromBasket(basket, "Cash");

		verify(productService, times(1)).removeFlowers(flower, 5);
		verify(simpleOrder, times(1)).addOrderLine(flower, Quantity.of(5));
		verify(simpleOrderService, times(1)).create(simpleOrder);
		assertTrue(basket.isEmpty());
		verify(eventPublisher, times(1)).publishEvent(any(OrderEvents.OrderPaid.class));
	}

	@Test
	void sellProductsFromBasket_shouldThrowExceptionForEmptyBasket() {
		List<BasketItem> basket = new ArrayList<>();

		assertThrows(
			IllegalArgumentException.class,
			() -> salesService.sellProductsFromBasket(basket, "CASH")
		);

	}

	@Test
	void buyProductsFromBasket_shouldBuyProductsSuccessfully() {
		List<BasketItem> basket = new ArrayList<>();
		Flower flower = new Flower("Lily", new Pricing(Money.of(20, "EUR"), Money.of(40, "EUR")), "White", 10);
		BasketItem item = new BasketItem(flower, 8);
		basket.add(item);

		WholesalerOrder wholesalerOrder = mock(WholesalerOrder.class);
		when(orderFactory.createWholesalerOrder()).thenReturn(wholesalerOrder);

		salesService.buyProductsFromBasket(basket, "Card");

		assertTrue(basket.isEmpty());
	}

	@Test
	void buyProductsFromBasket_shouldThrowExceptionForEmptyBasket() {
		List<BasketItem> basket = new ArrayList<>();

		assertThrows(
			IllegalArgumentException.class,
			() -> salesService.buyProductsFromBasket(basket, "Card")
		);
	}

	@Test
	void buyProductsFromBasket_shouldThrowExceptionForBouquets() {
		List<BasketItem> basket = new ArrayList<>();
		Bouquet bouquet = mock(Bouquet.class);
		BasketItem item = new BasketItem(bouquet, 2);
		basket.add(item);

		assertThrows(
			IllegalArgumentException.class,
			() -> salesService.buyProductsFromBasket(basket, "Card")
		);

	}
}
