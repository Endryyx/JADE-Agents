package mas_1_7;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
//Library to round decimals
import java.text.DecimalFormat;

/**@author endryys*/

public class PowerManager extends Agent {
    
        //Arrays destinate to store information that describes the system's evolution
    
        public String[] a_pcc_initial=new String[48];
        public String[] a_pcc_final=new String[48];
        public String[] a_p_batt_total=new String[48];
        //public float[] a_p_batt_i=new float[48];//Este debería ser almacenado en las baterías
        public float[] a_SOC_total=new float[48];
        //public float[] a_SOC_i=new float[48];//Este debería ser almacenado en las baterías
        
        //Variables para sustituir "." por ","
        private String pcc_initial_Str;  
        private String pinitial;
        
        private String p_baterias_Str;
        private String pbat_total;
        
        private String pcc_final_Str;  
        private String pfinal;
        
        
                
                
        private int a=0;//Variable auxiliar para el almacenamiento de los valores de los arrays
        
  	// The title of the book to buy
	private String potenciaDemanda_Str;
        private float potenciaDemanda;
        private String pm_name;

	// The list of known seller agents
	private AID[] AgentesGeneradores;
        private AID[] AgentesBaterias;
        private AID[] AgentesConsumidores;
        private  int count=0;
        private ACLMessage demand_point=new ACLMessage();
        
        /*Atributo  que debe determina los segundos cada cuando envía un punto de demanda, se define en 1800s
        Aunque este valor debe ser tomado de un textBox rellenado por el usuario en un formulario*/
        public float ti=1800; 

