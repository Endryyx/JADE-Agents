/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas_1_5;

/**
 *
 * @author endryys
 */
public class StrategyControl {
//Class method to control Peak Shaving
    public float[] PeakShaving(float pcc_initial){
        
        float pcc_upper;
        float pcc_lower;
        float p_diff=0;
        int status=0;
    
        /*Thresholds of deliveried power to grid (p_lower) or 
        absorbed power(p_upper)*/
        pcc_upper=100;
        pcc_lower=-100;
        //pcc_upper=0.1*p_max_generators;//Demand side
        //pcc_lower=0.85*p_max_generators;//Generation side
        //pcc_initial=p_consumers-p_generators;
        
        
        //If power is beyond upper threshold means that demand overcome generation
        if (pcc_initial>pcc_upper){           
            //Batteries must provide energy
            status=1;
            p_diff=pcc_initial-pcc_upper;
            System.out.println("\np_diff = "+p_diff);
        }
        
        //If power is below lower threshold means that generation overcome demand
        if(pcc_initial< pcc_lower){           
            //Batteries must absorb energy
            status=2;
            p_diff=pcc_initial-pcc_lower;//Take into account that pcc_initial (-) and pcc_lower(-), so p_diff=-200-(-100)=-100
        }
        /*If power is in range of thresholds, batteries can sent and absorb
        energy depending on their soc(%)*/
        if(pcc_lower<pcc_initial && pcc_initial<pcc_upper){   
            //If demand is superior to the generation
            status=3;
            if(pcc_initial>0){              
             //Batteries activate automatic charge
               p_diff=pcc_initial;
            }
            //If generation is superior to the demand
            if(pcc_initial<0){               
             //Batteries activate automatic discharge
             p_diff=pcc_initial;
            }           
        }
        float controlP[]={p_diff,status};
        System.out.println("controlP[] = "+controlP[0] +" , "+ controlP[1]+"\n");
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
