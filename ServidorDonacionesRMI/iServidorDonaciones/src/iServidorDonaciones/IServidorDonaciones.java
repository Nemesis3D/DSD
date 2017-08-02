/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author ghueli
 */
// IServidorDonaciones.java
package iServidorDonaciones;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServidorDonaciones extends Remote {
    int registrar(String valor) throws RemoteException;
    int consultar(String usuario) throws RemoteException;
    void donar(int valor, String usuario) throws RemoteException;
    int getSubtotal() throws RemoteException;
    int buscarUsuario(String usuario) throws RemoteException;
    void addusuario(String usuario, String valor) throws RemoteException;
    
}