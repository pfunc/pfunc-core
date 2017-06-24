/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.pfunc.loader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

/**
 * A helper class for working with pfuncs
 */
public class PFuncJar {
    public static final String PROPERTIES_FILE_PATH = "META-INF/services/io.pfunc/pfunc.properties";
    private static final String DEFAULT_BOOTSTRAP_CLASS = "io.pfunc.bootstrap.Bootstrap";
    private static final String INVOKE_METHOD = "invoke";
    private static Class<?>[] invokeMethodParameters = {
            String.class, Object[].class
    };
    private final ClassLoader classLoader;
    private final Class<?> boostrapClass;
    private final Method bootstrapMethod;
    private final Properties properties;

    public PFuncJar(ClassLoader classLoader, Class<?> boostrapClass, Method bootstrapMethod, Properties properties) {
        this.classLoader = classLoader;
        this.boostrapClass = boostrapClass;
        this.bootstrapMethod = bootstrapMethod;
        this.properties = properties;
    }

    public static PFuncJar create(ClassLoader classLoader) {
        Properties properties = new Properties();
        String className = DEFAULT_BOOTSTRAP_CLASS;
        URL resource = classLoader.getResource(PROPERTIES_FILE_PATH);
        if (resource != null) {
            try {
                properties.load(resource.openStream());
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to load " + PROPERTIES_FILE_PATH + ". " + e, e);
            }
            className = properties.getProperty(PropertiesKeys.BOOSTRAP_CLASS, DEFAULT_BOOTSTRAP_CLASS);
        }
        Class<?> clazz;
        try {
            clazz = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("ClassLoader does not contain boostrap class " + className + " in " + classLoader);
        }
        try {
            Method method = clazz.getMethod(INVOKE_METHOD, invokeMethodParameters);
            return new PFuncJar(classLoader, clazz, method, properties);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Bootstrap class " + clazz.getName() + " does not have a method "
                    + INVOKE_METHOD + "(" + Arrays.toString(invokeMethodParameters) + ") in " + classLoader);
        }
    }

    public PFunction withName(String name) {
        return new PFunction() {
            @Override
            public String toString() {
                return "PFunction." + name + "()";
            }

            @Override
            public Object invoke(Object... arguments) {
                if (arguments == null) {
                    arguments = new Object[0];
                }
                if (!(arguments instanceof Object[])) {
                    arguments = new Object[]{arguments};
                }
                Object[] bootstrapArguments = new Object[]{name, arguments};
                try {
                    return bootstrapMethod.invoke(null, bootstrapArguments);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Could not invoke " + bootstrapArguments + " with " + Arrays.asList(bootstrapArguments) + " due to: " + e, e);
                } catch (InvocationTargetException e) {
                    throw new IllegalArgumentException("Could not invoke " + bootstrapArguments + " with " + Arrays.asList(bootstrapArguments) + " due to: " + e, e);
                }
            }
        };
    }
}
