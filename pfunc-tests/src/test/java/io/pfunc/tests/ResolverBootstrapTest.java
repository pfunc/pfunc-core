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
package io.pfunc.tests;

import io.pfunc.loader.PFuncContext;
import io.pfunc.loader.PFunction;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 */
public class ResolverBootstrapTest {
    protected String methodName = "helloWorld";

    @Test
    public void testBootstrap() throws Exception {
        String basedir = System.getProperty("basedir", ".");
        String version = System.getProperty("version", "1.0-SNAPSHOT");

        System.setProperty("io.pfunc.libraries", basedir + "/target/test-classes/.pfunc.libraries");

        File jarFile = new File(basedir, "../pfunc-resolver/target/pfunc-resolver-" + version + ".jar");
        assertThat(jarFile).exists().isFile();

        ClassLoader parentClassLoader = getClass().getClassLoader();
        URL[] urlArray = {jarFile.toURI().toURL()};
        URLClassLoader classLoader = new URLClassLoader(urlArray, parentClassLoader);
        PFuncContext context = new PFuncContext(classLoader);

        Map<String, PFunction> map = context.getFunctionMap();
        assertThat(map).isNotEmpty();
        System.out.println("Loaded functions: " + map.keySet());

        PFunction function = context.withName(methodName);
        assertThat(function).describedAs("No function found!").isNotNull();

        Object result = function.invoke("James");
        System.out.println("Invoked " + function + " with result: " + result);
        assertThat(result).isEqualTo("Hello James");
    }

}
