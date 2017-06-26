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
package io.pfunc.sample;

import io.pfunc.bootstrap.PFuncInfo;

import static io.pfunc.bootstrap.BootstrapHelpers.argument;

/**
 */
public class Bootstrap {
    public static Object invoke(String functionName, Object[] arguments) {
        switch (functionName) {
            case "helloWorld":
                return MyFunc.helloWorld(argument(arguments, 0, String.class));
        }
        throw new IllegalArgumentException("Function does not exist: " + functionName);
    }

    public static PFuncInfo[] functionMetadata() throws NoSuchMethodException {
        return new PFuncInfo[]{
                new PFuncInfo(MyFunc.class.getMethod("helloWorld", String.class))
        };
    }

}
