package flowershop.sales;

import flowershop.product.Flower;

public class BasketItem {
    private Flower flower;
    private int quantity;

    public BasketItem(Flower flower, int quantity) {
        this.flower = flower;
        this.quantity = quantity;
    }

    public Flower getFlower() {
        return flower;
    }

    public void setFlower(Flower flower) {
        this.flower = flower;
    }

    public int getQuantity() {
        return quantity;
    }

    public void increaseQuantity() {
        this.quantity++;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
