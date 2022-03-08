/*
 * Copyright (c) 2010-2022, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script.runners;

import java.io.File;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.sikuli.script.ImagePath;
import org.sikuli.script.runnerSupport.IScriptRunner;

public abstract class AbstractLocalFileScriptRunner extends AbstractScriptRunner {

	private static final Deque<String> PREVIOUS_BUNDLE_PATHS = new ConcurrentLinkedDeque<>();

	@Override
	protected void adjustBundlePath(String script, IScriptRunner.Options options) {
		File file = new File(script);

		if (file.exists()) {
			String currentBundlePath = ImagePath.getBundlePath();
			if(currentBundlePath != null) {
			  PREVIOUS_BUNDLE_PATHS.push(currentBundlePath);
			}
			ImagePath.setBundleFolder(file.getParentFile());
		}
	}

	@Override
	protected void resetBundlePath(String script, IScriptRunner.Options options) {
		if (new File(script).exists() && !PREVIOUS_BUNDLE_PATHS.isEmpty()) {
		    ImagePath.setBundlePath(PREVIOUS_BUNDLE_PATHS.pop());
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
