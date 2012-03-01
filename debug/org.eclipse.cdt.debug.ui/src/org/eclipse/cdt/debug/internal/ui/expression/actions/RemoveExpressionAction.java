//
// Copyright (c) 2012 by Tensilica Inc.  ALL RIGHTS RESERVED.
// These coded instructions, statements, and computer programs are the
// copyrighted works and confidential proprietary information of Tensilica Inc.
// They may not be modified, copied, reproduced, distributed, or disclosed to
// third parties in any manner, medium, or form, in whole or in part, without
// the prior written consent of Tensilica Inc.
//

package org.eclipse.cdt.debug.internal.ui.expression.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.actions.AbstractRemoveActionDelegate;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.progress.WorkbenchJob;

public class RemoveExpressionAction extends AbstractRemoveActionDelegate
{
	protected IExpression[] getExpressions() {
		TreeSelection selection = (TreeSelection) getSelection();
		TreePath[] paths = selection.getPaths();
		List expressions = new ArrayList();
		for (int i = paths.length-1; i >=0; i--) {
			TreePath path = paths[i];
			for (int j=0, count=path.getSegmentCount(); j<count; j++)
			{
				Object segment = path.getSegment(j);
				if (segment instanceof IExpression) {
					expressions.add(segment);
					break;
				} else if (segment instanceof IAdaptable) {
				    IExpression expression = (IExpression)((IAdaptable)segment).getAdapter(IExpression.class);
				    if (expression != null) {
				        expressions.add(expression);
				        break;
				    }
				}
			}
		}
		return (IExpression[]) expressions.toArray(new IExpression[expressions.size()]);
	}

	@Override
	public void run(IAction action)
	{
		WorkbenchJob job = new WorkbenchJob("remove expression") { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IExpressionManager expManager = DebugPlugin.getDefault().getExpressionManager();
				IExpression[] exp = getExpressions();
				if (exp != null) {
					expManager.removeExpressions(exp);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		schedule(job);
	}

	@Override
	protected boolean isEnabledFor(Object element)
	{
		if (element instanceof IExpression)
			return true;
		if (element instanceof IAdaptable)
		{
			IExpression expression = (IExpression)((IAdaptable)element).getAdapter(IExpression.class);
		    if (expression != null)
		    	return true;
		}
		
		return false;
	}
}
