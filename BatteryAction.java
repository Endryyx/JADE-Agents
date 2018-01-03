package mas_1_4;

/** @author endryys*/
public class BatteryAction {
    
    public float ChargingStatus(float power,float interval, float capacity,float soc_max,float soc ){
        float soc_output;
        float p_charge,e_charge,e_capacity,e_available;
        
        p_charge=power;
        e_charge=p_charge*interval;
        e_capacity=capacity*3600;
        e_available=(soc_max-soc)*e_capacity/100;
        soc_output=(1+(e_charge/(e_capacity-e_available)))*soc;
       
        return soc_output;
    }
    
    public float DischargingStatus(float power,float interval, float capacity,float soc_min,float soc){
        float soc_output;
        float p_discharge,e_discharge,e_capacity,e_available;
        p_discharge=power;
        e_discharge=p_discharge*interval;
        e_capacity=capacity*3600;
        e_available=(soc-soc_min)*e_capacity/100;
        soc_output=(1-(e_discharge/e_available))*soc;
        
        return soc_output;
    }
}