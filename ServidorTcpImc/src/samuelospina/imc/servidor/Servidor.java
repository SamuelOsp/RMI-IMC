package samuelospina.imc.servidor;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Publica el objeto remoto con RMI estandar de Java SE: crea un registro
 * RMI embebido (no hace falta ejecutar el comando rmiregistry) y enlaza en
 * el la implementacion bajo un nombre que los clientes buscan.
 *
 * @author Samuel David Ospina De Avila
 */
public class Servidor {

    public static final String NOMBRE_SERVICIO = "CalculoImcRemoto";

    private int puerto = 9007;
    private final String host;
    private final CalculoRmiImcImplem.ILogListener listener;
    private Registry registro;
    private CalculoRmiImcImplem calculoImc;
    private boolean activo = false;

    public Servidor() {
        this(9007, null, null);
    }

    /**
     * @param host IP con la que los clientes alcanzan este equipo; puede ser null
     */
    public Servidor(int puerto, String host, CalculoRmiImcImplem.ILogListener listener) {
        if (puerto > 0) {
            this.puerto = puerto;
        }
        this.host = host;
        this.listener = listener;
    }

    public synchronized void iniciar() throws Exception {
        if (activo) {
            return;
        }
        // El stub viaja al cliente con la direccion del servidor adentro. Sin esta
        // propiedad Java puede anunciar una IP que otro equipo de la red no alcanza.
        if (host != null && !host.isEmpty()) {
            System.setProperty("java.rmi.server.hostname", host);
        }
        try {
            registro = LocateRegistry.createRegistry(puerto);
            // El objeto se exporta en el mismo puerto del registro: asi el
            // servicio completo usa un unico puerto TCP.
            calculoImc = new CalculoRmiImcImplem(puerto, listener);
            registro.rebind(NOMBRE_SERVICIO, calculoImc);
            activo = true;
        } catch (ExportException ex) {
            liberar();
            throw new Exception("No se pudo abrir el puerto " + puerto
                    + " (puede estar ocupado por otro proceso)");
        } catch (RemoteException ex) {
            liberar();
            throw new Exception("No es posible publicar el objeto remoto: " + ex.getMessage());
        }
    }

    public synchronized void detener() {
        if (activo) {
            try {
                registro.unbind(NOMBRE_SERVICIO);
            } catch (Exception ex) {
                // Ya no estaba enlazado.
            }
            liberar();
            activo = false;
        }
    }

    /**
     * Retira de RMI el objeto remoto y el registro. Cuando no queda nada
     * exportado en el puerto, Java cierra el socket de escucha y lo libera.
     */
    private void liberar() {
        try {
            if (calculoImc != null) {
                UnicastRemoteObject.unexportObject(calculoImc, true);
            }
        } catch (NoSuchObjectException ex) {
            // No llego a exportarse.
        }
        try {
            if (registro != null) {
                UnicastRemoteObject.unexportObject(registro, true);
            }
        } catch (NoSuchObjectException ex) {
            // No llego a exportarse.
        }
        calculoImc = null;
        registro = null;
    }

    public synchronized boolean isActivo() {
        return activo;
    }

    public int getPuerto() {
        return puerto;
    }
}
