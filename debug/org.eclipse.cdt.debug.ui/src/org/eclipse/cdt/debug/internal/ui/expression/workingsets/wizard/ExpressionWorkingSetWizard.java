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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;

public class ExpressionWorkingSetWizard extends Wizard
{
	private WorkingSetDefinitionPage definitionPage;
	
	public ExpressionWorkingSetWizard()
	{
		setWindowTitle("Expression Working Sets");

		definitionPage = new WorkingSetDefinitionPage();
	}
	
	public void addPages()
	{
		addPage(definitionPage);
	}

	@Override
	public boolean performFinish()
	{
		try
		{
			getContainer().run(true, false, new PerformFinishRunnable());
		}
		catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block left by Abeerb
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block left by Abeerb
		}
		
		return true;
	}
	
	private class PerformFinishRunnable implements IRunnableWithProgress
	{
		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException
		{
			monitor.beginTask(WorkingSetWizardMessages.Wizard_FinishTask, 1);
			
			definitionPage.performFinish(new SubProgressMonitor(monitor, 1));
			
			monitor.done();
		}
	}

	@Override
	public boolean needsProgressMonitor()
	{
		return true;
	}
}
