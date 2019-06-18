package com.github.bharatmicrosystems.composer_satis_update;

import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;

public class CommandHandler {
	
	public int executeCommand(String command, File workingDir) {
		CommandLine cmdLine = CommandLine.parse(command);
		DefaultExecutor executor = new DefaultExecutor();
		executor.setWorkingDirectory(workingDir);
		try {
			return executor.execute(cmdLine);
		} catch (ExecuteException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return 1;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return 1;
		}
	}

}
