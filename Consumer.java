package mas_1_4;

/**@author endryys*/
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Consumer extends Agent{

private float demandValue[];
private boolean end;
private AID[] powerManager; 

    protected void setup(){
    
        demandValue[0]=2000;
        demandValue[1]=300;
        demandValue[2]=3;

        end=false;
    
        // Register the consumer service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("demanda-PM");
        sd.setName("Cartera de consumos JADE");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }         
        addBehaviour(new SentDemand()); 
    }

    private class SentDemand extends Behaviour{
    

        private int count;
    
        public void action(){
    
            // Update the search of Power Manager
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("demanda-PM");
            template.addServices(sd);
            try {
                    DFAgentDescription[] pm = DFService.search(myAgent, template); 
                    if(pm.length>0){
                    System.out.println("Resultados de Power Manager:");
                    powerManager = new AID[pm.length];
                        for (int i = 0; i < pm.length; ++i) {
                            powerManager[i] = pm[i].getName();
                            System.out.println(powerManager[i].getName());                            
                        }
                    myAgent.addBehaviour(new DemandRequest());
                }else{
                    System.out.println("No se ha contactado con Power Manager... ");
                    count++;
                    if (count==8){
                        myAgent.doDelete();        
                     }
                                                  
                }
						
            }catch (FIPAException fe) {
                fe.printStackTrace();
            } 
        }

    
        public boolean done(){
            end=true;
            return end;
        }
    
    }
    
    private class DemandRequest extends Behaviour{
        
        private AID pm_id[];
        private String demandValue_Str[];
        private MessageTemplate mt;
        
        public void action(){
	
            // Send the inform to all power managers
            ACLMessage demand = new ACLMessage(ACLMessage.INFORM);
            for (int i = 0; i < powerManager.length; ++i) {
                demand.addReceiver(powerManager[i]);			            
            }
            
            //It's convert demandValue to String
            for(int i=0; i<demandValue.length;i++){
                demandValue_Str[i]=Float.toString(demandValue[i]);
            }
            
            //It's sent the demanded power
            pm_id=new AID[powerManager.length];
            demand.setContent(demandValue_Str[0]);
            demand.setConversationId("D_PM");
            demand.setReplyWith("demand"+System.currentTimeMillis()); // Unique value
            myAgent.send(demand);
            System.out.println("El consumidor envia Demanda: "+demand.getContent());
            
            // Prepare the template to get confirmation of solved demand, in other to sent the next value
            mt = MessageTemplate.and(MessageTemplate.MatchConversationId("D_PM"),MessageTemplate.MatchInReplyTo(demand.getReplyWith()));
            int j=0;
            
            while(j<demandValue.length){
                ACLMessage demandSolved = myAgent.receive(mt);
                if(demandSolved!=null){
                    
                    j++;
                    demand.setContent(demandValue_Str[j]);
                    demand.setConversationId("D_PM");
                    demand.setReplyWith("demand"+System.currentTimeMillis()); // Unique value
                    myAgent.send(demand);
                    System.out.println("El consumidor envia Demanda: "+demand.getContent());
                    
                    
                }else{
                    block();
                    
                }
                
            }
            
        }
        
        public boolean done(){
            end=true;
            return end;
        }
        
    }
    
    
    protected void takedown(){
    
    }
}
