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
package org.javamoney.moneta.format.internal;

import java.io.IOException;
import java.text.ParseException;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryParseException;

/**
 * Abstraction for a token that is part of a token stream, used for formatting
 * and parsing.
 * 
 * @author Anatole Tresch
 */
public interface FormatToken {
	/**
	 * Parse the context, based on the given {@link ParseContext}.
	 * 
	 * @param context
	 *            the current {@link ParseContext}.
	 * @throws ParseException
	 *             if parsing fails.
	 */
	public void parse(ParseContext context) throws MonetaryParseException;

	/**
	 * Formats the given {@link MonetaryAmount} to an {@link Appendable}.
	 * @param appendable the {@link Appendable}, not {@code null}.
	 * @param amount the {@link MonetaryAmount} to be formatted, not {@code null}.
	 * @throws IOException thrown by the {@link Appendable} on appending.
	 */
	public void print(Appendable appendable, MonetaryAmount amount)
			throws IOException;

}
