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
package it.cnr.istc.keen.validation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import it.cnr.istc.mc.langinterface.Domain;
import it.cnr.istc.mc.langinterface.Timeline;
import it.cnr.istc.mc.langinterface.Value;

public class QueryComponentInfo
{
    public int startLine;
    public String timelineName;
    public String domainName;
    public List<String> values;
    
    public QueryComponentInfo(int startLine, Timeline timeline, Domain domain)
    {
        this.startLine = startLine;
        this.timelineName = timeline.getName();
        this.domainName = domain.getName();
        List<Value> values = timeline.getComponent().getAllowedValues();
        this.values = values.stream().map(v -> v.getName()).collect(Collectors.toList());
    }
    
    public QueryComponentInfo(int startLine, String timeline, String domain)
    {
        this.startLine = startLine;
        this.timelineName = timeline;
        this.domainName = domain;
        values = Collections.singletonList("value");
    }
    
}
