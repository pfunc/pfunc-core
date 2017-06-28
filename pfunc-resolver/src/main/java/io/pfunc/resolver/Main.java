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

import java.util.Arrays;

/**
 */
public class Main {
    public static void main(String[] args) {
        int length = args.length;
        if (length == 0) {
            System.err.println("Usage: functionName [arguments]*");
            System.exit(1);
            return;
        }
        String functionName = args[0];
        String[] arguments = new String[length - 1];
        System.arraycopy(args, 1, arguments, 0, length - 1);
        try {
            Object result = Bootstrap.invoke(functionName, arguments);
            System.out.println("" + functionName + Arrays.toString(arguments) + " => " + result);
        } catch (Exception e) {
            System.err.println("Failed to invoke " + functionName + Arrays.toString(arguments) + " due to " + e);
            e.printStackTrace();
            System.exit(2);
        }
    }
}
