package flowershop.sales;

import flowershop.services.AbstractOrder;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import org.jetbrains.annotations.NotNull;
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
}
