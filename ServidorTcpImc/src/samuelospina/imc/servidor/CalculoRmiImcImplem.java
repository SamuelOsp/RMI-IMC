package samuelospina.imc.servidor;

import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import samuelospina.imc.rmi.lib.DatosImc;
import samuelospina.imc.rmi.lib.IRemotaCalculoImc;

/**
 * Objeto remoto de la guia: calcula el Indice de Masa Corporal
 * (peso / altura²) y lo interpreta. La formula vive UNICAMENTE aqui, en el
 * servidor; el cliente la invoca como si fuera un metodo local.
 *
 * En RMI estandar el objeto remoto extiende UnicastRemoteObject: su
 * constructor lo exporta (abre el puerto TCP y crea el stub), y por eso
 * declara RemoteException.
 *
 * @author Samuel David Ospina De Avila
 */
public class CalculoRmiImcImplem extends UnicastRemoteObject implements IRemotaCalculoImc {

    private static final long serialVersionUID = 1L;

    public interface ILogListener {

        void log(String mensaje);
    }

    private final transient ILogListener logListener;

    /**
     * @param puerto puerto TCP en el que se exporta el objeto (0 = uno anonimo)
     */
    public CalculoRmiImcImplem(int puerto, ILogListener logListener) throws RemoteException {
        super(puerto);
        this.logListener = logListener;
    }

    @Override
    public DatosImc calcularImc(DatosImc datos) throws RemoteException {
        if (datos == null) {
            return null;
        }
        log("[SOLICITUD] Datos recibidos -> Peso: " + datos.getPeso() + " kg | Altura: "
                + datos.getAltura() + " m desde " + clienteActual());

        // "!(x > 0)" tambien rechaza NaN, que con "x <= 0" pasaria como valido
        if (!(datos.getPeso() > 0) || !(datos.getAltura() > 0)) {
            datos.setResultado(0);
            datos.setInterpretacion("ERROR: El peso y la altura deben ser mayores que 0");
            log("[ERROR] " + datos.getInterpretacion());
            return datos;
        }

        float resultado = datos.getPeso() / (datos.getAltura() * datos.getAltura());
        resultado = Math.round(resultado * 100.0f) / 100.0f;   // dos decimales
        datos.setResultado(resultado);
        if (resultado < 18.5f) {
            datos.setInterpretacion("Debes consultar un Medico, tu peso es muy bajo");
        } else if (resultado <= 24.9f) {
            datos.setInterpretacion("Estas bien de peso");
        } else if (resultado <= 29.9f) {
            datos.setInterpretacion("Debes bajar un poco de peso");
        } else {
            datos.setInterpretacion("Debes consultar un Medico, tu peso es muy alto");
        }
        log("[CALCULO] IMC: " + resultado + " (" + datos.getInterpretacion() + ")");
        return datos;
    }

    /** IP del cliente que esta ejecutando la invocacion remota en este hilo. */
    private String clienteActual() {
        try {
            return getClientHost();
        } catch (ServerNotActiveException ex) {
            return "llamada local";
        }
    }

    private void log(String mensaje) {
        if (logListener != null) {
            logListener.log(mensaje);
        }
    }
}
