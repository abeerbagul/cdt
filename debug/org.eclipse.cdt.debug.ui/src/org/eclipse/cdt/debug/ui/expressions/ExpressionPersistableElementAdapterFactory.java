//
// Copyright (c) 2011 by Tensilica Inc.  ALL RIGHTS RESERVED.
// These coded instructions, statements, and computer programs are the
// copyrighted works and confidential proprietary information of Tensilica Inc.
// They may not be modified, copied, reproduced, distributed, or disclosed to
// third parties in any manner, medium, or form, in whole or in part, without
// the prior written consent of Tensilica Inc.
//

package org.eclipse.cdt.debug.ui.expressions;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.ui.IPersistableElement;

public class ExpressionPersistableElementAdapterFactory implements IAdapterFactory
{

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType)
	{
		if (adaptableObject instanceof IExpression)
		{
			if (adapterType.equals(IPersistableElement.class))
			{
				return new ExpressionPersistableElementAdapter((IExpression) adaptableObject);
			}
		}
		
		return null;
	}

	@Override
	public Class[] getAdapterList()
	{
		return new Class[] {IPersistableElement.class};
	}

}
