/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.idesupport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

public class IDESupport {

	public static Map<String, IIDESupport> ideSupporter = new HashMap<String, IIDESupport>();

	public static void initIDESupport() {
		ServiceLoader<IIDESupport> sloader = ServiceLoader.load(IIDESupport.class);
		Iterator<IIDESupport> supIterator = sloader.iterator();
		while (supIterator.hasNext()) {
			IIDESupport current = supIterator.next();
			try {
				for (String ending : current.getEndings()) {
					ideSupporter.put(ending, current);
				}
			} catch (Exception ex) {
			}
		}
	}
}
