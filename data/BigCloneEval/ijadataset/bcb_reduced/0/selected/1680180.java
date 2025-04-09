package Utilidades;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Miguel González y Jaime Bárez
 */
public class Analizador {

    private final String palabrasSeparadoras;

    private final String palabrasSeparadorasRegex;

    public Analizador() {
        palabrasSeparadoras = " ,.;=><()*+-\\/";
        palabrasSeparadorasRegex = "[^" + palabrasSeparadoras + "]+|[" + palabrasSeparadoras + "]+|`[.]+`";
    }

    /**
     * Desmiembra una cadena en un ArrayList de dos columnas: 
     * La primera para las distintas palabras y la segunda para un valor Boolean
     * que nos indica si la palabra es separadora o no. (Separadora->true)
     * @param miCadena Cadena a desmembrar
     * @return ArrayList de palabras y Boolean que representa si es separadora o no
     */
    public ArrayList<Tupla> desmembrar(String miCadena) {
        ArrayList<Tupla> miArrayList = new ArrayList<Tupla>();
        String palabraEncajada = new String();
        Boolean valorBooleano;
        Pattern patron = Pattern.compile(palabrasSeparadorasRegex);
        Matcher encaja = patron.matcher(miCadena);
        while (encaja.find()) {
            palabraEncajada = miCadena.substring(encaja.start(), encaja.end());
            if (palabrasSeparadoras.contains(String.valueOf(palabraEncajada.charAt(0)))) {
                valorBooleano = true;
            } else {
                valorBooleano = false;
            }
            miArrayList.add(new Tupla(palabraEncajada, valorBooleano));
        }
        return miArrayList;
    }
}
