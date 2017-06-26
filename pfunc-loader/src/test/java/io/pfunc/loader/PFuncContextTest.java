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

import io.pfunc.sample.MyFunc;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 */
public class PFuncContextTest {
    protected PFuncContext context;
    protected String methodName = "helloWorld";

    public static void assertFunctionHasValidMetadata(PFunction function) {
        PFuncInfo metadata = function.getMetadata();
        assertThat(metadata).describedAs("No metadata on function " + function).isNotNull();
        System.out.println("Function has metadata " + metadata);

        assertThat(metadata.getName()).isEqualTo("helloWorld");
        assertThat(metadata.getReturnType()).isNotNull();
        PFuncParameterInfo[] parameterInfos = metadata.getParameterInfos();
        assertThat(parameterInfos).isNotEmpty();
    }

    @Before
    public void init() throws IOException {
        context = new PFuncContext(MyFunc.class.getClassLoader());
    }

    @Test
    public void findFunctions() throws Exception {
        Map<String, PFunction> functionMap = context.getFunctionMap();
        assertThat(functionMap).describedAs("functionMap").isNotEmpty().containsKey(methodName);

        PFunction function = functionMap.get(methodName);
        assertThat(function).describedAs("No function found!").isNotNull();
        assertFunctionHasValidMetadata(function);
    }

    @Test
    public void testInvokeMethod() throws Exception {
        PFunction function = context.withName(methodName);
        assertThat(function).describedAs("No function found!").isNotNull();

        Object result = function.invoke("James");
        System.out.println("Invoked " + function + " with result: " + result);
        assertThat(result).isEqualTo("Hello James");
    }

    @Test
    public void validFunctionMetadata() throws Exception {
        PFunction function = context.withName(methodName);
        assertThat(function).describedAs("No function found!").isNotNull();
        assertFunctionHasValidMetadata(function);
    }


}
