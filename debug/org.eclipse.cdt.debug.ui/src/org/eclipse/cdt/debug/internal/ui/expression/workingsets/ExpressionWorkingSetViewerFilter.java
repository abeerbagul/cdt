/*******************************************************************************
 * Copyright (c) 2012 Tensilica Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Abeer Bagul (Tensilica Inc) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.expression.workingsets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.debug.ui.expressions.IExpressionWorkingSetConstants;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionsListener;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.views.expression.ExpressionView;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

/**
 * This filter is a placeholder to cache the working sets currently being used by the view.
 * Actual filtering out of unselected working sets is done by ExpressionWorkingSetVMNode.
 */
public class ExpressionWorkingSetViewerFilter extends ViewerFilter implements IPropertyChangeListener, IPartListener, IExpressionsListener
{
	private ExpressionView expressionView;
	private String[] workingSetNames;
	
	public ExpressionWorkingSetViewerFilter(ExpressionView expressionView)
	{
		this.expressionView = expressionView;
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(this);
		DebugPlugin.getDefault().getExpressionManager().addExpressionListener(this);
		
		PlatformUI.getWorkbench().getWorkingSetManager().addPropertyChangeListener(this);
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element)
	{	
		return true;
	}

	public void setWorkingSetNames(String[] workingSetNames)
	{
		this.workingSetNames = workingSetNames;
	}
	
	public String[] getWorkingSetNames()
	{
		return workingSetNames;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		if (event.getProperty().equals(IWorkingSetManager.CHANGE_WORKING_SET_REMOVE))
		{
			List<String> remainingWorkingSets = new ArrayList<String>(Arrays.asList(workingSetNames));
			boolean wsRemoved = false;
			IWorkingSet removedWS = (IWorkingSet) event.getOldValue();
			for (String wsName : workingSetNames)
			{
				if (removedWS.getName().equals(wsName))
				{
					remainingWorkingSets.remove(wsName);
					wsRemoved = true;
					break;
				}
			}
			if (wsRemoved)
			{
				String[] workingSetNames = remainingWorkingSets.toArray(new String[0]);

				ExpressionViewWorkingSetUtils utils = new ExpressionViewWorkingSetUtils();
				utils.updateView(expressionView, workingSetNames);
			}
		}
	}

	@Override
	public void partActivated(IWorkbenchPart part)
	{
		
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part)
	{
		
	}

	@Override
	public void partClosed(IWorkbenchPart part)
	{
		if (! (part == expressionView))
			return;
		
		expressionView = null;
		
		DebugPlugin.getDefault().getExpressionManager().removeExpressionListener(this);
		
		PlatformUI.getWorkbench().getWorkingSetManager().removePropertyChangeListener(this);
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null)
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().removePartListener(this);
	}

	@Override
	public void partDeactivated(IWorkbenchPart part)
	{
		
	}

	@Override
	public void partOpened(IWorkbenchPart part)
	{
		
	}

	@Override
	public void expressionsAdded(IExpression[] expressions)
	{

	}

	@Override
	public void expressionsRemoved(IExpression[] expressions)
	{
		//remove these expressions from all working sets where they are present
		for (IWorkingSet exprWorkingSet : PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets())
		{
			if (IExpressionWorkingSetConstants.ID.equals(exprWorkingSet.getId()))
			{
				List<IExpression> remainingExprs = new ArrayList<IExpression>();
				for (IAdaptable workingSetExpr : exprWorkingSet.getElements())
				{
					remainingExprs.add((IExpression) workingSetExpr);
				}
				
				for (IExpression removedExpr : expressions)
				{
					remainingExprs.remove(removedExpr);
				}
				
				exprWorkingSet.setElements(remainingExprs.toArray(new IAdaptable[remainingExprs.size()]));
			}
		}
	}

	@Override
	public void expressionsChanged(IExpression[] expressions)
	{
		
	}
}
