/*
 * Copyright (c) 2017 Rumen Nikiforov <unafraid89@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.unafraid.plugins.util;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

/**
 * A simple utility class to handle getting classes/packages.
 * @author UnAfraid
 */
@SuppressWarnings("unchecked")
public final class ClassPathUtil
{
	private ClassPathUtil()
	{
		// utility class
	}
	
	/**
	 * Gets a {@link Class#newInstance()} of the first class extending the given target class.
	 * @param <T>
	 * @param targetClass the given target class
	 * @return new instance of the given target class
	 * @throws Exception
	 */
	public static <T> T getInstanceOfExtending(Class<T> targetClass) throws Exception
	{
		for (Class<T> targetClassImpl : getAllClassesExtending(targetClass))
		{
			for (Constructor<?> constructor : targetClassImpl.getConstructors())
			{
				if (Modifier.isPublic(constructor.getModifiers()) && (constructor.getParameterCount() == 0))
				{
					return (T) constructor.newInstance();
				}
			}
		}
		throw new IllegalStateException("Couldn't find public constructor without prameters");
		
	}
	
	/**
	 * Gets all classes extending the given target class.
	 * @param <T>
	 * @param targetClass the given target class
	 * @return a list of classes
	 * @throws IOException
	 */
	public static <T> List<Class<T>> getAllClassesExtending(Class<T> targetClass) throws IOException
	{
		final ClassPath classPath = ClassPath.from(ClassLoader.getSystemClassLoader());
		//@formatter:off
		return classPath.getTopLevelClasses()
			.stream()
			.map(ClassPathUtil::loadClass)
			.filter(Objects::nonNull)
			.filter(clazz -> targetClass.isAssignableFrom(clazz))
			.filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
			.filter(clazz -> !Modifier.isInterface(clazz.getModifiers()))
			.map(clazz -> (Class<T>) clazz)
			.collect(Collectors.toList());
		//@formatter:on
	}
	
	/**
	 * Gets all classes extending the given target class.
	 * @param <T>
	 * @param packageName the package where you seek
	 * @param targetClass the given target class
	 * @return a list of classes
	 * @throws IOException
	 */
	public static <T> List<Class<T>> getAllClassesExtending(String packageName, Class<T> targetClass) throws IOException
	{
		final ClassPath classPath = ClassPath.from(ClassLoader.getSystemClassLoader());
		//@formatter:off
		return classPath.getTopLevelClassesRecursive(packageName)
			.stream()
			.map(ClassPathUtil::loadClass)
			.filter(Objects::nonNull)
			.filter(clazz -> targetClass.isAssignableFrom(clazz))
			.filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
			.filter(clazz -> !Modifier.isInterface(clazz.getModifiers()))
			.map(clazz -> (Class<T>) clazz)
			.collect(Collectors.toList());
		//@formatter:on
	}
	
	/**
	 * Gets all methods annotated with the specified annotation.
	 * @param annotationClass
	 * @return a list of methods
	 * @throws IOException
	 */
	public static List<Method> getAllMethodsAnnotatedWith(Class<? extends Annotation> annotationClass) throws IOException
	{
		final ClassPath classPath = ClassPath.from(ClassLoader.getSystemClassLoader());
		//@formatter:off
		return classPath.getTopLevelClasses()
			.stream()
			.map(ClassPathUtil::loadClass)
			.filter(Objects::nonNull)
			.flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods()))
			.filter(method -> method.isAnnotationPresent(annotationClass))
			.collect(Collectors.toList());
		//@formatter:on		
	}
	
	/**
	 * Gets all methods inside the package annotated with the specified annotation.
	 * @param packageName the name of the package
	 * @param annotationClass the annotation you seek
	 * @return a list of methods
	 * @throws IOException
	 */
	public static List<Method> getAllMethodsAnnotatedWith(String packageName, Class<? extends Annotation> annotationClass) throws IOException
	{
		final ClassPath classPath = ClassPath.from(ClassLoader.getSystemClassLoader());
		//@formatter:off
		return classPath.getTopLevelClassesRecursive(packageName)
			.stream()
			.map(ClassPathUtil::loadClass)
			.filter(Objects::nonNull)
			.flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods()))
			.filter(method -> method.isAnnotationPresent(annotationClass))
			.collect(Collectors.toList());
		//@formatter:on
	}
	
	/**
	 * Loads the class inside {@link ClassInfo}
	 * @param info the class info
	 * @return the loaded class
	 */
	private static Class<?> loadClass(ClassInfo info)
	{
		try
		{
			return info.load();
		}
		catch (NoClassDefFoundError e)
		{
			// ignore
		}
		
		return null;
	}
}
