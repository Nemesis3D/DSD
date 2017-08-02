/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
// servidor.java = Programa servidor
package servidor;
import servidorDonaciones.ServidorDonaciones;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.Scanner;


public class Servidor {
    public static void main(String[] args) throws AlreadyBoundException { 
        int nServidores=2;
        
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());}
        try {
            
            // Crea una instancia del servidor (id, númeroServidores, dirección)
            Registry reg=LocateRegistry.createRegistry(1099);
            ServidorDonaciones servidor = new ServidorDonaciones(0, nServidores, "localhost");
            reg.bind("servidor0", servidor);
            System.out.println("Servidor de donaciones 0 preparado");
            
            // Crea una instancia del servidor (id, númeroServidores, dirección)
            Registry reg1=LocateRegistry.createRegistry(1100);
            ServidorDonaciones servidor1 = new ServidorDonaciones(1, nServidores, "localhost");
            reg1.bind("servidor1", servidor1);
            System.out.println("Servidor de donaciones 1 preparado");
            
   
                
            System.out.println("Servidor RemoteException | MalformedURLExceptiondor preparado");
        }catch (RemoteException e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}