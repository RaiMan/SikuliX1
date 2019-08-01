/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.model;

/**
 * <p>
 * Model of a Java member (class, method, or field) used to create a help page.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public abstract class Py4JMember implements Comparable<Py4JMember> {

	// Currently not supported
	private final String javadoc;

	private final String name;

	public Py4JMember(String name, String javadoc) {
		super();
		this.name = name;
		this.javadoc = javadoc;
	}

	@Override
	public int compareTo(Py4JMember o) {
		return this.getName().compareTo(o.getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Py4JMember)) {
			return false;
		}
		return this.getSignature(false).equals(((Py4JMember) obj).getSignature(false));
	}

	@Override
	public int hashCode() {
		return getSignature(false).hashCode();
	}

	public String getJavadoc() {
		return javadoc;
	}

	public String getName() {
		return name;
	}

	public abstract String getSignature(boolean shortName);

}
