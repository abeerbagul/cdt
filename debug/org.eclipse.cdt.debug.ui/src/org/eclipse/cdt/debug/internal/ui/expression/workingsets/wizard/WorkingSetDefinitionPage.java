//
// Copyright (c) 2011 by Tensilica Inc.  ALL RIGHTS RESERVED.
// These coded instructions, statements, and computer programs are the
// copyrighted works and confidential proprietary information of Tensilica Inc.
// They may not be modified, copied, reproduced, distributed, or disclosed to
// third parties in any manner, medium, or form, in whole or in part, without
// the prior written consent of Tensilica Inc.
//

package org.eclipse.cdt.debug.internal.ui.expression.workingsets.wizard;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.debug.internal.ui.expression.workingsets.ExpressionWorkingSetElementAdapter;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.expressions.IExpressionWorkingSetConstants;
import org.eclipse.cdt.ui.dialogs.IInputStatusValidator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

/**
 * In this page, the user can define new working sets and update existing ones.
 * Operations supported are:
 * 1. Create a working set
 * 2. Create an expression
 * 3. Assign an expression to a working set
 * 4. Rename a working set
 * 5. Delete a working set
 * 6. Delete an expression
 * 
 * It will also show the name of the expression view it is working on.
 * 
 * This page will not allow the user to select working sets to show in the expression view. 
 * 
 * @author Abeerb
 *
 */
public class WorkingSetDefinitionPage extends WizardPage
{
	private static Pattern namePattern = Pattern.compile("\\w+"); //$NON-NLS-1$
	
	/**
	 * Validates the name of a working set while adding a new working set
	 * or while editing an existing working set.
	 * 
	 * @author Abeerb
	 *
	 */
	private class WorkingsetNameValidator implements IInputStatusValidator
	{	
		private String existingName = null;
		
		@Override
		public IStatus isValid(String newText)
		{
			newText = newText.trim();
			
			if (newText.equals(existingName))
				return Status.OK_STATUS;
			
			if (newText.length() == 0)
				return new Status(IStatus.ERROR, 
									CDebugUIPlugin.PLUGIN_ID, 
									WorkingSetWizardMessages.WorkingSetNameError_Empty);
			
			Matcher nameMatcher = namePattern.matcher(newText);
			if (!nameMatcher.matches())
				return new Status(IStatus.ERROR, 
									CDebugUIPlugin.PLUGIN_ID,
									MessageFormat.format(WorkingSetWizardMessages.WorkingSetNameError_Pattern,
															namePattern));
			
			if (originalWorkingSetNames.contains(newText))
				return new Status(IStatus.ERROR, 
									CDebugUIPlugin.PLUGIN_ID,
									WorkingSetWizardMessages.WorkingSetNameError_Exists);
			
			return Status.OK_STATUS;
		}
		
		public void setExistingName(String workingSetName)
		{
			this.existingName = workingSetName;
		}
	}
	
	private class WorkingSetSelectionListener implements ISelectionChangedListener
	{

		@Override
		public void selectionChanged(SelectionChangedEvent event)
		{
			Object selectedObj = ((IStructuredSelection)event.getSelection()).getFirstElement();
			if (selectedObj == null)
				selectWorkingSet(null);
			else if (selectedObj instanceof AddNewWorkingSet)
				selectWorkingSet(null);
			else
			{
    			selectWorkingSet((IWorkingSet) selectedObj);
			}
		}
		
	}
	
	private void selectWorkingSet(IWorkingSet workingset)
	{
		checkExpressions(workingset);
		
		btnSelectAllExpressions.setEnabled(workingset != null);
		btnDeselectAllExpressions.setEnabled(workingset != null);
	}
	
