package ibm.gse.eda.stores.infra;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import ibm.gse.eda.stores.domain.Item;

public class SimulatorRunnable implements Runnable {
    Logger logger = Logger.getLogger(SimulatorRunnable.class.getName());
    SimulatorGenerator generator;
    boolean running = true;
    StoreRepository storeRepository;
    int recordsToSend =0;
    List<Item> items = new ArrayList<Item>();

    public SimulatorRunnable(SimulatorGenerator gen,
                        StoreRepository storeRepository, 
                        int records) {
        this.generator = gen;
        this.storeRepository = storeRepository;
        this.recordsToSend = records;
    }

    @Override
    public void run() {
        while (running) {
            items = storeRepository.buildRandomItems(recordsToSend);
            for (Item item : items)  {
                generator.sendMessage(item);  
                try {
                    Thread.currentThread().sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    running=false;
                }
            }
        }
        logger.info("Terminating runnable loop");    
    }
    
    public void stop(){
        this.running = false;
    }

    public List<Item> getCurrentItems(){
        return items;
    }
}
