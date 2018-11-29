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

package org.drools.beliefs.bayes.assembler;

import org.drools.beliefs.bayes.BayesNetwork;
import org.drools.beliefs.bayes.JunctionTree;
import org.drools.beliefs.bayes.JunctionTreeBuilder;
import org.drools.beliefs.bayes.model.Bif;
import org.drools.beliefs.bayes.model.XmlBifParser;
import org.drools.compiler.builder.AbstractAssemblerService;
import org.drools.compiler.builder.AssemblerContext;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;

public class BayesAssemblerService extends AbstractAssemblerService<BayesPackage, JunctionTree> {

    @Override
    public ResourceType getResourceType() {
        return ResourceType.BAYES;
    }

    @Override
    protected BayesPackage createPackage(String namespace) {
        return new BayesPackage(namespace);
    }

    @Override
    protected JunctionTree compile(AssemblerContext ctx, Resource resource) {
        BayesNetwork network;
        JunctionTreeBuilder builder;


        Bif bif = XmlBifParser.loadBif(resource, ctx.getErrors());
        if (bif == null) {
            return null;
        }

        try {
            network = XmlBifParser.buildBayesNetwork(bif);
        } catch (Exception e) {
            ctx.getErrors().add(new BayesNetworkAssemblerError(resource, "Unable to parse opening Stream:\n" + e.toString()));
            return null;
        }

        try {
            builder = new JunctionTreeBuilder(network);
        }  catch ( Exception e ) {
            ctx.getErrors().add(new BayesNetworkAssemblerError(resource, "Unable to build Junction Tree:\n" + e.toString()));
            return null;
        }

        return builder.build(resource, network.getPackageName(), network.getName());
    }

}
