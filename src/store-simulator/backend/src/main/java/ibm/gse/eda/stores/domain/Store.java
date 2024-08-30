package ibm.gse.eda.stores.domain;

public class Store {
   public String name;
   public String city;
   public String state;
   public String zipcode;
   
   public Store(){}

   public Store(String name, String city, String state, String zipcode) {
      this.name = name;
      this.city = city;
      this.state = state;
      this.zipcode = zipcode;
   }
}
