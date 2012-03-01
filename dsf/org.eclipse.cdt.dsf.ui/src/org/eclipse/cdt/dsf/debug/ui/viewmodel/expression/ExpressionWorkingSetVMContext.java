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

import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMContext;

public class ExpressionWorkingSetVMContext extends AbstractVMContext
{
	private final String workingSetName;
	
	public ExpressionWorkingSetVMContext(ExpressionWorkingSetVMNode vmNode, String workingSetName)
	{
		super(vmNode);
		this.workingSetName = workingSetName;
	}

	@Override
	public boolean equals(Object obj)
	{	
		return (obj instanceof ExpressionWorkingSetVMContext) &&
				getVMNode().equals(((ExpressionWorkingSetVMContext) obj).getVMNode()) &&
				workingSetName.equals(((ExpressionWorkingSetVMContext) obj).getWorkingSetName());
	}

	@Override
	public int hashCode()
	{
		return getVMNode().hashCode() + workingSetName.hashCode();
	}

	public String getWorkingSetName()
	{
		return workingSetName;
	}
}
