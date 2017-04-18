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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;

public class FileExtensionSelectionDialog extends
        FilteredResourcesSelectionDialog
{
    private String extension;

    public FileExtensionSelectionDialog(Shell shell, boolean multi,
            IContainer container, int typesMask, String extension)
    {
        super(shell, multi, container, typesMask);
        this.extension = extension == null ? null : extension.toLowerCase();
    }
    
    protected ItemsFilter createFilter() {
        return new ExtResourceFilter();
    }

    
    protected class ExtResourceFilter extends ResourceFilter
    {
        public boolean matchItem(Object item)
        {
            if (item instanceof IFile)
            {
                String fname = ((IFile)item).getName();
                if (fname == null)
                    return false;
                int idx = fname.lastIndexOf('.');
                if (idx==-1 || !fname.substring(idx+1).toLowerCase().equals(extension))
                    return false;
            }
            return super.matchItem(item);
        }
    }

}
