package it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import ibm.gse.eda.stores.domain.Item;
import ibm.gse.eda.stores.domain.Store;
import ibm.gse.eda.stores.infra.api.StoreResource;
import ibm.gse.eda.stores.infra.kafka.KafkaItemGenerator;
import ibm.gse.eda.stores.infra.rabbitmq.RabbitMQItemGenerator;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;

@QuarkusTest
@TestHTTPEndpoint(StoreResource.class)
public class TestStoreResource   {

    @InjectMock
    RabbitMQItemGenerator rabbitMQGenerator;
    @InjectMock
    KafkaItemGenerator kafkaItemGenerator;

    @Test
    public void shouldHaveStore_1_fromGetStoreNames(){
        given().when().get("/names").then().statusCode(200).body(containsString("Store_1"));
    }
    
    @Test
    public void shouldNotHaveStore_7_fromGetStoreNames(){
        given().when().get("/names").then().statusCode(200).body(not(containsString("Store_7")));
    }

    @Test
    public void shouldHaveStores(){
        given().when().get().then().statusCode(200).body(notNullValue(Store.class));
    }

    /**
     * The following code need rabbitmq running with docker compose.
    */
    @Test
    public void shouldStartSendingOneMessageToRabbitMQ(){
        when(rabbitMQGenerator.preProcessing()).thenReturn(true);
        //when(rabbitMQGenerator.postProcessing()).thenReturn(null);
        given().when().post("/start/rmq/1").then().statusCode(200);
    }

    
    @Test
    public void shouldStartSendingOneMessageToKafka(){
        //when(kafkaItemGenerator.start(1,true)).thenReturn(new ArrayList<Item>());
        given().when().post("/start/kafka/1").then().statusCode(200);
    }
     
}