	private class ExpressionCheckedListener implements ICheckStateListener
	{
		@Override
		public void checkStateChanged(CheckStateChangedEvent event)
		{
			if (event.getElement() instanceof AddNewExpression)
			{
				if (event.getChecked())
				{
					setMessage(WorkingSetWizardMessages.CannotCheckAddExpression, INFORMATION);
					expressionViewer.setChecked(event.getElement(), false);
				}
				return;
			}
			
			Object selectedElement = ((IStructuredSelection)workingsetViewer.getSelection()).getFirstElement();
			if (selectedElement instanceof AddNewWorkingSet)
			{
				if (event.getChecked())
				{
					setMessage(WorkingSetWizardMessages.CannotAddExpressionToAddNewWorkingSet, INFORMATION);
					expressionViewer.setChecked(event.getElement(), false);
				}
			}
			
			IWorkingSet selectedWorkingSet = (IWorkingSet) selectedElement;
			if (selectedWorkingSet == null)
				return;
			
			IWatchExpression selectedExpression = (IWatchExpression) event.getElement();
			
			List<IWatchExpression> workingSetExpressions = workingSetExpressionsMap.get(selectedWorkingSet);
			if (event.getChecked())
			{
				workingSetExpressions.add(selectedExpression);
			}
			else
			{
				workingSetExpressions.remove(selectedExpression);
			}
		}
		
	}
	
/*	private class AddNewExpressionListener extends SelectionAdapter
	{
		public void widgetSelected(SelectionEvent event)
		{
			String expressionText = txtNewExpression.getText().trim();
			if (expressionText.length() == 0)
			{
				MessageDialog.openError(getShell(), 
										"Add new expression", 
										"Cannot add empty expression");
				return;
			}
			
			if (originalExpressions.contains(expressionText))
			{
				MessageDialog.openError(getShell(), 
                						"Add new expression", 
                						"Expression already exists");
				return;
			}
			
			expressionText = DefaultLabelProvider.encodeEsacpedChars(expressionText);
			IWatchExpression newExpression= DebugPlugin.getDefault().getExpressionManager().newWatchExpression(expressionText);
			originalExpressions.add(newExpression);
			
			String selectedWorkingSet = (String) ((IStructuredSelection)workingsetViewer.getSelection()).getFirstElement();
			if (selectedWorkingSet == null)
				return;
			
			List<String> workingSetExpressions = workingSetExpressionsMap.get(selectedWorkingSet);
			workingSetExpressions.add(expressionText);
			
			expressionViewer.add(expressionText);
			expressionViewer.setChecked(expressionText, true);
		}
	}
*/	
	private class SelectAllExpressionsListener extends SelectionAdapter
	{
		public void widgetSelected(SelectionEvent event)
		{
			IWorkingSet selectedWorkingSet = (IWorkingSet) ((IStructuredSelection)workingsetViewer.getSelection()).getFirstElement();
			if (selectedWorkingSet == null)
				return;
			
			List<IWatchExpression> workingSetExpressions = workingSetExpressionsMap.get(selectedWorkingSet);
			workingSetExpressions.clear();
			workingSetExpressions.addAll(originalExpressions);
			workingSetExpressions.addAll(addedExpressions);
			
			expressionViewer.setAllChecked(true);
			expressionViewer.setChecked(addNewExpressionNode, false);
		}
	}
	
	private class DeselectAllExpressionsListener extends SelectionAdapter
	{
		public void widgetSelected(SelectionEvent event)
		{
			IWorkingSet selectedWorkingSet = (IWorkingSet) ((IStructuredSelection)workingsetViewer.getSelection()).getFirstElement();
			if (selectedWorkingSet == null)
				return;
			
			List<IWatchExpression> workingSetExpressions = workingSetExpressionsMap.get(selectedWorkingSet);
			workingSetExpressions.clear();
			
			expressionViewer.setAllChecked(false);
		}
	}
	
	private class ExpressionModifier implements ICellModifier
	{

		@Override
		public boolean canModify(Object element, String property)
		{
			if (element instanceof AddNewExpression)
				return true;
			if (element instanceof IWatchExpression)
				return true;
			return false;
		}

		@Override
		public Object getValue(Object element, String property)
		{
			if (element instanceof AddNewExpression)
				return ""; //$NON-NLS-1$
			int exprIndex = -1;
			for (IExpression exprObj : originalExpressions)
			{
				exprIndex++;

				if (exprObj == element)
				{
					return originalExpressionValues.get(exprIndex);
				}
			}

			for (IWatchExpression exprObj : addedExpressions)
			{
				if (exprObj == element)
				{
					return exprObj.getExpressionText();
				}
			}
			
			return ""; //$NON-NLS-1$
		}

