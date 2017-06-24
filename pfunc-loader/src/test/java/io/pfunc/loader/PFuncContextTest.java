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
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 */
public class PFuncContextTest {
    @Ignore
    public void testContext() throws Exception {
        PFuncContext context = new PFuncContext();

        PFunction function = context.withName("myFunc");
        assertThat(function).describedAs("No function found!").isNotNull();

        Object result = function.invoke("James");
        assertThat(result).isEqualTo("Hello James");
    }

    @Test
    public void testJar() throws Exception {
        PFuncJar jar = PFuncJar.create(MyFunc.class.getClassLoader());

        PFunction function = jar.withName("myFunc");
        assertThat(function).describedAs("No function found!").isNotNull();

        Object result = function.invoke("James");
        System.out.println("Invoked " + function + " with result: " + result);
        assertThat(result).isEqualTo("Hello James");
    }

}
