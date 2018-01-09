package mas_1_5;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**@author endryys*/
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
    
    protected void takedown(){
        System.out.println("Fin del consumidor\n");
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
        private float demandCurrent_j=0;
        private String demandCurrent_j_Str;
        
        public void action(){
	
            if(j==0){
                
              // Define the type of message
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
               request.setContent(demandValue_Str[0]);
               request.setConversationId("D_PM");
               request.setReplyWith("request"+System.currentTimeMillis()); // Unique value
               System.out.println("El consumidor envia Demanda: "+request.getContent());
               myAgent.send(request);
               
               j++;
              
            }
            
            if(j>0){
              
             /*If it have just sent message of first consum, consumer is subject to receive a commit's message*/
             // myAgent.doWait(8000);
              // Prepare the template to get confirmation of solved demand, in other to sent the next value
               mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
              ACLMessage commit_demand=myAgent.receive(mt);
               
               if(commit_demand!=null){
               
                    System.out.println("El consumidor "+myAgent.getName()+" ha recibido la confirmación "+commit_demand.getContent());

                   /* ACLMessage reply_commit=commit_demand.createReply();
                    reply_commit.setPerformative(ACLMessage.AGREE);
                    myAgent.send(reply_commit);*/
                    
                     if(j>=demandValue.length){
                           // reply_commit.setPerformative(ACLMessage.CANCEL);
                            //myAgent.doWait(10000);
                            
                            System.out.println("El consumidor "+myAgent.getName()+" ha finalizado\n.");
                            //myAgent.send(reply_commit);
                            System.out.println("El consumidor "+myAgent.getName()+" envia la orden de finalizar a Power Manager\n.");
                            //myAgent.doWait(10000);
          
                             myAgent.doDelete();
                         /*}else{
                            block();
                         }  */
                     }
                     
                    if(j<demandValue.length){
                        
                        // Define the type of message
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
                        System.out.println("\nEl consumidor envia Demanda: "+request.getContent());
                        myAgent.send(request);
                        //myAgent.doWait(5000);
                        j++;
                        block();
                    }
                        
             }        
         }
        
      }
        
  }
    
}
