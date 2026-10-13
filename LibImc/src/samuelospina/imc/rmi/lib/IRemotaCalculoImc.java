package samuelospina.imc.rmi.lib;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interfaz remota del calculo del IMC para RMI estandar.
 *
 * A diferencia de LipeRMI, el RMI de Java exige que la interfaz extienda
 * java.rmi.Remote y que cada metodo declare RemoteException: cualquier
 * falla de red llega al cliente como esa excepcion.
 *
 * @author Samuel David Ospina De Avila
 */
public interface IRemotaCalculoImc extends Remote {

    public DatosImc calcularImc(DatosImc datos) throws RemoteException;
}
