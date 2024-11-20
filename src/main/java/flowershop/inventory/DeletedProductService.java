package flowershop.inventory;

import org.salespointframework.catalog.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeletedProductService {

	public DeletedProduct deleteProduct(Product product, int quantityDeleted) {
		return new DeletedProduct(product.getName(),
			product.getPrice().getNumber().doubleValueExact(),
			quantityDeleted,
			product.getPrice().getNumber().doubleValueExact() * quantityDeleted);
	}

	// TODO: deletedProductList should be stored in a catalog or repository but it's okay for now.
	public double getTotalLossSum (List<DeletedProduct> deletedProductList){
		return deletedProductList.stream()
			.mapToDouble(DeletedProduct::getTotalLoss)
			.sum();
	}

}