	// Put agent initializations here
	protected void setup() {
            String pDemandada_Str,power_demanded;
            String[] name_batt_a=new String[3];
            float[] p_batt_a=new float[3];
            
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
                
                String pm_id= new String();
                pm_id=myAgent.getName();
                String[] a_pm_id=pm_id.split("@");
                pm_name=a_pm_id[0];
                
                
               mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                 //Receive demand point
               // ACLMessage demand_point = myAgent.receive(mt);
               demand_point = myAgent.receive(mt);
                                
                if (demand_point!=null){
                    potenciaDemanda_Str = demand_point.getContent();
                    myAgent.addBehaviour(new GenerationBehaviour());
                
                }else{
                    //System.out.println("Esperando a que haya consumidores...\n");
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
                private AID bestOption_batt;
		private int bestPrice;  // The best offered price
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;
                //private String p_generacion_
                private float potenciaGenerada;
                private float potenciaBaterias;//Variable global para acumular potencia de baterias
                private float pBatts=0;
                private AgentFeatures [] agentsG1,agentsG2;
                private AgentFeatures [] agentsB1,agentsB2;
                //private AgentFeatures [][] matrix2;
                private PowerSelector powerSelect= new PowerSelector();
                private int j=0;
                private int k=0;
                private String pcc_initial,pcc_final;
                private String p_diff,status,threshold;
                private int numOrderBatt;
                private int numreplybatt=0;
                private float _aCD=0;
               
                
                private AgentFeatures[] agentBatts;
                private PowerSelector  battData=new PowerSelector();

                
		public void action() {

                    int i;
                    int n=AgentesGeneradores.length;
                    int n_batt;
                    AID gen;
                    AID batt;
                    AID[] generator_id,battery_id,consumer_id;
                    float p_generated,price;
                    float price_=0;
                    float priceS=0;
                    float pBat=0;
                    float potenciaGenerada;
                    float potenciaBaterias;
                    int numOrderDemand;
                    int numOrderBatt;
                    //int numreplybatt=0;
                    
                    float pcc_initial_,pcc_final_,p_batt,soc_batt,pcc_initial_0,pcc_initial_1;
                    float aCD_=0;//Automatic Charge and Discharge
                    int aCD=0;//Automatic Charge and Discharge
                    String batt_input,batt_propose,batt_output;
                    //String p_diff_,status_,threshold_;
                   // float p_diff_,status_;
                    
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
             
                                StrategyControl p_threshold=new StrategyControl();
                                for(i=0; i<n;i++){
                                    System.out.println("agentsG2: "+agentsG2[i].GetArrayAgent_AID());
                                }
                                //It's compared the power generated of each generated with the power demanded
                                potenciaGenerada=0;
                                i=0;
                                numOrderDemand=0;//Variable that show how many Demand's Order must be sent to satisfy demand
                                potenciaDemanda=Float.valueOf(potenciaDemanda_Str);
                                //while(potenciaDemanda-potenciaGenerada!=0 && potenciaDemanda-potenciaGenerada<= p_threshold.pcc_lower ||potenciaDemanda-potenciaGenerada!=0 &&  potenciaDemanda-potenciaGenerada >= p_threshold.pcc_upper){
                                while(potenciaDemanda-potenciaGenerada!=0 &&  potenciaDemanda-potenciaGenerada >= p_threshold.pcc_upper){
                                    //It's compared if the previous he pcc_initial(potenciaDemanda-potenciaGenerada) is smaller than next it's validated smallest.
                                    pcc_initial_0=potenciaDemanda-potenciaGenerada;
                                    pcc_initial_1=potenciaDemanda-potenciaGenerada+agentsG2[i].GetArrayAgent_p();
                                    //It's convert in positive value, in order to do a comparation
                                    if(pcc_initial_0<0){pcc_initial_0=-pcc_initial_0;}
                                    if(pcc_initial_1<0){pcc_initial_1=-pcc_initial_1;}
                                    if(potenciaGenerada!=0 && pcc_initial_0<pcc_initial_1){
                                      break;  
                                    }else{
                                        potenciaGenerada=potenciaGenerada+agentsG2[i].GetArrayAgent_p();
                                        i++;
                                        numOrderDemand++;                                        
                                    }

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
                                myAgent.doWait(3000);
                                pcc_initial_=potenciaDemanda-this.potenciaGenerada;
                                System.out.println("Pcc Inicial: "+pcc_initial_+"(kW)");
                                pcc_initial=Float.toString(pcc_initial_);
                                
                                DecimalFormat p_bateria_df = new DecimalFormat("0.00"); 
                                pcc_initial_Str=p_bateria_df.format(pcc_initial_);  
                                //pinitial=pcc_initial_Str.replaceAll(".", ",");
                                
                                //****************SE ALMACE ARRAY PCC_FINAL*****************************
                                 a_pcc_initial[a]=pcc_initial_Str;
                                //***********************************************************************
                                
                                if(pcc_initial_!=0){
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
                                            block();
                                                    
                                        }
                                    }catch(FIPAException fe){
                                        fe.printStackTrace();
                                    }
                                
                                    //It's sent CFP to the batteries
                                
                                    //Previously to sent the CFP, it's necessary calculate parameters pdiff and status
                                    StrategyControl controlP=new StrategyControl();
                                    float [] batt_input_=new float[3];
       
                                    batt_input_=controlP.PeakShaving(pcc_initial_);
                                    p_diff=Float.toString(batt_input_[0]);
                                    status=Float.toString(batt_input_[1]);
                                    threshold=Float.toString(batt_input_[2]);
                                
                                    batt_input="["+p_diff+","+status+","+threshold+"]";
                                
                                    ACLMessage cfp_batt = new ACLMessage(ACLMessage.CFP);
                                    for (i = 0; i < AgentesBaterias.length; ++i) {
                                            cfp_batt.addReceiver(AgentesBaterias[i]);
                                    }
                                    //p_generations=new int[AgentesGeneradores.length];
                                    battery_id=new AID[AgentesBaterias.length];
                                    cfp_batt.setContent(batt_input);
                                    cfp_batt.setConversationId("PM_BATT");
                                    cfp_batt.setReplyWith("cfp_batt"+System.currentTimeMillis()); // Unique value
                                    System.out.println("\nPower Manager envia CFP a las baterías con el mensaje: "+cfp_batt.getContent());
                                    myAgent.send(cfp_batt);
                                    
                                    //block();
                                
                                    // Prepare the template to get proposals
                                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("PM_BATT"),
						MessageTemplate.MatchInReplyTo(cfp_batt.getReplyWith()));                             
                                    step=5;
                                    
                                }else{
                                    
                                    System.out.println("\nLa demanda se ha cubierto con la generación.\n");
                                    System.out.println("No es necesaria la acción de las baterías.\n");
                                    pcc_final_= pcc_initial_;
                                    System.out.println("Pcc_final: " +pcc_final_ +"(kW)");
                                    step=9;//Va a la confirmación de la satisfacción de la demanda
                                }
                                
                                break;
                                
                        case 5:
                    
                            ACLMessage reply_batt = myAgent.receive(mt);
                            //System.out.println("Power Manager intenta recibir mensaje de batería. ");
                            pcc_initial_=potenciaDemanda-this.potenciaGenerada;
                            n_batt=AgentesBaterias.length;
                           
                                //Si se recibe el mensaje de información de la batería
                                if (reply_batt != null) {
                                    System.out.println("Power Manager recibe una propuesta de: "+reply_batt.getContent()+" de batería: "+reply_batt.getSender());
                                    // Reply received
                                    if (reply_batt.getPerformative() == ACLMessage.PROPOSE) {
                                                                               
                                         //Tratamiento de la propuesta recibida
                                        batt_propose = reply_batt.getContent();
                                        String[] items_batt = batt_propose.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
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
                                        aCD_=results_batt[2];
                                        
                                        if(aCD_==1){ aCD=1;}
                                        if(aCD_==-1){ aCD=-1;}
                                        if(aCD_==0){ aCD=0;}
                                        System.out.println("Pcc_inicial: " +pcc_initial_ +"(kW)");
                                        
                                        //Se detecta el nombre de la bateria del que ha recibido energia
                                        batt=reply_batt.getSender();
                                        i=0;
                                        if(k<n_batt){
                                            agentsB1=powerSelect.DataStorage_batt(n_batt, batt, p_batt,soc_batt ,aCD);
                                            k++;
                                        }
                                        
                                        if(k==n_batt){//Se han almacenado todos los valores de las baterías
                                            if(pcc_initial_>0){//Cuando existe mayor demanda que generación
                                                agentsB2=powerSelect.DataOrganizer_batt_Ascending(n_batt, agentsB1);
                                                bestOption_batt=agentsB2[0].GetArrayAgent_AID();                                     
                                            }
                                            if(pcc_initial_<0){//Cuando existe mayor generación que demanda
                                               agentsB2=powerSelect.DataOrganizer_batt_Descending(n_batt, agentsB1);
                                               bestOption_batt=agentsB2[0].GetArrayAgent_AID();
                                            }     
                                        }                                                                             
                                   }
                                    if(k<n_batt){
                                        step=5;
                                        break;
                                    }else{
                                    step=6;
                                    }
                                
                                }else{
                                    step=5;
                                }
                                break;
                           
                        case 6:
                            //Se calcula la potencia total resultante de las operaciones de las baterias
                            //Según la pcc_inicial
                            n_batt=AgentesBaterias.length;
                            pcc_initial_=potenciaDemanda-this.potenciaGenerada;
                            StrategyControl controlP_batts=new StrategyControl();
                            
                            for(i=0;i<n_batt;++i){
                                System.out.println("agentB2: "+agentsB2[i].GetArrayAgent_AID());
                            }
                            potenciaBaterias=0;
                            i=0;
                            numOrderBatt=0;
                            
                            if(pcc_initial_<0){
                                //Para que esté dentro de los márgenes -100 y 100 la formula debe ser pcc_initial+potenciaBaterias<p_upper
                                while(pcc_initial_+potenciaBaterias<controlP_batts.pcc_lower && pcc_initial_+potenciaBaterias<0 ){
                                    aCD=agentsB2[i].GetArrayAgent_aCD();
                                    if(aCD==0||aCD==1){
                                        pBat=agentsB2[i].GetArrayAgent_p();
                                        if(pBat<0){pBat=-pBat;}
                                        potenciaBaterias=potenciaBaterias+pBat;
                                        i++;
                                        numOrderBatt++;
                                    }else{
                                        pBat=agentsB2[i].GetArrayAgent_p();
                                        if(pBat<0){pBat=-pBat;}
                                        potenciaBaterias=potenciaBaterias-pBat;
                                        i++;
                                        numOrderBatt++;                                        
                                    }
                                    if(i==n_batt){
                                        break;
                                    }                                   
                                }
                                //this.potenciaBaterias=potenciaBaterias;
                                this.numOrderBatt=numOrderBatt;
                            }
                            
                            if(pcc_initial_>0){
                                //Para que esté dentro de los márgenes -100 y 100 la formula debe ser pcc_initial+potenciaBaterias>p_lower
                                while(pcc_initial_-potenciaBaterias>controlP_batts.pcc_upper && pcc_initial_-potenciaBaterias>0 ){
                                    aCD=agentsB2[i].GetArrayAgent_aCD();
                                    if(aCD==0||aCD==-1){
                                        pBat=agentsB2[i].GetArrayAgent_p();
                                        if(pBat<0){pBat=-pBat;}
                                        potenciaBaterias=potenciaBaterias+pBat;
                                        i++;
                                        numOrderBatt++;
                                    }else{
                                        pBat=agentsB2[i].GetArrayAgent_p();
                                        if(pBat<0){pBat=-pBat;}
                                        potenciaBaterias=potenciaBaterias-pBat;
                                        i++;
                                        numOrderBatt++;                                        
                                    }
                                    if(i==n_batt){
                                        break;
                                    }                                   
                                }
                                //this.potenciaBaterias=potenciaBaterias;
                                this.numOrderBatt=numOrderBatt;   
                            }
                            
                            /*Si pcc_initial esta dentro ambos límites se envía a todas las baterias 0(kW) por 
                            si tuvieran que cargar o descargar de forma automica*/
                           if(controlP_batts.pcc_lower<=pcc_initial_ && pcc_initial_<=controlP_batts.pcc_upper){
                              //this.potenciaBaterias recibe el valor en las anteriores sentencias
                              numOrderBatt=AgentesBaterias.length;
                              this.numOrderBatt=numOrderBatt;
                              
                            }
                            /*In this case, the initialization and instance of object order_batt it's done here
                            because if it would be done before in any point this info is lost*/
                            ACLMessage order_batt= new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                            for(i=0;i<numOrderBatt;i++){
                               bestOption_batt=agentsB2[i].GetArrayAgent_AID();
                               order_batt.addReceiver(bestOption_batt);
                               System.out.println("Power Manager da orden de acción a la batería"+bestOption_batt);
                            }
                            /*if(pcc_initial_>0){System.out.println("LA POTENCIA TOTAL QUE DEBERÍAN APORTAR LAS BATERÍAS SERÍA : " +this.potenciaBaterias+"(kW). \n");}
                            if(pcc_initial_<0){System.out.println("LA POTENCIA TOTAL QUE DEBERÍAN CONSUMIR LAS BATERÍAS SERÍA : " +this.potenciaBaterias+"(kW). \n");}*/
                            if(pcc_initial_>0){System.out.println("LA POTENCIA TOTAL QUE DEBERÍAN APORTAR LAS BATERÍAS SERÍA : " +potenciaBaterias+"(kW). \n");}
                            if(pcc_initial_<0){System.out.println("LA POTENCIA TOTAL QUE DEBERÍAN CONSUMIR LAS BATERÍAS SERÍA : " +potenciaBaterias+"(kW). \n");}
                            pcc_initial=Float.toString(pcc_initial_);
                            batt_input="["+p_diff+","+status+","+threshold+"]";
                            
                            order_batt.setContent( batt_input);
                            order_batt.setConversationId("PM_BAT_Action");
                            order_batt.setReplyWith("Orden"+System.currentTimeMillis());
                            System.out.println("PM envía a las baterías: "+batt_input);
                            //******ERRRORRRR!!!!!!!!!!!!!!**********
                            myAgent.send(order_batt);

                            mt = MessageTemplate.and(MessageTemplate.MatchConversationId("PM_BAT_Action"),
						MessageTemplate.MatchInReplyTo(order_batt.getReplyWith())); 

                            step=7;
                            break;
                            
                        case 7:
                            
                            float pcc_=0;
                            float soc_pre=0;
                            String soc_pre_str=new String();
                            
                            //PRUEBA UNITARIA
                            if(potenciaDemanda==1751){
                                potenciaDemanda=1751;
                            }
                            if(potenciaDemanda==1350){
                                potenciaDemanda=1350;
                            }
                            
                            

                            pcc_initial_=potenciaDemanda-this.potenciaGenerada;

                            reply_batt=myAgent.receive(mt);

                            if(reply_batt!=null){
                                
                                if(reply_batt.getPerformative()==ACLMessage.INFORM){
                                    
                                        numreplybatt++;
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
                                        aCD_=results_batt[2];
                                        this._aCD=aCD_;
					controlP_batts=new StrategyControl();
                                        BatteryAction batt_decision=new BatteryAction();

					//Condiciones donde la batería entrega energía
                                        if(pcc_initial_<0 && pcc_initial_>=controlP_batts.pcc_lower && aCD_==-1  || pcc_initial_>0 && pcc_initial_>controlP_batts.pcc_upper && aCD_==0 || pcc_initial_>0 && pcc_initial_<=controlP_batts.pcc_upper && aCD_==-1){
                                            //Se resta el valor porque la batería esta aportando
                                            this.pBatts=this.pBatts-p_batt;
                                        }
                                        //Condiciones donde la batería absorbe energía
                                        if(pcc_initial_<0 && pcc_initial_<controlP_batts.pcc_lower && aCD_==0 || pcc_initial_<0 && pcc_initial_>=controlP_batts.pcc_lower && aCD_==1|| pcc_initial_>0 && pcc_initial_<=controlP_batts.pcc_upper && aCD_==1){
                                            //Se suma el valor porque la batería esta absorbiendo
                                            this.pBatts=this.pBatts+p_batt;                              
                                        }
                                        pcc_= pcc_initial_+this.pBatts;

                                        if(pcc_initial_<0){
                                            
                                           //Se comprueba que no entra en conflicto con los límites de pcc.
                                           
                                           if(numreplybatt<=this.numOrderBatt){
                                               /*Si no se ha recibido la respuesta de la ultima bateria, 
                                               hay que preocuparse para pcc_initial<0 que no supere el limite superior, 
                                               guardando en arrays los datos de las baterias. De tal forma que cuando haya una 
                                               batería que realice un consumo que ponga en peligro el limite superior deberá
                                               paralizarse ese consumo de esa batería, eliminando los pasos y reestableciendo
                                               el soc anterior de esa bateria a la carga automática.*/
                                               
                                               /*Para almacenar valores de manera global debe ser necesario con una clase independiente
                                               esta clase AgentFeatures, solo puede recibir elementos de la misma clase, a través
                                               de otra clase powerSelector*/
                                               
                                               agentBatts=battData.DataStorage_batt(this.numOrderBatt, reply_batt.getSender(), p_batt, soc_batt, 0);
                                               
                                                  if(pcc_>controlP_batts.pcc_upper){
                                                      
                                                      //Entra en conflicto por limite superior
                                                      bestOption_batt=agentsB2[numreplybatt-1].GetArrayAgent_AID();
                                                      //Se define el tipo de mensaje de paralización de entrega
                                                      ACLMessage stop_batt= new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                                                      stop_batt.setConversationId("STOP_BATT");
                                                      stop_batt.setReplyWith("Orden"+System.currentTimeMillis());
                                                      stop_batt.addReceiver(bestOption_batt);
                                                      soc_pre=agentsB2[numreplybatt-1].GetArrayAgent_soc();
                                                      soc_pre_str=Float.toString(soc_pre);
                                                      stop_batt.setContent( soc_pre_str);
                                                      System.out.println("PM envía a "+agentBatts[numreplybatt-1].GetArrayAgent_AID()+" paralización de acción para evitar el conflicto con el límite superior de PeakShaving :"+controlP_batts.pcc_upper);  
                                                  
                                                      myAgent.send(stop_batt);
                                                      
                                                       //myAgent.doWait(1000);
                                                        
                                                  }else{
                                                      
                                                     //Si no entra en conflicto
                                                    if(aCD_==1){
                                                        System.out.println(p_batt+"(kW) "+"consume "+reply_batt.getSender().getName()+" mediante fase de carga automática.\n");
                                                        System.out.println(soc_batt+"(%) "+"queda "+reply_batt.getSender().getName()+".\n");
                                                        this.potenciaBaterias=this.potenciaBaterias+p_batt;
                                                    }
                                                    if(aCD_==-1){
                                                        System.out.println(p_batt+"(kW) "+"aporta "+reply_batt.getSender().getName()+" mediante fase de descarga automática.\n");
                                                        System.out.println(soc_batt+"(%) "+"queda "+reply_batt.getSender().getName()+".\n");
                                                        this.potenciaBaterias=this.potenciaBaterias-p_batt;
                                                    }
                                                    if(aCD_==0){ 
                                                        System.out.println(p_batt+"(kW) "+"consume "+reply_batt.getSender().getName());
                                                        System.out.println(soc_batt+"(%) "+"queda "+reply_batt.getSender().getName()+".\n");
                                                    
                                                        if(p_batt==0){
                                                            this.potenciaBaterias=this.potenciaBaterias+p_batt;   
                                                        }else{
                                                            this.potenciaBaterias=this.potenciaBaterias+p_batt;
                                                        }
                                                    }
                                                 }   
                                            }
                                           
                                           if(numreplybatt==this.numOrderBatt){
                                               /*Si se ha recibido la respuesta de la ultima bateria, 
                                               hay que preocuparse para pcc_initial<0 que no supere el limite inferior, 
                                               Si se superase y pcc_<controlP_batts.pcc_lower entonces habría que ir eliminando
                                               de la suma pcc_ las potencias de las baterias comenzando por la última hasta que
                                               se cumpliese que pcc_>=controlP_batts.pcc_lower. Paralizando así el proceso de descarga 
                                               automática de esas baterías eliminadas y reestableciendo el soc anterior a dicha descarga.*/ 
                                               i=1;
                                               if(pcc_<controlP_batts.pcc_lower && aCD_!=0){
                                                  //Entra en conflicto por limite inferior
                                                  while(pcc_<controlP_batts.pcc_lower){
                                                      
                                                      float p_agentbatt=agentBatts[numreplybatt-i].GetArrayAgent_p();
                                                      
                                                            /*p_agentbatt=agentBatts[1].GetArrayAgent_p();
                                                            p_agentbatt=agentBatts[2].GetArrayAgent_p();
                                                            p_agentbatt=agentBatts[3].GetArrayAgent_p();*/
                                                            
                                                      //Se define el tipo de mensaje de paralización de entrega
                                                      ACLMessage stop_batt= new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                                                      stop_batt.setConversationId("STOP_BATT");
                                                      stop_batt.setReplyWith("Orden"+System.currentTimeMillis());      
                                                      bestOption_batt=agentsB2[numreplybatt-i].GetArrayAgent_AID();
                                                      stop_batt.addReceiver(bestOption_batt);
                                                      soc_pre=agentsB2[numreplybatt-i].GetArrayAgent_soc();
                                                      soc_pre_str=Float.toString(soc_pre);
                                                      stop_batt.setContent( soc_pre_str);
                                                      System.out.println("PM envía a "+bestOption_batt+" paralización de acción para evitar el conflicto con el límite inferior de PeakShaving :"+controlP_batts.pcc_lower);
                                                      
                                                      myAgent.send(stop_batt);
                                                      
                                                      
                                                      
                                                      //float p_agentbatt=agentBatts[numreplybatt-i].GetArrayAgent_p();
                                                      pcc_=pcc_+p_agentbatt;//Se suma puesto que pcc_ es negativa y hay que reducir esa diferencia
                                                      this.potenciaBaterias=this.potenciaBaterias+p_agentbatt;
                                                      //Se reestablece el soc al anterior
                                                      //soc_batt=soc_a[numreplybatt-i];
                                                      //System.out.println("Se queda en SOC: "+soc_a[numreplybatt-i]);
                                                     
                                                       //myAgent.doWait(1000);
                                                      
                                                      i++;
                                                  }
                                                    DecimalFormat p_baterias_df = new DecimalFormat("0.00"); 
                                                    p_baterias_Str=p_baterias_df.format(this.potenciaBaterias);
                                                    //****************SE ALMACE ARRAY P_BATT_TOTAL**************************
                                                         //a_p_batt_total[a]=this.potenciaBaterias;
                                                         a_p_batt_total[a]=p_baterias_Str;
                                                  //************************************************************************
                                                  
                                               }else{
                                                  this.potenciaBaterias=batt_decision.BatteryDecision(aCD_, reply_batt.getSender().getName(), p_batt, soc_batt, this.potenciaBaterias); 
                                                  
                                                  
                                                    DecimalFormat p_baterias_df = new DecimalFormat("0.00"); 
                                                    p_baterias_Str=p_baterias_df.format(this.potenciaBaterias);
                                                  //****************SE ALMACE ARRAY P_BATT_TOTAL*****************************
                                                         //a_p_batt_total[a]=this.potenciaBaterias;
                                                         a_p_batt_total[a]=p_baterias_Str;
                                                  //*************************************************************************
                                               
                                               }
                                           }
                                           
                                         
                                        }

                                        if(pcc_initial_>0){
                                            
                                            if(numreplybatt<=this.numOrderBatt){
                                                
                                                agentBatts=battData.DataStorage_batt(this.numOrderBatt, reply_batt.getSender(), p_batt, soc_batt, 0);
                                               
                                                  if(pcc_<controlP_batts.pcc_lower){
                                                      
                                                      //Entra en conflicto por limite inferior
                                                      
                                                     //Se define el tipo de mensaje de paralización de entrega
                                                      ACLMessage stop_batt= new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                                                      stop_batt.setConversationId("STOP_BATT");
                                                      stop_batt.setReplyWith("Orden"+System.currentTimeMillis());
                                                      bestOption_batt=agentsB2[numreplybatt-1].GetArrayAgent_AID();
                                                      stop_batt.addReceiver(bestOption_batt);
                                                      soc_pre=agentsB2[numreplybatt-1].GetArrayAgent_soc();
                                                      soc_pre_str=Float.toString(soc_pre);
                                                      stop_batt.setContent( soc_pre_str);
                                                      System.out.println("PM envía a "+agentBatts[numreplybatt-1].GetArrayAgent_AID()+" paralización de acción para evitar el conflicto con el límite inferior de PeakShaving :"+controlP_batts.pcc_lower);  
                                                  
                                                      myAgent.send(stop_batt);
                                                      
                                                      
                                                      //myAgent.doWait(1000);
                                                        
                                                  }else{
                                                      
                                                     //Si no entra en conflicto
                                                    if(aCD_==1){
                                                        System.out.println(p_batt+"(kW) "+"consume "+reply_batt.getSender().getName()+" mediante fase de carga automática.\n");
                                                        System.out.println(soc_batt+"(%) "+"queda "+reply_batt.getSender().getName()+".\n");
                                                        this.potenciaBaterias=this.potenciaBaterias+p_batt;
                                                    }
                                                    if(aCD_==-1){
                                                        System.out.println(p_batt+"(kW) "+"aporta "+reply_batt.getSender().getName()+" mediante fase de descarga automática.\n");
                                                        System.out.println(soc_batt+"(%) "+"queda "+reply_batt.getSender().getName()+".\n");
                                                        this.potenciaBaterias=this.potenciaBaterias-p_batt;
                                                    }
                                                    if(aCD_==0){ 
                                                        System.out.println(p_batt+"(kW) "+"aporta "+reply_batt.getSender().getName());
                                                        System.out.println(soc_batt+"(%) "+"queda "+reply_batt.getSender().getName()+".\n");
                                                    
                                                        if(p_batt==0){
                                                            this.potenciaBaterias=this.potenciaBaterias-p_batt;   
                                                        }else{
                                                            this.potenciaBaterias=this.potenciaBaterias-p_batt;
                                                        }
                                                    }
                                                 }   
                                            }
                                            if(numreplybatt==this.numOrderBatt){
                                               /*Si se ha recibido la respuesta de la ultima bateria, 
                                               hay que preocuparse para pcc_initial<0 que no supere el limite inferior, 
                                               Si se superase y pcc_<controlP_batts.pcc_lower entonces habría que ir eliminando
                                               de la suma pcc_ las potencias de las baterias comenzando por la última hasta que
                                               se cumpliese que pcc_>=controlP_batts.pcc_lower. Paralizando así el proceso de descarga 
                                               automática de esas baterías eliminadas y reestableciendo el soc anterior a dicha descarga.*/ 
                                               i=1;
                                               if(pcc_>controlP_batts.pcc_upper && aCD_!=0 ){
                                                  //Entra en conflicto por limite inferior
                                                  //while(pcc_>controlP_batts.pcc_upper || (numreplybatt-i)>=0){
                                                  while(pcc_>controlP_batts.pcc_upper && (numreplybatt-i)>=0){
                                                      if((numreplybatt-i)<0){
                                                          break;
                                                      }
                                                      float p_agentbatt=agentBatts[numreplybatt-i].GetArrayAgent_p();
                                                            /*p_agentbatt=agentBatts[1].GetArrayAgent_p();
                                                            p_agentbatt=agentBatts[2].GetArrayAgent_p();
                                                            p_agentbatt=agentBatts[3].GetArrayAgent_p();*/
                                                            
                                                      //Se define el tipo de mensaje de paralización de entrega
                                                      ACLMessage stop_batt= new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                                                      stop_batt.setConversationId("STOP_BATT");
                                                      stop_batt.setReplyWith("Orden"+System.currentTimeMillis());      
                                                      bestOption_batt=agentsB2[numreplybatt-i].GetArrayAgent_AID();
                                                      stop_batt.addReceiver(bestOption_batt);
                                                      soc_pre=agentsB2[numreplybatt-i].GetArrayAgent_soc();
                                                      soc_pre_str=Float.toString(soc_pre);
                                                      stop_batt.setContent( soc_pre_str);
                                                      System.out.println(agentBatts[numreplybatt-i].GetArrayAgent_AID()+" se detiene para evitar el conflicto con el límite superior de PeakShaving :"+controlP_batts.pcc_upper);
                                                      
                                                      myAgent.send(stop_batt);
                                                      
                                                      //float p_agentbatt=agentBatts[numreplybatt-i].GetArrayAgent_p();
                                                      pcc_=pcc_-p_agentbatt;//Se resta puesto que pcc_ es positiva y hay que reducir esa diferencia
                                                      //if(p_agentbatt>0){p_agentbatt=-p_agentbatt;}
                                                      this.potenciaBaterias=this.potenciaBaterias-p_agentbatt;
                                                      
                                                        
                                                       //myAgent.doWait(1000);
                                                       
                                                      //Se reestablece el soc al anterior
                                                      //soc_batt=soc_a[numreplybatt-i];
                                                      //System.out.println("Se queda en SOC: "+soc_a[numreplybatt-i]);
                                                      i++;
                                                  }
                                                  
                                                  
                                                  DecimalFormat p_baterias_df = new DecimalFormat("0.00"); 
                                                  p_baterias_Str=p_baterias_df.format(this.potenciaBaterias);
                                                  //****************SE ALMACE ARRAY P_BATT_TOTAL*****************************
                                                   //a_p_batt_total[a]=this.potenciaBaterias;
                                                   a_p_batt_total[a]=p_baterias_Str;
                                                  //*************************************************************************
                                                  
                                               }else{
                                                   
                                                  this.potenciaBaterias=batt_decision.BatteryDecision(aCD_, reply_batt.getSender().getName(), p_batt, soc_batt, this.potenciaBaterias); 
                                                  
                                                  
                                                    DecimalFormat p_baterias_df = new DecimalFormat("0.00"); 
                                                    p_baterias_Str=p_baterias_df.format(this.potenciaBaterias);
                                                  //****************SE ALMACE ARRAY P_BATT_TOTAL*****************************
                                                         //a_p_batt_total[a]=this.potenciaBaterias;
                                                         a_p_batt_total[a]=p_baterias_Str;
                                                  //*************************************************************************
                                               
                                               }
                                           }  
                                        }
                                    }else{
                                      if(reply_batt.getPerformative()==ACLMessage.REFUSE){
                                        numreplybatt++; 
                                        System.out.println("Batería "+reply_batt.getSender().getName()+" parada, SOC adecuado.");
                                      }
                                    }
                                        
                                    if(numreplybatt<this.numOrderBatt){
                                        step= 7;
                                        break;
                                    }else{
                                        step=8;
                                    }
                                }else{
                                    block(); 
                                }
                            break;

                        case 8:
                            myAgent.doWait(1000);
                            controlP_batts=new StrategyControl();
                            pcc_initial_=potenciaDemanda-this.potenciaGenerada;
                            if(pcc_initial_<0){
                                
                                //if(this.potenciaBaterias<0){this.potenciaBaterias=-this.potenciaBaterias;}
                                
                                if(pcc_initial_>=controlP_batts.pcc_lower ){
                                    pcc_final_=pcc_initial_+this.potenciaBaterias;
                                    pcc_final=Float.toString(pcc_final_); 
                                    
                                    DecimalFormat pcc_final_df = new DecimalFormat("0.00"); 
                                    pcc_final_Str=pcc_final_df.format(pcc_final_);
                                    //****************SE ALMACE ARRAY PCC_FINAL*****************************
                                     //a_pcc_final[a]=pcc_final_;
                                     a_pcc_final[a]=pcc_final_Str;
                                    //**********************************************************************
                                
                                }
                                
                                if(pcc_initial_<controlP_batts.pcc_lower ){
                                    pcc_final_=pcc_initial_+this.potenciaBaterias;
                                    pcc_final=Float.toString(pcc_final_);
                                    
                                    DecimalFormat pcc_final_df = new DecimalFormat("0.00"); 
                                    pcc_final_Str=pcc_final_df.format(pcc_final_);
                                    //****************SE ALMACE ARRAY PCC_FINAL*****************************
                                     //a_pcc_final[a]=pcc_final_;
                                     a_pcc_final[a]=pcc_final_Str;
                                    //**********************************************************************
                                    
                                }
                               /* if(aCD_==-1 && pcc_initial_>=controlP_batts.pcc_lower){
                                   pcc_final_=pcc_initial_-this.potenciaBaterias;
                                    pcc_final=Float.toString(pcc_final_); 
                                }*/
                            }
                            if(pcc_initial_>0){
                                //if(this.potenciaBaterias<0){this.potenciaBaterias=-this.potenciaBaterias;}
                                
                                if(pcc_initial_<=controlP_batts.pcc_upper ){
                                    pcc_final_=pcc_initial_+this.potenciaBaterias;
                                    pcc_final=Float.toString(pcc_final_); 
                                    
                                    
                                    DecimalFormat pcc_final_df = new DecimalFormat("0.00"); 
                                    pcc_final_Str=pcc_final_df.format(pcc_final_);
                                     //****************SE ALMACE ARRAY PCC_FINAL*****************************
                                     //a_pcc_final[a]=pcc_final_;
                                     a_pcc_final[a]=pcc_final_Str;
                                    //**********************************************************************
                                    
                                }
                                
                                if(pcc_initial_>controlP_batts.pcc_upper ){
                                    pcc_final_=pcc_initial_+this.potenciaBaterias;
                                    pcc_final=Float.toString(pcc_final_);
                                    
                                    DecimalFormat pcc_final_df = new DecimalFormat("0.00"); 
                                    pcc_final_Str=pcc_final_df.format(pcc_final_);
                                    //****************SE ALMACE ARRAY PCC_FINAL*****************************
                                     //a_pcc_final[a]=pcc_final_;
                                     a_pcc_final[a]=pcc_final_Str;
                                    //**********************************************************************
                                    
                                }
                               /* if(aCD_==1 && pcc_initial_<=controlP_batts.pcc_upper){
                                    pcc_final_=pcc_initial_+this.potenciaBaterias;
                                    pcc_final=Float.toString(pcc_final_);
                                }*/
                            } 
 
                            System.out.println("Pcc_final: "+pcc_final+"(kW)");
                            step=9;
                            break;

                        case 9:
                            //Se genera una confirmación al consumidor, respondiendo al mensaje demand_point
                             ACLMessage commit_demand=new ACLMessage(ACLMessage.CONFIRM); 
                    
                            //Se busca el consumidor para enviarselo
		
                            DFAgentDescription template = new DFAgentDescription();
                            ServiceDescription sd = new ServiceDescription();
                            sd.setType("confirmacion-PM");
                            template.addServices(sd);
                            
                            try {
                                    DFAgentDescription[] result = DFService.search(myAgent, template); 
                                 
                                    if(result.length>0){
                                        AgentesConsumidores = new AID[result.length];
                                        for (i = 0; i < result.length; ++i) {
                                            AgentesConsumidores[i] = result[i].getName();                         
                                         }
                                    }else{
                                    System.out.println("No se ha podido contactar con ningún consumidor... ");
                                    count++;
                                    if (count==8){
                                        myAgent.doDelete();        
                                    }
                                                  
                                }
						
                            }catch (FIPAException fe) {
                                fe.printStackTrace();
                            }
                            
                            // Send the cfp to all sellers
                            for (i = 0; i < AgentesConsumidores.length; ++i) {
                                commit_demand.addReceiver(AgentesConsumidores[i]);
                            }
                            //p_generations=new int[AgentesGeneradores.length];
                            consumer_id=new AID[AgentesConsumidores.length];
                            commit_demand.setContent(potenciaDemanda_Str);
                            //commit_demand.setContent("Punto de demanda satisfecho");
                            commit_demand.setConversationId("PM_C");
                            commit_demand.setReplyWith("commit_demand"+System.currentTimeMillis()); // Unique value
                            myAgent.send(commit_demand);                            
                            mt=MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
                            step=10;
                            a++;
                            break;
                            
                        case 10:
                            
                            addBehaviour(new FinishMessage());
                            ACLMessage finish=receive(mt);
                            if(finish!=null){
                                System.out.println();
                                myAgent.doDelete();
                            }
                            step= 11;
                            break;                         
			}             
		}
                

		public boolean done() {
			if (step == 2 && bestSeller== null) {
				System.out.println("Falla: "+potenciaDemanda_Str+" no está disponible");
			}
                        if(step==8){
                            
                            step=8;  
                        }
			return ((step == 2 && bestSeller == null) || step == 11);
		}
	}  // End of inner class RequestPerformer
                
        private class FinishMessage extends CyclicBehaviour{
            
            private MessageTemplate mt; // The template to receive replies
       
            public void action() {
                mt=MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
                ACLMessage finish=receive(mt);
                ExportCSV filecsv=new ExportCSV();
                    if(finish!=null){
                    // Define the type of message to sent generators and batteries
                    ACLMessage finish_order= new ACLMessage(ACLMessage.CANCEL);
                    System.out.println("PM envia orden de finalizar a Generadores y Baterías \n");

                    for (int i = 0; i < AgentesGeneradores.length; ++i) {
                       finish_order.addReceiver(AgentesGeneradores[i]);	
                    }
                   for (int i = 0; i < AgentesBaterias.length; ++i) {	
                       finish_order.addReceiver(AgentesBaterias[i]);	
                    }
                   //Se crea el archivo .csv de salida
                   final String nombreDeArchivo = "/home/endryys/_"+pm_name+"_output.csv";
                   filecsv.CreatefileCSV(nombreDeArchivo,";", a_pcc_initial, a_pcc_final, a_p_batt_total);
                   
                    finish_order.setContent("Ultimo punto de demanda");
                    finish_order.setConversationId("Finish process");
                    finish_order.setReplyWith("Finish"+System.currentTimeMillis());
                    
                    myAgent.send(finish_order);
                    System.out.println();
                    myAgent.doDelete();
                    
                    }    
            }
        }    
}