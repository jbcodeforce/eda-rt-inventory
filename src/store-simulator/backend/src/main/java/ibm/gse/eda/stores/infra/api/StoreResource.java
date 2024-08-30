package ibm.gse.eda.stores.infra.api;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import ibm.gse.eda.stores.domain.Item;
import ibm.gse.eda.stores.domain.Store;
import ibm.gse.eda.stores.infra.SimulatorGenerator;
import ibm.gse.eda.stores.infra.StoreRepository;
import ibm.gse.eda.stores.infra.api.dto.SimulationControl;
import ibm.gse.eda.stores.infra.kafka.KafkaItemGenerator;
import ibm.gse.eda.stores.infra.mq.MQItemGenerator;
import ibm.gse.eda.stores.infra.rabbitmq.RabbitMQItemGenerator;
import io.smallrye.mutiny.Multi;

@Path("/api/stores/v1")
@ApplicationScoped
public class StoreResource {
    private static Logger logger = Logger.getLogger(StoreResource.class.getName());
    public static String RABBITMQ = "RABBITMQ";
    public static String IBMMQ = "IBMMQ";
    public static String KAFKA = "KAFKA";
    @Inject
    @ConfigProperty(name="app.target.messaging")
    String[] targetMessaging;

    @Inject
    RabbitMQItemGenerator rabbitMQGenerator;

    @Inject
    StoreRepository storeRepository;
    
    @Inject
    KafkaItemGenerator kafkaGenerator;

    @Inject
    MQItemGenerator mqGenerator;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Store> getStoresList() {
        return storeRepository.getStores();
    }

    @GET
    @Path("/backends")
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getBackends() {
        return targetMessaging;
    }

    @GET
    @Path("/names")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<String> getStoreNamesList() {
        return Multi.createFrom().items(storeRepository.getStoreNameList().stream());
    }


    @POST
    @Path("/start/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Item> startSendingMessage( SimulationControl control) {
        control.backend = control.backend.toUpperCase();
        logger.info("Received in /start " + control.toString());
        if (RABBITMQ.equals(control.backend)) {
            return startSendingMessageToRMQ(control);
        } else if (KAFKA.equals(control.backend)) {
            return startSendingMessageToKafka(control);
         } else if (IBMMQ.equals(control.backend)) {
            return startSendingMessageToMQ(control);
         }
         return Multi.createFrom().empty();
    }

    @POST
    @Path("/stop/")
    @Consumes(MediaType.APPLICATION_JSON)
    public void stopSendingMessage( SimulationControl control) {
        control.backend = control.backend.toUpperCase();
        logger.info("Received in /stop " + control.toString());
        if (RABBITMQ.equals(control.backend)) {
            rabbitMQGenerator.stop();
        } else if (KAFKA.equals(control.backend)) {
            kafkaGenerator.stop();
         } else if (IBMMQ.equals(control.backend)) {
            mqGenerator.stop();
         }
    }


    @POST
    @Path("/start/rmq/{records}")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Item> startSendingMessageToRMQOperation(@PathParam final int records){
        SimulationControl control = new SimulationControl(records,SimulatorGenerator.RANDOM);
        return startSendingMessageToRMQ(control);
    }

    public Multi<Item> startSendingMessageToRMQ(SimulationControl control) {
            return Multi.createFrom().items(rabbitMQGenerator.start(control).stream()); 
             
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/start/kafka/{records}")
    public Multi<Item> startSendingMessageToKafkaOperation(@PathParam final int records){
        SimulationControl control = new SimulationControl(records,SimulatorGenerator.RANDOM);
        return startSendingMessageToKafka(control);
    }

    public  Multi<Item>  startSendingMessageToKafka(SimulationControl control){
        return Multi.createFrom().items(kafkaGenerator.start(control).stream());     
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/start/mq/{records}")
    public  Multi<Item>  startSendingMessageToMQ(@PathParam final int records){
        SimulationControl control = new SimulationControl(records, SimulatorGenerator.RANDOM);
        return Multi.createFrom().items(mqGenerator.start(control).stream());
       
    }

    public  Multi<Item>  startSendingMessageToMQ(SimulationControl control){
        return Multi.createFrom().items(mqGenerator.start(control).stream());
       
    }
}