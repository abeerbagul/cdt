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

package org.eclipse.cdt.debug.internal.ui.expression.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.internal.ui.expression.workingsets.ExpressionViewWorkingSetUtils;
import org.eclipse.cdt.debug.ui.expressions.IExpressionWorkingSetConstants;
import org.eclipse.debug.internal.ui.views.expression.ExpressionView;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

public class SelectWorkingSetsDialog extends PopupDialog
{
	private class WorkingSetCheckListener implements ICheckStateListener
	{
		@Override
		public void checkStateChanged(CheckStateChangedEvent event)
		{
			if (event.getChecked())
				checkedWorkingSetNames.add((String) event.getElement());
			else
				checkedWorkingSetNames.remove(event.getElement());
		}
	}

	private ExpressionView exprView;
	
	private List<String> checkedWorkingSetNames = new ArrayList<String>();
	private CheckboxTableViewer workingSetsViewer;
	
	public SelectWorkingSetsDialog(Shell parentShell,
										ExpressionView exprView,
										String[] checkedWorkingSetNames)
	{
		super(parentShell,
				PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE,
				true,
				false,
				false,
				false,
				false,
				SelectWorkingSetsMessages.SelectWorkingSetsDialog_title,
				null);
		
		this.exprView = exprView;
		
		for (String workingSetName : checkedWorkingSetNames)
		{
			this.checkedWorkingSetNames.add(workingSetName);
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent)
	{
		workingSetsViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
//		gd.heightHint = 200;
		gd.widthHint = 100;
		workingSetsViewer.getTable().setLayoutData(gd);
		
		workingSetsViewer.setContentProvider(new ArrayContentProvider());
		workingSetsViewer.setLabelProvider(new LabelProvider());
		workingSetsViewer.addCheckStateListener(new WorkingSetCheckListener());
		
		populateData();
		
		return workingSetsViewer.getTable();
	}
	
	private void populateData()
	{
		//get a list of all expression working sets in the workspace
		List<String> workingSetNames = new ArrayList<String>();
		IWorkingSet[] workingsets = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
		for (IWorkingSet workingset : workingsets)
		{	
			if (workingset.getId() == null)
				continue;
			if (! workingset.getId().equals(IExpressionWorkingSetConstants.ID))
				continue;
			
			workingSetNames.add(workingset.getName());
		}
		
		workingSetsViewer.setInput(workingSetNames);
		workingSetsViewer.setCheckedElements(checkedWorkingSetNames.toArray(new String[checkedWorkingSetNames.size()]));
	}
	
	String[] getCheckedWorkingSetNames()
	{
		return checkedWorkingSetNames.toArray(new String[checkedWorkingSetNames.size()]);
	}
	
	@Override
	public boolean close()
	{
		try
		{
			//apply the checked working sets to the expression view
			String[] newWorkingSetNames = getCheckedWorkingSetNames();
    		ExpressionViewWorkingSetUtils utils = new ExpressionViewWorkingSetUtils();
    		utils.updateView(exprView, newWorkingSetNames);
    		if (newWorkingSetNames.length > 0)
    			utils.saveWorkingSet(exprView, newWorkingSetNames);
    		else
    			utils.removeWorkingSet(exprView);
		}
		finally
		{
			//make sure that super.close is always called
			//inspite of any exceptions in try block
		}

		return super.close();
	}
}
