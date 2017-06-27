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
package io.pfunc.resolver;


import io.pfunc.loader.PFuncContext;
import io.pfunc.loader.PFuncInfo;
import io.pfunc.loader.PFunction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bootstrap {
    private static final Logger LOGGER = Logger.getLogger(Bootstrap.class.getName());

    protected static PFuncContext context = new PFuncContext();

    static {
        init();
    }

    protected static void init() {
        // lets detect a properites file
        File file = new File(".pfunc.libraries");
        if (file.isFile() && file.exists()) {
            loadFile(file);
        }
        String property = System.getProperty("io.pfunc.libraries");
        if (property != null && property.length() > 0) {
            File propertyFile = new File(property);
            if (!file.getAbsolutePath().equals(propertyFile.getAbsolutePath())) {
                loadFile(propertyFile);
            }
        }
    }

    private static void loadFile(File file) {
        try {
            LOGGER.info("Loading boottrap file " + file);
            loadLibraries(context, file);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load file " + file + " due to " + e, e);
        }
    }

    public static void loadLibraries(PFuncContext context, File file) throws Exception {
        List<String> coordinates = new ArrayList<>();
        List<URL> urls = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.startsWith("mvn://")) {
                    coordinates.add(line.substring("mvn://".length()));
                } else if (line.startsWith("mvn:")) {
                    coordinates.add(line.substring("mvn:".length()));
                } else if (line.length() > 0) {
                    try {
                        URL u = new URL(line);
                        urls.add(u);
                    } catch (MalformedURLException e) {
                        LOGGER.log(Level.WARNING, "Failed to parse URL " + line + " due to " + e, e);
                    }
                }
            }
        }
        if (!coordinates.isEmpty()) {
            Resolver resolver = new Resolver();
            urls.addAll(resolver.resolveCoordinates(coordinates));
        }
        if (!urls.isEmpty()) {
            URL[] urlArray = new URL[urls.size()];
            urls.toArray(urlArray);
            ClassLoader parentClassLoader = Bootstrap.class.getClassLoader();
            URLClassLoader classLoader = new URLClassLoader(urlArray, parentClassLoader);
            context.loadFunctions(classLoader);
        }
    }

    public static Object invoke(String functionName, Object[] arguments) {
        return context.withName(functionName).invoke(arguments);
    }

    public static PFuncInfo[] functionMetadata() throws NoSuchMethodException {
        List<PFuncInfo> list = new ArrayList<>();
        Collection<PFunction> functions = context.getFunctionMap().values();
        for (PFunction function : functions) {
            list.add(function.getMetadata());
        }
        PFuncInfo[] answer = new PFuncInfo[list.size()];
        list.toArray(answer);
        return answer;
    }
}