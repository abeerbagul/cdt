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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.expressions.IExpressionWorkingSetConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.expression.ExpressionView;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

public class ExpressionViewWorkingSetUtils
{
	private static final String preferenceKey = "prefExpressionViewWorkingSets"; //$NON-NLS-1$
	private static final String mementoID = "ExpressionViewWorkingSetMemento"; //$NON-NLS-1$
	private static final String viewInstanceMementoID = "viewInstance"; //$NON-NLS-1$
	private static final String workingSetNameKey = "workingsetName"; //$NON-NLS-1$
	private static final String workingsetNameSeparator = ";"; //$NON-NLS-1$

	public XMLMemento readMemento()
	{
		IPreferenceStore store = CDebugUIPlugin.getDefault().getPreferenceStore();
		String mementoString = store.getString(preferenceKey);
		if(mementoString.length() > 0)
		{
        	ByteArrayInputStream bin = new ByteArrayInputStream(mementoString.getBytes());
        	InputStreamReader reader = new InputStreamReader(bin);
        	try
        	{
        		XMLMemento stateMemento = XMLMemento.createReadRoot(reader);
        		
        		return stateMemento;
        	}
    		catch (WorkbenchException e)
    		{
        	}
    		finally 
    		{
        		try
        		{
        			reader.close();
        			bin.close();
        		}
        		catch (IOException e){}
        	}
		}
		
		return null;
	}
	
	public void restoreMemento(ExpressionView exprView)
	{
		IMemento rootMemento = readMemento();
		if (rootMemento == null)
			return;
		
		IMemento viewInstanceMemento = null;
		IMemento[] viewInstanceMementos = rootMemento.getChildren(viewInstanceMementoID);
		for (IMemento memento : viewInstanceMementos)
		{
			if (memento.getID().equals(getCombinedPartId(exprView)))
			{
				viewInstanceMemento = memento;
				break;
			}
		}
		if (viewInstanceMemento != null)
		{
			String allNames = viewInstanceMemento.getString(workingSetNameKey);
			StringTokenizer tokenizer = new StringTokenizer(allNames, workingsetNameSeparator);
			List<String> workingSetNames = new ArrayList<String>();
			
			IWorkingSetManager workingsetManager = PlatformUI.getWorkbench().getWorkingSetManager();
			while (tokenizer.hasMoreTokens())
			{
				String workingSetName = tokenizer.nextToken();
				//check if a working set by this name exists in the workspace
				if (workingsetManager.getWorkingSet(workingSetName) != null)				
					workingSetNames.add(workingSetName);
			}
			updateView(exprView, workingSetNames.toArray(new String[0]));
		}
	}
	
