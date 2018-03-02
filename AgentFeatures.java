/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas_1_10;
import jade.core.AID;

/**
 *
 * @author endryys
 */
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
    public float p_price;
    public float soc;
    public int aCD;
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
    public float GetArrayAgent_soc(){
    
       return soc;
    }
    public void SetArrayAgent_soc(float soc){
        this.soc=soc;
        
    }
    public float GetArrayAgent_p_price(){
      
        return p_price;
    }
    public void SetArrayAgent_p_price(float p_price){
        this.p_price=p_price;
        
    }
    public int GetArrayAgent_aCD(){
        
        return aCD;
    }
    public void SetArrayAgent_aCD(int aCD){
        this.aCD=aCD;
        
    }        
}

