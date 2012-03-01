package org.eclipse.cdt.debug.internal.ui.expression.workingsets;

import org.eclipse.osgi.util.NLS;

public class ExpressionWorkingSetPageMessages extends NLS {

	static {
		initializeMessages("org.eclipse.cdt.debug.internal.ui.expression.workingsets.ExpressionWorkingSetPageMessages", ExpressionWorkingSetPageMessages.class); //$NON-NLS-1$
	}
	
	public static String PageTitle;
	public static String PageDescription;
	
	public static String LabelWorkingSetName;
	public static String LabelExpressions;
	public static String SelectAllExpressions;
	public static String SelectAllExpressionsTooltip;
	public static String DeselectAllExpressions;
	public static String DeselectAllExpressionsTooltip;
	
	public static String NameColumn;
	
	public static String ErrorWorkingSetWhitespace;
	public static String ErrorNameEmpty;
	public static String ErrorNameExists;
}
