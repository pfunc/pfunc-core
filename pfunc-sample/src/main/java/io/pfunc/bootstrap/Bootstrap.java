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
package io.pfunc.bootstrap;

import io.pfunc.sample.MyFunc;

/**
 */
public class Bootstrap {
    public static Object invoke(String functionName, Object[] arguments) {
        switch (functionName) {
            case "myFunc":
                return MyFunc.helloWorld(argument(arguments, 0, String.class));
        }
        throw new IllegalArgumentException("Function does not exist: " + functionName);
    }

    private static <T> T argument(Object[] arguments, int index, Class<T> clazz) {
        if (arguments == null || arguments.length < index) {
            return null;
        }
        Object value = arguments[index];
        if (value == null) {
            return null;
        }
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        throw new IllegalArgumentException("Argument " + index + " is not a " + clazz.getName() + " but is " + value.getClass().getName() + " " + value);
    }
}
