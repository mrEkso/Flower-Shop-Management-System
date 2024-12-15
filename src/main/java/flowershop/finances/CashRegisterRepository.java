package flowershop.finances;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CashRegisterRepository extends JpaRepository<CashRegister, Long> {
	Optional<CashRegister> findFirstByOrderById();
}
