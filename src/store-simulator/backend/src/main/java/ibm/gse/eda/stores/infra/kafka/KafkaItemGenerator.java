/*
* (c) Copyright IBM Corporation 2020
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
package ibm.gse.eda.stores.infra.kafka;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;

import ibm.gse.eda.stores.domain.Item;
import ibm.gse.eda.stores.infra.SimulatorGenerator;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;

/**
 * Simulate item sale activities between multiple store. As we do not want to
 * get polluted by kafka connection exceptions while not running in Kafka mode
 * we implement this using the KafkaProducer API and not the microprofile
 * reactive messaging API
 */
@ApplicationScoped
public class KafkaItemGenerator extends SimulatorGenerator {
    Logger logger = Logger.getLogger(KafkaItemGenerator.class.getName());

    @Inject
    @Channel("items")
    public MutinyEmitter<Item> eventProducer;

    public KafkaItemGenerator() {
    }

    @Override
    public void sendMessage(Item item) {
        logger.info("Send message: " + item.toString());
        Message<Item> msg = Message.of(item).addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
                .withKey(item.storeName).build()).withAck(() -> {
                    // Called when the message is acked
                    logger.info("Message acked");
                    return CompletableFuture.completedFuture(null);
                })
                .withNack(throwable -> {
                    // Called when the message is nacked
                    logger.severe(throwable.getMessage());
                    logger.warning("Message nacked");
                    return CompletableFuture.completedFuture(null);
                });
        eventProducer.send(msg);
    }

    @Override
    public boolean preProcessing() {
        return true;
    }
}