		@Override
		public void modify(Object element, String property, Object value)
		{
			try
			{
    			String expressionText = (String) value;
    			expressionText = expressionText.trim();
    			if (expressionText.length() == 0)
    				return;
    			
    			expressionText = DefaultLabelProvider.encodeEsacpedChars(expressionText);
    			
    			if (element instanceof TableItem)
    				element = ((TableItem) element).getData();
    			
    			if (element instanceof AddNewExpression)
    			{
    				IWatchExpression newExpression= DebugPlugin.getDefault().getExpressionManager().newWatchExpression(expressionText);
    				addedExpressions.add(newExpression);
    				
    				expressionViewer.insert(newExpression, expressionViewer.getTable().getItemCount() - 1);
    				return;
    			}			
    			//do not update the expr obj immediately with the new value.
    			//store the new value in the list of modified expr values 
    			//at the same index at which the expr obj is present in the original exprs list.
    			int exprIndex = 0;
    			if (originalExpressions.size() > 0)
    			{
        			for (IExpression exprObj : originalExpressions)
        			{
        				if (exprObj == element)
        					break;
        				
        				exprIndex++;
        			}
        			
        			if (exprIndex < originalExpressions.size())
        			{
            			originalExpressionValues.remove(exprIndex);
            			originalExpressionValues.add(exprIndex, expressionText);
            			
            			return;
        			}
    			}
    			if (addedExpressions.size() > 0)
    			{
    				for (IExpression exprObj : addedExpressions)
        			{
        				if (exprObj == element)
        				{
        					((IWatchExpression) exprObj).setExpressionText(expressionText);
        					
        					return;
        				}
        			}
    			}
			}
			finally
			{
				expressionViewer.update(element, null);
			}
		}
		
	}
	
	private class AddNewExpression
	{
		
	}
	
	private class AddNewWorkingSet
	{
		
	}
	
	private class ExpressionLabelProvider extends LabelProvider implements IFontProvider
	{
		private Font addNewExpressionFont = null;
		
		@Override
		public String getText(Object element)
		{
			if (element instanceof AddNewExpression)
				return WorkingSetWizardMessages.AddNewExpression;
			int exprIndex = 0;
			boolean isOriginal = false;
			for (IExpression exprObj : originalExpressions)
			{
				if (exprObj == element)
				{
					isOriginal = true;
					break;
				}
				
				exprIndex++;
			}
			if (isOriginal)
				return originalExpressionValues.get(exprIndex);
			else
				return ((IWatchExpression) element).getExpressionText();
		}
		
		@Override
		public Image getImage(Object element)
		{
			if (element instanceof AddNewExpression)
				return CDebugUIPlugin.getImageDescriptorRegistry().get(
						DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_MONITOR_EXPRESSION));

