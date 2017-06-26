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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

/**
 * A helper class for working with pfuncs
 */
public class PFuncJar {
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

    @Override
    public String toString() {
        return "PFuncJar{" +
                "classLoader=" + classLoader +
                ", boostrapClass=" + boostrapClass.getName() +
                '}';
    }

    public PFunction withName(String name, final PFuncInfo metadata) {
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

            @Override
            public PFuncInfo getMetadata() {
                return metadata;
            }
        };
    }
}
