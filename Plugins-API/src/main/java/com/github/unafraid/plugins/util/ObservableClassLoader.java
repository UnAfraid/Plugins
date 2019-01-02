package com.github.unafraid.plugins.util;

/**
 * Simple ClassLoader that allows looking up loaded class by name
 * @author UnAfraid
 */
public class ObservableClassLoader extends ClassLoader {
	public ObservableClassLoader(ClassLoader parent) {
		super(parent);
	}
	
	/**
	 * @param name the full name of the class
	 * @return the class if this ClassLoader or its parents contains the class, null otherwise.
	 */
	public Class<?> getLoadedClass(String name) {
		return findLoadedClass(name);
	}
	
	/**
	 * @param name the full name of the class
	 * @return {@code true} if this ClassLoader or its parents contains the class, {@code false} otherwise.
	 */
	public boolean hasLoadedClass(String name) {
		return findLoadedClass(name) != null;
	}
}
