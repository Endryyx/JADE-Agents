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
public class MainControl {
//Class method getSetPointsfloat
    public double[] getSetPoints(double[][]control_input){
        //infoD[3]={period, p_demand, q_demand};
        //infoG[5]={period, p_generation, q_generation, p_losses, cost_generation};
        //infoBatt[7]={period,p_batt_nom,capacity,soc_max,soc_min,soc_initial,u_nom};
        int strategy=1;
        double control_output[]=new double[2];
        double batteries_output[];
        StrategyControl controlP=new StrategyControl();
        StrategyControl controlQ=new StrategyControl();
        
        double p_demand=control_input[0][1];
        System.out.println("p_demand = "+p_demand);
        double p_generation=control_input[1][1];
        System.out.println("p_generation = "+p_generation);
        double pcc_initial=control_input[2][0];
        System.out.println("p_initial = "+pcc_initial);
               
        switch(strategy){
            
                case 1:
                    
                  // control_output=controlP.PeakShaving(pcc_initial);
                   System.out.println("control_output[0]="+control_output[0]);
                   System.out.println("control_output[1]="+control_output[1]);
                   return control_output;
                    
                case 2:
                    
                    control_output=controlQ.QControl();
                    //return infoProccessed;
                    
        }
       System.out.println("control_output[0]="+control_output[0]);
       System.out.println("control_output[1]="+control_output[1]);
       return control_output;
            

    }       
}
