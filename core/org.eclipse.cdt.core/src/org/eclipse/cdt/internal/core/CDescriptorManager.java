/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.core;

import java.util.HashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class CDescriptorManager implements IResourceChangeListener {
	private static HashMap fDescriptorMap;

	private IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public void shutdown() {
		getWorkspace().removeResourceChangeListener(this);
	}

	public void startup() {
		getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * Traverses the delta looking for added/removed/changed launch
	 * configuration files.
	 * 
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		if ( fDescriptorMap == null ) {
			return;
		}
		IResource resource = event.getResource();
				
		int eventType = event.getType();
		
		switch(eventType) {
			case IResourceChangeEvent.PRE_DELETE:
				if (resource.getType() == IResource.PROJECT){
					fDescriptorMap.remove(resource);
				}			
			break;
			case IResourceChangeEvent.POST_CHANGE:
				IResourceDelta delta= event.getDelta();				
				if (delta == null) {
					break;
				}
				IResource dResource = delta.getResource();
				//if (0 != (delta.getFlags() & IResourceDelta.OPEN)) {
					if (dResource instanceof IFile) {
						IFile file = (IFile)dResource;

						// the .cdtproject file has been deleted
						if ((file != null) && (file.getName().endsWith(CDescriptor.DESCRIPTION_FILE_NAME)) && !file.exists()){
							// must remove the corresponding reference to it in the fDescriptorMap
							if (fDescriptorMap != null){
								fDescriptorMap.remove(resource);
							}
						}
						
					}
				//}
			break;		
		}				
	}
	

	public synchronized ICDescriptor getDescriptor(IProject project) throws CoreException {
		if ( fDescriptorMap == null ) {
			fDescriptorMap = new HashMap();
		}
		CDescriptor cproject;
		cproject = (CDescriptor)fDescriptorMap.get(project) ;
		if ( cproject == null ) {
			cproject = new CDescriptor(project);
			fDescriptorMap.put(project, cproject);
		}
		return cproject;
	}

	public synchronized void configure(IProject project, String id) throws CoreException {
		CDescriptor cproject;
		if ( fDescriptorMap == null ) {
			fDescriptorMap = new HashMap();
		}
		if ( fDescriptorMap.get(project) != null ) {
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, CCorePlugin.STATUS_CDTPROJECT_EXISTS, "CDTProject already configured", (Throwable)null);
			throw new CoreException(status);
		}
		try {
			cproject = new CDescriptor(project, id);
		}
		catch (CoreException e) { // if .cdtproject already exists will use that
			IStatus status = e.getStatus();
			if ( status.getCode() == CCorePlugin.STATUS_CDTPROJECT_EXISTS ) {
				cproject = new CDescriptor(project);
				fDescriptorMap.put(project, cproject);
				return;
			}
			else
				throw e;
		}
		COwner cowner = new COwner(id);
		cowner.configure(project, cproject);
		cproject.saveInfo();
		cproject.setAutoSave(true);
		fDescriptorMap.put(project, cproject);
	}
	
    public synchronized void convert(IProject project, String id) throws CoreException {
		CDescriptor cproject;
		if ( fDescriptorMap == null ) {
			fDescriptorMap = new HashMap();
		}
		COwner cowner = new COwner(id);
		cproject = new CDescriptor(project, cowner);
		cowner.configure(project, cproject);
		cproject.saveInfo();
		cproject.setAutoSave(true);
		fDescriptorMap.put(project, cproject);
	}
	
	/**
     * Must remove an existing .cdtproject file before we generate a new one when converting
     */
//    public static void removeExistingCdtProjectFile(IProject project){
//    	IFile file = project.getFile(CDescriptor.DESCRIPTION_FILE_NAME);
//    	IProgressMonitor monitor = new  NullProgressMonitor();
//    		
//		// update the resource content
//		if ((file != null) && file.exists()) {
//			try{
//				file.delete(true, monitor);
//				// remove reference from the fDescriptorMap
//				if (fDescriptorMap != null){
//					fDescriptorMap.remove(project);
//				}	
//
//				project.refreshLocal(1, monitor);		
//			}catch(CoreException ce){
//				CCorePlugin.log(ce);
//			}
//		}    	
//    }
}
