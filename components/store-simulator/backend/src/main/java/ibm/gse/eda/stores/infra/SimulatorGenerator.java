package ibm.gse.eda.stores.infra;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.inject.Inject;

import ibm.gse.eda.stores.domain.Item;
import ibm.gse.eda.stores.infra.api.dto.SimulationControl;
import io.smallrye.mutiny.Multi;

public abstract class SimulatorGenerator {
    Logger logger = Logger.getLogger(SimulatorGenerator.class.getName());
    public static final String RANDOM_MAX = "randomMax";
    public static final String RANDOM = "random";
    public static final String CONTROLLED = "controlled";
    public static final String STOP = "stop";
    
    private ExecutorService executor;
    private SimulatorRunnable runIt;
    @Inject
    public StoreRepository storeRepository;


    public void sendMessage(Item item) {};

    public boolean preProcessing(){ return true;};

    public void postProcessing(){};

    public List<Item> start(SimulationControl control) {
        List<Item> items = new ArrayList<Item>();
        if (preProcessing()) {
          
            switch (control.type) {
                case RANDOM:
                    runIt = new SimulatorRunnable(this,storeRepository,control.records);
                    executor = Executors.newFixedThreadPool(1);
                    executor.execute(runIt);
                    // dirty trick for UI
                    return runIt.getCurrentItems();
                    
                case RANDOM_MAX:
                    items = storeRepository.buildRandomItems(control.records);
                    sendMessages(items);
                    break;
                case CONTROLLED:
                    items = storeRepository.buildControlledItems();
                    sendMessages(items);
                    break;
            }

            postProcessing();
            return items;
        }
        return items;
    }

    public void stop(){
        if (runIt != null) {
            runIt.stop();
            executor.shutdown();
        }
    }

    public void sendMessages( List<Item> items ){
        Multi.createFrom().items(items.stream()).subscribe().with(item -> {
            sendMessage(item);
        }, failure -> logger.severe("Failed with " + failure.getMessage()));
    }
}
