package flowershop.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GiftCardRepository extends JpaRepository<GiftCard, UUID> {

}
