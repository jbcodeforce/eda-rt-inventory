package ibm.gse.eda.stores.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Random;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Item {
        public static String RESTOCK = "RESTOCK";
        public static String SALE = "SALE";
        public long id;
        public String storeName;
        public String sku;
        public int quantity;
        public String type;
        public Double price;
        public String timestamp;


        static transient Random random = new Random();
        static transient int maxQuantity = 10;
        static transient int maxPrice = 140;
        static transient String[] skus = { "Item_1", "Item_2", "Item_3", "Item_4", "Item_5", "Item_6","Item_7"};
 
        public Item() {
        }

        public Item(long id,String store, String sku, String type, int quantity, double price) {
                this.id = id;
                this.storeName = store;
                this.sku = sku;
                this.type = type;
                this.quantity = quantity;
                this.price = price;
                this.timestamp = LocalDateTime.now().toString();
        }

        public Item(String store, String sku, String type, int quantity, double price) {
                this.storeName = store;
                this.sku = sku;
                this.type = type;
                this.quantity = quantity;
                this.price = price;
                this.timestamp = LocalDateTime.now().toString();
        }

        public Item(String store, String sku, String type, int quantity) {
                this.storeName = store;
                this.sku = sku;
                this.type = type;
                this.quantity = quantity;
                this.timestamp = LocalDateTime.now().toString();
        }

        public String toString() {
                Jsonb jsonb = JsonbBuilder.create();
                return jsonb.toJson(this);
        }

        public static Item buildRandomItem(long i,String[] stores) {
                Item item = new Item();
                item.id= i;
                item.storeName = stores[random.nextInt(stores.length)];
                item.quantity = random.nextInt(maxQuantity);
                item.sku = skus[random.nextInt(skus.length)];
                item.type = random.nextDouble() > 0.5 ? Item.RESTOCK : Item.SALE;
                BigDecimal bd = BigDecimal.valueOf(random.nextDouble() * maxPrice);
                bd = bd.setScale(2, RoundingMode.HALF_UP);
                item.price = bd.doubleValue();
                item.timestamp = LocalDateTime.now().toString();
                return item;
        }
}