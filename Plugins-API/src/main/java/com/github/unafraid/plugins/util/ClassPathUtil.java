/*
 * Copyright (C) 2004-2017 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
