
package mas_1_7;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
//Library to round decimals
import java.text.DecimalFormat;

/**
 *
 * @author endryys
 */
public class Generator extends Agent{
 	// The catalogue of books for sale (maps the title of a book to its price)
	//private Hashtable catalogo;
	// The GUI by means of which the user can add books in the catalogue
	//private GuiGenerator miGui;
        String p_generada;
        String price;
        String p1_generada;
        String price1;
        private int i;

	// Put agent initializations here
	protected void setup() {
            String i_;
           double p1_generada_;
           double price1_;
           String p1_generada_Str;
           String price1_Str;
		// Create the catalogue
                System.out.println("Bienvenido! Agente-Generador "+getAID().getName()+" es Leido.");
                //It's generated a random number of power with an upper threshold of 900(kW)
                //p1_generada_=(float) (Math.random() * 900) + 1;
                p1_generada_=1430;
                DecimalFormat p1_generada_df = new DecimalFormat("0.00"); 
                p1_generada_Str=p1_generada_df.format(p1_generada_);  
                p1_generada=p1_generada_Str.replaceAll(",", ".");
                
                //It's generated a random price with an upper threshold of 20(€/kW)
                price1_=(float)(Math.random()*20)+1;
                DecimalFormat price1_df = new DecimalFormat("0.00"); 
                price1_Str=price1_df.format(price1_);
                price1=price1_Str.replaceAll(",", ".");
                

		// Register the book-selling service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("demanda-generacion");
		sd.setName("Cartera de inversión JADE");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
                System.out.println("Agente-Generador "+getAID().getName()+" entrega:");
                System.out.println(p1_generada+"(kW) "+"insertados en DF. Precio = "+price1+"\n");

		// Add the behaviour serving queries from Power Manager agent
		addBehaviour(new DemandaOfertaServidor());

		// Add the behaviour serving purchase orders from Power Manager agent
		addBehaviour(new OrdenesCompraServidor());
                
                // Add the behaviour serving purchase orders from Power Manager agent
		addBehaviour(new FinishMessage());
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		System.out.println("Agente Generador "+getAID().getName()+" terminado.\n\n");
	}

	private class DemandaOfertaServidor extends CyclicBehaviour {
            
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {

				// CFP Message received. Process it
				String potencia = msg.getContent();
                                //System.out.println("El generador "+getAID().getName()+"ha recibido CFP del PM con una demanda de: "+potencia);
				ACLMessage respuesta = msg.createReply();
                                
                                p_generada=p1_generada;
                                price=price1;
                                        
				//Integer price = (Integer) catalogo.get(titulo);
				if (price != null) {
                                       // The requested book is available for sale. Reply with the price
                                        String propuesta="["+p_generada+","+price+"]";
					respuesta.setPerformative(ACLMessage.PROPOSE);
					respuesta.setContent(String.valueOf(propuesta));
				}
				else {
					// The requested book is NOT available for sale.
					respuesta.setPerformative(ACLMessage.REFUSE);
					respuesta.setContent("No Disponible");
				}
				myAgent.send(respuesta);
                                //System.out.println("Enviando el precio del suministro de la potencia");
			}
			else {
                                //System.out.println("Esperando a Power Manager...\n");
				block();
			}
		}
	}  // End of inner class OfferRequestsServer

	/**
	   Inner class PurchaseOrdersServer.
	   This is the behaviour used by Book-seller agents to serve incoming 
	   offer acceptances (i.e. purchase orders) from buyer agents.
	   The seller agent removes the purchased book from its catalogue 
	   and replies with an INFORM message to notify the buyer that the
	   purchase has been sucesfully completed.
	 */
	private class OrdenesCompraServidor extends CyclicBehaviour {
            
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
                        
			if (msg != null) {
				// ACCEPT_PROPOSAL Message received. Process it
                                System.out.println("\nEl generador "+myAgent.getName()+" ha recibido ACCEPT_PROPORSAL.");
                                System.out.println("\nSuministro en curso...");
				String potencia = msg.getContent();
				ACLMessage respuesta = msg.createReply();

				//Integer price = (Integer) catalogo.remove(titulo);
				if (price != null) {
					respuesta.setPerformative(ACLMessage.INFORM);
                                        respuesta.setContent(p_generada);
					System.out.println(myAgent.getName()+" suminista "+p_generada+"(kW) a agente "+msg.getSender().getName());
				}
				else {
					// The requested book has been sold to another buyer in the meanwhile .
					respuesta.setPerformative(ACLMessage.FAILURE);
					respuesta.setContent("no-disponible");
				}
                                myAgent.send(respuesta);
                                if (respuesta.getPerformative() == ACLMessage.INFORM){
                                    //myAgent.doDelete();
                                    System.out.println("El generador "+myAgent.getName()+" ha finalizado punto de demanda.\n");
                                }
                                if (respuesta.getPerformative() == ACLMessage.FAILURE){
                                    System.out.println(potencia+"NO ha sido suministada a agente "+msg.getSender().getName());
                                }
                                
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer    
        
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
