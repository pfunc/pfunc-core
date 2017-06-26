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

import io.pfunc.loader.helpers.IntrospectionHelper;

import java.beans.BeanInfo;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * A helper class for working with pfuncs
 */
public class PFuncContext {
    public static final String PROPERTIES_FILE_PATH = "META-INF/services/io.pfunc/pfunc.properties";
    private static final Logger LOGGER = Logger.getLogger(PFuncContext.class.getName());
    private static final String DEFAULT_BOOTSTRAP_CLASS = "io.pfunc.bootstrap.Bootstrap";
    private static final String INVOKE_METHOD = "invoke";
    private static final String GET_FUNCTION_METADATA_METHOD = "functionMetadata";

    private static Class<?>[] invokeMethodParameters = {
            String.class, Object[].class
    };

    private static Class<?>[] getFunctionMethodParameters = {
    };

    private final ConcurrentMap<String, PFuncJarFunction> cache = new ConcurrentHashMap<>();

    public PFuncContext() {
    }

    public PFuncContext(ClassLoader classLoader) throws IOException {
        this();
        loadFunctions(classLoader);
    }

    /**
     * Returns an immutable copy of the current registered functions so that they can be invokved or their metadata
     * invoked
     */
    public Map<String, PFunction> getFunctionMap() {
        return Collections.unmodifiableMap(new TreeMap(cache));
    }

    public void loadFunctions(ClassLoader classLoader) throws IOException {
        Properties properties = new Properties();
        String className = DEFAULT_BOOTSTRAP_CLASS;
        Enumeration<URL> resources = classLoader.getResources(PROPERTIES_FILE_PATH);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
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
            PFuncJar jar;
            try {
                Method method = clazz.getMethod(INVOKE_METHOD, invokeMethodParameters);
                jar = new PFuncJar(classLoader, clazz, method, properties);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Bootstrap class " + clazz.getName() + " does not have a method "
                        + INVOKE_METHOD + "" + asArgumentTypeText(invokeMethodParameters) + " in " + classLoader);
            }
            Method metadataMethod;
            try {
                metadataMethod = clazz.getMethod(GET_FUNCTION_METADATA_METHOD, getFunctionMethodParameters);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Bootstrap class " + clazz.getName() + " does not have a method "
                        + GET_FUNCTION_METADATA_METHOD + "" + asArgumentTypeText(getFunctionMethodParameters) + " in " + classLoader);
            }
            List<PFuncInfo> infos = new ArrayList<>();
            addFunctionMetadata(metadataMethod, infos);
            for (PFuncInfo info : infos) {
                addFunction(info, jar);
            }
        }
    }

    /**
     * Returns the array of classes into a textual representation of function arguments
     */
    private String asArgumentTypeText(Class<?>[] classes) {
        String text = Arrays.toString(classes);
        if (text.startsWith("[") && text.endsWith("]")) {
            return "(" + text.substring(1, text.length() - 1) + ")";
        }
        return text;
    }

    protected void addFunctionMetadata(Method metadataMethod, List<PFuncInfo> list) {
        try {
            Object result = metadataMethod.invoke(null);
            if (result != null) {
                if (result.getClass().isArray()) {
                    int length = Array.getLength(result);
                    for (int i = 0; i < length; i++) {
                        Object value = Array.get(result, i);
                        PFuncInfo info = convertToPFuncInfo(value);
                        if (info != null) {
                            list.add(info);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not invoke " + metadataMethod + " due to " + e, e);
        }
    }

    private PFuncInfo convertToPFuncInfo(Object value) {
        PFuncInfo answer = null;
        if (value != null) {
            BeanInfo info;
            info = IntrospectionHelper.introspect(value);
            answer = new PFuncInfo();
            String name = IntrospectionHelper.getStringProperty(info, BeanProperties.NAME, value);
            if (name == null || name.length() == 0) {
                return null;
            }
            answer.setName(name);
            answer.setDescription(IntrospectionHelper.getStringProperty(info, BeanProperties.DESCRIPTION, value));
            answer.setReturnType(IntrospectionHelper.getClassProperty(info, BeanProperties.RETURN_TYPE, value));
            Object parameters = IntrospectionHelper.getObjectProperty(info, BeanProperties.PARAMETER_INFOS, value);
            if (parameters != null && parameters.getClass().isArray()) {
                int length = Array.getLength(parameters);
                PFuncParameterInfo[] array = new PFuncParameterInfo[length];
                for (int i = 0; i < length; i++) {
                    Object paramValue = Array.get(parameters, i);
                    array[i] = convertToPFuncParameterInfo(paramValue);
                }
                answer.setParameterInfos(array);
            }
        }
        return answer;
    }

    private PFuncParameterInfo convertToPFuncParameterInfo(Object value) {
        PFuncParameterInfo answer = null;
        if (value != null) {
            BeanInfo info;
            info = IntrospectionHelper.introspect(value);
            answer = new PFuncParameterInfo();
            answer.setName(IntrospectionHelper.getStringProperty(info, BeanProperties.NAME, value));
            answer.setDescription(IntrospectionHelper.getStringProperty(info, BeanProperties.DESCRIPTION, value));
            answer.setType(IntrospectionHelper.getClassProperty(info, BeanProperties.TYPE, value));
            return answer;
        }
        return answer;
    }

    protected void addFunction(PFuncInfo info, PFuncJar jar) {
        PFuncJarFunction function = new PFuncJarFunction(info, jar);
        PFuncJarFunction oldValue = cache.putIfAbsent(info.getName(), function);
        if (oldValue != null) {
            LOGGER.warning("Cannot register " + function + " as there is already this function registered: " + oldValue);
        }
    }


    public PFunction withName(String name) {
        PFuncJarFunction function = cache.get(name);
        if (function == null) {
            throw new IllegalArgumentException("No such function: " + name);
        }
        return function;
    }
}
