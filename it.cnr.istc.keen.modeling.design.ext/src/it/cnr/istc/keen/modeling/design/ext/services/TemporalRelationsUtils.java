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
package it.cnr.istc.keen.modeling.design.ext.services;

import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.IParser;

import com.google.inject.Inject;
import com.google.inject.Injector;

import it.cnr.istc.keen.services.DdlGrammarAccess;
import it.cnr.istc.keen.ui.internal.KeenActivator;

public class TemporalRelationsUtils {
	
	@Inject
	private IParser xtextParser;
	
	@Inject
	private IGrammarAccess grammarAccess;

	public TemporalRelationsUtils() {
    	Injector inj = KeenActivator.getInstance().getInjector(KeenActivator.IT_CNR_ISTC_KEEN_DDL);
    	inj.injectMembers(this);
	}
	
    public String parseTemporalRelationType(EObject self, String args, String orig) {
    	ParserRule pr = ((DdlGrammarAccess)grammarAccess).getTemporalRelationTypeRule();
    	
    	IParseResult parseResult = xtextParser.parse(pr, new StringReader(args));
    	if (parseResult.hasSyntaxErrors())
    		return orig;
    	//else, clean the string:
    	String clean = args.replaceAll("^\\s*([^\\s]+).*$", "$1");
    	args = args.substring(args.indexOf(clean)+clean.length());
    	Matcher m = Pattern.compile("\\s*(\\[[^\\]]+\\])").matcher(args);
    	int lastPos = 0;
    	while (m.find(lastPos)) {
    		String tmp = m.group();
    		clean+=" "+ParsingUtils.parseRange(tmp,tmp);
    		lastPos=m.end();
    	}
    	return clean;
    }
}
