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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 */
public class PFuncInfo {
    private final String name;
    private final String description;
    private final Class<?> returnType;
    private final PFuncParameterInfo[] parameterInfos;

    public PFuncInfo(Method method) {
        this.name = method.getName();
        this.returnType = method.getReturnType();
        Parameter[] parameters = method.getParameters();
        int length = parameters.length;
        this.parameterInfos = new PFuncParameterInfo[length];
        for (int i = 0; i < length; i++) {
            this.parameterInfos[i] = new PFuncParameterInfo(parameters[i]);
        }
        // TODO find description via annotation?
        this.description = "";
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public PFuncParameterInfo[] getParameterInfos() {
        return parameterInfos;
    }
}
