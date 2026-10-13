package samuelospina.imc.rmi.lib;

import java.io.Serializable;

/**
 * Objeto que viaja entre el cliente y el servidor en la invocacion remota.
 * El cliente llena {@code peso} y {@code altura}; el servidor devuelve el
 * mismo objeto con {@code resultado} e {@code interpretacion} calculados.
 *
 * Debe ser Serializable porque se transmite por el socket TCP.
 *
 * @author Samuel David Ospina De Avila
 */
public class DatosImc implements Serializable {

    private static final long serialVersionUID = 1L;

    private float peso;
    private float altura;
    private float resultado;
    private String interpretacion;

    public DatosImc() {
    }

    public DatosImc(float peso, float altura) {
        this.peso = peso;
        this.altura = altura;
    }

    public float getPeso() {
        return peso;
    }

    public void setPeso(float peso) {
        this.peso = peso;
    }

    public float getAltura() {
        return altura;
    }

    public void setAltura(float altura) {
        this.altura = altura;
    }

    public float getResultado() {
        return resultado;
    }

    public void setResultado(float resultado) {
        this.resultado = resultado;
    }

    public String getInterpretacion() {
        return interpretacion;
    }

    public void setInterpretacion(String interpretacion) {
        this.interpretacion = interpretacion;
    }
}
