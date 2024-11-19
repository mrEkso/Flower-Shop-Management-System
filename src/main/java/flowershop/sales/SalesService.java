package flowershop.sales;

import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.ProductService;
import flowershop.services.OrderFactory;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.OrderEvents;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SalesService {
	private final ProductService productService;
	private final SimpleOrderService simpleOrderService;
	private final OrderFactory orderFactory;
	private final WholesalerOrderService wholesalerOrderService;

	public SalesService(ProductService productService, SimpleOrderService simpleOrderService, OrderFactory orderFactory, WholesalerOrderService wholesalerOrderService) {
		this.productService = productService;
		this.simpleOrderService = simpleOrderService;
		this.orderFactory = orderFactory;
		this.wholesalerOrderService = wholesalerOrderService;
	}

	public OrderEvents.OrderPaid sellProductsFromBasket(List<BasketItem> basket, String paymentMethod) throws IllegalArgumentException {
		if (basket == null || basket.isEmpty()) {
			throw new IllegalArgumentException("Basket is null or empty");
		}

		SimpleOrder simpleOrder = orderFactory.createSimpleOrder();
		for (BasketItem basketItem : basket) {
			Product product = basketItem.getProduct();

			if (product instanceof Flower) {
				productService.removeFlowers((Flower) product, basketItem.getQuantityAsInteger());
			} else if (product instanceof Bouquet) {
				//productService.removeBouquet((Bouquet) product, basketItem.getQuantity());
				productService.removeBouquet((Bouquet) product, basketItem.getQuantityAsInteger());
			} else {
				throw new IllegalArgumentException("Unsupported product type");
			}

			simpleOrder.addOrderLine(product, basketItem.getQuantity());
		}
		simpleOrder.setPaymentMethod(paymentMethod);
		simpleOrderService.create(simpleOrder);
		basket.clear();
		return OrderEvents.OrderPaid.of(simpleOrder);
	}


	public OrderEvents.OrderPaid buyProductsFromBasket(List<BasketItem> basket, String paymentMethod) throws IllegalArgumentException {
		if (basket == null || basket.isEmpty()) {
			throw new IllegalArgumentException("Basket is null or empty");
		}

		WholesalerOrder wholesalerOrder = orderFactory.createWholesalerOrder();
		for (BasketItem basketItem : basket) {
			Product product = basketItem.getProduct();

			if (product instanceof Flower) {
				productService.addFlowers((Flower) product, basketItem.getQuantityAsInteger());
				wholesalerOrder.addOrderLine(product, basketItem.getQuantity());
			} else if (product instanceof Bouquet) {
				throw new IllegalArgumentException("Unsupported product type: Bouquet cannot be bought from Wholesaler.");
			} else {
				throw new IllegalArgumentException("Unsupported product type");
			}

		}
		wholesalerOrder.setPaymentMethod(paymentMethod);
		wholesalerOrderService.create(wholesalerOrder);
		basket.clear();
		return OrderEvents.OrderPaid.of(wholesalerOrder);
	}

}

