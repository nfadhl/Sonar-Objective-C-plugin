package org.sonar.plugins.objectivec;

import java.io.File;
 
public class FileSearch {
  private String filepath;
 
  public String searchDirectory(File directory, String fileExtension) {	 
	if (directory.isDirectory())
	{
	   search(directory);
	} 
    return filepath;
  }
  

private void search(File file) {
	if(file.getName().endsWith(".xcodeproj"))
        filepath = file.getParent();        

     else if (file.isDirectory()) {
	    if (file.canRead()) 
	     {
		 for (File temp : file.listFiles())
		    if (temp.isDirectory()) {
			     search(temp);
		    } 		  
	    } 
	 } 
	} 
  }
 
