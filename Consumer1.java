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

private float[] demandValue;
private boolean end;
private AID[] powerManager; 

    protected void setup(){
        demandValue=new float[3];
        demandValue[0]=2000;
        demandValue[1]=300;
        demandValue[2]=3;
        
        //It's register in other to be localizated by Power Manager and receiving the confirmation the demand point
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
	ServiceDescription sd = new ServiceDescription();
	sd.setType("confirmacion-PM");
	sd.setName("Cartera de inversión JADE");
	dfd.addServices(sd);
	try {
            DFService.register(this, dfd);
	}catch (FIPAException fe) {
            fe.printStackTrace();
	}       
      
        addBehaviour(new SearchPM()); 
    }
    private class SearchPM extends Behaviour{
        
        private int count;
        
        public void action(){
            //Search the PM
            DFAgentDescription template = new DFAgentDescription();//DF template to generators		
            ServiceDescription sd = new ServiceDescription();  //Service description to generators                
            sd.setType("demanda-PM");                
            template.addServices(sd);                             
		
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template); 

                if(result.length>0){
                    System.out.println("Se encuentran los siguientes Power Manager:");
                    powerManager = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        powerManager[i] = result[i].getName();
                        System.out.println(powerManager[i].getName());                            
                    }
                    myAgent.addBehaviour(new DemandRequest());
                }else{
                    System.out.println("Esperando a que haya PMs... ");
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
    
    private class DemandRequest extends CyclicBehaviour{
        
        private AID pm_id[];
        private String[] demandValue_Str=new String[3];
        private MessageTemplate mt;
        private int step=0;
        private int j=0;
        
        public void action(){
	
            switch(step){
                
                case 0:
                 
                    // Send the inform to all power managers
                    ACLMessage request= new ACLMessage(ACLMessage.REQUEST);
                    for (int i = 0; i < powerManager.length; ++i) {
                        request.addReceiver(powerManager[i]);			            
                     }
            
                     //It's convert demandValue to String
                    for(int i=0; i<demandValue.length;i++){
                        demandValue_Str[i]=Float.toString(demandValue[i]);
                     }
                    //It's sent the demanded power
                    pm_id=new AID[powerManager.length];
                    request.setContent(demandValue_Str[j]);
                    request.setConversationId("D_PM");
                    request.setReplyWith("demand"+System.currentTimeMillis()); // Unique value
                    myAgent.send(request);
                    System.out.println("El consumidor envia Demanda: "+request.getContent());
                    
                    // Prepare the template to get confirmation of solved demand, in other to sent the next value
                    mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
                    
                    ACLMessage commit_demand=myAgent.receive(mt);
                    if(commit_demand!=null){
                        j++;

                        if(j<demandValue.length){
                           ACLMessage reply_commit=commit_demand.createReply();
                           reply_commit.setPerformative(ACLMessage.AGREE);
                           myAgent.send(reply_commit);
                           step=0; 
                        }else{
                           ACLMessage reply_commit=commit_demand.createReply();
                           reply_commit.setPerformative(ACLMessage.CANCEL);
                           myAgent.send(reply_commit); 
                           step=1;
                           myAgent.doDelete();
                        }
                    }else{
                        step=0;
                        block();
                        break;
                    }
                    break;
                
                case 1:
                    
                    //ACLMessage reply_commit=commit_demand.createReply();
                    /*ACLMessage commit_demand=myAgent.receive(mt);
                    if(commit_demand!=null){
                        j++;
                        if(j<demandValue.length){
                           step=0; 
                        }else{
                            myAgent.doDelete();
                        }
                    }else{
                        block();
                    }*/
       
                    break;
            }
        }
        
       /*public boolean done(){
            end=true;
            return end;
        }*/
        
    }
    
    
    protected void takedown(){
        System.out.println("El agente consumidor ha finalizado\n");
    }
}
