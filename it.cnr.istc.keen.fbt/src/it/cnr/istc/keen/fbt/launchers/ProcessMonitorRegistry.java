package it.cnr.istc.keen.fbt.launchers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.core.model.IProcess;

public class ProcessMonitorRegistry
{
    private static HashMap<IProcess, FbtExecProcessMonitor> map = new HashMap<IProcess, FbtExecProcessMonitor>();

    public static FbtExecProcessMonitor getMonitor(IProcess process)
    {
        prune();
        synchronized(map)
        {
            return map.get(process);
        }
    }
    
    protected static void addMonitor(IProcess process, FbtExecProcessMonitor monitor)
    {
        prune();
        synchronized(map)
        {
            map.put(process, monitor);
        }
    }
    
    private static void prune()
    {
        synchronized(map)
        {
            Set<Map.Entry<IProcess, FbtExecProcessMonitor>> set = map.entrySet();
            Iterator<Map.Entry<IProcess, FbtExecProcessMonitor>> it = set.iterator();
            while (it.hasNext())
            {
                Map.Entry<IProcess, FbtExecProcessMonitor> e = it.next();
                if (e.getKey().isTerminated())
                    it.remove();
            }
        }
    }
}
