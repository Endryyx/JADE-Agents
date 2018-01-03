package mas_1_4;

import jade.core.AID;

/**@author endryys*/
/*This class is a special kind of array, this array serves to storage differents
types of data from an agent. Mainly:
Agent's name (AID)
Agent's active power(float)
Agent's reactive power (float)
Agent's apparent power (float)
Active Power's price(float)
Reactive Power's price(float)*/


public class AgentFeatures {
  //Attributes
    public AID agentAID;
    public float p;
    public float q;
    public float p_price;
    public float q_price;
    public float s;
    //public int i; 

    public AID GetArrayAgent_AID(){
        
     return agentAID;
    }
    public void SetArrayAgent_AID(AID agentAID){
        this.agentAID=agentAID;
        
    }
    public float GetArrayAgent_p(){
    
       return p; 
    }
    public void SetArrayAgent_p(float p){
        this.p=p;
        
    }
    public float GetArrayAgent_q(){
    
       return q;
    }
    public void SetArrayAgent_q(float q){
        this.q=q;
        
    }
    public float GetArrayAgent_p_price(){
      
        return p_price;
    }
    public void SetArrayAgent_p_price(float p_price){
        this.p_price=p_price;
        
    }
    public float GetArrayAgent_q_price(){
        
        return q_price;
    }
    public void SetArrayAgent_q_price(float q_price){
        this.q_price=q_price;
        
    }  
    public float GetArrayAgent_s(){
        
        return s;
    }
    public void SetArrayAgent_s(float s){
        this.s=s;
        
    }    
}
