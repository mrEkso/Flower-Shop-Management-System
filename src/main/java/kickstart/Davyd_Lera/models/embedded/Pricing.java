package kickstart.Davyd_Lera.models.embedded;

import jakarta.persistence.Embeddable;
import org.javamoney.moneta.Money;

@Embeddable
public class Pricing {

	private Money buyPrice;
	private Money sellPrice;

	public Pricing(Money buyPrice, Money sellPrice) {
		this.buyPrice = buyPrice;
		this.sellPrice = sellPrice;
	}

	public Pricing() {
	}

	public Money getBuyPrice() {
		return buyPrice;
	}

	public void setBuyPrice(Money buyPrice) {
		this.buyPrice = buyPrice;
	}

	public Money getSellPrice() {
		return sellPrice;
	}

	public void setSellPrice(Money sellPrice) {
		this.sellPrice = sellPrice;
	}
}
