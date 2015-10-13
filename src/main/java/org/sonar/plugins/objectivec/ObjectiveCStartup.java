
package org.sonar.plugins.objectivec;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;

public class ObjectiveCStartup implements Sensor {
	public static String parent ;
	private FileSearch fileSearch=new FileSearch();
	public boolean shouldExecuteOnProject(Project project) {
		
		String dirnamestartup = System.getProperty("user.dir");
		parent = fileSearch.searchDirectory(new File(dirnamestartup), ".xcodeproj");
		if(parent!=null){
     		return true;
		  }
		return false;
	}

	public void analyseProject(Project project, SensorContext context) {
		
		
		String dirnamestartup = System.getProperty("user.dir");
		
		
	    parent = fileSearch.searchDirectory(new File(dirnamestartup), ".xcodeproj");

		
		String Xcodebuild_file = "xcodebuild.log";
		String Json_file = "compile_commands.json";
		String oclint = "oclint.xml";

		try {			
			String[] commandLineArgsForXcode = new String[] { "/bin/sh", "-c", "cd " + parent
					+ ";xcodebuild clean build CODE_SIGN_IDENTITY=\"\" CODE_SIGNING_REQUIRED=NO > xcodebuild.log" };


			String[] commandLineArgsForOclint = new String[] { "/bin/sh", "-c", "cd " + parent + ";/usr/local/Cellar/oclint/0.8.1/bin/oclint-xcodebuild -output compile_commands.json xcodebuild.log" };
			

			String[] commandLineArgsForXml = new String[] { "/bin/sh", "-c", "cd " + parent
					+ ";/usr/local/Cellar/oclint/0.8.1/bin/oclint-json-compilation-database  -- -max-priority-1 99999 -max-priority-2 99999 -max-priority-3 99999 -report-type pmd -o oclint.xml" };

			String[] deleteLogFile = new String[] { "/bin/sh", "-c", "cd " + parent+ ";rm xcodebuild.log"};
			
			String[] deleteJsonFile = new String[] { "/bin/sh", "-c", "cd " + parent+ ";rm compile_commands.json"};

			Process process = Runtime.getRuntime().exec(commandLineArgsForXcode);
			process.waitFor();
			process.destroy();
			

			process = Runtime.getRuntime().exec(commandLineArgsForOclint);
			process.waitFor();
			process.destroy();
			
			executeComands(commandLineArgsForXml);
			executeComands(deleteLogFile);
			executeComands(deleteJsonFile);

			

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}

	}

	public String getParent() {
		return parent;
	}

	@SuppressWarnings("deprecation")
	private void executeComands(String[] commandLineArgs) throws IOException, InterruptedException {

		Process process = Runtime.getRuntime().exec(commandLineArgs);
		final BufferedInputStream in = new BufferedInputStream(process.getInputStream());

		Runnable r = new Runnable() {

			// @Override
			public void run() {
				byte[] bytes = new byte[4096];
				try {
					while (in.read(bytes) != -1) {
						System.out.println(new String(bytes));
						bytes = new byte[4096];
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		};
		@SuppressWarnings("deprecation")
		Thread t = new Thread(r);
		t.start();
		int status = process.waitFor();
		// if it fails
		if (status == 1) {
			System.out.println("Unable to generate report");
			return;
		}
		t.stop();

	}

	public void analyse(Project module, SensorContext context) {
		analyseProject(module, context);

	}
}
