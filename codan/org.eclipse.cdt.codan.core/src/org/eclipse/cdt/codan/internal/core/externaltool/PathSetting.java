/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.externaltool;

import static org.eclipse.cdt.codan.core.param.IProblemPreferenceDescriptor.PreferenceType.TYPE_FILE;
import static org.eclipse.cdt.codan.internal.core.externaltool.Messages.ConfigurationSettings_path_format;

import org.eclipse.cdt.codan.core.externaltool.SingleConfigurationSetting;
import org.eclipse.cdt.codan.core.param.BasicProblemPreference;
import org.eclipse.cdt.codan.core.param.IProblemPreferenceDescriptor;

import java.io.File;

/**
 * User-configurable setting that specifies the path and name of an external tool's executable.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public class PathSetting extends SingleConfigurationSetting<File> {
	static final String KEY = "externalToolPath"; //$NON-NLS-1$

	/**
	 * Constructor.
	 * @param externalToolName the name of the external tool, to be displayed to the user.
	 * @param defaultValue the default value of the setting.
	 */
	public PathSetting(String externalToolName, File defaultValue) {
		super(newPreferenceDescriptor(externalToolName), defaultValue, File.class);
	}

	private static IProblemPreferenceDescriptor newPreferenceDescriptor(String externalToolName) {
		String label = String.format(ConfigurationSettings_path_format, externalToolName);
		return new BasicProblemPreference(KEY, label, TYPE_FILE);
	}
}
