//
// Copyright (c) 2011 by Tensilica Inc.  ALL RIGHTS RESERVED.
// These coded instructions, statements, and computer programs are the
// copyrighted works and confidential proprietary information of Tensilica Inc.
// They may not be modified, copied, reproduced, distributed, or disclosed to
// third parties in any manner, medium, or form, in whole or in part, without
// the prior written consent of Tensilica Inc.
//

package org.eclipse.cdt.debug.ui.expressions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

public class ExpressionFactory implements IElementFactory
{
	public static final String ID = "org.eclipse.cdt.debug.ui.expressions.ExpressionFactory"; //$NON-NLS-1$

	@Override
	public IAdaptable createElement(IMemento memento)
	{
		String expressionText = memento.getString(ExpressionPersistableElementAdapter.TAG_EXPRESSION_TEXT);
		
		IExpression[] globalExpressions = DebugPlugin.getDefault().getExpressionManager().getExpressions();
		for (IExpression expr : globalExpressions)
		{
			if (expr.getExpressionText().equals(expressionText))
				return expr;
		}
		
		return null;
	}

}