			return CDebugUIPlugin.getImageDescriptorRegistry().get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_EXPRESSION ) );
		}

		@Override
		public Font getFont(Object element) 
		{
			if (element instanceof AddNewExpression)
			{
				if (addNewExpressionFont == null)
				{
					// Display the "Add new expression" element in italic to 
		            // distinguish it from user elements in view.
		            FontData fontData = JFaceResources.getFontDescriptor(IDebugUIConstants.PREF_VARIABLE_TEXT_FONT).getFontData()[0];
		            fontData.setStyle(SWT.ITALIC);
		            addNewExpressionFont = new Font(Display.getCurrent(), fontData);
				}
				
				return addNewExpressionFont;
			}

			return null;
		}
		
		@Override
		public void dispose()
		{
			if (addNewExpressionFont != null)
				addNewExpressionFont.dispose();
			
			addNewExpressionFont = null;
		}
	}
	
	private class ExpressionContentProvider implements IStructuredContentProvider
	{

		@Override
		public void dispose()
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object[] getElements(Object inputElement)
		{
			List<Object> allExpressions = new ArrayList<Object>();
			allExpressions.addAll(originalExpressions);
			allExpressions.add(addNewExpressionNode);
			
			return allExpressions.toArray(new Object[0]);
		}
		
	}
	
	private class DeleteExpressionListener implements KeyListener
	{

		@Override
		public void keyPressed(KeyEvent e)
		{
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			if (e.keyCode == SWT.DEL)
				deleteExpression();
		}
	}

	private void deleteExpression()
	{
		if (expressionViewer.getSelection().isEmpty())
			return;

		if (((IStructuredSelection) expressionViewer.getSelection()).size() > 1)
			return;
		
		Object selectedObj = ((IStructuredSelection) expressionViewer.getSelection()).getFirstElement();
		if (selectedObj == addNewExpressionNode)
		{
			setMessage(WorkingSetWizardMessages.CannotDeleteAddExpression, INFORMATION);
			return;
		}
		
		IWatchExpression exprToDelete = (IWatchExpression) selectedObj;
		boolean isAddedExpression = false;
		int addedIndex = 0;
		for (IWatchExpression addedExpr : addedExpressions)
		{
			if (addedExpr == exprToDelete)
			{
				isAddedExpression = true;
				break;
			}
			addedIndex++;
		}
		
		if (isAddedExpression)
		{
			addedExpressions.remove(addedIndex);
			expressionViewer.remove(exprToDelete);
			return;
		}
		
		boolean isOriginalExpression = false;
		int originalIndex = 0;
		for (IWatchExpression originalExpr : originalExpressions)
		{
			if (originalExpr == exprToDelete)
			{
				isOriginalExpression = true;
				break;
			}
			originalIndex++;
		}
		
		if (isOriginalExpression)
		{
			originalExpressions.remove(originalIndex);
			originalExpressionValues.remove(originalIndex);
			
			deletedExpressions.add(exprToDelete);
		}
		
		expressionViewer.remove(exprToDelete);
	}
	
	private class WorkingSetLabelProvider extends LabelProvider implements IFontProvider
	{
		private Font addNewWorkingsetFont = null;
		
		@Override
		public String getText(Object element)
		{
			if (element instanceof AddNewWorkingSet)
				return WorkingSetWizardMessages.AddNewWorkingSet;
			int workingsetIndex = 0;
			boolean isOriginal = false;
			for (IWorkingSet workingsetObj : originalWorkingSets)
			{
				if (workingsetObj == element)
				{
					isOriginal = true;
					break;
				}
				
				workingsetIndex++;
			}
			if (isOriginal)
				return originalWorkingSetNames.get(workingsetIndex);
			else
				return ((IWorkingSet) element).getName();
		}
		
		@Override
		public Image getImage(Object element)
		{
			if (element instanceof AddNewWorkingSet)
				return CDebugUIPlugin.getImageDescriptorRegistry().get(
							DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_MONITOR_EXPRESSION));
			return CDebugUIPlugin.getImageDescriptor("icons/elcl16/expression_workingset.gif").createImage(); //$NON-NLS-1$
//			return CDebugUIPlugin.getImageDescriptorRegistry().get( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_VIEW_EXPRESSIONS ) );
		}

		@Override
		public Font getFont(Object element) 
		{
			if (element instanceof AddNewWorkingSet)
			{
				if (addNewWorkingsetFont == null)
				{
					// Display the "Add new expression" element in italic to 
		            // distinguish it from user elements in view.
		            FontData fontData = JFaceResources.getFontDescriptor(IDebugUIConstants.PREF_VARIABLE_TEXT_FONT).getFontData()[0];
		            fontData.setStyle(SWT.ITALIC);
		            addNewWorkingsetFont = new Font(Display.getCurrent(), fontData);
				}
				
				return addNewWorkingsetFont;
			}

			return null;
		}
		
		@Override
		public void dispose()
		{
			if (addNewWorkingsetFont != null)
				addNewWorkingsetFont.dispose();
			
			addNewWorkingsetFont = null;
		}
	}
	
	private class WorkingSetContentProvider implements IStructuredContentProvider
	{

		@Override
		public void dispose()
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object[] getElements(Object inputElement)
		{
			List<Object> allWorkingSets = new ArrayList<Object>();
			allWorkingSets.addAll(originalWorkingSets);
			allWorkingSets.add(addNewWorkingSetNode);
			
			return allWorkingSets.toArray(new Object[0]);
		}
		
	}
	
	private class WorkingSetNameModifier implements ICellModifier
	{

		@Override
		public boolean canModify(Object element, String property)
		{
			if (element instanceof AddNewWorkingSet)
				return true;
			if (element instanceof IWorkingSet)
				return true;
			return false;
		}

		@Override
		public Object getValue(Object element, String property)
		{
			if (element instanceof AddNewWorkingSet)
				return ""; //$NON-NLS-1$
			int workingsetIndex = 0;
			for (IWorkingSet workingSetObj : originalWorkingSets)
			{
				if (workingSetObj == element)
					break;
				
				workingsetIndex++;
			}
			if (workingsetIndex < originalWorkingSetNames.size())
				return originalWorkingSetNames.get(workingsetIndex);
			
			return ((IWorkingSet) element).getName();
		}

		@Override
		public void modify(Object element, String property, Object value)
		{
			String workingSetName = (String) value;
			workingSetName = workingSetName.trim();
			if (workingSetName.length() == 0)
				return;
			
			if (element instanceof TableItem)
				element = ((TableItem) element).getData();
			
			if (element instanceof AddNewWorkingSet)
			{
				WorkingsetNameValidator validator = new WorkingsetNameValidator();
				validator.setExistingName(null);
				IStatus nameStatus = validator.isValid(workingSetName);
				if (!nameStatus.isOK())
				{
					MessageDialog.openError(getShell(), 
											WorkingSetWizardMessages.WorkingSetNameError_Title, 
											nameStatus.getMessage());
					return;
				}

				IWorkingSet newWorkingSet= PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSet(workingSetName, new IAdaptable[0]);
				addedWorkingSets.add(newWorkingSet);
				
				workingSetExpressionsMap.put(newWorkingSet, new ArrayList<IWatchExpression>());
				
				workingsetViewer.insert(newWorkingSet, workingsetViewer.getTable().getItemCount() - 1);
				workingsetViewer.setSelection(new StructuredSelection(newWorkingSet));
			}			
			//do not update the expr obj immediately with the new value.
			//store the new value in the list of modified expr values 
			//at the same index at which the expr obj is present in the original exprs list.
			else
			{
				IWorkingSet workingsetToModify = (IWorkingSet) element;
				
				WorkingsetNameValidator validator = new WorkingsetNameValidator();
				int workingsetIndex = 0;
				for (IWorkingSet workingSetObj : originalWorkingSets)
				{
					if (workingSetObj == workingsetToModify)
						break;
					
					workingsetIndex++;
				}
				if (workingsetIndex < originalWorkingSetNames.size())
					validator.setExistingName(originalWorkingSetNames.get(workingsetIndex));
				else
					validator.setExistingName(workingsetToModify.getName());

				IStatus nameStatus = validator.isValid(workingSetName);
				if (!nameStatus.isOK())
				{
					MessageDialog.openError(getShell(), 
											WorkingSetWizardMessages.WorkingSetNameError_Title,
											nameStatus.getMessage());
					return;
				}

				if (workingsetToModify.getName().equals(workingSetName))
					return;
				
    			int originalIndex = 0;
    			for (IWorkingSet workingSetObj : originalWorkingSets)
    			{
    				if (workingSetObj == element)
    					break;
    				
    				originalIndex++;
    			}
    			
    			if (originalIndex < originalWorkingSetNames.size())
    			{
        			originalWorkingSetNames.remove(originalIndex);
        			originalWorkingSetNames.add(originalIndex, workingSetName);
    			}
    			else
    			{
    				List<IWatchExpression> expressions = workingSetExpressionsMap.remove(workingsetToModify);
    				
    				workingsetToModify.setName(workingSetName);
    				workingSetExpressionsMap.put(workingsetToModify, expressions);
    			}
    			
    			workingsetViewer.update(element, null);
			}
		}
		
	}

	private class DeleteWorkingSetListener implements KeyListener
	{

		@Override
		public void keyPressed(KeyEvent e)
		{
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			if (e.keyCode == SWT.DEL)
				deleteWorkingSet();
		}
	}

	private void deleteWorkingSet()
	{
		if (workingsetViewer.getSelection().isEmpty())
			return;

		if (((IStructuredSelection) workingsetViewer.getSelection()).size() > 1)
			return;
		
		Object selectedObj = ((IStructuredSelection) workingsetViewer.getSelection()).getFirstElement();
		if (selectedObj == addNewWorkingSetNode)
		{
			setMessage(WorkingSetWizardMessages.CannotDeleteAddWorkingSet, INFORMATION);
			return;
		}
		
		IWorkingSet workingsetToDelete = (IWorkingSet) selectedObj;
		boolean isAddedWorkingSet = false;
		int addedIndex = 0;
		for (IWorkingSet addedWorkingSet : addedWorkingSets)
		{
			if (addedWorkingSet == workingsetToDelete)
			{
				isAddedWorkingSet = true;
				break;
			}
			addedIndex++;
		}
		
		if (isAddedWorkingSet)
		{
			addedWorkingSets.remove(addedIndex);
			workingsetViewer.remove(workingsetToDelete);
			return;
		}
		
		boolean isOriginalWorkingSet = false;
		int originalIndex = 0;
		for (IWorkingSet originalWorkingSet : originalWorkingSets)
		{
			if (originalWorkingSet == workingsetToDelete)
			{
				isOriginalWorkingSet = true;
				break;
			}
			originalIndex++;
		}
		
		if (isOriginalWorkingSet)
		{
			originalWorkingSets.remove(originalIndex);
			originalWorkingSetNames.remove(originalIndex);
			
			deletedWorkingSets.add(workingsetToDelete);
		}
		
		workingsetViewer.remove(workingsetToDelete);
	}
	
