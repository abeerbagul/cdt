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

package org.eclipse.cdt.dsf.debug.ui.viewmodel.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMModelProxy;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

public class ExpressionWorkingSetVMNode extends AbstractVMNode implements IElementLabelProvider, IPropertyChangeListener
{
	//same as IExpressionWorkingSetConstants.PROP_EXPRESSION_WORKINGSETS
	public static String PROP_EXPRESSION_WORKINGSETS = "ExpressionWorkingSets"; //$NON-NLS-1$
	//same as IExpressionWorkingSetConstants.ID
	public static String ID = "org.eclipse.cdt.debug.ui.expressionWorkingSets"; //$NON-NLS-1$
	public static String WORKINGSET_OTHERS = "Others (No working set)"; //$NON-NLS-1$
	
	private String[] workingSetNames = null;
	
	public ExpressionWorkingSetVMNode(ExpressionVMProvider provider, String[] workingSetNames)
	{
		super(provider);
		this.workingSetNames = workingSetNames;
		
		PlatformUI.getWorkbench().getWorkingSetManager().addPropertyChangeListener(this);
	}
	
	@Override
	public int getDeltaFlags(Object event)
	{
		int retVal = 0;
		
		if (event instanceof ExpressionsChangedEvent)
		{
			retVal = retVal | IModelDelta.CONTENT;
		}
		else if (event instanceof PropertyChangeEvent)
		{
			PropertyChangeEvent propEvent = (PropertyChangeEvent) event;
			
			if (propEvent.getProperty().equals(PROP_EXPRESSION_WORKINGSETS))
			{
				retVal |= IModelDelta.CONTENT;
				
				workingSetNames = (String[]) propEvent.getNewValue();
			}
		}
		
		return retVal;
	}

	@Override
	public void buildDelta(Object event, VMDelta parentDelta, int nodeOffset,
			RequestMonitor requestMonitor)
	{
		parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
		
		requestMonitor.done();
	}
	
	@Override
	public void update(IChildrenCountUpdate[] updates)
	{
		for (final IChildrenCountUpdate update : updates)
		{
            if (!checkUpdate(update)) continue;
            
            update.setChildCount(workingSetNames.length + 1);
            update.done();
		}
	}

	@Override
	public void update(IChildrenUpdate[] updates)
	{
		for (final IChildrenUpdate update : updates)
		{
            if (!checkUpdate(update)) continue;

            int updateIdx = update.getOffset() != -1 ? update.getOffset() : 0;
            int endIdx = updateIdx + (update.getLength() != -1 ? update.getLength() : (workingSetNames.length));
            endIdx += 1;
            while (updateIdx < endIdx && updateIdx < workingSetNames.length)
            {
                update.setChild(new ExpressionWorkingSetVMContext(this, workingSetNames[updateIdx]), updateIdx);
                updateIdx++;
            }
            
            update.setChild(new ExpressionWorkingSetVMContext(this, WORKINGSET_OTHERS), updateIdx); 
        
            update.done();
		}
	}

	@Override
	public void update(IHasChildrenUpdate[] updates)
	{
		for (final IHasChildrenUpdate update : updates)
		{
            if (!checkUpdate(update)) continue;
            
        	update.setHasChilren(workingSetNames != null);
        	update.done();
		}
	}
	
	@Override
	public void update(ILabelUpdate[] updates)
	{
		for (ILabelUpdate update : updates)
		{
			String[] columnIds = update.getColumnIds() != null ? 
	            update.getColumnIds() : new String[] { IDebugVMConstants.COLUMN_ID__EXPRESSION };
	            
	        String workingSetName = ((ExpressionWorkingSetVMContext) update.getElement()).getWorkingSetName();
	            
        	for (int i = 0; i < columnIds.length; i++)
        	{
        		if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(columnIds[i]))
        		{
        			update.setLabel(workingSetName, i);
        			update.setImageDescriptor(DsfUIPlugin.getImageDescriptor("icons/expression_workingset.gif"), i); //$NON-NLS-1$
        		}
        		else
        		{
        			update.setLabel("", i); //$NON-NLS-1$
        		}
        	}
        	
        	update.done();
		}
	}
	
	@Override
	public void dispose()
	{
		PlatformUI.getWorkbench().getWorkingSetManager().removePropertyChangeListener(this);
		
		super.dispose();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		if (event.getProperty().equals(IWorkingSetManager.CHANGE_WORKING_SET_ADD)
			|| event.getProperty().equals(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE))
		{
			IWorkingSet addedWorkingSet = (IWorkingSet) event.getNewValue();
			List<String> visibleWorkingSetNames = Arrays.asList(workingSetNames);
			if (visibleWorkingSetNames.contains(addedWorkingSet.getName()))
				return;
			
			List<IExpression> movedExpressions = new ArrayList<IExpression>();
			
			if (ID.equals(addedWorkingSet.getId()))
			{
				for (IAdaptable workingSetExpr : addedWorkingSet.getElements())
				{
					movedExpressions.add((IExpression) workingSetExpr);
				}
			}
			
			Set<Object> rootElements = new HashSet<Object>();
	        for (IVMModelProxy proxy : ((ExpressionVMProvider) getVMProvider()).getActiveModelProxies()) {
	            rootElements.add(proxy.getRootElement());
	        }
			
			((AbstractVMProvider) getVMProvider()).handleEvent(
					new ExpressionsChangedEvent(ExpressionsChangedEvent.Type.MOVED, 
												rootElements, 
												movedExpressions.toArray(new IExpression[0]), 
												-1));
		}
	}
}
