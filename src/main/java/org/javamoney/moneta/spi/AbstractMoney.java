/*
 * Copyright (c) 2012, 2013, Credit Suisse (Anatole Tresch), Werner Keil.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.javamoney.moneta.spi;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.MonetaryContext;
import javax.money.NumberValue;
import javax.money.MonetaryException;

/**
 * Platform RI: This base class simplifies implementing {@link MonetaryAmount},
 * by providing the common functionality. The different explicitly typed methods
 * are all reduced to methods using {@link BigDecimal} as input, hereby
 * performing any conversion to {@link BigDecimal} as needed. Obviosly this
 * takes some time, so implementors that want to avoid this overhead should
 * implement {@link MonetaryAmount} directly.
 * 
 * @author Anatole Tresch
 */
public abstract class AbstractMoney implements
		MonetaryAmount, Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/** The currency of this amount. */
	protected CurrencyUnit currency;

	/** the {@link MonetaryContext} used by this instance, e.g. on division. */
	protected MonetaryContext monetaryContext;

	/**
	 * Required for deserialization.
	 */
	protected AbstractMoney() {
	}

	/**
	 * Constructor of {@link AbstractMoney}.
	 * 
	 * @param currency
	 *            the currency, not {@code null}.
	 */
	protected AbstractMoney(CurrencyUnit currency) {
		this(currency, null);
	}

	/**
	 * Creates a new instance os {@link AbstractMoney}.
	 * 
	 * @param currency
	 *            the currency, not {@code null}.
	 * @param monetaryContext
	 *            the {@link MonetaryContext}, not {@code null}.
	 */
	protected AbstractMoney(CurrencyUnit currency,
			MonetaryContext monetaryContext) {
		Objects.requireNonNull(currency, "Currency is required.");
		this.currency = currency;
		if (monetaryContext != null) {
			this.monetaryContext = monetaryContext;
		}
		else {
			this.monetaryContext = getDefaultMonetaryContext();
		}
		Objects.requireNonNull(this.monetaryContext);
	}

	/**
	 * Method to be implemented by superclasses to provide the default
	 * {@link MonetaryContext}, when not explicit {@link MonetaryContext} is
	 * available.
	 * 
	 * @return the default {@link MonetaryContext}, never {@code null}.
	 */
	protected abstract MonetaryContext getDefaultMonetaryContext();

	/**
	 * Returns the amount’s currency, modelled as {@link CurrencyUnit}.
	 * Implementations may co-variantly change the return type to a more
	 * specific implementation of {@link CurrencyUnit} if desired.
	 * 
	 * @return the currency, never {@code null}
	 * @see javax.money.MonetaryAmount#getCurrency()
	 */
	@Override
	public CurrencyUnit getCurrency() {
		return currency;
	}

	/**
	 * Access the {@link MonetaryContext} used by this instance.
	 * 
	 * @return the {@link MonetaryContext} used, never null.
	 * @see javax.money.MonetaryAmount#getMonetaryContext()
	 */
	@Override
	public MonetaryContext getMonetaryContext() {
		return this.monetaryContext;
	}

	// Supporting methods

	/**
	 * Creates a {@link BigDecimal} from the given {@link Number} doing the
	 * valid conversion depending the type given.
	 * 
	 * @param num
	 *            the number type
	 * @return the corresponding {@link BigDecimal}
	 */
	protected static BigDecimal getBigDecimal(long num) {
		return BigDecimal.valueOf(num);
	}

	/**
	 * Creates a {@link BigDecimal} from the given {@link Number} doing the
	 * valid conversion depending the type given.
	 * 
	 * @param num
	 *            the number type
	 * @return the corresponding {@link BigDecimal}
	 */
	protected static BigDecimal getBigDecimal(double num) {
		return new BigDecimal(String.valueOf(num));
	}

	/**
	 * Creates a {@link BigDecimal} from the given {@link Number} doing the
	 * valid conversion depending the type given.
	 * 
	 * @param num
	 *            the number type
	 * @return the corresponding {@link BigDecimal}
	 */
	protected static BigDecimal getBigDecimal(Number num) {
		checkNumberParameter(num);
        BigDecimal result = null;
		if(num instanceof NumberValue){
			result = ((NumberValue)num).numberValue(BigDecimal.class);
		}
		// try fast equality check first (delegates to identity!)
		if (result==null && BigDecimal.class.equals(num.getClass())) {
			result = ((BigDecimal) num);
		}
		if (result==null && (Long.class.equals(num.getClass())
				|| Integer.class.equals(num.getClass())
				|| Short.class.equals(num.getClass())
				|| Byte.class.equals(num.getClass())
				|| AtomicLong.class.equals(num.getClass()))) {
			return BigDecimal.valueOf(num.longValue());
		}
		if (result==null && (Float.class.equals(num.getClass())
				|| Double.class.equals(num.getClass()))) {
			return new BigDecimal(num.toString());
		}
		// try instance of (slower)
		if (result==null && num instanceof BigDecimal) {
			result = ((BigDecimal) num);
		}
		if (result==null && num instanceof BigInteger) {
			return new BigDecimal((BigInteger) num);
		}
        if(result==null){
            try{
                // Avoid imprecise conversion to double value if at all possible
                result = new BigDecimal(num.toString());
            }
            catch(NumberFormatException e){
            }
        }
        if(result.signum()==0){
            return BigDecimal.ZERO;
        }
        if(result==null){
            result = BigDecimal.valueOf(num.doubleValue());
        }
        if(result.scale()>0){
            return result.stripTrailingZeros();
        }
        return result;
	}

	/**
	 * Creates a {@link BigDecimal} from the given {@link Number} doing the
	 * valid conversion depending the type given, if a {@link MonetaryContext}
	 * is given, it is applied to the number returned.
	 * 
	 * @param num
	 *            the number type
	 * @return the corresponding {@link BigDecimal}
	 */
	protected static BigDecimal getBigDecimal(Number num,
			MonetaryContext moneyContext) {
		BigDecimal bd = getBigDecimal(num);
		if (moneyContext != null) {
			return new BigDecimal(bd.toString(),
					getMathContext(moneyContext, RoundingMode.HALF_EVEN));
		}
		return bd;
	}

	/**
	 * Evaluates the {@link MathContext} from the given {@link MonetaryContext}.
	 * 
	 * @param monetaryContext
	 *            the {@link MonetaryContext}
	 * @param defaultMode
	 *            the default {@link RoundingMode}, to be used if no one is set
	 *            in {@link MonetaryContext}.
	 * @return the corresponding {@link MathContext}
	 */
	protected static MathContext getMathContext(
			MonetaryContext monetaryContext,
			RoundingMode defaultMode) {
		MathContext ctx = monetaryContext.getAttribute(MathContext.class);
		if (ctx != null) {
			return ctx;
		}
		if (defaultMode != null) {
			return new MathContext(monetaryContext.getPrecision(),
					monetaryContext.getAttribute(RoundingMode.class,
							defaultMode));
		}
		return new MathContext(monetaryContext.getPrecision(),
				monetaryContext.getAttribute(RoundingMode.class,
						RoundingMode.HALF_EVEN));
	}

	/**
	 * Method to check if a currency is compatible with this amount instance.
	 * 
	 * @param amount
	 *            The monetary amount to be compared to, never null.
	 * @throws MonetaryException
	 *             If the amount is null, or the amount's {@link CurrencyUnit} is not
	 *             compatible, meaning has a different value of
	 *             {@link CurrencyUnit#getCurrencyCode()}).
	 */
	protected void checkAmountParameter(MonetaryAmount amount) {
		Objects.requireNonNull(amount, "Amount must not be null.");
		final CurrencyUnit amountCurrency = amount.getCurrency();
		if (!(this.currency
				.getCurrencyCode().equals(amountCurrency.getCurrencyCode()))) {
			throw new MonetaryException("Currency mismatch: "
					+ this.currency + '/' + amountCurrency);
		}
	}

	/**
	 * Internal method to check for correct number parameter.
	 * 
	 * @param number
	 * @throws IllegalArgumentException
	 *             If the number is null
	 */
	protected static void checkNumberParameter(Number number) {
		Objects.requireNonNull(number, "Number is required.");
	}

}