//	private ExpressionView expressionView;

	private TableViewer workingsetViewer;
	private AddNewWorkingSet addNewWorkingSetNode = new AddNewWorkingSet();

	private CheckboxTableViewer expressionViewer;
	private AddNewExpression addNewExpressionNode = new AddNewExpression();

	private Button btnSelectAllExpressions;
	private Button btnDeselectAllExpressions;
	
	private List<IWorkingSet> originalWorkingSets = new ArrayList<IWorkingSet>();
	private List<String> originalWorkingSetNames = new ArrayList<String>();
	private List<IWorkingSet> addedWorkingSets = new ArrayList<IWorkingSet>();
	private List<IWorkingSet> deletedWorkingSets = new ArrayList<IWorkingSet>();
	
	private Map<IWorkingSet, List<IWatchExpression>> workingSetExpressionsMap = new HashMap<IWorkingSet, List<IWatchExpression>>();
	
	private List<IWatchExpression> originalExpressions = new ArrayList<IWatchExpression>();
	private List<String> originalExpressionValues = new ArrayList<String>();
	private List<IWatchExpression> addedExpressions = new ArrayList<IWatchExpression>();
	private List<IWatchExpression> deletedExpressions = new ArrayList<IWatchExpression>();

	public WorkingSetDefinitionPage()
	{
		super("WorkingSetDefinitionPage"); //$NON-NLS-1$
		setTitle(WorkingSetWizardMessages.WorkingSetDefinitionPage_Title);
		
//		this.expressionView = expressionView;
	}

	@Override
	public void createControl(Composite parent)
	{
		//create a sashform split vertically to create a LHS and RHS
		SashForm verticalSash = new SashForm(parent, SWT.HORIZONTAL);
		
		//create a checkbox list on LHS for working sets
		Composite cmpWorkingSets = new Composite(verticalSash, SWT.NONE);
		GridLayout gl = new GridLayout(3, false);
		cmpWorkingSets.setLayout(gl);
		
		Label lblWorkingSets = new Label(cmpWorkingSets, SWT.NONE);
		lblWorkingSets.setText(WorkingSetWizardMessages.WorkingSetsComposite);
		GridData gd = new GridData();
		gd.horizontalSpan = 3;
		lblWorkingSets.setLayoutData(gd);
		
		workingsetViewer = new TableViewer(cmpWorkingSets, SWT.BORDER | SWT.MULTI);
				//CheckboxTableViewer.newCheckList(cmpWorkingSets, SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 3;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.heightHint = 300;
		workingsetViewer.getTable().setLayoutData(gd);
		
		workingsetViewer.setLabelProvider(new WorkingSetLabelProvider());
		workingsetViewer.setContentProvider(new WorkingSetContentProvider());		
		workingsetViewer.addSelectionChangedListener(new WorkingSetSelectionListener());
		workingsetViewer.getTable().addKeyListener(new DeleteWorkingSetListener());
		
		workingsetViewer.setColumnProperties(new String[] {"workingsetname"}); //$NON-NLS-1$
		CellEditor[] workingSetEditors = new CellEditor[1];
		workingSetEditors[0] = new TextCellEditor(workingsetViewer.getTable(), SWT.BORDER);
		workingsetViewer.setCellEditors(workingSetEditors);
		workingsetViewer.setCellModifier(new WorkingSetNameModifier());
		
		//create a checkbox list on RHS for all expressions in workspace
		Composite cmpExpressions = new Composite(verticalSash, SWT.NONE);
		gl = new GridLayout(3, false);
		cmpExpressions.setLayout(gl);
		
		Label lblExpressions = new Label(cmpExpressions, SWT.NONE);
		lblExpressions.setText(WorkingSetWizardMessages.ExpressionsComposite);
		gd = new GridData();
		gd.horizontalSpan = 3;
		lblExpressions.setLayoutData(gd);
		
		expressionViewer = CheckboxTableViewer.newCheckList(cmpExpressions, SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 3;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.heightHint = 300;
		expressionViewer.getTable().setLayoutData(gd);
		
		expressionViewer.setContentProvider(new ExpressionContentProvider());
		expressionViewer.setLabelProvider(new ExpressionLabelProvider());
		
		expressionViewer.addCheckStateListener(new ExpressionCheckedListener());
		expressionViewer.getTable().addKeyListener(new DeleteExpressionListener());
		
		expressionViewer.setColumnProperties(new String[] {"expression"}); //$NON-NLS-1$
		CellEditor[] expressionEditors = new CellEditor[1];
		expressionEditors[0] = new TextCellEditor(expressionViewer.getTable(), SWT.BORDER);
		expressionViewer.setCellEditors(expressionEditors);
		
		expressionViewer.setCellModifier(new ExpressionModifier());
		
		btnSelectAllExpressions = new Button(cmpExpressions, SWT.PUSH);
		btnSelectAllExpressions.setText(WorkingSetWizardMessages.SelectAllExpressions);
		btnSelectAllExpressions.setEnabled(false);
		btnSelectAllExpressions.addSelectionListener(new SelectAllExpressionsListener());
		
		btnDeselectAllExpressions = new Button(cmpExpressions, SWT.PUSH);
		btnDeselectAllExpressions.setText(WorkingSetWizardMessages.DeselectAllExpressions);
		btnDeselectAllExpressions.setEnabled(false);
		btnDeselectAllExpressions.addSelectionListener(new DeselectAllExpressionsListener());
		
		verticalSash.setWeights(new int[]{50, 50});
		
		setControl(verticalSash);
		
		populateData();
	}

	private void populateData()
	{
		IExpression[] allExprObjs = DebugPlugin.getDefault().getExpressionManager().getExpressions();
		for (IExpression exprObj : allExprObjs)
		{
			originalExpressions.add((IWatchExpression) exprObj);
			originalExpressionValues.add(exprObj.getExpressionText());
		}
		
		//get a list of all expression working sets in the workspace
		IWorkingSet[] workingsets = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
		for (IWorkingSet workingset : workingsets)
		{	
			if (workingset.getId() == null)
				continue;
			if (! workingset.getId().equals(IExpressionWorkingSetConstants.ID))
				continue;
			
			List<IWatchExpression> workingsetExpressions = new ArrayList<IWatchExpression>();			
			ExpressionWorkingSetElementAdapter expressionWorkingSetAdapter = new ExpressionWorkingSetElementAdapter();
			IAdaptable[] workingSetAdapters = expressionWorkingSetAdapter.adaptElements(workingset, workingset.getElements());
			
			for (IAdaptable workingSetAdapter : workingSetAdapters)
			{
				workingsetExpressions.add((IWatchExpression) workingSetAdapter);
			}

			if (!originalWorkingSets.contains(workingset))
			{
				originalWorkingSets.add(workingset);
				originalWorkingSetNames.add(workingset.getName());
			}
			
			workingSetExpressionsMap.put(workingset, workingsetExpressions);
		}
		
		workingsetViewer.setInput(originalWorkingSets);
		
		expressionViewer.setInput(originalExpressions);
	}
	
	private void checkExpressions(IWorkingSet workingset)
	{
		expressionViewer.setCheckedElements(new Object[0]);
		
		if (workingset == null)
			return;
		
		List<IWatchExpression> workingSetExpressions = workingSetExpressionsMap.get(workingset);
		
		//get a list of expressions corresponding to the expression values included in the working set
		expressionViewer.setCheckedElements(workingSetExpressions.toArray(new IWatchExpression[workingSetExpressions.size()]));
	}
	
	public void performFinish(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	{
		if (monitor.isCanceled())
			return;
		
		int totalwork = originalExpressions.size() + 
						deletedExpressions.size() + 
						addedExpressions.size() + 
						originalWorkingSets.size() +
						deletedWorkingSets.size() +
						addedWorkingSets.size();
		monitor.beginTask(WorkingSetWizardMessages.WorkingSetDefinitionPage_FinishTask, totalwork);
		
		try
		{
    		Map<String, IWatchExpression> newExpressions = new HashMap<String, IWatchExpression>();
    		
    		//update edited expressions
    		for (int i=0, size=originalExpressions.size(); i<size; i++)
    		{
    			IWatchExpression originalExpression = originalExpressions.get(i);
    			String editedExpression = originalExpressionValues.get(i);
    			
    			if (!originalExpression.getExpressionText().equals(editedExpression))
    				originalExpression.setExpressionText(editedExpression);
    			
    			newExpressions.put(originalExpression.getExpressionText(), originalExpression);
    			
    			monitor.worked(1);
    		}
    		
    		//delete expressions
    		for (IWatchExpression deletedExpression : deletedExpressions)
    		{
    			DebugPlugin.getDefault().getExpressionManager().removeExpression(deletedExpression);
    			monitor.worked(1);
    		}
    		
    		//add expressions
    		for (IWatchExpression addedExpression : addedExpressions)
    		{
    			DebugPlugin.getDefault().getExpressionManager().addExpression(addedExpression);
    			newExpressions.put(addedExpression.getExpressionText(), addedExpression);
    			monitor.worked(1);
    		}
    		
    		//rename original working sets
    		for (int i=0, size=originalWorkingSets.size(); i<size; i++)
    		{
    			IWorkingSet originalWorkingSet = originalWorkingSets.get(i);
    			String editedWorkingSetName = originalWorkingSetNames.get(i);
    			
    			if (! originalWorkingSet.getName().equals(editedWorkingSetName))
    				originalWorkingSet.setName(editedWorkingSetName);
    			
    			List<IWatchExpression> workingSetExpressions = workingSetExpressionsMap.get(originalWorkingSet);
    			originalWorkingSet.setElements(workingSetExpressions.toArray(new IAdaptable[0]));
    			
    			monitor.worked(1);
    		}
    		
    		//delete working sets
    		for (IWorkingSet deletedWorkingSet : deletedWorkingSets)
    		{
    			PlatformUI.getWorkbench().getWorkingSetManager().removeWorkingSet(deletedWorkingSet);
    			
    			monitor.worked(1);
    		}
    		
    		//add working sets
    		for (IWorkingSet addedWorkingSet : addedWorkingSets)
    		{
    			List<IWatchExpression> workingSetExpressions = workingSetExpressionsMap.get(addedWorkingSet);
    			IWorkingSet workingSetToAdd = PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSet(addedWorkingSet.getName(), workingSetExpressions.toArray(new IAdaptable[0]));
    			workingSetToAdd.setId(IExpressionWorkingSetConstants.ID);
    			PlatformUI.getWorkbench().getWorkingSetManager().addWorkingSet(workingSetToAdd);
    			
    			monitor.worked(1);
    		}
		}
		finally
		{
			monitor.done();
		}
	}
	
	List<String> getWorkingSetNames()
	{
		List<String> workingSetNames = new ArrayList<String>();
		workingSetNames.addAll(originalWorkingSetNames);
		for (IWorkingSet addedWorkingSet : addedWorkingSets)
		{
			workingSetNames.add(addedWorkingSet.getName());
		}
		
		return workingSetNames;
	}
}
