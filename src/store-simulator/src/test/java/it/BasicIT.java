package it;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;


public abstract class BasicIT {
    
    private static Network network = Network.newNetwork();
    public static GenericContainer rabbitMQ = new GenericContainer(DockerImageName.parse("rabbitmq:3-management")).withExposedPorts(5672);
   
    public static StrimziContainer kafkaContainer = new StrimziContainer()
            .withNetwork(network)
            .withNetworkAliases("kafka")
            .withExposedPorts(9092);

    @BeforeAll
    public static void startAll() {
        Startables.deepStart(Stream.of(kafkaContainer,rabbitMQ)).join();
        System.setProperty("KAFKA_BOOTSTRAP_SERVERS", kafkaContainer.getBootstrapServers());
    }

    @AfterAll
    public static void stopAll() {
        kafkaContainer.stop();
        rabbitMQ.stop();
    }
}
