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

public class EpslProcessMonitor implements Runnable
{
    private AtomicInteger idleCount = new AtomicInteger(1);
    
    private StdoutMonitor outMon;
    private StderrMonitor errMon;
    
    private IProgressMonitor monitor;
    private IStreamsProxy sp;
    private ConsoleAdapter con;
    private String initCmd;
    private String planArgs;
    private boolean doExit;
    private IProcess process;
    private String exportFile;
    
    private class StdoutMonitor implements IStreamListener
    {
        @Override
        public void streamAppended(String text, IStreamMonitor monitor)
        {
            if (text==null)
                return;
            text = text.replaceAll("\\s+", " ").trim();
            
            //prompt
            if (text.endsWith("epsl-agent$"))
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
                            "An EPSL problem occurred. %nPlanner said: %n%s",
                            text)),
                            StatusManager.SHOW);
        }
    }
    
    private interface ConsoleAdapter
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
    
    public EpslProcessMonitor(String ddlFile, String pdlFile, String planArgs,
            boolean doExit, String exportFile,
            IStreamsProxy sp, IOConsole iocon, IProgressMonitor monitor, IProcess process)
    {
        if (iocon!=null)
            this.con = new IOConsoleAdapter(iocon);
        else
            this.con = new IStreamConsoleAdapter(sp);
        this.sp = sp;
        this.monitor = monitor;
        this.planArgs = planArgs;
        this.doExit = doExit;
        this.process = process;
        this.exportFile = "".equals(exportFile) ? null : exportFile;
        if (this.exportFile!=null)
            this.doExit=true;
        initCmd = String.format("init %s %s%n", ddlFile,pdlFile);
        
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
        t.start();
    }
    
    private void waitPrompt()
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
            waitPrompt();
            con.write(initCmd);

            if (monitor.isCanceled())
                return;

            String planCmd = String.format("plan %s%n", planArgs);
            waitPrompt();
            con.write(planCmd);

            if (monitor.isCanceled())
                return;

            waitPrompt();
            if (exportFile!=null)
                con.write(String.format("export %s%n", exportFile));
            else
                con.write(String.format("display%n"));

            if (doExit)
            {
                waitPrompt();
                con.write(String.format("exit%n"));
            }

            monitor.worked(1);
            
            //FIXME: handle VerifierHandler
//            if (exportFile!=null)
//                PlatformUI.getWorkbench().getDisplay().asyncExec(
//                        new VerifierHandler(exportFile));
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
}
