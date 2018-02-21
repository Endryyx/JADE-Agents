/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas_1_7;

/** @author endryys*/

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportCSV {
    
    public void CreatefileCSV(String filepath, String delim,String[] pcc_initial, String[] pcc_final, String[] p_batt ){
        
        final String NEXT_LINE = "\n";
        int i=0;
        String pcc_initial_str,pcc_final_str,p_batt_str=new String();
        
        try {
		FileWriter fw = new FileWriter(filepath);
                
                /*fw.append("pcc_initial").append(NEXT_LINE);
                for(i=0; i<29;i++){
                  fw.append(Float.toString(pcc_initial[i])).append(NEXT_LINE);  
                }*/
                
                fw.append("pcc_initial;p_batt;pcc_final\n");
                for(i=0; i<29;i++){
                  
                  //pcc_initial_str=Float.toString(pcc_initial[i]);
                  pcc_initial_str=pcc_initial[i];
                  pcc_final_str=pcc_final[i];
                  p_batt_str=p_batt[i];
                  
                  fw.append(pcc_initial_str+";"+p_batt_str+";"+pcc_final_str+"\n"); 
                }
                
		/*fw.append("testing").append(delim);
		fw.append("123").append(NEXT_LINE);

		fw.append("value1");
		fw.append(delim);
		fw.append("312");
		fw.append(NEXT_LINE);

		fw.append("anotherthing,888\n");*/

		fw.flush();
		fw.close();
		} catch (IOException e) {
		// Error al crear el archivo, por ejemplo, el archivo 
		// está actualmente abierto.
		e.printStackTrace();
		}
    }
    
    //public void CreatefileCSVBATT(String filepath, String delim,float[] p_batt, float[] soc_batt){
    public void CreatefileCSVBATT(String filepath, String delim,String[] p_batt, String[] soc_batt){
        final String NEXT_LINE = "\n";
        int i=0;
        String p_batt_str,soc_batt_str=new String();
        
        try {
		FileWriter fw = new FileWriter(filepath);
                
                /*fw.append("pcc_initial").append(NEXT_LINE);
                for(i=0; i<29;i++){
                  fw.append(Float.toString(pcc_initial[i])).append(NEXT_LINE);  
                }*/
                
                fw.append("p_batt;soc\n");
                for(i=0; i<29;i++){
                  p_batt_str=p_batt[i];
                  soc_batt_str=soc_batt[i];
                  /*p_batt_str=Float.toString(p_batt[i]);
                  soc_batt_str=Float.toString(soc_batt[i]);*/
                  
                  fw.append(p_batt_str+";"+ soc_batt_str+"\n"); 
                }
                
		/*fw.append("testing").append(delim);
		fw.append("123").append(NEXT_LINE);

		fw.append("value1");
		fw.append(delim);
		fw.append("312");
		fw.append(NEXT_LINE);

		fw.append("anotherthing,888\n");*/

		fw.flush();
		fw.close();
		} catch (IOException e) {
		// Error al crear el archivo, por ejemplo, el archivo 
		// está actualmente abierto.
		e.printStackTrace();
		}
    }
     
            
  /* public static void main(String[] args) {
     
        List<usuario> usuarios = new ArrayList<usuario>();
         
        usuarios.add(new Usuario("1001","Jose","Ramirez Torres","jramirez89@hotmail.com"));
        usuarios.add(new Usuario("1002","Saul","Gaviria Garcia","sgaviria12@gmail.com"));
        usuarios.add(new Usuario("1003","Maria","Torres Mendoza","mtorres12@yahoo.com"));
         
        String outputFile = "test/usuarios_export.csv";
        boolean alreadyExists = new File(outputFile).exists();
         
        if(alreadyExists){
            File ficheroUsuarios = new File(outputFile);
            ficheroUsuarios.delete();
        }        
         
        try {
         
            CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');
             
            csvOutput.write("Codigo");
            csvOutput.write("Nombres");
            csvOutput.write("Apellidos");
            csvOutput.write("Correo");
            csvOutput.endRecord();
             
            for(Usuario us : usuarios){
                 
                csvOutput.write(us.getCodigo());
                csvOutput.write(us.getNombres());
                csvOutput.write(us.getApellidos());
                csvOutput.write(us.getCorreo());
                csvOutput.endRecord();                   
            }
             
            csvOutput.close();
         
        } catch (IOException e) {
            e.printStackTrace();
        }
    } */
}
