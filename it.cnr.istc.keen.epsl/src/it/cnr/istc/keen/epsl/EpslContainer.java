/*
 * Copyright (c) 2016-2017 PST (http://istc.cnr.it/group/pst).
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Giulio Bernardi
 *   
 * Inspired by org.eclipse.jdt.internal.launching.VMDefinitionsContainer
 */
package it.cnr.istc.keen.epsl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class EpslContainer
{
    private IEPSLInstall dflt;
    private ArrayList<IEPSLInstall> planners;
    private MultiStatus fStatus;
    
    public EpslContainer()
    {
        dflt = null;
        planners = new ArrayList<IEPSLInstall>();
    }
    
    public IEPSLInstall getDefault()
    {
        if (dflt == null && planners.size()>0)
            return planners.get(0);
        return dflt;
    }
    
    public IEPSLInstall[] getPlanners()
    {
        return planners.toArray(new IEPSLInstall[planners.size()]);
    }
    
    public void setDefault(IEPSLInstall epsl)
    {
        this.dflt = epsl;
    }

    public void addInstallation(IEPSLInstall epsl)
    {
        planners.add(epsl);
    }
    
    private void addStatus(IStatus status)
    {
        if (fStatus == null)
            fStatus = new MultiStatus(Activator.PLUGIN_ID, 0, "Installed EPSL Planners", null);
        fStatus.add(status);
    }
    
    public IStatus getStatus()
    {
        return fStatus;
    }

    private Element plannerAsXML(Document doc, IEPSLInstall epsl)
    {
        Element element = doc.createElement("planner");
        element.setAttribute("id", epsl.getId());
        element.setAttribute("name", epsl.getName());

        File installLocation = epsl.getInstallLocation();
        String installPath = "";
        if (installLocation != null)
            installPath = installLocation.getAbsolutePath();
        element.setAttribute("path", installPath);

        return element;
    }

    private static IEPSLInstall plannerFromXML(Element element, EpslContainer container)
    {
        String id = element.getAttribute("id");
        String name = element.getAttribute("name");
        String path = element.getAttribute("path");
        File installLocation = null;
        if (path != null)
            installLocation = new File(path);
        
        String errName = !name.isEmpty() ? name : 
            !path.isEmpty() ? String.format("with path \"%s\"",path) :
            !id.isEmpty() ? String.format("with path \"%s\"",id) :
                "<unnamed entry>";
        
        IStatus status = null;
        if (id.isEmpty())
            status = new Status(IStatus.ERROR,Activator.PLUGIN_ID,
                    String.format("Planner entry %s was removed because it has no id.",errName)); 
        if (name.isEmpty())
            status = new Status(IStatus.ERROR,Activator.PLUGIN_ID,
                    String.format("Planner entry %s was removed because it has no name.",errName)); 
        if (path.isEmpty())
            status = new Status(IStatus.ERROR,Activator.PLUGIN_ID,
                    String.format("Planner entry %s was removed because it has no installation path.",errName));
        
        if (status != null)
        {
            container.addStatus(status);
            return null;
        }
        
        EpslInstallImpl epsl = new EpslInstallImpl(id);
        epsl.setName(name);
        epsl.setInstallLocation(installLocation);

        return epsl;
    }

    public String getAsXML() throws CoreException
    {
        Document doc = DebugPlugin.newDocument();
        Element config = doc.createElement("epslSettings");
        doc.appendChild(config);
        
        if (dflt != null)
            config.setAttribute("defaultId", dflt.getId());

        for (IEPSLInstall epsl : planners)
            config.appendChild(plannerAsXML(doc, epsl));

        return DebugPlugin.serializeDocument(doc);
    }
    
    public static void parseXMLIntoContainer(InputStream inputStream,
            EpslContainer container) throws IOException
    {
        InputStream stream = new BufferedInputStream(inputStream);

        Element config = null;
        try
        {
            DocumentBuilder parser = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            parser.setErrorHandler(new DefaultHandler());
            config = parser.parse(new InputSource(stream)).getDocumentElement();
        }
        catch (SAXException e)
        {
            throw new IOException("Invalid format");
        }
        catch (ParserConfigurationException e)
        {
            stream.close();
            throw new IOException("Invalid format");
        }
        finally
        {
            stream.close();
        }

        if (!config.getNodeName().equalsIgnoreCase("epslSettings"))
            throw new IOException("Invalid format");

        String defaultID = config.getAttribute("defaultId");
        IEPSLInstall defaultEpsl = null;

        NodeList list = config.getChildNodes();
        int length = list.getLength();
        for (int i = 0; i < length; ++i)
        {
            Node node = list.item(i);
            short type = node.getNodeType();
            if (type == Node.ELEMENT_NODE)
            {
                Element el = (Element) node;
                if (el.getNodeName().equalsIgnoreCase("planner"))
                {
                    IEPSLInstall epsl = plannerFromXML(el, container);
                    if (epsl == null)
                        continue;
                    if (epsl.getId().equals(defaultID))
                        defaultEpsl=epsl;
                    container.addInstallation(epsl);
                }
            }
        }
        if (!defaultID.equals(""))
        {
            if (defaultEpsl==null)
                container.addStatus(new Status(IStatus.ERROR,Activator.PLUGIN_ID,
                		String.format(
                        "Default planner with id \"%s\" not found.",defaultID)));
            container.setDefault(defaultEpsl);
        }
    }
}
