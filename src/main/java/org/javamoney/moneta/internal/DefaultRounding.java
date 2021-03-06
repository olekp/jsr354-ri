/*
 * CREDIT SUISSE IS WILLING TO LICENSE THIS SPECIFICATION TO YOU ONLY UPON THE
 * CONDITION THAT YOU ACCEPT ALL OF THE TERMS CONTAINED IN THIS AGREEMENT.
 * PLEASE READ THE TERMS AND CONDITIONS OF THIS AGREEMENT CAREFULLY. BY
 * DOWNLOADING THIS SPECIFICATION, YOU ACCEPT THE TERMS AND CONDITIONS OF THE
 * AGREEMENT. IF YOU ARE NOT WILLING TO BE BOUND BY IT, SELECT THE "DECLINE"
 * BUTTON AT THE BOTTOM OF THIS PAGE. Specification: JSR-354 Money and Currency
 * API ("Specification") Copyright (c) 2012-2013, Credit Suisse All rights
 * reserved.
 */
package org.javamoney.moneta.internal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.MonetaryCurrencies;
import javax.money.MonetaryOperator;

/**
 * Implementation class providing rounding {@link javax.money.MonetaryOperator} instances
 * for {@link CurrencyUnit} instances. modeling rounding based on standard JDK
 * math, a scale and {@link RoundingMode}.
 * <p>
 * This class is thread safe.
 * 
 * @author Anatole Tresch
 * @author Werner Keil
 * @see RoundingMode
 */
final class DefaultRounding implements MonetaryOperator {

	/** The {@link RoundingMode} used. */
	private final RoundingMode roundingMode;
	/** The scale to be applied. */
	private final int scale;

	/**
	 * Creates an rounding instance.
	 * 
	 * @param roundingMode
	 *            The {@link java.math.RoundingMode} to be used, not {@code null}.
	 */
	DefaultRounding(int scale, RoundingMode roundingMode) {
		Objects.requireNonNull(roundingMode, "RoundingMode required.");
		if (scale < 0) {
			scale = 0;
		}
		this.scale = scale;
		this.roundingMode = roundingMode;
	}

	/**
	 * Creates an {@link DefaultRounding} for rounding {@link MonetaryAmount}
	 * instances given a currency.
	 * 
	 * @param currency
	 *            The currency, which determines the required precision. As
	 *            {@link RoundingMode}, by default, {@link RoundingMode#HALF_UP}
	 *            is sued.
	 * @return a new instance {@link javax.money.MonetaryOperator} implementing the
	 *         rounding.
	 */
	DefaultRounding(CurrencyUnit currency,
			RoundingMode roundingMode) {
		this(currency.getDefaultFractionDigits(), roundingMode);
	}

	/**
	 * Creates an {@link javax.money.MonetaryOperator} for rounding {@link MonetaryAmount}
	 * instances given a currency.
	 * 
	 * @param currency
	 *            The currency, which determines the required precision. As
	 *            {@link RoundingMode}, by default, {@link RoundingMode#HALF_UP}
	 *            is sued.
	 * @return a new instance {@link javax.money.MonetaryOperator} implementing the
	 *         rounding.
	 */
	DefaultRounding(CurrencyUnit currency) {
		this(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryFunction#apply(java.lang.Object)
	 */
	// unchecked cast {@code (T)amount.with(MonetaryOperator)} is
	// safe, if the operator is implemented as specified by this JSR.
	@SuppressWarnings("unchecked")
	@Override
	public <T extends MonetaryAmount> T apply(T amount){
		return (T)amount.getFactory().setCurrency(amount.getCurrency()).setNumber(
				((BigDecimal) amount.getNumber().numberValue(BigDecimal.class)).setScale(
						this.scale,
						this.roundingMode)).create();
	}

}
