package flowershop.sales;

import flowershop.finances.BalanceService;
import flowershop.product.*;
import flowershop.services.OrderFactory;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.OrderEvents;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Optional;
import java.util.UUID;

@Service
public class SalesService {
	private final ProductService productService;
	private final SimpleOrderService simpleOrderService;
	private final OrderFactory orderFactory;
	private final WholesalerOrderService wholesalerOrderService;
	private final ApplicationEventPublisher eventPublisher;
	private final GiftCardService giftCardService;
	private final BalanceService balanceService;

	public SalesService(ProductService productService, SimpleOrderService simpleOrderService,
						OrderFactory orderFactory, WholesalerOrderService wholesalerOrderService,
						ApplicationEventPublisher eventPublisher, GiftCardService giftCardService,
						BalanceService balanceService) {
		this.productService = productService;
		this.simpleOrderService = simpleOrderService;
		this.orderFactory = orderFactory;
		this.wholesalerOrderService = wholesalerOrderService;
		this.eventPublisher = eventPublisher;
		this.giftCardService = giftCardService;
		this.balanceService = balanceService;
	}

	/**
	 * Processes the sale of products from a cart and creates a corresponding order.
	 *
	 * @param cart          the cart containing products to sell
	 * @param paymentMethod the payment method for the sale
	 * @throws IllegalArgumentException if the cart is null, empty, or contains unsupported product types
	 */
	public void sellProductsFromBasket(Cart cart, String paymentMethod, UUID giftCardId)
		throws IllegalArgumentException, InsufficientFundsException {
		if (cart == null || cart.isEmpty()) {
			throw new IllegalArgumentException("Basket is null or empty");
		}
		if (paymentMethod.equals("GiftCard")) {
			handleGiftCardPayment(cart, giftCardId);
		}

		SimpleOrder simpleOrder = orderFactory.createSimpleOrder();
		for (CartItem cartItem : cart) {
			Product product = cartItem.getProduct();

			if (product instanceof Flower) {
				productService.removeFlowers((Flower) product, (int) cartItem.getQuantity().getAmount().doubleValue());
			} else if (product instanceof Bouquet) {
				productService.removeBouquet((Bouquet) product, (int) cartItem.getQuantity().getAmount().doubleValue());
			} else {
				throw new IllegalArgumentException("Unsupported product type");
			}

			Product p = productService.getAllProducts().stream()
				.filter(f -> f.getId().equals(product.getId()))
				.findFirst().get();

			System.out.println(p.getName() + "\t" + 
			(p instanceof Flower? ((Flower)p).getPricing().getSellPrice().getNumber().doubleValue()
				: ((Bouquet)p).getPrice().getNumber().doubleValue()));

			simpleOrder.addOrderLine(p, cartItem.getQuantity());
		}
		System.out.println("------GET-TOTAL------");
		System.out.println(simpleOrder.getTotal());
		simpleOrder.getOrderLines().stream().forEach(ol -> {
			System.out.println(ol.getPrice());
		});
		System.out.println("--------END--------");
		simpleOrder.setPaymentMethod(paymentMethod);
		simpleOrderService.create(simpleOrder);
		cart.clear();

		var event = OrderEvents.OrderPaid.of(simpleOrder);
		eventPublisher.publishEvent(event); // Needed for Finances
	}

	private void handleGiftCardPayment(Cart cart, UUID giftCardId)
		throws IllegalArgumentException, InsufficientFundsException {
		Optional<GiftCard> giftCardOptional = giftCardService.findGiftCardById(giftCardId);
		if (giftCardOptional.isEmpty()) {
			throw new IllegalArgumentException("Gift card not found");
		}

		GiftCard giftCard = giftCardOptional.get();

		// Calculate the full price to pay
		Money fullPrice = Money.of(0, "EUR");
		for (CartItem cartItem : cart) {
			Product product = cartItem.getProduct();
			fullPrice = fullPrice.add(product.getPrice()).multiply(cartItem.getQuantity().getAmount());
		}

		if (giftCard.getBalance().subtract(fullPrice).isNegative()) {
			throw new InsufficientFundsException();
		}
		giftCard.subtractBalance(fullPrice);
	}

