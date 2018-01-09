/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas_1_5;
import jade.core.AID;
/**
 *
 * @author endryys
 */
public class PowerSelector {
  int j, k,n;
    private float lastPrice;
    private AID lastGen;
    private float lastPower;
   
    //Inside this array it will be saved all the elements
    public AgentFeatures[] agentsG=new AgentFeatures[30];
    public int i;
    public AgentFeatures[]agentsG2=new AgentFeatures[30];

 
    public AgentFeatures[] DataStorage(int numAgents,AID generator_id, float p_generated, float price){
        
    AgentFeatures ag=new AgentFeatures();
    ag.SetArrayAgent_AID(generator_id);
    ag.SetArrayAgent_p(p_generated);
    ag.SetArrayAgent_p_price(price);
    
    agentsG[i]=ag;      
     i++;

     //if (i>1){
        //System.out.println("matrix1"+"["+0+"]"+"["+1+"]"+":"+matrixAgentFeatures[0][1].GetArrayAgent_AID());
        /*System.out.println("agentsG: "+agentsG[1].GetArrayAgent_AID());
        System.out.println("agentsG: "+agentsG[1].GetArrayAgent_p());
        System.out.println("agentsG: "+agentsG[1].GetArrayAgent_p_price());
        System.out.println("agentsG: "+agentsG[0].GetArrayAgent_AID());
        System.out.println("agentsG: "+agentsG[0].GetArrayAgent_p());
        System.out.println("agentsG: "+agentsG[0].GetArrayAgent_p_price());*/
     //}
     return  agentsG; 
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
     AID[] aid_d=new AID[numAgents];
     AID[] aid_a=new AID[numAgents];
     float[] p_d=new float[numAgents];
     float[] p_a=new float[numAgents];
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
             /*System.out.println("aid["+l+"] :"+aid[l]);
             System.out.println("price["+l+"] :"+price[l]);
             System.out.println("p["+l+"] :"+p_d[l]);*/
             /*System.out.println("aid_d["+e0+"] :"+aid_d[e0]);
             System.out.println("price_d["+e0+"] :"+price_d[e0]);
             System.out.println("p_d["+e0+"] :"+p_d[e0]);*/
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
               /*if(price[l]>lastPrice){
                 lastPrice=price[l];
                 lastGen=aid[l];
                 lastPower=p[l];
                 h=l+1;
                 for(k=h;k<numAgents;++k){
                    price[k-1]=price[k];
                    aid[k-1]=aid[k];
                    p[k-1]=p[k];
                 }
                 price[numAgents-1]=lastPrice;
                 aid[numAgents-1]=lastGen;
                 p[numAgents-1]=lastPower;
                 
             }else{
                 System.out.println("El precio price["+l+"]="+price[l]+"no es el precio más alto.");
                 for(k=1;k<numAgents;++k){
                     if(price[l]>price[numAgents-k]){
                         h=l+1;
                         for(k=h; k<numAgents-1;++k){
                             price[k-1]=price[k];
                             aid[k-1]=aid[k];
                             p[k-1]=p[k];
                         }
                         price[numAgents-k]=price[l];
                         aid[numAgents-k]=aid[l];
                         p[numAgents-k]=p[l];
                     }
                 }
             }*/
             
         }else{
             arrange++;
             
             System.out.println("El precio price["+l+"]="+price[l]+" es menor al precio medio establecido--> price_upper: "+price_upper);
             //It's saved in arranger's array
             aid_a[e]=aid[l];             
             p_a[e]=p[l];
             price_a[e]=price[l];
            /* System.out.println("aid_a["+e+"]: "+aid_a[e]);
             System.out.println("p_a["+e+"]: "+p_a[e]);
             System.out.println("price_a["+e+"]: "+price_a[e]);*/
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
             
             
             /*//It's used the bubble algorithm for arraging the info from arrays
             //The arrange of prices are commented, and it's arrange the power
             for(h=0; h<numAgents-1-discard;++h){
                 for(j=0;j<numAgents-1-discard-h;++j){
                     //System.out.println("price[j+1] :"+price[j+1]);
                     //System.out.println("price[j] :"+price[j]);
                     System.out.println("p[j+1] :"+p[j+1]);
                     System.out.println("p[j] :"+p[j]);
                     //if(price[j+1]>price[j]){
                     if(p[j+1]>p[j]){
                         
                         //Order price
                         aux2=price[j+1];
                         price[j+1]=price[j];
                         price[j]=aux2;
                         
                         //Order power
                         
                         aux1=p[j+1];
                         p[j+1]=p[j];
                         p[j]=aux1;
                         
                         //Order aid
                         aux0=aid[j+1];
                         aid[j+1]=aid[j];
                         aid[j]=aux0;
                     }
                     
                 }
                 
             }*/
         }
         //It's perform the inverse transform from arrays to matrix2
         /*System.out.println("aid["+l+"]: "+aid[l]);
         System.out.println("p["+l+"]: "+p[l]);
         System.out.println("price["+l+"]: "+price[l]);*/
         
       /*¡¡IT'S VERY IMPORTANT THAT THE INIZIALITATION OF OBJECT BE PLACED 
         HERE, JUST BEFORE TO STORAGE NEW VALUES BECAUSE, IT MUST BE A NEW 
         OBJECT FOR EACH VALUE OF ARRAY, NOT THE SAME OBJECT!!*/
        /*AgentFeatures ag2=new AgentFeatures();
        ag2.SetArrayAgent_AID(aid[l]);
        ag2.SetArrayAgent_p(p[l]);
        ag2.SetArrayAgent_p_price(price[l]);
        agentsG2[l]=ag2;*/

     }
     if(l==numAgents){
         /*for(h=0;h<discard;h++){
             System.out.println("aid_d["+h+"] :"+aid_d[h]);
             System.out.println("p_d["+h+"] :"+p_d[h]);
             System.out.println("price_d["+h+"] :"+price_d[h]);             
         }
         for(h=0;h<arrange;h++){
             System.out.println("aid_a["+h+"] :"+aid_a[h]);
             System.out.println("p_a["+h+"] :"+p_a[h]);
             System.out.println("price_a["+h+"] :"+price_a[h]);             
         }*/
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
}
