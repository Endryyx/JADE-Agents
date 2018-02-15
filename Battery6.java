package mas_1_7;
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
    
    //Battery's attribute in order to send message mainly and interpret data
    
    private String p_nominal;
    private String capacity;
    private String soc;
    private Boolean charge,discharge;
    private String p_batt_output; 
    private String acD;
   
    /*Atributo  que debe determina los segundos cada cuando envía un punto de demanda, se define en 1800s
     Aunque este valor debe ser tomado de un textBox rellenado por el usuario en un formulario*/
     public float ti=1800; 
        
    protected void setup(){
        
        String p_nominal_Str;
        float p_nominal_;
        float capacity_;
        float soc_;
  
        BatteryAction batt_attr=new BatteryAction();
        p_nominal_=batt_attr.p_nominal_;
        capacity_=batt_attr.capacity_;
        soc_=batt_attr.soc_;
       
        DecimalFormat p1_generada_df = new DecimalFormat("0.00"); 
        p_nominal_Str=p1_generada_df.format(p_nominal_);  
        p_nominal=p_nominal_Str.replaceAll(",", ".");
        
      
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
		addBehaviour(new DemandInfo_Server());

		// Add the behaviour serving purchase orders from Power Manager agent
		addBehaviour(new OrderAction_Server());
                
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
    
    private class DemandInfo_Server extends CyclicBehaviour{
        
        public void action(){
            
                String batt_input;
                float p_diff,status_,threshold_;
                float soc_=0;
                String batt_name;
                float[] batt_info_=new float[2];
                String p_batt_info,soc_info,aCD_info;
               //It's prepared to receive the message with structure ACCEPT_PROPORSAL
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                ACLMessage msg = myAgent.receive(mt);
                
                if(msg!=null){
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
                  
                    p_diff=results[0];
                    status_=results[1];
                    threshold_=results[2]; 
                    
                    //PRUEBA UNITARIA
                    if(p_diff==221){
                        p_diff=221;
                    }
                    
                     // Set parameters to the information consume or supply and soc
                     BatteryAction cfp_batteries=new BatteryAction();
                     
                     float p_nominal_=Float.parseFloat(p_nominal);
                     soc_=Float.valueOf(soc);
                     batt_name=myAgent.getAID().getName();
                    
                     //Function "BATTERY_INFORMATION" that determines parameters p_batt_max, soc_info, aCD_info
                     /*p_batt_max, es la potencia máxima que puede ofrecer la batería según la demanda
                     es decir, la potencia máxima que puede ofrecer es la p_diff demandada por PM, si no
                     pudiera llegar a ese valor la p_batt_max=p_nominal y si la batería se encontrase
                     en un SOC tan mínimo que no pudiera llegar ni a p_diff ni a p_nominal entregará
                     aquella energía que le permita su soc hasta llegar al soc_min, lo mismo ocurriría a
                     la hora de consumir, pero con soc_max*/
                     
                     batt_info_=cfp_batteries.Battery_Information(p_diff, status_, threshold_, soc_, batt_name,ti);
                     
                     p_batt_info=Float.toString(batt_info_[0]);
                     soc_info=Float.toString(batt_info_[1]);
                     aCD_info=Float.toString(batt_info_[2]);

                     String batt_propose="["+p_batt_info+","+soc_info+","+aCD_info+"]";
                     ACLMessage reply_cfp = msg.createReply();
                     reply_cfp.setConversationId("PM_BATT");
                     reply_cfp.setPerformative(ACLMessage.PROPOSE);
                     reply_cfp.setContent(String.valueOf(batt_propose));
                     System.out.println("Batería " +batt_name+" envía la propuesta : "+batt_propose+"\n");
                     myAgent.send(reply_cfp); 
                      
                     //myAgent.doWait(1000);
                }
        }
    }    
    
    //Implementation fo behaviours
    private class OrderAction_Server extends CyclicBehaviour{
    
            public void action(){
                
                String batt_input;
                float p_diff,status_,threshold_;
                int status=0,threshold=0;
                float soc_max,soc_min,soc_upper,soc_lower,soc_current;
                float ti=1800; //The time interval always is 30 minutes
                float p_abs,p_deliver,p_charge,p_discharge;
                float soc_,soc_output;
                float p_batt_output_=0;
                float p_nominal_,p_batt_max;
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
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
                ACLMessage msg = myAgent.receive(mt); 
                
                if (msg != null) {
                    
                   // CFP Message received. Process it
                    batt_input = msg.getContent();
                    System.out.println("La bateria "+getAID().getName()+" ha recibido ACCEPT PROPOSAL del PM con unos datos de: "+batt_input );
                    
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
                    
                    //PRUEBA UNITARIA
                    if(p_diff==221){
                        p_diff=221;
                    }
                    
                    
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
                            
                            //Se calcula la potencia máxima que puede entregar
                            p_batt_max=process.PowerDischargingTillSOCmin(ti, soc_);
                            
                            System.out.println("La batería "+myAgent.getName()+" entra en fase de entrega de energía");
                            
                            if(soc_<=soc_min){
                                
                               System.out.println("La bateria "+myAgent.getName()+" tiene un nivel demasiado bajo, no puede entregar energía.");
                               p_batt_output_=0;
                               soc_output=soc_;
                               
                            }else{
                                
                                if(p_diff>=p_nominal_){
                                    
                                    p_batt_output_=p_nominal_;
                                    soc_output=process.DischargingStatus(p_batt_output_,ti,soc_);
                                    if(soc_output<1.03*soc_min){
                                        soc_output=soc_min;
                                        p_batt_output_=process.PowerDischargingTillSOCmin( ti, soc_);
                                    }
                                    
                                }else{
                                    p_batt_output_=p_diff;
                                    soc_output=process.DischargingStatus(p_batt_output_,ti,soc_);
                                    if(soc_output<1.03*soc_min){
                                        soc_output=soc_min;
                                        p_batt_output_=process.PowerDischargingTillSOCmin( ti, soc_);
                                    }
                                }
                            }
                            
                            break;
                            
                        case 2://Battery consum energy
                            
                            System.out.println("La batería "+myAgent.getName()+" entra en fase de consumo de energía");
                            
                            if(soc_>=soc_max){
                               System.out.println("La bateria "+myAgent.getName()+" tiene un nivel demasiado alto, no puede absorber más energía.");
                               p_batt_output_=0;
                               soc_output=soc_;
                            }else{
                                p_diff=-p_diff;
                                if(p_diff>=p_nominal_){
                                    p_batt_output_=p_nominal_;
                                    soc_output=process.ChargingStatus(p_batt_output_,ti,soc_);
                                    if(soc_output>1.03*soc_max){
                                        soc_output=soc_max;
                                        p_batt_output_=process.PowerChargingTillSOCmax(ti,soc_);
                                    }
                                        
                                }else{
                                    p_batt_output_=p_diff;
                                    soc_output=process.ChargingStatus(p_batt_output_,ti, soc_);
                                    if(soc_output>1.03*soc_max){
                                        soc_output=soc_max;
                                        p_batt_output_=process.PowerChargingTillSOCmax(ti,soc_);
                                    }                                   
                                }
                            }    
                            
                            break;
                            
                        case 3://Battery automatic charge/discharge
                            
                            if(charge==true){
                                System.out.println("La batería "+myAgent.getName()+" entra en fase de carga automática");
                                if(threshold==0 || threshold==-1){
                                    
                                    p_charge=(p_nominal_+p_nominal_*((soc_lower-soc_)/(soc_lower-soc_min)))/2;
                                    if(p_charge<=p_diff){
                                        soc_output=process.ChargingStatus(p_charge,ti,soc_);
                                        aCD=1;
                                        if(p_charge<0){p_charge=-p_charge;}    
                                            p_batt_output_=p_charge;
                                            System.out.println(myAgent.getName()+" absorbe una potencia: "+p_batt_output_ +" y se queda en SOC: "+soc_output);
                                            //Se deja en cero p_batt_output, ya que en este caso no debe consumir energia del sistema dado que esta dentro de los limites
                                            //p_batt_output_=0;
                                    }else{
                                        p_charge=p_diff;
                                        aCD=1;
                                        if(p_charge<0){p_charge=-p_charge;}
                                            soc_output=process.ChargingStatus(p_charge,ti, soc_);
                                            p_batt_output_=p_charge;
                                            System.out.println(myAgent.getName()+" absorbe una potencia: "+p_batt_output_ +" y se queda en SOC: "+soc_output);  
                                    }
                                }else{
                                    System.out.println("Paralizado el proceso de carga automática de "+myAgent.getName()+" por haber alcanzado límite superior.\n");
                                    soc_output=soc_;
                                    p_batt_output_=0;
                                    aCD=0;
                                }
                            }
                            if(discharge==true){
                                System.out.println("La batería "+myAgent.getName()+"entra en fase de descarga automática");
                                if(threshold==0 || threshold==1){
                                    p_discharge=(p_nominal_+p_nominal_*((soc_-soc_upper)/(soc_max-soc_upper)))/2;
                                    if(p_discharge<=p_diff){
                                    aCD=-1;
                                    if(p_discharge<0){p_discharge=-p_discharge;} 
                                        soc_output=process.DischargingStatus(p_discharge,ti,soc_);
                                        p_batt_output_=p_discharge;
                                        System.out.println(myAgent.getName()+" cede una potencia: "+p_batt_output_ +" y se queda en SOC: "+soc_output);
                                        //Se deja en cero p_batt_output, ya que en este caso no debe entregar energia del sistema dado que esta dentro de los limites
                                        //p_batt_output_=0;
                                    }else{
                                        p_discharge=p_diff;              
                                        aCD=-1;    
                                        if(p_discharge<0){p_discharge=-p_discharge;} 
                                        soc_output=process.DischargingStatus(p_discharge,ti,soc_);
                                        p_batt_output_=p_discharge;
                                        System.out.println(myAgent.getName()+" cede una potencia: "+p_batt_output_ +" y se queda en SOC: "+soc_output);
                                        
                                    }
                                }else{
                                    System.out.println("Paralizado el proceso de descarga automática de "+myAgent.getName()+" por haber alcanzado límite inferior.\n");
                                    soc_output=soc_;
                                    p_batt_output_=0;
                                    aCD=0;
                                }                                
                            }
                            //Fase de carga y descarga eficiente
                            /*Esta fase entra cuando el SOC actual de la batería esta dentro de los rangos establecidos, que no tiene la necesidad de carga/descarga automática
                            y puede aportar o consumir del sistema para amortiguar más la curva Pcc_final.*/
                            if(charge==false && discharge==false){
                                System.out.println("Batería "+myAgent.getName()+" parada, SOC adecuado.");
                                p_batt_output_=0;
                                soc_output=soc_;
                                aCD=0;                               
                            }
                        
                            break;
                                
                    }
                     p_batt_output=Float.toString(p_batt_output_);
                     soc=Float.toString(soc_output);
                     acD=Float.toString(aCD);
                    
                    // Set parameters to the propose
                     String action="["+p_batt_output+","+soc+","+acD+"]";
                     ACLMessage reply_action = msg.createReply();
                     reply_action.setConversationId("PM_BAT_Action");
                     reply_action.setPerformative(ACLMessage.INFORM);
                     reply_action.setContent(String.valueOf(action));
                     myAgent.send(reply_action); 
                     System.out.println("Batería "+myAgent.getName()+" envía la acción : "+action+"\n");
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