/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
// cliente.java
package cliente;
import iServidorDonaciones.IServidorDonaciones;
import java.rmi.registry.LocateRegistry;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.util.Scanner;




public class Cliente {
    public static void main(String[] args) {
        String host = "localhost";
        String nombreCliente = "";
        int idServidor = 0; //cambiar para conectarse a los distintos servidores
        String nombreServidor = "servidor" + idServidor;
        int puerto = 1099 + idServidor;
        int opcion = 0;
        int donacion = 0;
        
        Scanner teclado = new Scanner (System.in);
        
        //System.out.println ("Escriba el nombre o IP del servidor: ");
        //host = teclado.nextLine();
        System.out.println ("Escriba el nombre del cliente: ");
        nombreCliente = teclado.nextLine();
        
            
        // Crea e instala el gestor de seguridad
        if (System.getSecurityManager() == null) {System.setSecurityManager(new SecurityManager());}
        try {
            // Crea el stub para el cliente especificando el nombre del servidor
            Registry mireg = LocateRegistry.getRegistry(host, puerto);
            IServidorDonaciones miServidor = (IServidorDonaciones)mireg.lookup(nombreServidor);
            
            while(opcion != 1 && opcion != 2){
                System.out.println ("****************************************************************************");
                System.out.println ("Bienvenido al sistema de donaciones RMI");
                System.out.println ("****************************************************************************");   
                System.out.println ("Seleccione una opción:");   
                System.out.println ("1. REGISTRARSE / CONECTAR.");                  
                System.out.println ("2. SALIR.");
                System.out.println ("****************************************************************************");
                
                opcion = Integer.parseInt(teclado.nextLine());
            }

            if(opcion == 1){  //REGISTRAR
                int id = miServidor.registrar(nombreCliente);
                if(idServidor != id){
                    idServidor = id;
                    nombreServidor = "servidor" + idServidor;
                    puerto = 1099 + idServidor;
                    mireg = LocateRegistry.getRegistry(host, puerto);
                    miServidor = (IServidorDonaciones)mireg.lookup(nombreServidor);
                }
                while(opcion != 3){
                    System.out.println ("**************************************************************************************************");   
                    System.out.println ("Seleccione una opción:");                  
                    System.out.println ("1. DONAR. (Numeros enteros)"); 
                    System.out.println ("2. CONSULTAR."); 
                    System.out.println ("3. SALIR.");
                    System.out.println ("5. SUBTOTAL. (PARA HACER PRUEBAS)");
                    System.out.println ("**************************************************************************************************");

                    opcion = Integer.parseInt(teclado.nextLine());

                    if(opcion==1){          //DONAR
                        System.out.println ("DONAR");
                        System.out.println ("Elige cantidad a donar:");
                        donacion = Integer.parseInt(teclado.nextLine());
                        miServidor.donar(donacion, nombreCliente);
                    }else if(opcion==2){    //CONSULTAR DONACIONES    
                        int resultado = miServidor.consultar(nombreCliente);
                        if(resultado != -1)
                            System.out.println ("Total donaciones actual: " + resultado);
                        else
                            System.out.println ("Debe hacer una donación para poder consultar el total recaudado, disculpe las molestias ");
                    }else if(opcion==5){     //SUBTOTAL
                        int resultado = miServidor.getSubtotal();
                        System.out.println ("Total donaciones actual: " + resultado);
                    }
                } 
            }
            
            
        } catch(NotBoundException | RemoteException e) {
            System.err.println("Exception del sistema: " + e);
        }
        System.exit(0);
    }
}