
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
    private String acD;
            
    protected void setup(){
        
        String p_nominal_Str;
        float p_nominal_;
        float capacity_;
        float soc_;
  
        
       /*It's generated a random number of battery's power nominal with an 
        upper threshold of 150(kW)*/
        //p_nominal_=(float) (Math.random() * 150) + 1;
        p_nominal_=(float)149.46;
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
                
                //Add the behaviour for finish software
                addBehaviour(new FinishMessage());
       
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
                float p_diff,status_,threshold_;
                int status=0,threshold=0;
                float soc_max,soc_min,soc_upper,soc_lower,soc_current;
                float ti=1800; //The time interval always is 30 minutes
                float p_abs,p_deliver,p_charge,p_discharge;
                float soc_,soc_output;
                float p_batt_output_=0;
                float p_nominal_;
                float capacity_;
               float aCD;
                
               aCD=0;
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
                    threshold_=results[2];
                    
                    //if(p_diff<0){p_diff=-p_diff;}
                    if(status_==1.0){status=1;}
                    if(status_==2.0){status=2;}
                    if(threshold_==-1.0){threshold=-1;}
                    if(threshold_==0.0){threshold=0;} 
                    if(threshold_==1.0){threshold=1;} 
                    //El status 3 entra solo cuando soc no está fuera de los limites de soc_lower y soc_upper
                    //y cuando la pcc_initial se encuentra dentro de los límites del PeakShaving
                    if(status_==3.0){
                        status=3;
                        if (soc_<soc_lower){
                          
                            charge=true;
                            discharge=false;
                        }
                        if(soc_>soc_upper){
                           
                            discharge=true;
                            charge=false;
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
                            }else{
                                
                                if(p_diff>=p_nominal_){
                                    p_batt_output_=p_nominal_;
                                    soc_output=process.DischargingStatus(p_batt_output_,ti,capacity_, soc_min, soc_);
                                    
                                }else{
                                    p_batt_output_=p_diff;
                                    soc_output=process.DischargingStatus(p_batt_output_,ti,capacity_, soc_min, soc_);
                                }
                            }
                            
                            break;
                            
                        case 2://Battery consum energy
                            System.out.println("La batería entra en fase de consumo de energía");
                            
                            if(soc_>=soc_max){
                               System.out.println("La bateria tiene un nivel demasiado alto, no puede absorber más energía.");
                               p_batt_output_=0;
                               soc_output=soc_;
                            }else{
                                p_diff=-p_diff;
                                if(p_diff>=p_nominal_){
                                    p_batt_output_=p_nominal_;
                                    soc_output=process.ChargingStatus(p_batt_output_,ti,capacity_, soc_max, soc_);
                                        
                                }else{
                                    p_batt_output_=p_diff;
                                    soc_output=process.ChargingStatus(p_batt_output_,ti,capacity_, soc_max, soc_);
                                }
                            }    
                            
                            break;
                            
                        case 3://Battery automatic charge/discharge
                            
                            if(charge==true){
                                System.out.println("La batería entra en fase de carga automática");
                                if(threshold==0 || threshold==-1){
                                    
                                    p_charge=(p_nominal_+p_nominal_*((soc_lower-soc_)/(soc_lower-soc_min)))/2;
                                    if(p_charge<=p_diff){
                                        soc_output=process.ChargingStatus(p_charge,ti,capacity_, soc_max, soc_);
                                        aCD=1;
                                        if(p_charge<0){p_charge=-p_charge;}    
                                            p_batt_output_=p_charge;
                                            System.out.println("\nAbsorbe una potencia: "+p_batt_output_ +" y se queda en SOC: "+soc_output);
                                            //Se deja en cero p_batt_output, ya que en este caso no debe consumir energia del sistema dado que esta dentro de los limites
                                            //p_batt_output_=0;
                                    }else{
                                        p_charge=p_diff;
                                        aCD=1;
                                        if(p_charge<0){p_charge=-p_charge;}
                                            soc_output=process.ChargingStatus(p_charge,ti,capacity_, soc_max, soc_);
                                            p_batt_output_=p_charge;
                                            System.out.println("\nAbsorbe una potencia: "+p_batt_output_ +" y se queda en SOC: "+soc_output);  
                                    }
                                }else{
                                    System.out.println("Paralizado el proceso de carga automática por haber alcanzado límite superior.\n");
                                    soc_output=soc_;
                                    p_batt_output_=0;
                                    aCD=0;
                                }
                            }
                            if(discharge==true){
                                System.out.println("La batería entra en fase de descarga automática");
                                if(threshold==0 || threshold==1){
                                    p_discharge=(p_nominal_+p_nominal_*((soc_-soc_upper)/(soc_max-soc_upper)))/2;
                                    if(p_discharge<=p_diff){
                                    aCD=-1;
                                    if(p_discharge<0){p_discharge=-p_discharge;} 
                                        soc_output=process.DischargingStatus(p_discharge,ti,capacity_, soc_min, soc_);
                                        p_batt_output_=p_discharge;
                                        System.out.println("\nCede una potencia: "+p_batt_output_ +" y se queda en SOC: "+soc_output);
                                        //Se deja en cero p_batt_output, ya que en este caso no debe entregar energia del sistema dado que esta dentro de los limites
                                        //p_batt_output_=0;
                                    }else{
                                        p_discharge=p_diff;
                                        soc_output=process.DischargingStatus(p_discharge,ti,capacity_, soc_min, soc_);
                                        aCD=-1;    
                                        if(p_discharge<0){p_discharge=-p_discharge;} 
                                        p_batt_output_=p_discharge;
                                        System.out.println("\nCede una potencia: "+p_batt_output_ +" y se queda en SOC: "+soc_output);
                                        
                                    }
                                }else{
                                    System.out.println("Paralizado el proceso de descarga automática por haber alcanzado límite inferior.\n");
                                    soc_output=soc_;
                                    p_batt_output_=0;
                                    aCD=0;
                                }                                
                            }
                            //Fase de carga y descarga eficiente
                            /*Esta fase entra cuando el SOC actual de la batería esta dentro de los rangos establecidos, que no tiene la necesidad de carga/descarga automática
                            y puede aportar o consumir del sistema para amortiguar más la curva Pcc_final.*/
                            if(charge==false && discharge==false){
                               
                                if (p_diff!=0){
                                    if(p_diff>0){
                                        System.out.println("La batería entra en fase de descarga eficiente"); 
                                        p_discharge=p_diff;
                                        soc_output=process.DischargingStatus(p_discharge,ti,capacity_, soc_min, soc_);
                                        aCD=0;
                                        p_batt_output_=p_discharge;                                        
                                    }else{
                                        System.out.println("La batería entra en fase de carga eficiente");
                                        p_charge=p_diff;
                                        soc_output=process.ChargingStatus(p_charge,ti,capacity_, soc_max, soc_);
                                        p_batt_output_=p_charge;
                                        aCD=0;
                                    }
                                }else{
                                p_batt_output_=0;
                                soc_output=soc_;
                                aCD=0;
                                }
                            }
                        
                            break;
                                
                    }
                     p_batt_output=Float.toString(p_batt_output_);
                     soc=Float.toString(soc_output);
                     acD=Float.toString(aCD);
                    
                    // Set parameters to the propose
                     String propuesta="["+p_batt_output+","+soc+","+acD+"]";
                     ACLMessage respuesta = msg.createReply();
                     respuesta.setConversationId("PM_BATT");
                     respuesta.setPerformative(ACLMessage.PROPOSE);
                     respuesta.setContent(String.valueOf(propuesta));
                     myAgent.send(respuesta); 
                     System.out.println("\nBatería envía la propuesta : "+propuesta+"\n");
                }
            }
    }

        private class FinishMessage extends CyclicBehaviour{
            
            private MessageTemplate mt; // The template to receive replies
       
            public void action() {
                mt=MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
                ACLMessage finish=receive(mt);
                
                    if(finish!=null){
                        System.out.println();
                        myAgent.doDelete();
                    }    
            }
        }     
}
