package flowershop.sales;

import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.ProductService;
import flowershop.services.OrderFactory;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.OrderEvents;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SalesService {
	private final ProductService productService;
	private final SimpleOrderService simpleOrderService;
	private final OrderFactory orderFactory;
	private final WholesalerOrderService wholesalerOrderService;
	private final ApplicationEventPublisher eventPublisher;

	public SalesService(ProductService productService, SimpleOrderService simpleOrderService, OrderFactory orderFactory, WholesalerOrderService wholesalerOrderService, ApplicationEventPublisher eventPublisher) {
		this.productService = productService;
		this.simpleOrderService = simpleOrderService;
		this.orderFactory = orderFactory;
		this.wholesalerOrderService = wholesalerOrderService;
		this.eventPublisher = eventPublisher;
	}

	public void sellProductsFromBasket(Cart cart, String paymentMethod) throws IllegalArgumentException {
		if (cart == null || cart.isEmpty()) {
			throw new IllegalArgumentException("Basket is null or empty");
		}

		SimpleOrder simpleOrder = orderFactory.createSimpleOrder();
		for (CartItem cartItem : cart) {
			Product product = cartItem.getProduct();

			if (product instanceof Flower) {
				productService.removeFlowers((Flower) product, (int)cartItem.getQuantity().getAmount().doubleValue());
			} else if (product instanceof Bouquet) {
				//productService.removeBouquet((Bouquet) product, basketItem.getQuantity());
				productService.removeBouquet((Bouquet) product, (int)cartItem.getQuantity().getAmount().doubleValue());
			} else {
				throw new IllegalArgumentException("Unsupported product type");
			}

			simpleOrder.addOrderLine(product, cartItem.getQuantity());
		}
		simpleOrder.setPaymentMethod(paymentMethod);
		simpleOrderService.create(simpleOrder);
		cart.clear();

		var event = OrderEvents.OrderPaid.of(simpleOrder);
		eventPublisher.publishEvent(event); // Needed for Finances
	}


	public void buyProductsFromBasket(Cart cart, String paymentMethod) throws IllegalArgumentException {
		if (cart == null || cart.isEmpty()) {
			throw new IllegalArgumentException("Basket is null or empty");
		}

		WholesalerOrder wholesalerOrder = orderFactory.createWholesalerOrder();
		for (CartItem cartItem : cart) {
			Product product = cartItem.getProduct();

			if (product instanceof Flower) {
				productService.addFlowers((Flower) product, (int)cartItem.getQuantity().getAmount().doubleValue());
				wholesalerOrder.addOrderLine(product, cartItem.getQuantity());
			} else if (product instanceof Bouquet) {
				throw new IllegalArgumentException("Unsupported product type: Bouquet cannot be bought from Wholesaler.");
			} else {
				throw new IllegalArgumentException("Unsupported product type");
			}

		}
		wholesalerOrder.setPaymentMethod(paymentMethod);
		wholesalerOrderService.create(wholesalerOrder);
		cart.clear();

		var event = OrderEvents.OrderPaid.of(wholesalerOrder);
		eventPublisher.publishEvent(event); // Needed for Finances
	}

}

