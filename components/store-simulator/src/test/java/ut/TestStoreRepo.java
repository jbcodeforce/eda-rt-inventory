package ut;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ibm.gse.eda.stores.domain.Store;
import ibm.gse.eda.stores.infra.StoreRepository;
import io.smallrye.mutiny.Multi;


public class TestStoreRepo {
    static StoreRepository repo = new StoreRepository();

    @Test
    public void testGetStores(){
        Multi<Store> stores = repo.getStores();
        Assertions.assertNotNull(stores);
        stores.subscribe().with( i -> System.out.println(i.name + " " + i.city));
    }

    @Test
    public void testGetStoreNames(){
        
        String[] stores = repo.getStoreNames();
        Assertions.assertNotNull(stores);
        Assertions.assertTrue(stores.length > 2);
    }
}
