/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.core.impl;

import org.drools.core.WorkingMemoryEventManager;
import org.kie.api.event.KieRuntimeEventManager;
import org.kie.api.logger.KieRuntimeLogger;

public abstract class AbstractRuntime {

    protected KieRuntimeLogger logger;
    protected KieRuntimeEventManager eventManager;
    protected WorkingMemoryEventManager workingMemoryEventManager;

    public KieRuntimeLogger getLogger() {
        return logger;
    }

    public void setLogger(KieRuntimeLogger logger) {
        this.logger = logger;
    }

    public KieRuntimeEventManager getKieRuntimeEventManager() {
        return eventManager;
    }
}
