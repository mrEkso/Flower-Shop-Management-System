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
		// Creating Floris Nichte
		UserAccount dummyShopWorker = userAccountManagement.create("shop_worker", Password.UnencryptedPassword.of("password"), Role.of("ROLE_USER"));
		dummyShopWorker.setFirstname("Shop");
		dummyShopWorker.setLastname("Worker");
		userAccountManagement.save(dummyShopWorker);
	}
}
