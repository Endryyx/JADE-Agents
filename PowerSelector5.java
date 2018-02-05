package mas_1_7;
import jade.core.AID;

/**@author endryys*/

public class PowerSelector {
    public int j, k,n;
    private float lastPrice;
    private AID lastGen;
    private float lastPower;
   
    //Inside this array it will be saved all the elements
    public int i;
    public AgentFeatures[] agentsG=new AgentFeatures[30];
    public AgentFeatures[]agentsG2=new AgentFeatures[30];
    public AgentFeatures[] agentsB1=new AgentFeatures[30];
    public AgentFeatures[] agentsB2=new AgentFeatures[30];
 
    public AgentFeatures[] DataStorage(int numAgents,AID generator_id, float p_generated, float price){
        
    AgentFeatures ag=new AgentFeatures();
    ag.SetArrayAgent_AID(generator_id);
    ag.SetArrayAgent_p(p_generated);
    ag.SetArrayAgent_p_price(price);
    agentsG[i]=ag;      
    i++;
    return  agentsG; 
    }
    
    public AgentFeatures[] DataStorage_batt(int numBatts,AID batt_id, float p_batt, float soc, int aCD){
        
    AgentFeatures ab=new AgentFeatures();
    ab.SetArrayAgent_AID(batt_id);
    ab.SetArrayAgent_p(p_batt);
    ab.SetArrayAgent_soc(soc);
    ab.SetArrayAgent_aCD(aCD);
    agentsB1[k]=ab;
    k++;
    return agentsB1; 
    }
    
