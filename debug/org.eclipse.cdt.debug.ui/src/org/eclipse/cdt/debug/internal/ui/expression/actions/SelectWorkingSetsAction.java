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

import org.eclipse.cdt.debug.internal.ui.expression.workingsets.ExpressionWorkingSetViewerFilter;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.expression.ExpressionView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

public class SelectWorkingSetsAction extends Action
{
	private ExpressionView exprView;
	
	public SelectWorkingSetsAction(ExpressionView exprView)
	{
		super("", CDebugUIPlugin.getImageDescriptor("icons/elcl16/expression_workingset.gif")); //$NON-NLS-1$ //$NON-NLS-2$
		setToolTipText(SelectWorkingSetsMessages.SelectWorkingSetsAction_tooltip);
		
		this.exprView = exprView;
	}
	
	@Override
	public void runWithEvent(Event event)
	{
		ExpressionWorkingSetViewerFilter expressionFilter = null;		
		TreeModelViewer treeViewer = (TreeModelViewer) exprView.getViewer();
		ViewerFilter[] allFilters = treeViewer.getFilters();
		for (ViewerFilter filter : allFilters)
		{
			if (filter instanceof ExpressionWorkingSetViewerFilter)
			{
				expressionFilter = (ExpressionWorkingSetViewerFilter) filter;
				break;
			}
		}
		String[] oldWorkingSetNames = expressionFilter != null ? expressionFilter.getWorkingSetNames() : new String[0];
		
		SelectWorkingSetsDialog dropdownDlg = new SelectWorkingSetsDialog(exprView.getSite().getShell(),
																					exprView,
																					oldWorkingSetNames);
		
		dropdownDlg.open();
		
		Point dropdownSize = dropdownDlg.getShell().getSize();

		Display display = PlatformUI.getWorkbench().getDisplay();
		ToolItem item = (ToolItem) event.widget;
		Rectangle rect = item.getBounds();
		Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
	    Rectangle displayRect = display.getClientArea();
	    if ((pt.x + dropdownSize.x + 10) > displayRect.width)
	    	pt.x -= (pt.x + dropdownSize.x + 10 - displayRect.width);
	    pt.y += rect.height;
		dropdownDlg.getShell().setLocation(pt);
	}
}
