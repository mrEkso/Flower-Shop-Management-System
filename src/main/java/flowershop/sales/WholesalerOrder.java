package flowershop.sales;

import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.services.AbstractOrder;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import org.jetbrains.annotations.NotNull;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.OrderLine;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.UserAccount;

import javax.money.MonetaryAmount;

/**
 * Represents an order made by FlowerShop from wholesaler. This type of order is used to handle
 * transactions where the total amount is negated for business logic purposes, without
 * modifying the stored value in the database.
 *
 * <p>
 *     Use cases include orders for wholesalers to later sell the product at a higher price and make profit.
 * </p>
 */
@Entity
public class WholesalerOrder extends AbstractOrder {
    public WholesalerOrder(UserAccount user) {
        super(user);
    }

    protected WholesalerOrder() {
    }

    @Override
    @NotNull
    @Transient
    public MonetaryAmount getTotal() {
        // Simply negate the total because we lose money and not earn it.
        return super.getTotal().negate();
    }
	@Override
	public OrderLine addOrderLine(Product product, Quantity quantity){
		Product fakeProduct;
		if (product instanceof Bouquet) {
			fakeProduct = new Product(product.getName(), ((Bouquet)product).getPricing().getBuyPrice());
		} else if (product instanceof Flower) {
			fakeProduct = new Product(product.getName(), ((Flower)product).getPricing().getBuyPrice());
		}
		else{
			throw new IllegalArgumentException("Unsupported product type");
		}
		return super.addOrderLine(fakeProduct, quantity);
	}
}