    public AgentFeatures[]DataOrganizer(int numAgents,AgentFeatures[] arrAgentFeatures,float price_mean){
        
     int discard=0;
     int arrange=0;
     int h=0;
     int l=0;
     int e=0;
     int e0=0;
     double price_upper=1.2*price_mean;
     float aux1,aux2;
     AID aux0;
     
     AID[] aid=new AID[numAgents];
     float[] p=new float[numAgents];
     float[] price=new float[numAgents];
     
     //Auxiliar variables to arranging and classifying the values of prices and power mainly
     AID[] aid_d=new AID[numAgents];//aid_discard
     AID[] aid_a=new AID[numAgents];//aid_arrange
     float[] p_d=new float[numAgents];//power_discard
     float[] p_a=new float[numAgents];//power_arrange
     float[] price_d=new float[numAgents];
     float[] price_a=new float[numAgents];
     
     
     /**It's saved the values of matrix in arrays in order to be more 
      * comfortable to manage the information*/
     
     for(i=0;i<numAgents;++i){
         price[i]=arrAgentFeatures[i].GetArrayAgent_p_price();
         p[i]=arrAgentFeatures[i].GetArrayAgent_p();
         aid[i]=arrAgentFeatures[i].GetArrayAgent_AID();
     }
     for(i=0;i<numAgents;i++){
        /* System.out.println("price["+i+"]: "+price[i]);
         System.out.println("p["+i+"]: "+p[i]);
         System.out.println("aid["+i+"]: "+aid[i]);*/
     }

     for(l=0;l<numAgents;++l){
         System.out.println("Se analiza el precio del vector price["+l+"] ="+price[l]);
         price_upper=1.2*price_mean;
         if(price[l]>price_upper){
             discard++;
             
             System.out.println("El precio price["+l+"]: "+price[l]+" es demasiado alto. Supera al precio límite: "+price_upper);
             //It's saved in discard's array
             aid_d[e0]=aid[l];             
             price_d[e0]=price[l];
             p_d[e0]=p[l];
             e0++;
             
             //It's classified by price from cheaper to expensive by bubble method
             for(h=0; h<discard-1; ++h){
                 for(j=0;j<discard-1-h; ++j){
                     /*System.out.println("price[j+1] :"+price_d[j+1]);
                     System.out.println("price[j] :"+price_d[j]);*/

                     if(price_d[j+1]<price_d[j]){
               
                         //Order price
                         aux2=price_d[j+1];
                         price_d[j+1]=price_d[j];
                         price_d[j]=aux2;
                         
                         //Order power
                         aux1=p_d[j+1];
                         p_d[j+1]=p_d[j];
                         p_d[j]=aux1;
                         
                         //Order aid
                         aux0=aid_d[j+1];
                         aid_d[j+1]=aid_d[j];
                         aid_d[j]=aux0;
                     }
                     
                 }
                 
             }             
             
         }else{
             arrange++;
             
             System.out.println("El precio price["+l+"]="+price[l]+" es menor al precio medio establecido--> price_upper: "+price_upper);
             //It's saved in arranger's array
             aid_a[e]=aid[l];             
             p_a[e]=p[l];
             price_a[e]=price[l];
              e++;
             //It's classified by price from cheaper to expensive by bubble method
             for(h=0; h<arrange-1; ++h){
                 for(j=0;j<arrange-1-h; ++j){
                     /*System.out.println("p[j+1] :"+p_a[j+1]);
                     System.out.println("p[j] :"+p_a[j]);*/

                     if(p_a[j+1]>p_a[j]){
               
                         //Order price
                         aux2=price_a[j+1];
                         price_a[j+1]=price_a[j];
                         price_a[j]=aux2;
                         
                         //Order power
                         aux1=p_a[j+1];
                         p_a[j+1]=p_a[j];
                         p_a[j]=aux1;
                         
                         //Order aid
                         aux0=aid_a[j+1];
                         aid_a[j+1]=aid_a[j];
                         aid_a[j]=aux0;
                     }
                     
                 }
                 
             }             
         }
     }
     if(l==numAgents){
         //It's componed the array general by arrange's and discard's values 
         e=0;
         for(h=0;h<numAgents;h++){
             if(h<arrange){
                aid[h]=aid_a[h]; 
                p[h]=p_a[h];
                price[h]=price_a[h];
                /*System.out.println("aid["+h+"] :"+aid[h]);
                System.out.println("p["+h+"] :"+p[h]);
                System.out.println("price["+h+"] :"+price[h]);*/
                /*¡¡IT'S VERY IMPORTANT THAT THE INIZIALITATION OF OBJECT BE PLACED 
                  HERE, JUST BEFORE TO STORAGE NEW VALUES BECAUSE, IT MUST BE A NEW 
                  OBJECT FOR EACH VALUE OF ARRAY, NOT THE SAME OBJECT!!*/
                AgentFeatures ag2=new AgentFeatures();
                ag2.SetArrayAgent_AID(aid[h]);
                ag2.SetArrayAgent_p(p[h]);
                ag2.SetArrayAgent_p_price(price[h]);
                agentsG2[h]=ag2;
             }
             else{
                aid[h]=aid_d[e]; 
                p[h]=p_d[e];
                price[h]=price_d[e];
                /*System.out.println("aid["+h+"] :"+aid[h]);
                System.out.println("p["+h+"] :"+p[h]);
                System.out.println("price["+h+"] :"+price[h]);*/
                e++;
                /*¡¡IT'S VERY IMPORTANT THAT THE INIZIALITATION OF OBJECT BE PLACED 
                  HERE, JUST BEFORE TO STORAGE NEW VALUES BECAUSE, IT MUST BE A NEW 
                  OBJECT FOR EACH VALUE OF ARRAY, NOT THE SAME OBJECT!!*/
                AgentFeatures ag2=new AgentFeatures();
                ag2.SetArrayAgent_AID(aid[h]);
                ag2.SetArrayAgent_p(p[h]);
                ag2.SetArrayAgent_p_price(price[h]);
                agentsG2[h]=ag2;                
             }
         }
         
         
     }
     //Show the results
     for(h=0;h<numAgents;h++){
         
         System.out.println("agentsG2["+h+"]"+"AID :"+agentsG2[h].GetArrayAgent_AID());
         System.out.println("agentsG2["+h+"]"+"Potencia :"+agentsG2[h].GetArrayAgent_p()+"W");
         System.out.println("agentsG2["+h+"]"+"precio :"+agentsG2[h].GetArrayAgent_p_price()+"€");
     }
     
    
     return agentsG2;
        
    }
    
