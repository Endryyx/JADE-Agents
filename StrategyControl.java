/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas_1_10;

/**
 *
 * @author endryys
 */
public class StrategyControl {
    
    //Attributes 
    public float pcc_upper=100;
    public float pcc_lower=-100;
    
    
    //Class method to control Peak Shaving
    public float[] PeakShaving(float pcc_initial){
        
        float pcc_upper;
        float pcc_lower;
        float p_diff=0;
        int status=0;
        int threshold=0;
    
        /*Thresholds of deliveried power to grid (p_lower) or 
        absorbed power(p_upper)*/
        pcc_upper=this.pcc_upper;
        pcc_lower=this.pcc_lower;
        //pcc_upper=0.1*p_max_generators;//Demand side
        //pcc_lower=0.85*p_max_generators;//Generation side
        //pcc_initial=p_consumers-p_generators;
        
        
        //If power is beyond upper threshold means that demand overcome generation
        if (pcc_initial>pcc_upper){           
            //Batteries must provide energy
            status=1;
            threshold=0;
            p_diff=pcc_initial-pcc_upper;
            System.out.println("\np_diff = "+p_diff);
        }
        
        //If power is below lower threshold means that generation overcome demand
        if(pcc_initial< pcc_lower){           
            //Batteries must absorb energy
            status=2;
            threshold=0;
            p_diff=pcc_initial-pcc_lower;//Take into account that pcc_initial (-) and pcc_lower(-), so p_diff=-200-(-100)=-100
        }
        /*If power is in range of thresholds, batteries can sent and absorb
        energy depending on their soc(%)*/
        if(pcc_lower<=pcc_initial && pcc_initial<=pcc_upper){   
            //If demand is superior to the generation
            status=3;
            if(pcc_initial>0){              
             //Batteries activate automatic charge
                if(pcc_initial==pcc_upper){
                  threshold=1;
                  p_diff=pcc_upper;  
                }else{
                  threshold=0;
                  //p_diff=pcc_upper-pcc_initial;
                  p_diff=pcc_initial; 
                }
               
            }
            //If generation is superior to the demand
            if(pcc_initial<0){               
             //Batteries activate automatic discharge
             if(pcc_initial==pcc_lower){
               threshold=-1;
               p_diff=pcc_lower;  
             }else{
                 threshold=0;
                 //limite al que se puede llegar para no sobre pasar los limites de potencia
               //p_diff=pcc_initial-pcc_lower;
               p_diff=pcc_initial;
             }
             
            }           
        }
        float controlP[]={p_diff,status,threshold};
        System.out.println("controlP[] = "+controlP[0] +" , "+ controlP[1]+" , "+controlP[2]+"\n");
        return controlP;    

    }
    //Class method to control Q control
    public double[] QControl(){
        double qcc_final[]={0,0};
        return qcc_final;
        
    }
    //Class method to control Smoothing
    public void Smoothing(){
        
    }        
     
}
