
package mas_1_5;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.text.DecimalFormat;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
/**
 *
 * @author endryys
 */
public class Battery extends Agent {
    private String p_nominal;
    private String capacity;
    private String soc;
    private Boolean charge,discharge;
    private String p_batt_output; 
            
    protected void setup(){
        
        String p_nominal_Str;
        float p_nominal_;
        float capacity_;
        float soc_;
  
        
       /*It's generated a random number of battery's power nominal with an 
        upper threshold of 150(kW)*/
        p_nominal_=(float) (Math.random() * 150) + 1;
        DecimalFormat p1_generada_df = new DecimalFormat("0.00"); 
        p_nominal_Str=p1_generada_df.format(p_nominal_);  
        p_nominal=p_nominal_Str.replaceAll(",", ".");
        
        capacity_=1000;
        soc_=35;
        capacity=String.valueOf(capacity_);
        //La misma conversion que lo de arriba pero de otra manera
        soc=Float.toString(soc_);
        
        // Register the battery service in the yellow pages
        DFAgentDescription dfd_batt = new DFAgentDescription();
	dfd_batt.setName(getAID());
	ServiceDescription sd_batt = new ServiceDescription();
	sd_batt.setType("pcc-baterias");
	sd_batt.setName("Cartera de inversión 2 JADE");
	dfd_batt.addServices(sd_batt);
	try {
            DFService.register(this, dfd_batt);
	}
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
            
        System.out.println("Agente-Bateria "+getAID().getName()+" tiene las caracteríticas:");
        System.out.println("Pnominal: "+p_nominal+"(kW) "+"insertados en DF.");
        System.out.println("Capacidad: "+capacity+"(kWh) "+"insertados en DF.");
        System.out.println("SOC: "+soc+"(%)"+" insertados en DF.\n");     
        
		// Add the behaviour serving queries from Power Manager agent
		addBehaviour(new DemandaRespuestaServidor());

		// Add the behaviour serving purchase orders from Power Manager agent
		//addBehaviour(new OrdenesSuministroServidor());        
       
    }
    protected void takeDown(){
    // Deregister from the yellow pages
	try {
            DFService.deregister(this);
        }
	catch (FIPAException fe) {
            fe.printStackTrace();
	}

	System.out.println("Agente Batería "+getAID().getName()+" terminado.\n\n");        
    }
    
    //Implementation fo behaviours
    private class DemandaRespuestaServidor extends CyclicBehaviour{
    
