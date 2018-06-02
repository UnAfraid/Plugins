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

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

/**
 * @author UnAfraid
 */
public class JarClassLoader extends URLClassLoader {
	public JarClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}
	
	public JarClassLoader(URL[] urls) {
		super(urls);
	}
	
	@Override
	public void close() throws IOException {
		try {
			final Field ucpField = getClass().getDeclaredField("ucp");
			ucpField.setAccessible(true);
			
			final Object classPath = ucpField.get(this);
			final Field loadersField = classPath.getClass().getDeclaredField("loaders");
			loadersField.setAccessible(true);
			
			final Object loadersCollection = loadersField.get(classPath);
			if (loadersCollection instanceof Collection) {
				for (Object jarClassLoader : ((Collection) loadersCollection)) {
					try {
						final Field jarField = jarClassLoader.getClass().getDeclaredField("jar");
						jarField.setAccessible(true);
						
						final Object jarFile = jarField.get(jarClassLoader);
						if (jarFile instanceof Closeable) {
							((Closeable) jarFile).close();
						}
					}
					catch (Exception e) {
						// ignore
					}
				}
			}
		}
		catch (Exception e) {
			// ignore
		}
		
		super.close();
	}
}
