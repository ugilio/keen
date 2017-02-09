package it.cnr.istc.keen.ui.syntaxcoloring

import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor
import org.eclipse.swt.graphics.RGB
import org.eclipse.swt.SWT

class DdlHighlightingConfiguration extends DefaultHighlightingConfiguration {
	public static final String VARIABLE_ID = "variable";
	public static final String ENUM_LITERAL = "enumLiteral";
	public static final String TIMELINE_PARAMS = "timelineParams";
	public static final String PARAMETER_TYPE= "parType";
	
	override configure(IHighlightingConfigurationAcceptor acceptor) {
		acceptor.acceptDefaultHighlighting(KEYWORD_ID, "Keyword", keywordTextStyle());
		acceptor.acceptDefaultHighlighting(PUNCTUATION_ID, "Punctuation character", punctuationTextStyle());
		acceptor.acceptDefaultHighlighting(COMMENT_ID, "Comment", commentTextStyle());
		acceptor.acceptDefaultHighlighting(NUMBER_ID, "Number", numberTextStyle());
		acceptor.acceptDefaultHighlighting(DEFAULT_ID, "Default", defaultTextStyle());
		acceptor.acceptDefaultHighlighting(INVALID_TOKEN_ID, "Invalid Symbol", errorTextStyle());
		
		acceptor.acceptDefaultHighlighting(VARIABLE_ID, "Variable", variableTextStyle());
		acceptor.acceptDefaultHighlighting(ENUM_LITERAL, "Enumeration literal", enumLiteralTextStyle());
		acceptor.acceptDefaultHighlighting(TIMELINE_PARAMS, "Timeline parameters", timelineParametersTextStyle());
		acceptor.acceptDefaultHighlighting(PARAMETER_TYPE, "Parameter type", parTypeTextStyle());
	}
	
	def variableTextStyle() {
		val textStyle = defaultTextStyle().copy();
		textStyle.setColor(new RGB(0, 0, 192));
		return textStyle;
	}

	def enumLiteralTextStyle() {
		val textStyle = defaultTextStyle().copy();
		textStyle.setColor(new RGB(0, 0, 192));
		textStyle.setStyle(SWT.BOLD + SWT.ITALIC);
		return textStyle;
	}
	
	def timelineParametersTextStyle() {
		val textStyle = defaultTextStyle().copy();
		textStyle.setStyle(SWT.ITALIC);
		return textStyle;
	}

	def parTypeTextStyle() {
		val textStyle = defaultTextStyle().copy();
		textStyle.setColor(new RGB(0xb7, 0x41, 0x0e));
		return textStyle;
	}

	
}