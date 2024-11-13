package kickstart.Davyd_Lera.services;

import kickstart.Davyd_Lera.models.products.Bouquet;
import kickstart.Davyd_Lera.models.products.Flower;
import kickstart.Davyd_Lera.repositories.ProductCatalog;
import org.salespointframework.catalog.Product;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;

@Service
public class ProductService {

	private final ProductCatalog productCatalog;

	public ProductService(ProductCatalog productCatalog) {
		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
		this.productCatalog = productCatalog;
	}

	public Flower addFlower(Flower flower) {
		return productCatalog.save(flower);
	}

	public Bouquet addBouquet(Bouquet bouquet) {
		return productCatalog.save(bouquet);
	}

	public Iterable<Product> getAllProducts() {
		return productCatalog.findAll();
	}

	public Optional<Product> getProductById(Long id) {
		return productCatalog.findById(getProductId(id));
	}

	public void deleteProductById(Long id) {
		productCatalog.findById(getProductId(id)).ifPresent(productCatalog::delete);
	}

	private Product.ProductIdentifier getProductId(Long id) {
		return Product.ProductIdentifier.of(id.toString());
	}
}
