package ut;

import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;

import ibm.gse.eda.stores.domain.Item;
import ibm.gse.eda.stores.infra.SimulatorGenerator;
import ibm.gse.eda.stores.infra.api.dto.SimulationControl;
import ibm.gse.eda.stores.infra.kafka.KafkaItemGenerator;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.reactive.messaging.MutinyEmitter;

@QuarkusTest
public class TestKafkaGenerator {

   
    @Inject
    KafkaItemGenerator generator;
    
    @BeforeAll
    public static void setup() {
    }

   @Test
   public void shoudGenerateOneRandomMessage(){
        SimulationControl control = new SimulationControl(1,SimulatorGenerator.RANDOM_MAX);
       List<Item> items = generator.start(control);
       Assertions.assertEquals(1, items.size());
   }

   @Test
   public void shoudGenerateControlledMessages(){
       SimulationControl control = new SimulationControl(2,SimulatorGenerator.CONTROLLED);
       List<Item> items = generator.start(control);
       Assertions.assertEquals(9, items.size());
   }

   @Test
   public void shoudGenerateRandomMessage() throws InterruptedException{
       SimulationControl control = new SimulationControl(5,SimulatorGenerator.RANDOM);
       List<Item> items = generator.start(control);
       Thread.currentThread().sleep(6000);
       generator.stop();
       Assertions.assertEquals(0, items.size());
      
   }
}
