package mas_1_10;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/** @author endryys*/

public class ImportCSV {
    
   public static final char SEPARATOR=';';
   public static final char QUOTE='"';
   
 public String[] ImportFile(String file_path) throws IOException{
     
     String line=new String();
     int contador = 0;
     String[] data_output;
     int j=0,i=0;
     data_output=new String[48];
     int n_line=-1; //Se inicializa a -1 ya que la primer linea que lee la sentencia corresponde a un identificador del archivo[]
    
     
     //Se genera el objeto para poder leer el archivo csv
     BufferedReader buffer_reader=new BufferedReader(new FileReader(file_path));
     
     while(line!=null){
         
         //Se recogen los datos de la línea separado por ";"
         String [] data=line.split(String.valueOf(SEPARATOR));
         
         //Se imprime la línea
         //System.out.println(Arrays.toString(data));
         
         //Recorremos el arrar de string
            for (i = 0; i<data.length; i++) {
                
                //Se imprime el primer caracter de el arreglo de strings
               // System.out.println(data[i]);
                if(i==1 && n_line>0){
                    data_output[j]=data[i];
                    j++;
                }
                
            }  
         //Se vuelve a leer la línea
         line=buffer_reader.readLine();
         n_line++;
     }
     
     // Se cierra el buffer de lectura
     if (buffer_reader != null) {
        buffer_reader.close();
     }
     
     return data_output;
 }
 
}
