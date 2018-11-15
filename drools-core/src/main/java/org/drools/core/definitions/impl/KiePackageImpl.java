/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.core.definitions.impl;

import java.util.HashMap;
import java.util.Map;

import org.kie.api.internal.io.ResourceTypePackage;
import org.kie.api.io.ResourceType;

public class KiePackageImpl {

    /**
     * Name of the pkg.
     */
    private String name;

    private Map<ResourceType, ResourceTypePackage> packages;

    public KiePackageImpl() {
        this(null);
    }

    /**
     * Construct.
     * @param name The name of this <code>Package</code>.
     */
    public KiePackageImpl(final String name) {
        this.name = name;
        packages = new HashMap<>();
    }

    Map<ResourceType, ResourceTypePackage> getResourceTypePackages() {
        return this.packages;
    }


}
