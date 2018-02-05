package mas_1_7;
import jade.core.AID;
/**@author endryys */
public class BatteryFunction {

    public float[] Battery_Information(float p_diff, float status_, float threshold_,float soc_,float p_nominal_, String agentBattery_name){
    
        float [] batt_output=new float[3];
    
        int status=0,threshold=0;
        float soc_max,soc_min,soc_upper,soc_lower;
        float ti=1800; //The time interval always is 30 minutes
        float p_charge,p_discharge;

        float p_batt_output_=0;
        float aCD;
        boolean charge=false,discharge=false;
    
        soc_min=5;
        soc_max=98;
        soc_upper=40;
        soc_lower=30;

    
        if(status_==1.0){status=1;}
        if(status_==2.0){status=2;}
        if(threshold_==-1.0){threshold=-1;}
        if(threshold_==0.0){threshold=0;} 
        if(threshold_==1.0){threshold=1;} 
    
        //El status 3 entra solo cuando soc no está fuera de los limites de soc_lower y soc_upper
        //y cuando la pcc_initial se encuentra dentro de los límites del PeakShaving
        if(status_==3.0){
            status=3;
            if (soc_<soc_lower){
                          
                charge=true;
                discharge=false;
            }
            if(soc_>soc_upper){
                           
                discharge=true;
                charge=false;
            }
                        
            if(soc_>=soc_lower && soc_<=soc_upper){
                               
                charge=false;
                discharge=false;
            }
        }
                    
            switch(status){
                        
                        case 1: //Battery deliver energy
                            //System.out.println("La batería entra en fase de entrega de energía");
                            aCD=0;
                            if(soc_<=soc_min){
                
                                System.out.println("La bateria "+agentBattery_name+" tiene un nivel demasiado bajo, no puede entregar energía.");
                                p_batt_output_=0;
                                batt_output[0]=p_batt_output_;
                                batt_output[1]=soc_;
                                batt_output[2]=aCD;
                            }else{
                                if(p_diff>=p_nominal_){
                                    p_batt_output_=p_nominal_;
                                    batt_output[0]=p_batt_output_;
                                    batt_output[1]=soc_;
                                    batt_output[2]=aCD;                                    
                                }else{
                                    p_batt_output_=p_diff;
                                    batt_output[0]=p_batt_output_;
                                    batt_output[1]=soc_;
                                    batt_output[2]=aCD;                                    
                                }
                            }
                            
                            break;
                            
                        case 2://Battery consum energy
                            //System.out.println("La batería entra en fase de consumo de energía");
                            aCD=0;
                            if(soc_>=soc_max){
                               System.out.println("La bateria "+agentBattery_name+" tiene un nivel demasiado alto, no puede absorber más energía.");
                               p_batt_output_=0;
                                batt_output[0]=p_batt_output_;
                                batt_output[1]=soc_;
                                batt_output[2]=aCD;
                            }else{
                                p_diff=-p_diff;
                                if(p_diff>=p_nominal_){
                                    p_batt_output_=p_nominal_;
                                    batt_output[0]=p_batt_output_;
                                    batt_output[1]=soc_;
                                    batt_output[2]=aCD;                                    
                                }else{
                                    p_batt_output_=p_diff;
                                    batt_output[0]=p_batt_output_;
                                    batt_output[1]=soc_;
                                    batt_output[2]=aCD;       
                                }
                            }    
                            
                            break;
                            
                        case 3://Battery automatic charge/discharge
                            
                            if(charge==true){
                                //System.out.println("La batería entra en fase de carga automática");
                                if(threshold==0 || threshold==-1){
                                    p_charge=(p_nominal_+p_nominal_*((soc_lower-soc_)/(soc_lower-soc_min)))/2;
                                    if(p_charge<=p_diff){
                                        aCD=1;
                                        if(p_charge<0){p_charge=-p_charge;}    
                                            p_batt_output_=p_charge;
                                            System.out.println("\nLa bateria "+agentBattery_name+" puede absorber una potencia: "+p_batt_output_);
                                            batt_output[0]=p_batt_output_;
                                            batt_output[1]=soc_;
                                            batt_output[2]=aCD;
                                            //Se deja en cero p_batt_output, ya que en este caso no debe consumir energia del sistema dado que esta dentro de los limites
                                            //p_batt_output_=0;
                                    }else{
                                        p_charge=p_diff;
                                        aCD=1;
                                        if(p_charge<0){p_charge=-p_charge;}
                                            p_batt_output_=p_charge;
                                            System.out.println("\nLa bateria "+agentBattery_name+" puede absorber una potencia: "+p_batt_output_);
                                            batt_output[0]=p_batt_output_;
                                            batt_output[1]=soc_;
                                            batt_output[2]=aCD;
                                    }
                                }else{
                                    p_batt_output_=0;
                                    aCD=0;
                                    batt_output[0]=p_batt_output_;
                                    batt_output[1]=soc_;
                                    batt_output[2]=aCD;
                                }
                            }
                            if(discharge==true){
                                //System.out.println("La batería entraría en fase de descarga automática");
                                if(threshold==0 || threshold==1){
                                    p_discharge=(p_nominal_+p_nominal_*((soc_-soc_upper)/(soc_max-soc_upper)))/2;
                                    if(p_discharge<=p_diff){
                                    aCD=-1;
                                    if(p_discharge<0){p_discharge=-p_discharge;} 
                                        p_batt_output_=p_discharge;
                                        System.out.println("\nLa bateria "+agentBattery_name+" cedería una potencia: "+p_batt_output_);
                                        batt_output[0]=p_batt_output_;
                                        batt_output[1]=soc_;
                                        batt_output[2]=aCD;
                                        //Se deja en cero p_batt_output, ya que en este caso no debe entregar energia del sistema dado que esta dentro de los limites
                                        //p_batt_output_=0;
                                    }else{
                                        p_discharge=p_diff;
                                        aCD=-1;    
                                        if(p_discharge<0){p_discharge=-p_discharge;} 
                                        p_batt_output_=p_discharge;
                                        System.out.println("\nLa bateria "+agentBattery_name+" cedería una potencia: "+p_batt_output_);
                                        batt_output[0]=p_batt_output_;
                                        batt_output[1]=soc_;
                                        batt_output[2]=aCD;                                        
                                    }
                                }else{
                                    p_batt_output_=0;
                                    aCD=0;
                                    batt_output[0]=p_batt_output_;
                                    batt_output[1]=soc_;
                                    batt_output[2]=aCD;                                    
                                }                                
                            }
                            //Fase de carga y descarga eficiente
                            /*Esta fase entra cuando el SOC actual de la batería esta dentro de los rangos establecidos, que no tiene la necesidad de carga/descarga automática
                            y puede aportar o consumir del sistema para amortiguar más la curva Pcc_final.*/
                            if(charge==false && discharge==false){
                               
                                if (p_diff!=0){
                                    if(p_diff>0){
                                        p_discharge=p_diff;
                                        aCD=0;
                                        p_batt_output_=p_discharge;  
                                        System.out.println("La bateria "+agentBattery_name+" cedería :"+p_batt_output_);
                                        batt_output[0]=p_batt_output_;
                                        batt_output[1]=soc_;
                                        batt_output[2]=aCD;                                        
                                    }else{
                                        p_charge=p_diff;
                                        p_batt_output_=p_charge;
                                        aCD=0;
                                        System.out.println("La bateria "+agentBattery_name+" absorbería :"+p_batt_output_);
                                        batt_output[0]=p_batt_output_;
                                        batt_output[1]=soc_;
                                        batt_output[2]=aCD;
                                    }
                                }else{
                                p_batt_output_=0;
                                aCD=0;
                                batt_output[0]=p_batt_output_;
                                batt_output[1]=soc_;
                                batt_output[2]=aCD;
                                
                                }
                            }
                            break;        
                    }
    return batt_output;
    }     
}
