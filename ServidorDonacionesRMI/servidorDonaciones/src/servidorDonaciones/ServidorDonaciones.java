/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ghueli
 */
// contador.java
package servidorDonaciones;

import iServidorDonaciones.IServidorDonaciones;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class ServidorDonaciones extends UnicastRemoteObject implements IServidorDonaciones {
    private int suma;           //subtotal donaciones en el servidor
    private int nServidores;    //número de servidores
    private int idServidor;     //id del servidor
    private String direccionServidor;   //localhost en nuestro caso
    private ArrayList<String> registro = new ArrayList<String>();   //lista usuarios registrados
    private ArrayList<String> donaciones = new ArrayList<String>(); //indica si los usuarios han donado o no
    private int nUsuarios;      //número de usuarios registrados
       
    public ServidorDonaciones(int id, int valor, String direccion) throws RemoteException{
        idServidor = id;
        suma = 0;
        nServidores = valor;
        direccionServidor = direccion;
        nUsuarios = 0;
    }
    
    /* 
    Comprueba si el usuario está registrado en todas las réplicas y devuelve el id del servidor 
    donde se ha registrado, tanto si estaba registrado ya o lo ha hecho en la llamada actual    
    */
    public int registrar(String usuario) throws RemoteException{
        boolean registrado = false;
        int servidorFinal = idServidor;
        
        if(!registro.contains(usuario)){ //si no registrado en este servidor se busca en los otros
            int contador = registro.size(); 
            int i; int puerto; int aux;
            String nombreServidor;
            Registry mireg ;
            IServidorDonaciones miServidor ;

            for(i=0; i<nServidores; i++){
                if(i != idServidor){
                    puerto = 1099 + i;
                    nombreServidor ="servidor" + i;
                    if (System.getSecurityManager() == null) {System.setSecurityManager(new SecurityManager());}
                    try {
                        // Crea el stub para el cliente especificando el nombre del servidor
                        mireg = LocateRegistry.getRegistry(direccionServidor, puerto);
                        miServidor = (IServidorDonaciones)mireg.lookup(nombreServidor);
                        aux = miServidor.buscarUsuario(usuario);
                        if(aux == -1){
                            registrado = true;
                            servidorFinal = i;
                            break;
                        }else if(aux < contador){
                            contador = aux;
                            servidorFinal = i;
                        }

                    } catch(NotBoundException | RemoteException e) {
                        System.err.println("Exception del sistema: " + e);
                    }
                }
            }
            if (System.getSecurityManager() == null) {System.setSecurityManager(new SecurityManager());}
                try {
                //registramos el usuario en el servidor con menos registrados
                puerto = 1099 + servidorFinal;
                nombreServidor ="servidor" + servidorFinal;
                mireg = LocateRegistry.getRegistry(direccionServidor, puerto);
                miServidor = (IServidorDonaciones)mireg.lookup(nombreServidor);
                miServidor.addusuario(usuario, "0");
                } catch(NotBoundException | RemoteException e) {
                    System.err.println("Exception del sistema: " + e);
                }
            System.out.println ("No encontrado --->servidor"+servidorFinal);
            nUsuarios++;
        }else{ //registrado en este servidor
            registrado = true;
            System.out.println ("Encontrado --->servidor"+servidorFinal);
        }

        
        return servidorFinal;
    }
    
    /*
    Comprueba si el usuario que quiere hacer la consulta del total donado ha hecho alguna donación,
    si es así, devuelve la suma de los subtotales de cada réplica y si no, devuelve -1
    */
    public int consultar(String usuario) throws RemoteException {
        int resultado = 0; int i; int puerto; int aux;
        String nombreServidor;
        Registry mireg ;
        IServidorDonaciones miServidor ;
        
        int pos = registro.indexOf(usuario);
        String haDonado = donaciones.get(pos);
                
        if(haDonado.equals("1")){      
            for(i=0; i<nServidores; i++){
                if(i != idServidor){
                    puerto = 1099 + i;
                    nombreServidor ="servidor" + i;
                    if (System.getSecurityManager() == null) {System.setSecurityManager(new SecurityManager());}
                    try {
                        // Crea el stub para el cliente especificando el nombre del servidor
                        mireg = LocateRegistry.getRegistry(direccionServidor, puerto);
                        miServidor = (IServidorDonaciones)mireg.lookup(nombreServidor);
                        resultado += miServidor.getSubtotal();

                    } catch(NotBoundException | RemoteException e) {
                        System.err.println("Exception del sistema: " + e);
                    }
                }
            }
            resultado +=suma;
            return resultado;
        }
        return -1;
    }
    
    /*
    Devuelve el subtotal de donaciones almacenado en esta réplica de servidor
    */
    public int getSubtotal() throws RemoteException{
        return suma;
    }
    
    /*
    Aumenta el valor de las donaciones almacenadas en la réplica del servidor y marca al usuario como donante
    */
    public void donar(int valor, String usuario) throws RemoteException {
        if(valor > 0){
            int pos = registro.indexOf(usuario);
            registro.remove(pos);
            donaciones.remove(pos);
            this.addusuario(usuario, "1");

            suma += valor;
        }
    }
    
    /*
    Busca a un usuario en una réplica del servidor. Devuelve -1 si el usuario está registrado o el total
    de usuarios registrados en la réplica si no lo ha encontrado
    */
    public int buscarUsuario(String usuario) throws RemoteException{
        if(registro.contains(usuario))  //devuelve -1 si tiene el usuario registrado
            return -1;
        else  //devuelve el numero de usuarios registrados si no encuentra el buscado
            return registro.size();                
    }
    
    /*
    Mete a un usuario en la estructura de datos para el registro de usuarios
    */
    public void addusuario(String usuario, String valor) throws RemoteException{
        registro.add(usuario);
        donaciones.add(valor);
    }
}



