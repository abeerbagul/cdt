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
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetElementAdapter;

public class ExpressionWorkingSetElementAdapter implements IWorkingSetElementAdapter
{

	@Override
	public IAdaptable[] adaptElements(IWorkingSet ws, IAdaptable[] elements)
	{
		for (int i = 0; i < elements.length; i++) {
	        IExpression expression = (IExpression)DebugPlugin.getAdapter(elements[i], IExpression.class);			
			if (expression != null) {
				return selectExpression(elements);
			}
		}
		return new IAdaptable[] {};
	}
	
	private IAdaptable[] selectExpression(IAdaptable[] elements)
	{
		List<IAdaptable> expressions = new ArrayList<IAdaptable>(elements.length);
		for (int i = 0; i < elements.length; i++) {
			IExpression expr = (IExpression)DebugPlugin.getAdapter(elements[i], IExpression.class);            
			if (expr != null) {
				expressions.add(expr);
			}
		}
		return expressions.toArray(new IAdaptable[expressions.size()]);
	}

	@Override
	public void dispose()
	{
		//noop
	}

}