	/**
	 * Processes the purchase of products from a cart and creates a corresponding wholesaler order.
	 *
	 * @param cart          the cart containing products to buy
	 * @param paymentMethod the payment method for the purchase
	 * @throws IllegalArgumentException if the cart is null, empty, or contains unsupported product types
	 */
	public void buyProductsFromBasket(Cart cart, String paymentMethod)
		throws IllegalArgumentException, InsufficientFundsException {
		if (cart == null || cart.isEmpty()) {
			throw new IllegalArgumentException("Basket is null or empty");
		}

		WholesalerOrder wholesalerOrder = orderFactory.createWholesalerOrder();
		addFlowersFromCart(cart, wholesalerOrder);
		wholesalerOrder.setPaymentMethod(paymentMethod);
		wholesalerOrderService.create(wholesalerOrder);
		if (balanceService.denies(wholesalerOrder)) {
			throw new InsufficientFundsException();
		}
		cart.clear();
		var event = OrderEvents.OrderPaid.of(wholesalerOrder);
		eventPublisher.publishEvent(event); // Needed for Finances
		//cashRegisterService.onOrderPaid(event);
	}

	/**
	 * Processes the purchase of products from a cart and creates a corresponding wholesaler order.
	 *
	 * @param cart          the cart containing products to buy
	 * @param paymentMethod the payment method for the purchase
	 * @param deliveryDate  the delivery date
	 * @throws IllegalArgumentException if the cart is null, empty, or contains unsupported product types
	 */
	public void buyProductsFromBasket(Cart cart, String paymentMethod, String deliveryDate)
		throws IllegalArgumentException {
		if (cart == null || cart.isEmpty()) {
			throw new IllegalArgumentException("Basket is null or empty");
		}

		WholesalerOrder wholesalerOrder = orderFactory.createWholesalerOrder();
		addFlowersFromCart(cart, wholesalerOrder);
		wholesalerOrder.setPaymentMethod(paymentMethod);
		wholesalerOrderService.create(wholesalerOrder);
		wholesalerOrder.setNotes(deliveryDate);
		cart.clear();
		var event = OrderEvents.OrderPaid.of(wholesalerOrder);
		eventPublisher.publishEvent(event); // Needed for Finances
	}

	private void addFlowersFromCart(Cart cart, WholesalerOrder wholesalerOrder) {
		for (CartItem cartItem : cart) {
			Product product = cartItem.getProduct();

			if (product instanceof Flower) {
				wholesalerOrder.addOrderLine(product, cartItem.getQuantity());
			} else if (product instanceof Bouquet) {
				throw new IllegalArgumentException("Unsupported product type: Bouquet cannot be bought from Wholesaler.");
			} else {
				throw new IllegalArgumentException("Unsupported product type");
			}
		}
	}

	/**
	 * Calculates the total price of the items in a cart, based on whether the cart is for selling or buying.
	 *
	 * @param model      the model to hold attributes for the view
	 * @param cart       the cart containing products
	 * @param isSellPage whether the calculation is for the selling page (true) or buying page (false)
	 * @return the total price of the items in the cart
	 */
	public double calculateFullCartPrice(Model model, Cart cart, Boolean isSellPage) {
		// Initialize a default value for the price
		// For buy page, pricePerItem remains 0 as Bouquets are not available
		// If it's not a Flower or Bouquet, pricePerItem remains 0

		return cart.get()
			.mapToDouble(bi -> {
				double pricePerItem = 0; // Initialize a default value for the price

				if (bi.getProduct() instanceof Flower flower) {
					pricePerItem = isSellPage
						? productService.findAllFlowers().stream()
							.filter(f -> f.getId().equals(flower.getId())).findFirst().orElse(null)
							.getPricing().getSellPrice().getNumber().doubleValue()
						: productService.findAllFlowers().stream()
							.filter(f -> f.getId().equals(flower.getId())).findFirst().orElse(null)
							.getPricing().getBuyPrice().getNumber().doubleValue();

				} else if (bi.getProduct() instanceof Bouquet bouquet && isSellPage) {
					// For buy page, pricePerItem remains 0 as Bouquets are not available
					pricePerItem = productService.findAllBouquets().stream()
						.filter(b -> b.getId().equals(bouquet.getId())).findFirst().orElse(null)
						.getPrice().getNumber().doubleValue();
				}
				// If it's not a Flower or Bouquet, pricePerItem remains 0
				return pricePerItem * bi.getQuantity().getAmount().doubleValue();
			}).sum();
	}

}