            public void action(){
                
                String batt_input;
                float p_diff,status_;
                int status=0;
                float soc_max,soc_min,soc_upper,soc_lower,soc_current;
                float ti=1800; //The time interval always is 30 minutes
                float p_abs,p_deliver,p_charge,p_discharge;
                float soc_,soc_output;
                float p_batt_output_=0;
                float p_nominal_;
                float capacity_;
                
                capacity_=Float.parseFloat(capacity);
                p_nominal_=Float.parseFloat(p_nominal); 
                soc_=Float.valueOf(soc);
                soc_min=5;
                soc_max=98;
                soc_upper=40;
                soc_lower=30;
                soc_output=0;
                
                //It's prepared to receive the message with structure CFP
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                ACLMessage msg = myAgent.receive(mt); 
                
                if (msg != null) {
                    
                   // CFP Message received. Process it
                    batt_input = msg.getContent();
                    System.out.println("La bateria "+getAID().getName()+" ha recibido CFP del PM con unos datos de: "+batt_input );
                    
                    //It's convert the variable String to float
                    String arr =  batt_input;
                    String[] items = arr.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
                    float[] results = new float[items.length];

                    for (int i = 0; i < items.length; i++) {
                        try {                   
                            results[i]=Float.parseFloat(items[i]);
                        } catch (NumberFormatException nfe) {
                         //NOTE: write something here if you need to recover from formatting errors
                        }
                    }                    
                    //String battery_data="["+p_generada+","+price+"]";
                    p_diff=results[0];
                    status_=results[1];
                    if(status_==1.0){status=1;}
                    if(status_==2.0){status=2;}
                    
                    if(status_==3.0){
                        status=3;
                        if (soc_<soc_lower){
                            
                            charge=true;
                        }
                        if(soc_>soc_upper){
                            
                            discharge=true;
                        }
                        if(soc_>=soc_lower && soc_<=soc_upper){
                            
                            charge=false;
                            discharge=false;
                        }
                    
                    }
                    System.out.println("Los resultados float son: p_diff= "+p_diff+"  status= "+status);
                    
                    
                    BatteryAction process=new BatteryAction();
                    switch(status){
                        case 1://Battery deliver energy
                            System.out.println("La batería entra en fase de entrega de energía");
                            if(soc_<=soc_min){
                               System.out.println("La bateria tiene un nivel demasiado bajo, no puede entregar energía.");
                               p_batt_output_=0;
                               soc_output=soc_;
                            }
                            if(p_diff>=p_nominal_){
                                soc_output=process.DischargingStatus(p_nominal_,ti,capacity_, soc_min, soc_);
                                p_batt_output_=p_nominal_;
                            }else{
                                p_batt_output_=p_diff;
                                soc_output=soc_;
                            }
                            
                            break;
                            
                        case 2://Battery consum energy
                            System.out.println("La batería entra en fase de consumo de energía");
                            p_diff=-p_diff;
                            if(soc_>=soc_max){
                               System.out.println("La bateria tiene un nivel demasiado alto, no puede absorber más energía.");
                               p_batt_output_=0;
                               soc_output=soc_;
                            }else{
                            
                                if(p_diff>=p_nominal_){
                                    p_batt_output_=p_nominal_;
                                    soc_output=process.ChargingStatus(p_nominal_,ti,capacity_, soc_max, soc_);
                                        
                                }else{
                                    p_batt_output_=p_diff;
                                    soc_output=soc_;
                                }
                            }    
                            
                            break;
                            
                        case 3://Battery automatic charge/discharge
                            
                            if(charge==true){
                                System.out.println("La batería entra en fase de carga automática");
                                p_charge=(p_nominal_+p_nominal_*((soc_lower-soc_)/(soc_lower-soc_min)))/2;
                                soc_output=process.ChargingStatus(p_charge,ti,capacity_, soc_max, soc_);
                                p_batt_output_=p_charge;
                            }
                            if(discharge==true){
                                System.out.println("La batería entra en fase de descarga automática");
                                p_discharge=(p_nominal_+p_nominal_*((soc_-soc_upper)/(soc_max-soc_upper)))/2;
                                soc_output=process.DischargingStatus(p_discharge,ti,capacity_, soc_min, soc_);
                                p_batt_output_=p_discharge;                                
                            }
                            if(charge==false && discharge==false){
                               
                                if (p_diff!=0){
                                    if(p_diff>0){
                                        System.out.println("La batería entra en fase de descarga eficiente"); 
                                        p_discharge=-p_diff;
                                        soc_output=process.DischargingStatus(p_discharge,ti,capacity_, soc_min, soc_);
                                        p_batt_output_=p_discharge;                                        
                                    }else{
                                        System.out.println("La batería entra en fase de carga eficiente");
                                        p_charge=p_diff;
                                        soc_output=process.ChargingStatus(p_charge,ti,capacity_, soc_max, soc_);
                                        p_batt_output_=p_charge;
                                    }
                                }else{
                                p_batt_output_=0;
                                soc_output=soc_;
                                }
                            }
                        
                            break;
                                
                    }
                     p_batt_output=Float.toString(p_batt_output_);
                     soc=Float.toString(soc_output);
                    
                    // Set parameters to the propose
                     String propuesta="["+p_batt_output+","+soc+"]";
                     ACLMessage respuesta = msg.createReply();
                     respuesta.setConversationId("PM_BATT");
                     respuesta.setPerformative(ACLMessage.PROPOSE);
                     respuesta.setContent(String.valueOf(propuesta));
                     myAgent.send(respuesta); 
                     System.out.println("\nBatería envía la propuesta : "+propuesta+"\n");
                }
            }
    }    
}
