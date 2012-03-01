//
// Copyright (c) 2011 by Tensilica Inc.  ALL RIGHTS RESERVED.
// These coded instructions, statements, and computer programs are the
// copyrighted works and confidential proprietary information of Tensilica Inc.
// They may not be modified, copied, reproduced, distributed, or disclosed to
// third parties in any manner, medium, or form, in whole or in part, without
// the prior written consent of Tensilica Inc.
//

package org.eclipse.cdt.debug.internal.ui.expression.workingsets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetPage;

/**
 * This page allows the user to create and edit working sets of expressions.
 * 
 * @author Abeerb
 *
 */
public class ExpressionWorkingSetPage extends WizardPage implements IWorkingSetPage
{	
	private class ExpressionLabelProvider implements ITableLabelProvider
	{

		@Override
		public void addListener(ILabelProviderListener listener)
		{
			//noop
		}

		@Override
		public void dispose()
		{
			//noop
		}

		@Override
		public boolean isLabelProperty(Object element, String property)
		{
			//noop
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener)
		{
			//noop
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex)
		{
			if (element instanceof IExpression)
				return CDebugUIPlugin.getImageDescriptorRegistry().get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_EXPRESSION ) );
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex)
		{
			if (element instanceof IExpression)
				return ((IExpression) element).getExpressionText();
			return null;
		}
		
	}
	
	private static final String PAGE_NAME = "ExpressionWorkingSetPage"; //$NON-NLS-1$
	
	private IWorkingSet fWorkingSet;
	
	private Text fWorkingSetName;
	private CheckboxTableViewer expressionViewer;
	
	private boolean fFirstCheck;

	public ExpressionWorkingSetPage()
	{
		super(PAGE_NAME, 
			ExpressionWorkingSetPageMessages.PageTitle, 
			DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_WIZBAN_DEBUG));
		setDescription(ExpressionWorkingSetPageMessages.PageDescription);
		fFirstCheck= true;
	}
	
	@Override
	public void createControl(Composite parent)
	{
		initializeDialogUnits(parent);
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);
		Label label= new Label(composite, SWT.WRAP);
		label.setText(ExpressionWorkingSetPageMessages.LabelWorkingSetName);
		GridData gd= new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);
		fWorkingSetName= new Text(composite, SWT.SINGLE | SWT.BORDER);
		fWorkingSetName.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		fWorkingSetName.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			}
		);
		fWorkingSetName.setFocus();
		label= new Label(composite, SWT.WRAP);
		label.setText(ExpressionWorkingSetPageMessages.LabelExpressions);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);
		expressionViewer = createExpressionViewer(composite);
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(2, false));
		buttonComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		Button selectAllButton = SWTFactory.createPushButton(buttonComposite, 
															ExpressionWorkingSetPageMessages.SelectAllExpressions, 
															null);
		selectAllButton.setToolTipText(ExpressionWorkingSetPageMessages.SelectAllExpressionsTooltip);
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				expressionViewer.getTable().selectAll();
				expressionViewer.setCheckedElements(((IStructuredSelection)expressionViewer.getSelection()).toArray());
				expressionViewer.setGrayedElements(new Object[] {});
				expressionViewer.getTable().deselectAll();
				validateInput();
			}
		});
		Button deselectAllButton = SWTFactory.createPushButton(buttonComposite, 
																ExpressionWorkingSetPageMessages.DeselectAllExpressions,
																null);
		deselectAllButton.setToolTipText(ExpressionWorkingSetPageMessages.DeselectAllExpressionsTooltip);
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				expressionViewer.setCheckedElements(new Object[] {});
				validateInput();
			}
		});
		
		populateData();
		validateInput();
		Dialog.applyDialogFont(composite);
	}
	
	private CheckboxTableViewer createExpressionViewer(Composite parent)
	{
		CheckboxTableViewer expressionViewer = new CheckboxTableViewer(new Table(parent, SWT.MULTI | SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER));
		
		TableColumn nameColumn = new TableColumn(expressionViewer.getTable(), SWT.NONE);
		nameColumn.setText(ExpressionWorkingSetPageMessages.NameColumn);
		nameColumn.setWidth(100);
		
//		TableColumn workingSetNameColumn = new TableColumn(expressionViewer.getTable(), SWT.NONE);
//		workingSetNameColumn.setText("Working set");
//		workingSetNameColumn.setWidth(100);
		
		expressionViewer.getTable().setHeaderVisible(true);
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 150;
		expressionViewer.getTable().setLayoutData(gd);
		
		expressionViewer.setContentProvider(new ArrayContentProvider());
		expressionViewer.setLabelProvider(new ExpressionLabelProvider());
		
		expressionViewer.setInput(DebugPlugin.getDefault().getExpressionManager().getExpressions());
		
		return expressionViewer;
	}
	
	private void populateData()
	{
		if (fWorkingSet != null)
		{
			fWorkingSetName.setText(fWorkingSet.getName());
			
			IAdaptable[] workingSetExpressions = fWorkingSet.getElements();
			expressionViewer.setCheckedElements(workingSetExpressions);
		}
	}

	@Override
	public void finish()
	{
		String workingSetName = fWorkingSetName.getText();
		Object[] adaptable = expressionViewer.getCheckedElements();
		List<IAdaptable> elements = new ArrayList<IAdaptable>();
		//weed out non-breakpoint elements since 3.2
		for(int i = 0; i < adaptable.length; i++) {
            IExpression selectedExpression = (IExpression)DebugPlugin.getAdapter(adaptable[i], IExpression.class);
			if(selectedExpression != null) {
				elements.add(selectedExpression);
			}//end if
		}//end for
		if (fWorkingSet == null) {
			IWorkingSetManager workingSetManager= PlatformUI.getWorkbench().getWorkingSetManager();
			fWorkingSet = workingSetManager.createWorkingSet(workingSetName, elements.toArray(new IAdaptable[elements.size()]));
		} else {
			fWorkingSet.setName(workingSetName);
			fWorkingSet.setElements(elements.toArray(new IAdaptable[elements.size()]));
		}
	}

	@Override
	public IWorkingSet getSelection()
	{
		return fWorkingSet;
	}

	@Override
	public void setSelection(IWorkingSet workingSet)
	{
		fWorkingSet= workingSet;
		if (getContainer() != null && getShell() != null && fWorkingSetName != null) {
			fFirstCheck= false;
			fWorkingSetName.setText(fWorkingSet.getName());
			validateInput();
		}
	}

	/**
	 * validates the current input of the page to determine if the finish button can be enabled
	 */
	private void validateInput() {
		String errorMessage= null; 
		String newText= fWorkingSetName.getText();

		if (newText.equals(newText.trim()) == false)
			errorMessage = ExpressionWorkingSetPageMessages.ErrorWorkingSetWhitespace; 
		if (newText.equals(IInternalDebugCoreConstants.EMPTY_STRING)) {
			if (fFirstCheck) {
				setPageComplete(false);
				fFirstCheck= false;
				return;
			}		
			errorMessage= ExpressionWorkingSetPageMessages.ErrorNameEmpty; 
		}
		fFirstCheck= false;
		if (errorMessage == null && (fWorkingSet == null || newText.equals(fWorkingSet.getName()) == false)) {
			IWorkingSet[] workingSets= PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
			for (int i= 0; i < workingSets.length; i++) {
				if (newText.equals(workingSets[i].getName())) {
					errorMessage= ExpressionWorkingSetPageMessages.ErrorNameExists; 
				}
			}
		}
		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}
}
