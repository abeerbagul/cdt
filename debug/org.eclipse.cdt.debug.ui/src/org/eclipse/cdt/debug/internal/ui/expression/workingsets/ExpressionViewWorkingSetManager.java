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

import org.eclipse.cdt.debug.internal.ui.expression.actions.SelectWorkingSetsAction;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.views.expression.ExpressionView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;



/**
 * Since we do not have expression working set support in the platform ui,
 * we need a workaround to apply working sets to an expression view 
 * when it opens, as well as when the workbench initializes.
 * 
 * This class is similar to ViewIDCounterManager.
 *
 */
public class ExpressionViewWorkingSetManager {
	
	private static final String GROUP_EXPRESSION = "expressionGroup"; //$NON-NLS-1$
	private static final String ID_REMOVE_SELECTED_EXPRESSIONS = "org.eclipse.debug.ui.expresssionsView.toolbar.remove"; //$NON-NLS-1$
	private static final String ID_REMOVE_ALL_EXPRESSIONS = "org.eclipse.debug.ui.expresssionsView.toolbar.removeAll"; //$NON-NLS-1$
	
	private static ExpressionViewWorkingSetManager INSTANCE = null;
	
	private static boolean fInitialized = false;	
	
	private ExpressionViewWorkingSetManager() {
		initListeners();
	}

	/**
	 * Returns an instance of the view id counter manager.
	 * 
	 * @return the counter manager.
	 */
	synchronized public static ExpressionViewWorkingSetManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ExpressionViewWorkingSetManager();
		}
		return INSTANCE;
	}
	
	synchronized public void init() {
		if (fInitialized) return;
		fInitialized = true;
		
		new WorkbenchJob("Applying working sets to Expression Views") { //$NON-NLS-1$
			{ setSystem(true); }
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
				for (IWorkbenchWindow window : windows) {
					IViewReference[] viewRefs = window.getActivePage().getViewReferences();
					for (IViewReference viewRef : viewRefs) {
						if (viewRef.getId().equals(IDebugUIConstants.ID_EXPRESSION_VIEW)) {
							customizeExpressionView(viewRef.getPart(false));
						}
					}
				}
				
				return Status.OK_STATUS;
			}
		}.schedule();
	}
	
	private void initListeners() {
		final IPartListener2 partListener = new IPartListener2() {
			@Override
			public void partVisible(IWorkbenchPartReference partRef) {}					
			@Override
			public void partInputChanged(IWorkbenchPartReference partRef) {}						
			@Override
			public void partHidden(IWorkbenchPartReference partRef) {}						
			@Override
			public void partDeactivated(IWorkbenchPartReference partRef) {}																		
			@Override
			public void partBroughtToTop(IWorkbenchPartReference partRef) {}						
			@Override
			public void partActivated(IWorkbenchPartReference partRef) {}
			@Override
			public void partClosed(IWorkbenchPartReference partRef) {}

			@Override
			public void partOpened(IWorkbenchPartReference partRef) {
				if (partRef instanceof IViewReference) {
					if (partRef.getId().equals(IDebugUIConstants.ID_EXPRESSION_VIEW)) {
						customizeExpressionView(((IViewReference) partRef).getView(false));
					}
				}
			}
		};
		
		IWorkbench wb = PlatformUI.getWorkbench();

		// subscribe to existing workbench window listener
		for (IWorkbenchWindow ww : wb.getWorkbenchWindows()) {
			ww.getPartService().addPartListener(partListener);
		}
		
		// subscribe to new workbench window listener
		wb.addWindowListener(new IWindowListener() {					
			@Override
			public void windowDeactivated(IWorkbenchWindow window) {}												
			@Override
			public void windowActivated(IWorkbenchWindow window) {}				
			@Override
			public void windowClosed(IWorkbenchWindow window) {}
			
			@Override
			public void windowOpened(IWorkbenchWindow window) {
				window.getPartService().addPartListener(partListener);
			}		
		});
	}

	protected void customizeExpressionView(final IWorkbenchPart view)
	{
		if (view == null)
			return;
		
		final ExpressionView exprView = (ExpressionView) view;
		
		ExpressionViewWorkingSetUtils utils = new ExpressionViewWorkingSetUtils();
		utils.restoreMemento(exprView);

		replaceRemoveActions(exprView);
		addWorkingSetAction(exprView);
	}
	
	private void addWorkingSetAction(ExpressionView exprView)
	{
		IToolBarManager toolbarMgr = exprView.getViewSite().getActionBars().getToolBarManager();
		toolbarMgr.appendToGroup(GROUP_EXPRESSION, new SelectWorkingSetsAction(exprView));
		toolbarMgr.update(false);
	}
	
	private void replaceRemoveActions(ExpressionView exprView)
	{
		IToolBarManager toolbarMgr = exprView.getViewSite().getActionBars().getToolBarManager();
		toolbarMgr.remove(ID_REMOVE_SELECTED_EXPRESSIONS);
		IContributionItem originalRemoveAll = toolbarMgr.remove(ID_REMOVE_ALL_EXPRESSIONS);
		
		toolbarMgr.appendToGroup(GROUP_EXPRESSION, originalRemoveAll);
		
		toolbarMgr.update(false);
	}
}
