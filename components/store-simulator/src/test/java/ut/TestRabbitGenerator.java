package ut;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ibm.gse.eda.stores.infra.rabbitmq.RabbitMQItemGenerator;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class TestRabbitGenerator {
    
    @Inject
    RabbitMQItemGenerator generator;

    @Test
    public void shouldHaveLoadedProperties(){
        System.out.println(generator.toString());
        /**
         See comment https://github.com/quarkusio/quarkus/issues/1632#issuecomment-475527981
         This is a well known limitation of CDI client proxies - the only way to access the state of the underlying bean instance (contextual instance per the spec) is to invoke a method of a client proxy (contextual reference for normal scoped beans). Only non-static public fields are disallowed
        */
        Assertions.assertNotNull(generator.getHost());   
        Assertions.assertEquals("localhost",generator.getHost());   
        Assertions.assertEquals(5672,generator.getPort());   
        Assertions.assertNotNull(generator.getQueueName());         
    }

    
}
