//
// Copyright (c) 2011 by Tensilica Inc.  ALL RIGHTS RESERVED.
// These coded instructions, statements, and computer programs are the
// copyrighted works and confidential proprietary information of Tensilica Inc.
// They may not be modified, copied, reproduced, distributed, or disclosed to
// third parties in any manner, medium, or form, in whole or in part, without
// the prior written consent of Tensilica Inc.
//

package org.eclipse.cdt.debug.internal.ui.expression.actions;

import org.eclipse.debug.internal.ui.views.expression.ExpressionView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public abstract class ExpressionWorkingSetAction implements IViewActionDelegate
{
	protected ExpressionView expressionView;

	@Override
	public void selectionChanged(IAction action, ISelection selection)
	{

	}

	@Override
	public void init(IViewPart view)
	{
		this.expressionView = (ExpressionView) view;
	}
}
