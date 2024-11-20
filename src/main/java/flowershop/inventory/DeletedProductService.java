package flowershop.inventory;

import org.salespointframework.catalog.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeletedProductService {

	public DeletedProduct deleteProduct(Product product, int quantity) {
		double loss = product.getPrice().getNumber().doubleValueExact() * quantity;
		return new DeletedProduct(product.getName(), product.getPrice().getNumber().doubleValueExact(), quantity, loss);
	}

	public double getTotalLossSum(List<DeletedProduct> deletedProducts) {
		return deletedProducts.stream()
			.mapToDouble(DeletedProduct::getTotalLoss)
			.sum();
	}
}
