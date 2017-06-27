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

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 */
public class Resolver {
    public List<URL> resolveCoordinates(Iterable<String> coordinates) throws Exception {
        List<Dependency> dependencies = createDependencies(coordinates);
        return resolveCoordinates(dependencies);
    }

    public List<URL> resolveCoordinates(String... coordinates) throws Exception {
        List<Dependency> dependencies = createDependencies(coordinates);
        return resolveCoordinates(dependencies);
    }

    public List<URL> resolveCoordinates(List<Dependency> dependencies) throws Exception {
        DefaultServiceLocator serviceLocator = MavenRepositorySystemUtils.newServiceLocator();
        serviceLocator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        serviceLocator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        RepositorySystem repositorySystem = serviceLocator.getService(RepositorySystem.class);
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepository = new LocalRepository(System.getProperty("user.home") + "/.m2/repository");
        session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepository));

        RemoteRepository remoteRepository = new RemoteRepository.Builder("central", "default", "http://central.maven.org/maven2").build();
        CollectRequest collectRequest = new CollectRequest(null, Arrays.asList(remoteRepository));

        collectRequest.setDependencies(dependencies);
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);
        DependencyResult result = repositorySystem.resolveDependencies(session, dependencyRequest);

        List<URL> answer = new ArrayList<URL>();
        for (ArtifactResult artifact : result.getArtifactResults()) {
            answer.add(artifact.getArtifact().getFile().toURI().toURL());
        }
        return answer;
    }

    private List<Dependency> createDependencies(String[] allCoordinates) {
        List<Dependency> dependencies = new ArrayList<Dependency>();
        for (String coordinate : allCoordinates) {
            dependencies.add(new Dependency(new DefaultArtifact(coordinate), null));
        }
        return dependencies;
    }

    private List<Dependency> createDependencies(Iterable<String> allCoordinates) {
        List<Dependency> dependencies = new ArrayList<Dependency>();
        for (String coordinate : allCoordinates) {
            dependencies.add(new Dependency(new DefaultArtifact(coordinate), null));
        }
        return dependencies;
    }
}
