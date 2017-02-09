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