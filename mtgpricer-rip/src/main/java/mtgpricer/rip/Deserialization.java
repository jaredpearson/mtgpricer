package mtgpricer.rip;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for properties to designate when the property should
 * be deserialized from JSON. This allows the same JSON file to be
 * deserialized conditionally. The main use case is that a light-weight
 * price info can be loaded for basic use cases in the site and then
 * the full price information can be loaded when needed.
 * 
 * @author jared.pearson
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Deserialization {
	DeserializationType only();
	
	public enum DeserializationType {
		/**
		 * Signals that this property should only be loaded when
		 * "cards" are needed.
		 */
		CARDS
	}
}