    public AgentFeatures[]DataOrganizer_batt_Ascending(int numBatts,AgentFeatures[] arrAgentFeatures){
        
     int arrange=0;
     int h=0;
     int l=0;
     int e=0;
     int e0=0;
     float aux1,aux2;
     int aux3;
     AID aux0;
     
     AID[] aid=new AID[numBatts];
     float[] p=new float[numBatts];
     float[] soc=new float[numBatts];
     int[] aCD=new int[numBatts];
     
     //Auxiliar variables to arranging and classifying the values of soc and power mainly

     AID[] aid_a=new AID[numBatts];//aid_arrange
     float[] p_a=new float[numBatts];//power_arrange
     float[] soc_a=new float[numBatts];
     int[] aCD_a=new int[numBatts];
     
     
     /**It's saved the values of matrix in arrays in order to be more 
      * comfortable to manage the information*/
     
     for(i=0;i<numBatts;++i){
         soc[i]=arrAgentFeatures[i].GetArrayAgent_soc();
         p[i]=arrAgentFeatures[i].GetArrayAgent_p();
         aCD[i]=arrAgentFeatures[i].GetArrayAgent_aCD();
         aid[i]=arrAgentFeatures[i].GetArrayAgent_AID();
     }
     for(i=0;i<numBatts;i++){
        /* System.out.println("price["+i+"]: "+price[i]);
         System.out.println("p["+i+"]: "+p[i]);
         System.out.println("aid["+i+"]: "+aid[i]);*/
     }

     for(l=0;l<numBatts;++l){
         
         System.out.println("Se analiza el SOC de la batería "+aid[l]+":  soc["+l+"] ="+soc[l]+"\n");
         arrange++;
         //It's saved in arranger's array
         aid_a[e]=aid[l];             
         p_a[e]=p[l];
         soc_a[e]=soc[l];
         aCD_a[e]=aCD[l];
         e++;
         //It's classified by price from cheaper to expensive by bubble method
         for(h=0; h<arrange-1; ++h){
                for(j=0;j<arrange-1-h; ++j){
                /*System.out.println("p[j+1] :"+p_a[j+1]);
                System.out.println("p[j] :"+p_a[j]);*/
                
                   if(soc_a[j+1]>soc_a[j]){
               
                        //Order SOC
                        aux2=soc_a[j+1];
                        soc_a[j+1]=soc_a[j];
                        soc_a[j]=aux2;
                         
                        //Order power
                        aux1=p_a[j+1];
                        p_a[j+1]=p_a[j];
                        p_a[j]=aux1;
                        
                        //Order aCD automatic Charge/Discharge
                        aux3=aCD_a[j+1];
                        aCD_a[j+1]=aCD_a[j];
                        aCD_a[j]=aux3;
                         
                        //Order aid
                        aux0=aid_a[j+1];
                        aid_a[j+1]=aid_a[j];
                        aid_a[j]=aux0;
                    }
                     
                }
                 
            }             
        }
     
     if(l==numBatts){
         //It's componed the array general by arrange's and discard's values 
         e=0;
         for(h=0;h<numBatts;h++){
             
                aid[h]=aid_a[h]; 
                p[h]=p_a[h];
                soc[h]=soc_a[h];
                aCD[h]=aCD_a[h];
                /*¡¡IT'S VERY IMPORTANT THAT THE INIZIALITATION OF OBJECT BE PLACED 
                  HERE, JUST BEFORE TO STORAGE NEW VALUES BECAUSE, IT MUST BE A NEW 
                  OBJECT FOR EACH VALUE OF ARRAY, NOT THE SAME OBJECT!!*/
                AgentFeatures ab2=new AgentFeatures();
                ab2.SetArrayAgent_AID(aid[h]);
                ab2.SetArrayAgent_p(p[h]);
                ab2.SetArrayAgent_soc(soc[h]);
                ab2.SetArrayAgent_aCD(aCD[h]);
                agentsB2[h]=ab2;                      
         }    
     }
     System.out.println("Se ordena el SOC de MAYOR a menor estado de carga\n");
     //Show the results
     for(h=0;h<numBatts;h++){
         
         System.out.println("agentsB2["+h+"]"+"AID :"+agentsB2[h].GetArrayAgent_AID());
         System.out.println("agentsB2["+h+"]"+"Potencia :"+agentsB2[h].GetArrayAgent_p()+"W");
         System.out.println("agentsB2["+h+"]"+"SOC :"+agentsB2[h].GetArrayAgent_soc()+"%");
         System.out.println("agentsB2["+h+"]"+"aCD :"+agentsB2[h].GetArrayAgent_aCD()+".\n");
     }
     return agentsB2;   
    }
    
    
    public AgentFeatures[]DataOrganizer_batt_Descending(int numBatts,AgentFeatures[] arrAgentFeatures){
        
     int arrange=0;
     int h=0;
     int l=0;
     int e=0;
     int e0=0;
     float aux1,aux2;
     int aux3;
     AID aux0;
     
     AID[] aid=new AID[numBatts];
     float[] p=new float[numBatts];
     float[] soc=new float[numBatts];
     int[] aCD=new int[numBatts];
     
     //Auxiliar variables to arranging and classifying the values of soc and power mainly

     AID[] aid_a=new AID[numBatts];//aid_arrange
     float[] p_a=new float[numBatts];//power_arrange
     float[] soc_a=new float[numBatts];
     int[] aCD_a=new int[numBatts];
     
     
     /**It's saved the values of matrix in arrays in order to be more 
      * comfortable to manage the information*/
     
     for(i=0;i<numBatts;++i){
         soc[i]=arrAgentFeatures[i].GetArrayAgent_soc();
         p[i]=arrAgentFeatures[i].GetArrayAgent_p();
         aCD[i]=arrAgentFeatures[i].GetArrayAgent_aCD();
         aid[i]=arrAgentFeatures[i].GetArrayAgent_AID();
     }
     for(i=0;i<numBatts;i++){
        /* System.out.println("price["+i+"]: "+price[i]);
         System.out.println("p["+i+"]: "+p[i]);
         System.out.println("aid["+i+"]: "+aid[i]);*/
     }

     for(l=0;l<numBatts;++l){
         
         System.out.println("Se analiza el SOC de la batería "+aid[l]+":  soc["+l+"] ="+soc[l]+"\n");
         arrange++;
         //It's saved in arranger's array
         aid_a[e]=aid[l];             
         p_a[e]=p[l];
         soc_a[e]=soc[l];
         aCD_a[e]=aCD[l];
         e++;
         //It's classified by price from cheaper to expensive by bubble method
         for(h=0; h<arrange-1; ++h){
                for(j=0;j<arrange-1-h; ++j){
                /*System.out.println("p[j+1] :"+p_a[j+1]);
                System.out.println("p[j] :"+p_a[j]);*/

                   if(soc_a[j+1]<soc_a[j]){
               
                        //Order SOC
                        aux2=soc_a[j+1];
                        soc_a[j+1]=soc_a[j];
                        soc_a[j]=aux2;
                         
                        //Order power
                        aux1=p_a[j+1];
                        p_a[j+1]=p_a[j];
                        p_a[j]=aux1;
                        
                        //Order aCD automatic Charge/Discharge
                        aux3=aCD_a[j+1];
                        aCD_a[j+1]=aCD_a[j];
                        aCD_a[j]=aux3;
                         
                        //Order aid
                        aux0=aid_a[j+1];
                        aid_a[j+1]=aid_a[j];
                        aid_a[j]=aux0;
                    }
                     
                }
                 
            }             
        }
     
     if(l==numBatts){
         //It's componed the array general by arrange's and discard's values 
         e=0;
         for(h=0;h<numBatts;h++){
             
                aid[h]=aid_a[h]; 
                p[h]=p_a[h];
                soc[h]=soc_a[h];
                aCD[h]=aCD_a[h];
                /*¡¡IT'S VERY IMPORTANT THAT THE INIZIALITATION OF OBJECT BE PLACED 
                  HERE, JUST BEFORE TO STORAGE NEW VALUES BECAUSE, IT MUST BE A NEW 
                  OBJECT FOR EACH VALUE OF ARRAY, NOT THE SAME OBJECT!!*/
                AgentFeatures ab2=new AgentFeatures();
                ab2.SetArrayAgent_AID(aid[h]);
                ab2.SetArrayAgent_p(p[h]);
                ab2.SetArrayAgent_soc(soc[h]);
                ab2.SetArrayAgent_aCD(aCD[h]);
                agentsB2[h]=ab2;                      
         }    
     }
     System.out.println("Se ordena el SOC de MENOR a mayor estado de carga\n");
     //Show the results
     for(h=0;h<numBatts;h++){
         
         System.out.println("agentsB2["+h+"]"+"AID :"+agentsB2[h].GetArrayAgent_AID());
         System.out.println("agentsB2["+h+"]"+"Potencia :"+agentsB2[h].GetArrayAgent_p()+"W");
         System.out.println("agentsB2["+h+"]"+"SOC :"+agentsB2[h].GetArrayAgent_soc()+"%");
         System.out.println("agentsB2["+h+"]"+"aCD :"+agentsB2[h].GetArrayAgent_aCD()+".\n");
     }
     return agentsB2;   
    }
}
