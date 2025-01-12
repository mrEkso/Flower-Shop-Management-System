package flowershop.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class GiftCardService {

	private final GiftCardRepository giftCardRepository;

	@Autowired
	public GiftCardService(GiftCardRepository giftCardRepository) {
		this.giftCardRepository = giftCardRepository;
	}

	public Optional<GiftCard> findGiftCardById(UUID id) {
		return giftCardRepository.findById(id);
	}

}