	public void updateView(ExpressionView exprView, String[] workingsetNames)
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
		if (expressionFilter == null  && workingsetNames.length > 0)
		{
			expressionFilter = new ExpressionWorkingSetViewerFilter(exprView);
			expressionFilter.setWorkingSetNames(workingsetNames);
			
			ViewerFilter[] newFilters = new ViewerFilter[allFilters.length + 1];
			System.arraycopy(allFilters, 0, newFilters, 0, allFilters.length);
			newFilters[allFilters.length] = expressionFilter;
			treeViewer.setFilters(newFilters);
			
			treeViewer.getPresentationContext().setProperty(IExpressionWorkingSetConstants.PROP_EXPRESSION_WORKINGSETS, workingsetNames);
			
//			exprView.getViewer().refresh();
		}
		else if (expressionFilter != null && workingsetNames.length > 0)
		{
//			String[] oldWorkingSetNames = expressionFilter.getWorkingSetNames();
			
			expressionFilter.setWorkingSetNames(workingsetNames);
			
			treeViewer.getPresentationContext().setProperty(IExpressionWorkingSetConstants.PROP_EXPRESSION_WORKINGSETS, workingsetNames);
			
//			if (oldWorkingSetNames == null || oldWorkingSetNames.length == 0)
//				exprView.getViewer().refresh();
		}
		else if (expressionFilter != null)
		{
			treeViewer.removeFilter(expressionFilter);
			
			treeViewer.getPresentationContext().setProperty(IExpressionWorkingSetConstants.PROP_EXPRESSION_WORKINGSETS, null);
			
//			exprView.getViewer().refresh();
		}
	}
	
	public static IWorkingSet[] getExpressionWorkingsets()
	{
		IWorkingSet[] allWorkingSets = PlatformUI.getWorkbench().getWorkingSetManager().getAllWorkingSets();
		List<IWorkingSet> allExpressionWorkingSets = new ArrayList<IWorkingSet>();
		for (IWorkingSet workingset : allWorkingSets)
		{
			if (IExpressionWorkingSetConstants.ID.equals(workingset.getId()))
			{
				allExpressionWorkingSets.add(workingset);
			}
		}
		
		return allExpressionWorkingSets.toArray(new IWorkingSet[0]);
	}


	public void removeWorkingSet(ExpressionView exprView)
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(bout);

		try
		{
			XMLMemento savedMemento = readMemento();
			if (savedMemento == null)
				return;
			
			IMemento viewInstanceMemento = null;
			IMemento[] viewInstanceMementos = savedMemento.getChildren(viewInstanceMementoID);
			for (IMemento memento : viewInstanceMementos)
			{
				if (memento.getID().equals(getCombinedPartId(exprView)))
				{
					viewInstanceMemento = memento;
					break;
				}
			}
			if (viewInstanceMemento == null)
				return;
			
			XMLMemento rootMemento = XMLMemento.createWriteRoot(mementoID);
			for (IMemento savedChild : savedMemento.getChildren(viewInstanceMementoID))
			{
				if (savedChild.getID().equals(getCombinedPartId(exprView)))
					continue;
				
				rootMemento.copyChild(savedChild);
			}
			
			rootMemento.save(writer);
			
			IPreferenceStore store = CDebugUIPlugin.getDefault().getPreferenceStore();
			String xmlString = bout.toString();
			store.putValue(preferenceKey, xmlString);
		}
		catch (IOException e)
		{
		}
		finally
		{
    		try
    		{
    			writer.close();
    			bout.close();
    		}
    		catch (IOException e)
    		{
    		}
		}
	}

	public void saveWorkingSet(ExpressionView exprView, String[] workingSetNames)
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(bout);

		try
		{
			XMLMemento savedMemento = readMemento();
			
			XMLMemento rootMemento = XMLMemento.createWriteRoot(mementoID);
			if (savedMemento != null)
			{
				for (IMemento savedChild : savedMemento.getChildren(viewInstanceMementoID))
					rootMemento.copyChild(savedChild);
			}
			IMemento viewInstanceMemento = null;
			IMemento[] viewInstanceMementos = rootMemento.getChildren(viewInstanceMementoID);
			for (IMemento memento : viewInstanceMementos)
			{
				if (memento.getID().equals(getCombinedPartId(exprView)))
				{
					viewInstanceMemento = memento;
					break;
				}
			}
			if (viewInstanceMemento == null)
			{
				viewInstanceMemento = rootMemento.createChild(viewInstanceMementoID, getCombinedPartId(exprView));
			}
			
			StringBuilder allNames = new StringBuilder();
			for (int i=0; i<workingSetNames.length; i++)
			{
				allNames.append(workingSetNames[i]);
				allNames.append(workingsetNameSeparator);
			}
			
			viewInstanceMemento.putString(workingSetNameKey, allNames.toString());
			
			rootMemento.save(writer);
			
			IPreferenceStore store = CDebugUIPlugin.getDefault().getPreferenceStore();
			String xmlString = bout.toString();
			store.putValue(preferenceKey, xmlString);
		}
		catch (IOException e)
		{
		}
		finally
		{
    		try
    		{
    			writer.close();
    			bout.close();
    		}
    		catch (IOException e)
    		{
    		}
		}
	
	}
	
	private static String getCombinedPartId(IWorkbenchPart part)
	{
		if (part.getSite() instanceof IViewSite) { 
            IViewSite site = (IViewSite)part.getSite();
            String partId = site.getId();
            String secondaryId = site.getSecondaryId();
            
            return partId + (secondaryId != null ? ":" + secondaryId : ""); //$NON-NLS-1$ //$NON-NLS-2$
            
        } else { 
            return part.getSite().getId(); 
        } 
	}
}
