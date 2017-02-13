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

import org.eclipse.xtext.conversion.ValueConverter
import org.eclipse.xtext.conversion.IValueConverter
import com.google.inject.Inject
import org.eclipse.xtext.common.services.DefaultTerminalConverters

class DdlValueConverter extends DefaultTerminalConverters {
	
	@Inject
	private NumberValueConverter numConverter;
	
	@ValueConverter(rule = "it.cnr.istc.keen.Ddl.PosNumber")
    def IValueConverter<Integer> getPosNumberNameConverter() {
        return numConverter;
    }
    
	@ValueConverter(rule = "it.cnr.istc.keen.Ddl.NegNumber")
    def IValueConverter<Integer> getNegNumberNameConverter() {
        return numConverter;
    }
}