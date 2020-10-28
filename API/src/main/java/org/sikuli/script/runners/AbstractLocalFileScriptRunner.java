/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.sikuli.script.ImagePath;
import org.sikuli.script.support.IScriptRunner;

public abstract class AbstractLocalFileScriptRunner extends AbstractScriptRunner {

	private Deque<String> previousBundlePaths = new ArrayDeque<>();

	protected void prepareFileLocation(File scriptFile, IScriptRunner.Options options) {		
		if (!options.isRunningInIDE() && scriptFile.exists()) {
			previousBundlePaths.push(ImagePath.getBundlePath());
			ImagePath.setBundleFolder(scriptFile.getParentFile());
		}
	}
	
	public void resetFileLocation() {		
		if (!previousBundlePaths.isEmpty()) {
			ImagePath.setBundlePath(previousBundlePaths.pop());
		}
	}

	@Override
	public boolean canHandle(String identifier) {
		if (identifier != null) {
			/*
			 * Test if we have a network protocol in front of the identifier. In such a case
			 * we cannot handle the identifier directly
			 */
			int protoSepIndex = identifier.indexOf("://");
			if (protoSepIndex > 0 && protoSepIndex <= 5) {
				return false;
			}

			return super.canHandle(identifier);
		}

		return false;
	}
}
