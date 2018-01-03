package mas_1_4;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.text.DecimalFormat;

/** @author endryys*/
public class PowerManager extends Agent {
    
   	// The title of the book to buy
	private String potenciaDemanda_Str;
        private float potenciaDemanda;

	// The list of known seller agents
	private AID[] AgentesGeneradores;
        private AID[] AgentesBaterias;
        private AID[] AgentesConsumidores;
        private  int count=0;

	// Put agent initializations here
	protected void setup() {
            String pDemandada_Str,power_demanded;
            
		// Printout a welcome message
		System.out.println("Hola! POWERMANAGER "+getAID().getName()+" es Leido.");
                
                //It's register and added the service to consumer's demand
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("demanda-PM");
		sd.setName("Cartera de inversión JADE");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
                
                addBehaviour(new ReceiveDemand());
                
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Agente-Powermanager "+getAID().getName()+
                        " Terminado.");
	}
          
        private class ReceiveDemand extends CyclicBehaviour{
            
            boolean fin=false;
            private MessageTemplate mt; // The template to receive replies
            
            public void action(){
                
               mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                 //Receive demand point
                ACLMessage demand_point = myAgent.receive(mt);
                                
                if (demand_point!=null){
                    potenciaDemanda_Str = demand_point.getContent();
                    addBehaviour(new GenerationBehaviour());
                }else{
                    System.out.println("Esperando a que haya consumidores...\n");
                    block();//It's blocked until demand_point was different null
                }                    
            }
        }        
