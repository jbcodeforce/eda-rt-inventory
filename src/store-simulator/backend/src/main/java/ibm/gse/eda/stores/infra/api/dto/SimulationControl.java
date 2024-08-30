package ibm.gse.eda.stores.infra.api.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class SimulationControl {
    
    public String backend;
    public int records;
    public String type;

    public SimulationControl(){}

    public SimulationControl(int nbRecords,String type){
        this.records = nbRecords;
        this.type = type;
    }

    public SimulationControl(String back,int nbRecords,String type){
        this.records = nbRecords;
        this.type = type;
        this.backend = back;
    }

    @Override
    public String toString() {
        return "Backend: " + backend + " type: " + type + " with " + records + " records";
    }
}
