package flowershop.sales;

public class InsufficientFundsException extends Exception {
	public InsufficientFundsException() {
		super("You have insufficient funds to perform this operation");
	}
}
