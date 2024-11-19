package flowershop.sales;

import org.salespointframework.catalog.Product;
import org.salespointframework.quantity.Quantity;

public class BasketItem {
    private Product product;
    private int quantity;

    public BasketItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Quantity getQuantity() {
        return Quantity.of(quantity);
    }

	public int getQuantityAsInteger() {
		return quantity;
	}

    public void increaseQuantity() {
        this.quantity++;
    }

	public void decreaseQuantity() {
		this.quantity--;
	}

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