/*Behaviour that initialize the operation*/
        private class GenerationBehaviour extends Behaviour{
            
            boolean fin=false;
            
            public void action(){
                
                System.out.println("Necesito una potencia de generacion igual a "+potenciaDemanda_Str);
                
		// Update the list of generators agents
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("demanda-generacion");
		template.addServices(sd);
		try {
                    DFAgentDescription[] result = DFService.search(myAgent, template); 
                    if(result.length>0){
                        System.out.println("Se encuentran los siguientes agentes generadores:");
                        AgentesGeneradores = new AID[result.length];
                        for (int i = 0; i < result.length; ++i) {
				AgentesGeneradores[i] = result[i].getName();
				System.out.println(AgentesGeneradores[i].getName());                            
                        }
                        myAgent.addBehaviour(new SolicitudCompra());
                        }else{
                            System.out.println("Esperando a que haya generadores... ");
                            count++;
                            if (count==8){
                                 myAgent.doDelete();        
                            }
                                                  
                        }
						
		}catch (FIPAException fe) {
                    fe.printStackTrace();
		}
	
            }
            
            public boolean done() {
                fin=true;
		return fin;
            }       
        }

        
	/**Inner class RequestPerformer.
	   This is the behaviour used by Book-buyer agents to request seller 
	   agents the target book.*/
	private class SolicitudCompra extends Behaviour {
		private AID bestSeller; // The agent who provides the best offer 
		private int bestPrice;  // The best offered price
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;
                //private String p_generacion_
                private float potenciaGenerada;
                private AgentFeatures [] agentsG1,agentsG2;
                //private AgentFeatures [][] matrix2;
                private PowerSelector powerSelect= new PowerSelector();
                private int j=0;
                private String pcc_initial,pcc_final;
                
		public void action() {
                    int i;
                    int k=0;
                    int n=AgentesGeneradores.length;
                    AID gen;
                    AID[] generator_id,battery_id;
                    float p_generated,price;
                    float price_=0;
                    float priceS=0;
                    float potenciaGenerada;
                    int numOrderDemand;
                    float pcc_initial_,pcc_final_,p_batt,soc_batt;
                    String batt_input,batt_output;
                    String p_diff,status;
                    float p_diff_,status_;
			switch (step) {
			case 0:
				// Send the cfp to all sellers
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (i = 0; i < AgentesGeneradores.length; ++i) {
					cfp.addReceiver(AgentesGeneradores[i]);
				}
                                //p_generations=new int[AgentesGeneradores.length];
                                generator_id=new AID[AgentesGeneradores.length];
				cfp.setContent(potenciaDemanda_Str);
				cfp.setConversationId("G_PM");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
                                System.out.println("Power Manager envia CFP: "+cfp.getContent());
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("G_PM"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				// Receive all proposals/refusals from seller agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
                                        System.out.println("Power Manager recibe una propuesta de: "+reply.getContent());
					// Reply received
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
                                             //Tratamiento de la propuesta recibida
                                             String arr = reply.getContent();
                                             String[] items = arr.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
                                             //int[] results = new int[items.length];
                                             float[] results = new float[items.length];

                                             for (i = 0; i < items.length; i++) {
                                                try {
                                                        //results[i] = Integer.parseInt(items[i]);
                                                        results[i]=Float.parseFloat(items[i]);
                                                } catch (NumberFormatException nfe) {
                                                   //NOTE: write something here if you need to recover from formatting errors
                                                }
                                             }
                                             //System.out.println("Los valores convertidos a int son:    results[0]= "+results[0]+"  results[1]= "+results[1]);
                                             //int potenciaDemanda=Integer.parseInt(potenciaDemanda_Str);
                                             //int price = Integer.parseInt(reply.getContent());
                                             p_generated=results[0];
                                             price=results[1];
                                             i=0;
                                             gen=reply.getSender();
        
                                             //Sources are storage
                                             if(j<n){
                                                agentsG1=powerSelect.DataStorage(n,gen,p_generated,price);                                          
                                                j++;
                                             }
                                             //When all sources have been storage
                                             if(j==n){
                                            //It's printed the result of matrix1
                                             for (i=0;i<n;i++){
                                             /*          System.out.println("agentsG"+"["+i+"]"+":"+agentsG1[i].GetArrayAgent_AID());
                                                         System.out.println("agentsG"+"["+i+"]"+":"+agentsG1[i].GetArrayAgent_p());
                                                         System.out.println("agentsG"+"["+i+"]"+":"+agentsG1[i].GetArrayAgent_p_price());*/
                                             }
                                                //It's calculated price_mean of energy
                                                for(i=0; i<n;i++){
                                                    priceS=agentsG1[i].GetArrayAgent_p_price();
                                                    price_=price_+priceS;
                                                }
                                                float price_mean=price_/n;
                                                System.out.println("El precio medio de la potencia generada es: "+price_mean);
                                                //Sources are classified according their power and prices
                                                agentsG2=powerSelect.DataOrganizer(n, agentsG1, price_mean);
                                                bestSeller=agentsG2[0].GetArrayAgent_AID();
                                             }
                                             
                                        }repliesCnt++;
                                        /*Si el nº de respuestas es igual o mayor a numero de generadores registrados
                                          quiere decir que ha recibido todas las propuestas de todos los generadores
                                          y ya puede pasar a la siguiente etapa.*/
					if (repliesCnt >= AgentesGeneradores.length) {
						// We received all replies
						step = 2;
                                                break;
					}
				}
				else {
					block();
				}
				break;
			case 2:
				// Send the purchase order to the seller that provided the best offer
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                                for(i=0; i<n;i++){
                                System.out.println("agentsG2: "+agentsG2[i].GetArrayAgent_AID());
                                }
                                //It's compared the power generated of each generated with the power demanded
                                potenciaGenerada=0;
                                i=0;
                                numOrderDemand=0;//Variable that show how many Demand's Order must be sent to satisfy demand
                                potenciaDemanda=Float.valueOf(potenciaDemanda_Str);
                                while(potenciaDemanda>potenciaGenerada){
                                    potenciaGenerada=potenciaGenerada+agentsG2[i].GetArrayAgent_p();
                                    i++;
                                    numOrderDemand++;
                                    if (i==n){
                                        break;
                                    }
                                }
                                this.potenciaGenerada=potenciaGenerada;
                                
                               
                               this.potenciaGenerada=potenciaGenerada;
                                for(i=0;i<numOrderDemand;i++){
                                    bestSeller=agentsG2[i].GetArrayAgent_AID();
                                    order.addReceiver(bestSeller);
                                    System.out.println("Power Manager envia orden de suministro al receptor"+bestSeller);
                                }
                                System.out.println("La potencia total generada que se suministrará es:"+this.potenciaGenerada);
                                
                                /*bestSeller=agentsG2[0].GetArrayAgent_AID();
				order.addReceiver(bestSeller);*/
				order.setContent(potenciaDemanda_Str);
				order.setConversationId("G_PM");
				order.setReplyWith("Orden"+System.currentTimeMillis());
				myAgent.send(order);
                                //System.out.println("Power Manager envia orden de suministro de una potencia de: "+order.getContent()+"al receptor "+bestSeller);
				// Prepare the template to get the purchase order reply
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("G_PM"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;
				break;
			case 3:      
				// Receive the purchase order reply
				reply = myAgent.receive(mt);
				if (reply != null) {
					// Purchase order reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful. We can terminate
						System.out.println(reply.getContent()+"(kW)"+" suministrado con éxito de agente "+reply.getSender().getName()+" a "+ myAgent.getName());
						//System.out.println("Precio = "+bestPrice);
						//myAgent.doDelete();
					}
					else {
						System.out.println("falla: la potencia solicitada ya está suministrada.");
					}

					step = 4;
				}
				else {
					block();
				}
				break;
                                
                        case 4:
                                
                                pcc_initial_=potenciaDemanda-this.potenciaGenerada;
                                System.out.println("Pcc Inicial: "+pcc_initial_+"(kW)");
                                pcc_initial=Float.toString(pcc_initial_);
                                
                                //Search of batteries
                                DFAgentDescription template_batt = new DFAgentDescription();
					ServiceDescription sd_batt = new ServiceDescription();
					sd_batt.setType("pcc-baterias");
					template_batt.addServices(sd_batt);
                                try {
                                    DFAgentDescription[] result_batt = DFService.search(myAgent, template_batt); 
                                    if(result_batt.length>0){
                                        System.out.println("Se encuentran los siguientes sistemas de baterías:");
                                        AgentesBaterias = new AID[result_batt.length];
                                        
                                        for (i = 0; i < result_batt.length; ++i) {
                                            AgentesBaterias[i] = result_batt[i].getName();
                                            System.out.println(AgentesBaterias[i].getName());            
                                         }
                                    }else{
                                        System.out.println("Esperando a que haya baterías... ");
                                                    
                                    }
                                }catch(FIPAException fe){
                                    fe.printStackTrace();
                                }
                                
                                //It's sent CFP to the batteries
                                
                                //Previously to sent the CFP, it's necessary calculate parameters pdiff and status
                                StrategyControl controlP=new StrategyControl();
                                float [] batt_input_=new float[2];
       
                                batt_input_=controlP.PeakShaving(pcc_initial_);
                                p_diff=Float.toString(batt_input_[0]);
                                status=Float.toString(batt_input_[1]);
                                
                                batt_input="["+p_diff+","+status+"]";
                                
				ACLMessage cfp_batt = new ACLMessage(ACLMessage.CFP);
				for (i = 0; i < AgentesBaterias.length; ++i) {
					cfp_batt.addReceiver(AgentesBaterias[i]);
				}
                                //p_generations=new int[AgentesGeneradores.length];
                                battery_id=new AID[AgentesBaterias.length];
				cfp_batt.setContent(batt_input);
				cfp_batt.setConversationId("PM_BATT");
				cfp_batt.setReplyWith("cfp_batt"+System.currentTimeMillis()); // Unique value
                                System.out.println("Power Manager envia CFP a las baterías con el mensaje: "+cfp_batt.getContent());
				myAgent.send(cfp_batt);
                                //block();
                                
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("PM_BATT"),
						MessageTemplate.MatchInReplyTo(cfp_batt.getReplyWith()));                             
                                step=5;
                                
                                break;
                                
                        case 5:
                    
                            ACLMessage reply_batt = myAgent.receive(mt);
                            //System.out.println("Power Manager intenta recibir mensaje de batería. ");
                            pcc_initial_=potenciaDemanda-this.potenciaGenerada;
                           
                                if (reply_batt != null) {
                                    System.out.println("Power Manager recibe una propuesta de: "+reply_batt.getContent());
                                    // Reply received
                                    if (reply_batt.getPerformative() == ACLMessage.PROPOSE) {

                                         //Tratamiento de la propuesta recibida
                                        batt_output = reply_batt.getContent();
                                        String[] items_batt = batt_output.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
                                        float[] results_batt = new float[items_batt.length];

                                        for (i = 0; i < items_batt.length; i++) {
                                            try {
                                                results_batt[i]=Float.parseFloat(items_batt[i]);
                                            }catch (NumberFormatException nfe) {
                                                   //NOTE: write something here if you need to recover from formatting errors
                                            }
                                        }
                                        p_batt=results_batt[0];
                                        soc_batt=results_batt[1];
                                        System.out.println("Pcc_inicial: " +pcc_initial_ +"(kW)");
                                        if (pcc_initial_>0){
                                        pcc_final_= pcc_initial_-p_batt;
                                        System.out.println("Pcc_final: " +pcc_final_ +"(kW)");
                                        }
                                        if(pcc_initial_<0){
                                         pcc_final_= pcc_initial_+p_batt;
                                         System.out.println("Pcc_final: " +pcc_final_ +"(kW)");
                                        }
                                        
                                   }
                                    step=6;
                                
                                }else{
                                    //System.out.println("Power Manager no ha recibido ninguna propuesta de las baterías .");
                                    step=5;
                                }
                           break;   
			}             
		}
                

		public boolean done() {
			if (step == 2 && bestSeller== null) {
				System.out.println("Falla: "+potenciaDemanda_Str+" no está disponible");
			}
			return ((step == 2 && bestSeller == null) || step == 7);
		}
	}  // End of inner class RequestPerformer
        
}