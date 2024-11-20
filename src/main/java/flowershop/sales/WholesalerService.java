package flowershop.sales;

import flowershop.product.Flower;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WholesalerService {

	private final WholesalerRepository wholesalerRepository;

	@Autowired
	public WholesalerService(WholesalerRepository wholesalerRepository) {
		this.wholesalerRepository = wholesalerRepository;
	}

	public List<Flower> findAllFlowers() {
		return (List<Flower>) wholesalerRepository.findAll(); // Retrieve all flowers from the repository
	}


	public List<Flower> findFlowersByColor(String color) {
		return wholesalerRepository.findByColorIgnoreCase(color);
	}

	public List<Flower> findFlowersByName(String subString) {
		return wholesalerRepository.findByNameIgnoreCase(subString);
	}

	public Set<String> findAllFlowerColors() {
		return findAllFlowers()
			.stream()
			.map(Flower::getColor)
			.collect(Collectors.toSet());
	}
}

