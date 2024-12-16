package flowershop.services;

import java.util.List;

import java.util.List;

import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.Password.UnencryptedPassword;
import org.salespointframework.useraccount.Password.UnencryptedPassword;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * The `UserInitializer` class is responsible for initializing user accounts in the system.
 * It implements the `DataInitializer` interface provided by Salespoint.
 */
@Component
@Order(10)
public class UserInitializer implements DataInitializer {

	private final UserAccountManagement userAccountManagement;

	/**
	 * Constructs a `UserInitializer` with the specified user account management.
	 *
	 * @param userAccountManagement the user account management to use
	 * @throws IllegalArgumentException if the userAccountManagement is null
	 */
	public UserInitializer(UserAccountManagement userAccountManagement) {
		Assert.notNull(userAccountManagement, "UserAccountManager must not be null!");
		this.userAccountManagement = userAccountManagement;
	}

	/**
	 * Initializes the user accounts by creating a dummy shop worker account.
	 */
	@Override
	public void initialize() {

		// Skip creation if database was already populated
		if (userAccountManagement.findByUsername("boss").isPresent()) {
			return;
		}
		
		userAccountManagement.create("boss", 
			UnencryptedPassword.of("123"), 
			List.of(Role.of("BOSS"), Role.of("USER")));

		// Creating Floris Nichte
		userAccountManagement.create("shop_worker", 
		UnencryptedPassword.of("123"), Role.of("USER"));
		// dummyShopWorker.setFirstname("Shop");
		// dummyShopWorker.setLastname("Worker");
		// userAccountManagement.save(dummyShopWorker);
		// userAccountManagement.save(boss);
		userAccountManagement.create("shop_worker", 
		UnencryptedPassword.of("123"), Role.of("USER"));
		// dummyShopWorker.setFirstname("Shop");
		// dummyShopWorker.setLastname("Worker");
		// userAccountManagement.save(dummyShopWorker);
		// userAccountManagement.save(boss);
	}
}
