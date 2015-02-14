package mtgpricer;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Representation of a money amount.
 * @author jared.pearson
 */
public class Money {
	private final BigDecimal value;
	
	public Money(String value) {
		this.value = new BigDecimal(value);
		this.value.setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}
	
	public Money(BigDecimal value) {
		this.value = value;
		this.value.setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}
	
	public Money subtract(Money value) {
		assert value != null;
		return new Money(this.value.subtract(value.value, new MathContext(2, RoundingMode.HALF_EVEN)));
	}
	
	public double doubleValue() {
		return this.value.doubleValue();
	}
	
	@Override
	public String toString() {
		return this.value.toString();
	}
}