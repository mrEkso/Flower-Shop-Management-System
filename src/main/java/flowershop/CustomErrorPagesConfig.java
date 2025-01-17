package flowershop;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CustomErrorPagesConfig implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

	@Override
	public void customize(ConfigurableWebServerFactory factory) {
		factory.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/404"),
			new ErrorPage(HttpStatus.FORBIDDEN, "/403"),
			new ErrorPage(HttpStatus.BAD_REQUEST, "/400"));
	}
}
