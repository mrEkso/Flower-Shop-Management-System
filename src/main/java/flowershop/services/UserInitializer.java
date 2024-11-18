package flowershop.services;

import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Order(10)
public class UserInitializer implements DataInitializer {

	private final UserAccountManagement userAccountManagement;

	public UserInitializer(UserAccountManagement userAccountManagement) {
		Assert.notNull(userAccountManagement, "UserAccountManager must not be null!");
		this.userAccountManagement = userAccountManagement;
	}

	@Override
	public void initialize() {
		System.out.println("-------------------------- initialize --------------------------");

		// Creating Frau Floris
		UserAccount frauFloris = userAccountManagement.create("frau_floris", Password.UnencryptedPassword.of("password"), Role.of("ROLE_ADMIN"));
		frauFloris.setFirstname("Frau");
		frauFloris.setLastname("Floris");
		userAccountManagement.save(frauFloris);

		// Creating Floris Nichte
		UserAccount florisNichte = userAccountManagement.create("floris_nichte", Password.UnencryptedPassword.of("password"), Role.of("ROLE_ADMIN"));
		florisNichte.setFirstname("Floris");
		florisNichte.setLastname("Nichte");
		userAccountManagement.save(florisNichte);

		// Creating Floris Nichte
		UserAccount dummyShopWorker = userAccountManagement.create("shop_worker", Password.UnencryptedPassword.of("password"), Role.of("ROLE_USER"));
		dummyShopWorker.setFirstname("Shop");
		dummyShopWorker.setLastname("Worker");
		userAccountManagement.save(dummyShopWorker);
		System.out.println("-------------------------- Default user created --------------------------");
	}
}
