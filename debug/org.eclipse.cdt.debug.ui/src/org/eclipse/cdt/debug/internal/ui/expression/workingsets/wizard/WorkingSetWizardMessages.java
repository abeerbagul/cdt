//
// Copyright (c) 2011 by Tensilica Inc.  ALL RIGHTS RESERVED.
// These coded instructions, statements, and computer programs are the
// copyrighted works and confidential proprietary information of Tensilica Inc.
// They may not be modified, copied, reproduced, distributed, or disclosed to
// third parties in any manner, medium, or form, in whole or in part, without
// the prior written consent of Tensilica Inc.
//

package org.eclipse.cdt.debug.internal.ui.expression.workingsets.wizard;

import org.eclipse.osgi.util.NLS;

public class WorkingSetWizardMessages extends NLS
{
	static 
	{
		initializeMessages("org.eclipse.cdt.debug.internal.ui.expression.workingsets.wizard.WorkingSetWizardMessages", WorkingSetWizardMessages.class); //$NON-NLS-1$
	}
	
	public static String WorkingSetNameError_Empty;
	public static String WorkingSetNameError_Pattern;
	public static String WorkingSetNameError_Exists;
	public static String WorkingSetNameError_Title;
	
	public static String CannotCheckAddExpression;
	public static String CannotCheckAddWorkingSet;
	public static String CannotAddExpressionToAddNewWorkingSet;
	
	public static String AddNewExpression;
	public static String AddNewWorkingSet;

	public static String CannotDeleteAddExpression;
	public static String CannotDeleteAddWorkingSet;
	
	public static String WorkingSetDefinitionPage_Title;
	public static String WorkingSetDefinitionPage_FinishTask;
	
	public static String WorkingSetSelectionPage_Title;

	public static String WorkingSetsComposite;
	public static String ExpressionsComposite;
	
	public static String SelectAllWorkingSets;
	public static String DeselectAllWorkingSets;
	public static String SelectAllExpressions;
	public static String DeselectAllExpressions;
	
	public static String Debugtarget_Name;
	
	public static String PinCoreMessage;
	public static String PinCoreError;
	
	public static String PinCorePage_Title;
	public static String PinCorePage_FinishTask;
	
	public static String Wizard_FinishTask;
}
