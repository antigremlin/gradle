/*
 * Copyright 2011 the original author or authors.
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

package org.gradle.tooling.internal.consumer.connection;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.UnsupportedVersionException;
import org.gradle.tooling.internal.adapter.ProtocolToModelAdapter;
import org.gradle.tooling.internal.build.VersionOnlyBuildEnvironment;
import org.gradle.tooling.internal.consumer.Distribution;
import org.gradle.tooling.internal.consumer.parameters.ConsumerOperationParameters;
import org.gradle.tooling.internal.protocol.ConnectionVersion4;
import org.gradle.tooling.model.build.BuildEnvironment;

/**
 * An adapter for unsupported connection using a {@code ConnectionVersion4} based provider.
 */
public class ConnectionVersion4BackedConsumerConnection implements ConsumerConnection {
    private final Distribution distribution;
    private final ProtocolToModelAdapter adapter;
    private final String version;

    public ConnectionVersion4BackedConsumerConnection(Distribution distribution, ConnectionVersion4 delegate, ProtocolToModelAdapter adapter) {
        this.distribution = distribution;
        this.adapter = adapter;
        version = delegate.getMetaData().getVersion();
    }

    public void stop() {
    }

    public String getDisplayName() {
        return distribution.getDisplayName();
    }

    public <T> T run(Class<T> type, CancellationToken cancellationToken, ConsumerOperationParameters operationParameters) throws UnsupportedOperationException, IllegalStateException {
        if (type.equals(BuildEnvironment.class)) {
            return adapter.adapt(type, doGetBuildEnvironment());
        }
        throw fail();
    }

    private Object doGetBuildEnvironment() {
        return new VersionOnlyBuildEnvironment(version);
    }

    public <T> T run(BuildAction<T> action, CancellationToken cancellationToken, ConsumerOperationParameters operationParameters) throws UnsupportedOperationException, IllegalStateException {
        throw fail();
    }

    private UnsupportedVersionException fail() {
        return new UnsupportedVersionException(String.format("Support for Gradle version %s was removed in tooling API version 2.0. You should upgrade your Gradle build to use Gradle 1.0-milestone-8 or later.", version));
    }
}
