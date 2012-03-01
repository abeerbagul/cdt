//
// Copyright (c) 2011 by Tensilica Inc.  ALL RIGHTS RESERVED.
// These coded instructions, statements, and computer programs are the
// copyrighted works and confidential proprietary information of Tensilica Inc.
// They may not be modified, copied, reproduced, distributed, or disclosed to
// third parties in any manner, medium, or form, in whole or in part, without
// the prior written consent of Tensilica Inc.
//

package org.eclipse.cdt.debug.ui.expressions;

import org.eclipse.debug.core.model.IExpression;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

public class ExpressionPersistableElementAdapter implements IPersistableElement
{
	public static final String TAG_EXPRESSION_TEXT = "TAG_EXPRESSION_TEXT"; //$NON-NLS-1$
	
	private IExpression expression;
	
	public ExpressionPersistableElementAdapter(IExpression expression)
	{
		this.expression = expression;
	}

	@Override
	public void saveState(IMemento memento)
	{
		memento.putString(TAG_EXPRESSION_TEXT, expression.getExpressionText());
	}

	@Override
	public String getFactoryId()
	{
		return ExpressionFactory.ID;
	}

}
