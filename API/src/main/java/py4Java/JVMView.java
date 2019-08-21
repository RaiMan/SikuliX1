/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java;

import py4Java.reflection.TypeUtil;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

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
        boolean removed;
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
        return namesSet.toArray(new String[namesSet.size()]);
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
