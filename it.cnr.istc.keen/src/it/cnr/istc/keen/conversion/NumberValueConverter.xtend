/*
 * Copyright (c) 2016-2017 PST (http://istc.cnr.it/group/pst).
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Giulio Bernardi
 */
package it.cnr.istc.keen.conversion

import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.util.Strings
import org.eclipse.xtext.conversion.impl.AbstractValueConverter
import org.eclipse.xtext.conversion.IValueConverter
import org.eclipse.xtext.AbstractRule

class NumberValueConverter extends AbstractValueConverter<Integer> implements IValueConverter.RuleSpecific {
	
	private int MAX_VALUE = Integer.MAX_VALUE;
	private int MIN_VALUE = Integer.MIN_VALUE;
	
	override toValue(String string, INode node) throws ValueConverterException {
		if (Strings.isEmpty(string))
			throw new ValueConverterException("Couldn't convert empty string to an int value.", node, null);
		val clean = string.replace(" ","");
		if ("+INF".equals(clean))
			return MAX_VALUE
		else if ("-INF".equals(clean))
			return MIN_VALUE;
		try {
			val value = Integer.parseInt(clean);
			return Integer.valueOf(value);
		}
		catch (NumberFormatException e) {
			throw new ValueConverterException("Couldn't convert '" + string + "' to an integer value.", node, e);
		}
	}
	
	override toString(Integer value) {
		return
		switch (value) {
			case MAX_VALUE: "+INF"
			case MIN_VALUE: "-INF"
			default: value.toString()
		}
	}
	
	override setRule(AbstractRule rule) throws IllegalArgumentException {
		
	}
	
}