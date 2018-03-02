package mas_1_10;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.*;

/**@author endryys*/
public class Consumer2 extends Agent{

//public Consumer consumer=this;

private float[] demandValue;
private boolean end;
private AID[] powerManager; 
private int j=0;// Auxiliar variable that serve to instance DemandRequest Behavoiur or finishing the Agent
private String[] demandValue_Str;


/*Atributo  que debe determina los segundos cada cuando envía un punto de demanda, se define en 1800s
Aunque este valor debe ser tomado de un textBox rellenado por el usuario en un formulario*/
public float ti=1800; 

    protected void setup(){
        
        System.out.println("Bienvenido consumidor "+this.getName()+".");
        
        demandValue=new float[47];
        demandValue_Str=new String[47];
        String path=new String();
        path="/home/endryys/Datos_REE2.csv";
        
        ImportCSV import_csv=new ImportCSV();
       
        try{
            
            demandValue_Str=import_csv.ImportFile(path);
            
        }catch(IOException ioe){
            // Error al crear el archivo, por ejemplo, el archivo 
            // está actualmente abierto.
            ioe.printStackTrace();
        }
         
        demandValue[0]=1000;
        demandValue[1]=800;
        demandValue[2]=1000;
        demandValue[3]=900;
        demandValue[4]=1150;
        demandValue[5]=1400;
        demandValue[6]=1550;
        /*demandValue[0]=1600;
        demandValue[1]=1550;
        demandValue[2]=1450;
        demandValue[3]=1500;
        demandValue[4]=1450;
        demandValue[5]=1400;
        demandValue[6]=1450;*/
        demandValue[7]=1350;
        demandValue[8]=1250;
        demandValue[9]=1000;
        demandValue[10]=1500;
        demandValue[11]=1450;
        demandValue[12]=1000;
        demandValue[13]=1800;
        demandValue[14]=1900;
        demandValue[15]=1350;
        demandValue[16]=1900;
        demandValue[17]=2000;
        demandValue[18]=1700;
        demandValue[19]=1750;
        demandValue[20]=1800;
        demandValue[21]=1850;
        demandValue[22]=1751;
        demandValue[23]=1655;
        demandValue[24]=1550;
        demandValue[25]=1800;
        demandValue[26]=1753;
        demandValue[27]=1700;
        demandValue[28]=1600;
        demandValue[29]=1510;
        
        
        //It's convert demandValue to String
        for(int i=0; i<demandValue.length;i++){ 
            //demandValue_Str[i]=Float.toString(demandValue[i]);
            System.out.println(demandValue_Str[i]);
        }
        
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
      /*  this.doWait(1000);
        
        gui=new GUI(this);
        gui.showGui();*/
        
    }
   /* public Consumer ExtractAgent(AID agent){
        
        Consumer consumer_agent=new Consumer();
        
        if(agent==this.getAID()){
            
            consumer_agent=this;
        }
        
        return consumer_agent;
    }
    
    public void Start_System(){     
            gui.dispose();
            addBehaviour(new SearchPM());       
    }*/
   /* protected void takedown(){
        System.out.println("Fin del consumidor\n");
    }*/
    
    private class SearchPM extends Behaviour{
        
        private int count;
        DFAgentDescription[] result1=new DFAgentDescription[1]; 
        
        public void action(){
            
            //Search the PM
            DFAgentDescription template = new DFAgentDescription();//DF template to generators		
            ServiceDescription sd = new ServiceDescription();  //Service description to generators                
            sd.setType("demanda-PM");                
            template.addServices(sd);                             
		
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template); 
                result1=result;
                if(result.length>0){
                    
                    System.out.println("Se encuentran los siguientes Power Manager:");
                    powerManager = new AID[result.length];
                    
                    for (int i = 0; i < result.length; ++i) {
                        
                        powerManager[i] = result[i].getName();
                        System.out.println(powerManager[i].getName());                            
                    }
                    myAgent.addBehaviour(new DemandRequest());
                }else{
                    System.out.println("Esperando a que haya PM... ");
                    //myAgent.doSuspend();
                    block();
                    myAgent.doWait(20000);
                  /*  count++;
                    if (count==8){
                      myAgent.doDelete();        
                    }*/
                                                  
                }
            }catch (FIPAException fe) {
                    fe.printStackTrace();
		}
            
            
        }
       public boolean done(){
            end=true;
            return (result1.length>0);
        }
    }
    
    private class DemandRequest extends Behaviour{
        
        private AID pm_id[];
        private MessageTemplate mt;

        
        public void action(){

              // Define the type of message
              ACLMessage request= new ACLMessage(ACLMessage.REQUEST);
              
              for (int i = 0; i < powerManager.length; ++i) {
                 request.addReceiver(powerManager[i]);	
                 }
              
              //It's convert demandValue to String
              //for(int i=0; i<demandValue.length;i++){
                  //demandValue_Str[i]=Float.toString(demandValue[i]);
               //  }
              
              //It's sent the demanded power
               pm_id=new AID[powerManager.length];
               request.setContent(demandValue_Str[j]);
               request.setConversationId("D_PM");
               request.setReplyWith("request"+System.currentTimeMillis()); // Unique value
               System.out.println("El consumidor "+myAgent.getName()+" envia Demanda: "+request.getContent());
               myAgent.send(request);
               j++;
            
            if(j>0 && j<demandValue.length){
                addBehaviour(new PM_Commit());
            }          
        }        

        public boolean done(){
         end=true;
         return end;
        }   
    }
    
    private class PM_Commit extends CyclicBehaviour{
        
      private MessageTemplate mt;

        public void action(){
            
              mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
              ACLMessage commit_demand=myAgent.receive(mt);
               
               if(commit_demand!=null){
               
                    System.out.println("\n\n\n\nEl consumidor "+myAgent.getName()+" ha recibido la confirmación "+commit_demand.getContent()+"\n\n\n\n");

                   if(j<demandValue.length){
    
                       addBehaviour(new DemandRequest());             
                    }
                   if(j>=demandValue.length){
                        // Define the type of message
                        ACLMessage finish= new ACLMessage(ACLMessage.CANCEL);
                  
                        System.out.println("\nEl consumidor "+myAgent.getName()+" ha finalizado.");
                  
                        for (int i = 0; i < powerManager.length; ++i) {
                            finish.addReceiver(powerManager[i]);	
                        }
                        finish.setContent("Ultimo punto de demanda");
                        finish.setConversationId("Finish process");
                        finish.setReplyWith("Finish"+System.currentTimeMillis());
                        System.out.println("El consumidor envia orden de finalizar a Power Manager \n");
                        myAgent.send(finish);
                        doDelete();
                    }                   
                   
                }else{        
                    block();
               }               
        }    
    }    
}
