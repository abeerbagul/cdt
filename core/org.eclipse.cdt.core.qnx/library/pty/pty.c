/*******************************************************************************
 * Copyright (c) 2002 - 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
#include "PTY.h"
#include "openpty.h"

/*
 * Class:     org_eclipse_cdt_utils_pty_PTY
 * Method:    forkpty
 * Signature: ()I
 */
JNIEXPORT jstring JNICALL
Java_org_eclipse_cdt_utils_pty_PTY_openMaster (JNIEnv *env, jobject jobj) {
	jfieldID fid; /* Store the field ID */
	jstring jstr = NULL;
	int master = -1;
	char line[1024];	/* FIXME: Should be enough */
	jclass cls;

	line[0] = '\0';

	master = ptym_open(line);
	if (master >= 0) {
		/* Get a reference to the obj's class */
		cls = (*env)->GetObjectClass(env, jobj);

		/* Set the master fd.  */
		fid = (*env)->GetFieldID(env, cls, "master", "I");
		if (fid == NULL) {
			return NULL;
		}
		(*env)->SetIntField(env, jobj, fid, (jint)master);

		/* Create a new String for the slave.  */
		jstr = (*env)->NewStringUTF(env, line);
	}
	return jstr;
}
