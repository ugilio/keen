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
package it.cnr.istc.keen.epsl.launchers;

import it.cnr.istc.keen.epsl.Activator;
import it.cnr.istc.keen.epsl.ConfigurationData;
import it.cnr.istc.keen.epsl.extensions.IRunModeExtension;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.statushandlers.StatusManager;

public abstract class BaseEpslProcessMonitor implements Runnable
{
    private AtomicInteger idleCount = new AtomicInteger(1);
    
    private StdoutMonitor outMon;
    private StderrMonitor errMon;
    
    protected IProgressMonitor monitor;
    protected IStreamsProxy sp;
    protected ConsoleAdapter con;
    protected IProcess process;
    
    protected abstract String getPrompt();
    
    private class StdoutMonitor implements IStreamListener
    {
        @Override
        public void streamAppended(String text, IStreamMonitor monitor)
        {
            if (text==null)
                return;
            text = text.replaceAll("\\s+", " ").trim();
            
            //prompt
            if (text.endsWith(getPrompt()))
                idleCount.set(1);
        }
    }

    private class StderrMonitor implements IStreamListener
    {
        @Override
        public void streamAppended(String text, IStreamMonitor monitor)
        {
            if (text==null)
                return;
            text = text.replaceAll("\\s+", " ").trim();
            
            if (text.isEmpty())
                return;
            
            StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    String.format(
                    		getConfigurationData().get(ConfigurationData.EXEC_ERROR),
                            text)),
                            StatusManager.SHOW);
        }
    }
    
    public interface ConsoleAdapter
    {
        public void write(String s) throws IOException;
    }
    
    public class IStreamConsoleAdapter implements ConsoleAdapter
    {
        private IStreamsProxy proxy;
        
        public IStreamConsoleAdapter(IStreamsProxy proxy)
        {
            this.proxy = proxy;
        }
        
        @Override
        public void write(String s) throws IOException
        {
            proxy.write(s);
        }
    }
    
    public class IOConsoleAdapter implements ConsoleAdapter
    {
        private IDocument conDoc;
        
        public IOConsoleAdapter(IOConsole console)
        {
            this.conDoc = console.getDocument();
        }
        
        @Override
        public void write(final String s) throws IOException
        {
            final IOException[] ex = new IOException[1];
            Display.getDefault().syncExec(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        conDoc.replace(conDoc.getLength(), 0, s);
                    }
                    catch (BadLocationException e)
                    {
                        ex[0]=new IOException(e);
                    }
                }
            });
            if (ex[0]!=null)
                throw ex[0];
        }
    }
    
    protected abstract void initArgs(String ddlFile, String pdlFile, String planArgs,
            boolean doExit, IRunModeExtension extHandler, Object extraData, IStreamsProxy sp, IProgressMonitor monitor,
            IProcess process);
    
    protected abstract ConfigurationData getConfigurationData();
    
    protected void beforeThreadStart(Thread t)
    {
    	//do nothing by default
    }
    
    public BaseEpslProcessMonitor(String ddlFile, String pdlFile, String planArgs,
            boolean doExit, IRunModeExtension extHandler, Object extraData,
            IStreamsProxy sp, IOConsole iocon, IProgressMonitor monitor, IProcess process)
    {
        if (iocon!=null)
            this.con = new IOConsoleAdapter(iocon);
        else
            this.con = new IStreamConsoleAdapter(sp);
        initArgs(ddlFile, pdlFile, planArgs, doExit, extHandler, extraData, sp, monitor, process);
        
        outMon = new StdoutMonitor();
        errMon = new StderrMonitor();
        
        IStreamMonitor strOutMon = sp.getOutputStreamMonitor();
        IStreamMonitor strErrMon = sp.getErrorStreamMonitor();
        //parse previous output that might otherwise get lost
        outMon.streamAppended(strOutMon.getContents(), strOutMon);
        errMon.streamAppended(strErrMon.getContents(), strErrMon);
        
        strOutMon.addListener(outMon);
        strErrMon.addListener(errMon);
        
        Thread t = new Thread(this);
        beforeThreadStart(t);
        t.start();
    }
    
    protected void waitPrompt()
    {
        try
        {
            
            while (!idleCount.compareAndSet(1, 0))
            {
                if (process.isTerminated())
                    return;
                Thread.sleep(50);
            }
        }
        catch (InterruptedException e) {}
    }
    
    @Override
    public void run()
    {
        try
        {
        	sendCommands();
        }
        catch (IOException e)
        {
            //FIXME: log
        }
        finally
        {
            sp.getErrorStreamMonitor().removeListener(errMon);
            sp.getOutputStreamMonitor().removeListener(outMon);
        }
    }
    
    protected abstract void sendCommands() throws IOException;
}
