package xtracker;
/*
 * @(#)GenericFileFilter.java	04/08/08
 * XViewer Project
 * Copyright Rothamsted Research. 
 * All rights reserved.
 */

import java.io.File;
/**
 * This class is a method of having a generic file filter to be used with any file
 * extension that you require. At construction time this information is supplied
 * to the constructor<br>
 * @author Jun Fan
 * @version 1.0
 */
public class GenericFileFilter extends javax.swing.filechooser.FileFilter{
	private String[] fileExts;
	private String description;

	public GenericFileFilter(String[] filesExtsIn, String description){
		this.fileExts=filesExtsIn;
		this.description=description;
	}
	/**
     * Whether the given file is accepted by this filter.
     */
	@Override
	public boolean accept(File f) {
		if(f != null) {
			//By accepting all directories, this filter allows the user to navigate around the file system
			//otherwise limited to the directory with which the chooser is initialized
			if(f.isDirectory()) {
				return true;
			}
			String extension = Utils.getExtension(f);
			if(extension != null){
				for(String ext:fileExts){
					if(extension.equalsIgnoreCase(ext))	return true;
				}
			};
		}
	    return false;
	}
    /**
     * The description of this filter. For example: "JPG and GIF Images"
     */
	@Override
	public String getDescription() {
		return description;
	}
	
	public String[] getFileExts(){
		return fileExts;
	}
}

