//
// Copyright (c) 2011 by Tensilica Inc.  ALL RIGHTS RESERVED.
// These coded instructions, statements, and computer programs are the
// copyrighted works and confidential proprietary information of Tensilica Inc.
// They may not be modified, copied, reproduced, distributed, or disclosed to
// third parties in any manner, medium, or form, in whole or in part, without
// the prior written consent of Tensilica Inc.
//

package org.eclipse.cdt.debug.internal.ui.expression.actions;

import org.eclipse.cdt.debug.internal.ui.expression.workingsets.wizard.ExpressionWorkingSetWizard;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

public class DefineWorkingSetAction extends ExpressionWorkingSetAction
{
	@Override
	public void run(IAction action)
	{
		ExpressionWorkingSetWizard wiz = new ExpressionWorkingSetWizard();
		
		WizardDialog wizDlg = new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), wiz);
		wizDlg.open();
	}
}
