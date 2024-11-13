package flowershop.repositories;

import flowershop.models.accounting.CashRegister;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CashRegisterRepository extends JpaRepository<CashRegister, Long>{
	Optional<CashRegister> findFirstByOrderById();
}
