/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.configuration.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.metadata.Node
import io.micronaut.context.ApplicationContext
import io.micronaut.context.DefaultApplicationContext
import io.micronaut.context.env.MapPropertySource
import spock.lang.Specification

class CassandraConfigurationSpec extends Specification {

    void "test no configuration"() {
        given:
        ApplicationContext applicationContext = new DefaultApplicationContext("test")
        applicationContext.start()

        expect: "No beans are created"
        !applicationContext.containsBean(CassandraConfiguration)
        !applicationContext.containsBean(CqlSession)

        cleanup:
        applicationContext.close()
    }

    void "test single cluster connection"() {
        given:
        // tag::single[]
        ApplicationContext applicationContext = new DefaultApplicationContext("test")
        applicationContext.environment.addPropertySource(MapPropertySource.of(
                'test',
                ['cassandra.datasource.default.basic.contact-points': ["localhost:9042"]]
        ))
        applicationContext.start()
        // end::single[]

        expect:
//        !applicationContext.getBean(ClusterBuilderListener).invoked
        applicationContext.containsBean(CassandraConfiguration)
        applicationContext.containsBean(CqlSession)

        when:
        CqlSession session = applicationContext.getBean(CqlSession)
//        applicationContext.getBean(ClusterBuilderListener).invoked
        List<Node> inetSocketAddresses = session.metadata.getNodes().values()

        then:
        cluster.getClusterName() == "ociCluster"
        inetSocketAddresses[0].getBroadcastAddress().get().hostName == "localhost"
        inetSocketAddresses[0].getBroadcastAddress().get().port == 9042
//        cluster.getConfiguration().getProtocolOptions().getMaxSchemaAgreementWaitSeconds() == 20
//        cluster.getConfiguration().getProtocolOptions().getSSLOptions() != null

        cleanup:
        applicationContext.close()
    }

//    void "test multiple cluster connections"() {
//        given:
//        // tag::multiple[]
//        ApplicationContext applicationContext = new DefaultApplicationContext("test")
//        applicationContext.environment.addPropertySource(MapPropertySource.of(
//                'test',
//                ['cassandra.default.contactPoint': "localhost",
//                 'cassandra.default.port': 9042,
//                 'cassandra.secondary.contactPoint': "127.0.0.2",
//                 'cassandra.secondary.port': 9043]
//        ))
//        applicationContext.start()
//        // end::multiple[]
//
//        when:
//        Cluster defaultCluster = applicationContext.getBean(Cluster)
//        Cluster secondaryCluster = applicationContext.getBean(Cluster, Qualifiers.byName("secondary"))
//        List<InetSocketAddress> defaultInetSocketAddresses = defaultCluster.manager.contactPoints
//        List<InetSocketAddress> secondaryInetSocketAddresses = secondaryCluster.manager.contactPoints
//
//        then:
//        defaultInetSocketAddresses[0].getHostName() == "localhost"
//        defaultInetSocketAddresses[0].getPort() == 9042
//
//        secondaryInetSocketAddresses[0].getHostName() == "127.0.0.2"
//        secondaryInetSocketAddresses[0].getPort() == 9043
//
//        cleanup:
//        applicationContext.close()
//    }
//
//    @Singleton
//    static class ClusterBuilderListener implements BeanCreatedEventListener<Cluster.Builder> {
//
//        boolean invoked = false
//        @Override
//        Cluster.Builder onCreated(BeanCreatedEvent<Cluster.Builder> event) {
//            def builder = event.getBean()
//            invoked = builder != null
//            return builder
//        }
//    }
}
