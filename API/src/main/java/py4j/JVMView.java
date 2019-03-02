/******************************************************************************
 * Copyright (c) 2009-2018, Barthelemy Dagenais and individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * - The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************/
package py4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import py4j.reflection.TypeUtil;

/**
 * <p>
 * A JVM view keeps track of imports and import searches. A Python client can
 * have multiple JVM views (e.g., one for each module) so that imports in one
 * view do not conflict with imports from other views.
 * </p>
 * 
 * <p>
 * JVM views are not hierarchical: they do not inherit from each other so an
 * import in the default view does not affect the other views.
 * </p>
 * 
 * @author Barthelemy Dagenais
 */
public class JVMView {

	private ConcurrentMap<String, String> singleImportsMap;

	private Set<String> starImports;

	private Set<String> lastImportSearches;

	private String name;

	private String id;

	/**
	 * Running count of changes to the view so that we know whether we need to
	 * rebuild info.
	 *
	 * The sequenceId should be incremented for every change made that is
	 * visible in getImportedNames.
	 */
	private AtomicInteger sequenceId = new AtomicInteger(1);

	public final static String JAVA_LANG_STAR_IMPORT = "java.lang";

	public JVMView(String name, String id) {
		super();
		this.name = name;
		this.id = id;
		this.starImports = new ConcurrentSkipListSet<String>();
		this.lastImportSearches = new ConcurrentSkipListSet<String>();
		this.singleImportsMap = new ConcurrentHashMap<String, String>();
		this.starImports.add(JAVA_LANG_STAR_IMPORT);
	}

	/**
	 * 
	 * @param singleImport
	 *            Single import statement of the form
	 *            package1.package2.SimpleName
	 */
	public void addSingleImport(String singleImport) {
		String simpleName = TypeUtil.getName(singleImport, true);
		singleImportsMap.putIfAbsent(simpleName, singleImport);
		sequenceId.incrementAndGet();
	}

	/**
	 * 
	 * @param starImport
	 *            Star Import of the form "package1.package2.*"
	 */
	public void addStarImport(String starImport) {
		String packageName = TypeUtil.getPackage(starImport);
		if (!starImports.contains(packageName)) {
			starImports.add(packageName);
		}
	}

	public void clearImports() {
		this.singleImportsMap.clear();
		this.starImports.clear();
		this.starImports.add(JAVA_LANG_STAR_IMPORT);
		sequenceId.incrementAndGet();
	}

	public String getId() {
		return id;
	}

	public Set<String> getLastImportSearches() {
		return lastImportSearches;
	}

	public String getName() {
		return name;
	}

	public Map<String, String> getSingleImportsMap() {
		return singleImportsMap;
	}

	public Set<String> getStarImports() {
		return starImports;
	}

	public boolean removeSingleImport(String importString) {
		boolean removed = false;
		String simpleName = TypeUtil.getName(importString, true);
		removed = singleImportsMap.remove(simpleName, importString);
		sequenceId.incrementAndGet();
		return removed;
	}

	public boolean removeStarImport(String starImport) {
		String packageName = TypeUtil.getPackage(starImport);
		boolean result = starImports.remove(packageName);
		return result;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Return the current list of imports known to this view.
	 * @return list of class simple names.
	 */
	public String[] getImportedNames() {
		Set<String> namesSet = singleImportsMap.keySet();
		return (String[]) namesSet.toArray(new String[namesSet.size()]);
	}

	/**
	 * Sequence ID for getImportedNames(). The sequence ID can be compared to a
	 * previous call to determine if getImportedNames() will return a different
	 * value. The sequence ID is changed after the contents of
	 * getImportedNames() changes.
	 *
	 * @return sequence ID
	 */
	public int getSequenceId() {
		return sequenceId.get();
	}

}
