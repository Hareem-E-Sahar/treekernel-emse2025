package logica;

import ui.DocumentoDeTexto;
import ui.ScriptorPpal;
import java.util.Vector;
import java.util.regex.*;

/**
 * Esta clase es utilizada para procesar los comandos
 * Se utiliza en el desarrolo de la aplicación correspondiente al
 * Trabajo Práctico Nº 1 de Diseño e Implementación de Estructuras de Datos.
 * Universidad Tecnológica Nacional - Facultad Regional Santa Fe.
 *
 * @author Cátedra DIED 2007.
 */
public class ProcesadorDeComandos {

    /** Aplicación. */
    private ScriptorPpal aplicacion;

    /** Documento actual. */
    private DocumentoDeTexto documentoActual;

    private Vector seleccionadas;

    private int seleccion;

    /**
     * Constructor del procesador de comandos.
     * @param aplicacion    Aplicación.
     */
    public ProcesadorDeComandos(ScriptorPpal aplicacion) {
        this.aplicacion = aplicacion;
        seleccionadas = new Vector();
        seleccion = 0;
    }

    /**
     * Comprueba que el comando recibido tenga la cantidad de parámetros
     * correcta y de no ser así explica la sintaxis del mismo
     * @param comando   Comando a procesar.
     * @param cantParametros Indica la cantidad de parametros del comando.
     * @return boolean.
     */
    public boolean argumentosValidos(String comando, int cantParametros) {
        String primerArgumento = primerArgumento(comando);
        String segundoArgumento = segundoArgumento(comando);
        System.out.println(primerArgumento);
        if (cantParametros == 1) {
            if (!(primerArgumento.equalsIgnoreCase(""))) {
                return true;
            }
        } else if (cantParametros == 2) {
            if (!(primerArgumento.equalsIgnoreCase(""))) if (!(segundoArgumento.equalsIgnoreCase(""))) return true;
        }
        System.out.print("Sintaxis:\n    " + nombreComando(comando) + "(");
        for (int i = 0; i < cantParametros - 1; i++) {
            System.out.print("<parametro>, ");
        }
        System.out.println("<expresionRegular>)");
        return false;
    }

    /**
     * Método para el comando capitalizar.
     * @param comando   Comando a procesar.
     * @param posicionCursor Indica la posicion del cursor para comenzar
     * la búsqueda desde esta.
     */
    public void comandoCapitalizar(String comando, int posicionCursor) {
        String capitalizar = primerArgumento(comando);
        String expresionRegular = expresionRegularCorrecta(segundoArgumento(comando));
        String texto = documentoActual.getTexto();
        String textoRemplazado = texto.toString();
        Pattern patron = null;
        Matcher encaja = null;
        try {
            patron = Pattern.compile(expresionRegular);
            encaja = patron.matcher(texto);
        } catch (PatternSyntaxException pse) {
            System.out.println("Expresion regular mal formada");
            return;
        } catch (OutOfMemoryError p) {
            System.out.println("Expresion regular mal formada");
            return;
        }
        Vector reemplazarPalabras = new Vector();
        while (encaja.find(posicionCursor)) {
            reemplazarPalabras.add(texto.substring(encaja.start(), encaja.end()));
            posicionCursor = encaja.end();
        }
        if (reemplazarPalabras.size() > 0) {
            if (capitalizar.equals("M")) {
                for (int i = 0; i < reemplazarPalabras.size(); i++) {
                    String palabraOriginal = (String) reemplazarPalabras.elementAt(i);
                    String palabraModificada = ((String) reemplazarPalabras.elementAt(i)).toUpperCase();
                    imprimirConsola(palabraOriginal, palabraModificada);
                    textoRemplazado = textoRemplazado.replaceAll((String) (reemplazarPalabras.elementAt(i)), ((String) reemplazarPalabras.elementAt(i)).toUpperCase());
                }
            } else if (capitalizar.equals("m")) {
                for (int i = 0; i < reemplazarPalabras.size(); i++) {
                    System.out.println("Cambio: " + (String) reemplazarPalabras.elementAt(i));
                    System.out.println("Por: " + ((String) reemplazarPalabras.elementAt(i)).toLowerCase());
                    textoRemplazado = textoRemplazado.replaceAll((String) reemplazarPalabras.elementAt(i), ((String) reemplazarPalabras.elementAt(i)).toLowerCase());
                }
            } else if (capitalizar.equals("Mm")) {
                for (int i = 0; i < reemplazarPalabras.size(); i++) {
                    String palabra = reemplazarPalabras.elementAt(i).toString();
                    Character letraInicial = new Character(palabra.charAt(0));
                    String letrasFinales = palabra.substring(1, palabra.length());
                    String capitalizada = letraInicial.toUpperCase(palabra.charAt(0)) + letrasFinales.toLowerCase();
                    System.out.println("Cambio: " + palabra);
                    System.out.println("Por: " + capitalizada);
                    textoRemplazado = textoRemplazado.replaceAll((String) reemplazarPalabras.elementAt(i), capitalizada);
                }
            } else this.aplicacion.mensajeDeError("Opcion desconocida");
        } else System.out.println("No hay macheo");
        documentoActual.setTexto(textoRemplazado);
    }

    /**
     * Método para el comando buscar.
     * @param comando   Comando a procesar.
     */
    public void comandoBuscar(String comando) {
        seleccionadas.clear();
        if (cursorEnFinDeTexto()) {
            this.aplicacion.limpiar();
            this.aplicacion.msjResultado("El cursor no debería estar en el final del texto.");
            System.out.println("El cursor no debería estar en el final del texto.");
        } else {
            String expresionRegular = expresionRegularCorrecta(primerArgumento(comando));
            String texto = documentoActual.getTexto();
            Pattern patron = null;
            Matcher encaja = null;
            try {
                patron = Pattern.compile(expresionRegular);
                encaja = patron.matcher(texto);
            } catch (PatternSyntaxException pse) {
                System.out.print("Expresion regular mal conformada, por favor consultar sintaxis");
                return;
            }
            int posicionCursor = documentoActual.getCursorPosition();
            this.aplicacion.limpiar();
            this.aplicacion.msjResultado("Resultado de " + comando);
            System.out.println("Resultado de buscar con " + comando);
            System.out.println("Expresion Regular: " + expresionRegular);
            while (encaja.find(posicionCursor)) {
                System.out.println(texto.substring(encaja.start(), encaja.end()) + " " + encaja.start() + "-" + encaja.end());
                int[] posiciones = { encaja.start(), encaja.end() };
                seleccionadas.addElement(posiciones);
                posicionCursor = encaja.end();
            }
            int[] posiciones = (int[]) seleccionadas.elementAt(seleccion);
            documentoActual.setSelect(posiciones[0], posiciones[1]);
            System.out.println(posiciones[0] + " " + posiciones[1] + " " + seleccion);
            seleccion++;
        }
    }

    /**
     * Método que procesa el comando eliminar.
     * @param comando   Comando a procesar.
     */
    public void comandoEliminar(String comando) {
        String expresionRegular = expresionRegularCorrecta(primerArgumento(comando));
        String texto = documentoActual.getTexto();
        String textoRemplazado = texto.replaceAll(expresionRegular, "");
        System.out.println(textoRemplazado);
        documentoActual.setTexto(textoRemplazado);
    }

    /**
     * Método que imprime en consola los cambios realizados
     * @param pSinModificar
     * @param pModificada
     */
    public void imprimirConsola(String pSinModificar, String pModificada) {
        System.out.println("Cambio: " + pSinModificar);
        System.out.println("Por: " + pModificada);
    }

    /**
     * Método que procesa el comando eliminarSiguiente.
     * @param comando   Comando a procesar.
     */
    public void comandoEliminarSiguiente(String comando) {
        String expresionRegular = expresionRegularCorrecta(primerArgumento(comando));
        String texto = documentoActual.getTexto();
        Pattern patron = null;
        Matcher encaja = null;
        int posicionCursor = documentoActual.getCursorPosition();
        try {
            patron = Pattern.compile(expresionRegular);
            encaja = patron.matcher(texto);
        } catch (PatternSyntaxException pse) {
            System.out.println("Expresion regular mal conformada, por favor consultar sintaxis");
            return;
        }
        encaja.find(posicionCursor);
        String primerTexto = texto.substring(0, encaja.start());
        String segundoTexto = texto.substring(encaja.start());
        String textoRemplazado = segundoTexto.replaceFirst(expresionRegular, "");
        documentoActual.setTexto(primerTexto + textoRemplazado);
    }

    /**
     * Método que procesa el comando recibido como parámetro.
     * @param comando    Comando a procesar.
     */
    public void procesarComando(String comando) {
        comando = comando.replace("\n", "");
        System.out.println("\n------------------------------------------------");
        documentoActual = this.aplicacion.getDocumentoActual();
        if (documentoActual == null) {
            this.aplicacion.mensajeDeError("No hay documento seleccionado");
        } else if (comando.equalsIgnoreCase("limpiar")) {
            this.documentoActual.setTexto("");
            this.aplicacion.mensajeDeInformacion("Comando exitoso");
        } else if (comando.equalsIgnoreCase("salir")) {
            System.exit(0);
        } else if (comando.equalsIgnoreCase("limpiartodo")) {
            this.documentoActual.setTexto("");
            this.aplicacion.mensajeDeInformacion("");
        } else if (comando.equalsIgnoreCase("limpiarcomandos")) {
            this.aplicacion.mensajeDeInformacion("");
        } else if (nombreComando(comando).equalsIgnoreCase("buscar")) {
            if (this.argumentosValidos(comando, 1)) {
                this.comandoBuscar(comando);
                seleccion = 0;
            }
        } else if (nombreComando(comando).equalsIgnoreCase("eliminar")) {
            if (this.argumentosValidos(comando, 1)) {
                this.comandoEliminar(comando);
            }
        } else if (nombreComando(comando).equalsIgnoreCase("eliminarSiguiente")) {
            if (this.argumentosValidos(comando, 1)) {
                this.comandoEliminarSiguiente(comando);
            }
        } else if (nombreComando(comando).equalsIgnoreCase("capitalizar")) {
            if (this.argumentosValidos(comando, 2)) {
                this.comandoCapitalizar(comando, 0);
            }
        } else if (nombreComando(comando).equalsIgnoreCase("capitalizarSiguiente")) {
            if (this.argumentosValidos(comando, 2)) {
                int posicionCursor = documentoActual.getCursorPosition();
                this.comandoCapitalizar(comando, posicionCursor);
            }
        } else if (comando.equalsIgnoreCase("next")) {
            if (seleccionadas.size() > seleccion) {
                int[] posiciones = (int[]) seleccionadas.elementAt(seleccion);
                documentoActual.setSelect(posiciones[0], posiciones[1]);
                System.out.println(posiciones[0] + " " + posiciones[1] + " " + seleccion);
                seleccion++;
            }
        } else if (nombreComando(comando).equalsIgnoreCase("reemplazar")) {
            if (this.argumentosValidos(comando, 2)) {
                this.comandoReemplazar(comando, 0);
            }
        } else if (nombreComando(comando).equalsIgnoreCase("reemplazarSiguiente")) {
            if (this.argumentosValidos(comando, 2)) {
                int posicionCursor = documentoActual.getCursorPosition();
                this.comandoReemplazar(comando, posicionCursor);
            }
        } else {
            this.aplicacion.mensajeDeError("Comando desconocido");
            this.aplicacion.getPanelDeComando().getLineaDeComandos().requestFocus();
            String segundoArgumento = segundoArgumento(comando);
            String nombreVariable = nombreVariable(comando);
            String primerArgumento = primerArgumento(comando);
            String expresionRegular = primerArgumento.replace(nombreVariable, "");
            System.out.println(nombreComando(comando));
            System.out.println(primerArgumento);
            System.out.println(segundoArgumento);
            System.out.println(nombreVariable);
            System.out.println(expresionRegular);
        }
    }

    public void comandoReemplazar(String comando, int posicionCursor) {
        String segundoArgumento = segundoArgumento(comando);
        String nombreVariable = nombreVariable(comando);
        String primerArgumento = primerArgumento(comando);
        String expresionRegular = primerArgumento.replace(nombreVariable, "");
        String texto = documentoActual.getTexto();
        System.out.println(comando);
        System.out.println(primerArgumento);
        System.out.println(segundoArgumento);
        System.out.println(nombreVariable);
        System.out.println(expresionRegular);
        Pattern patron = null;
        Matcher matcher = null;
        String expresionRegularCorrecta = expresionRegularCorrecta(expresionRegular);
        System.out.println(expresionRegularCorrecta);
        try {
            patron = Pattern.compile(expresionRegularCorrecta);
            matcher = patron.matcher(texto);
        } catch (PatternSyntaxException pse) {
            System.out.println("Expresion regular mal conformada, por favor consultar sintaxis");
            return;
        }
        while (matcher.find(posicionCursor)) {
            String cadenaOriginal = texto.substring(matcher.start(), matcher.end());
            String[] string = primerArgumento.split("/" + nombreVariable + ".");
            System.out.println(string[0]);
            System.out.println(string[1]);
            String matcheoVariable = cadenaOriginal.replace(string[0], "");
            matcheoVariable = matcheoVariable.replace(string[1], "");
            System.out.println(matcheoVariable);
            String cadenaModificada = segundoArgumento.replace("/" + nombreVariable + "/", matcheoVariable);
            System.out.println(cadenaModificada);
            texto = texto.replaceFirst(cadenaOriginal, cadenaModificada);
            System.out.println(cadenaOriginal + " " + matcher.start() + "-" + matcher.end());
            posicionCursor = matcher.end();
        }
        documentoActual.setTexto(texto);
    }

    public String nombreComando(String texto) {
        String nombreComando = new String("");
        String expresionRegular = ".*\\(";
        Pattern patron = Pattern.compile(expresionRegular);
        Matcher matcher = patron.matcher(texto);
        if (matcher.find()) {
            nombreComando = texto.substring(matcher.start(), matcher.end() - 1);
        }
        return nombreComando;
    }

    public String primerArgumento(String texto) {
        String argumento = new String("");
        String expresionRegular = "\\(.*(,|\\))";
        Pattern patron = Pattern.compile(expresionRegular);
        Matcher matcher = patron.matcher(texto);
        if (matcher.find()) {
            argumento = texto.substring(matcher.start() + 1, matcher.end() - 1);
            argumento = argumento.split(",")[0];
        }
        return argumento;
    }

    public String segundoArgumento(String texto) {
        String argumento = new String("");
        String expresionRegular = ", .*\\)";
        Pattern patron = Pattern.compile(expresionRegular);
        Matcher matcher = patron.matcher(texto);
        if (matcher.find()) {
            argumento = texto.substring(matcher.start() + 2, matcher.end() - 1);
        }
        return argumento;
    }

    public String nombreVariable(String texto) {
        String nombreVariable = new String("");
        String expresionRegular = "/[a-zA-Z]*.";
        Pattern patron = Pattern.compile(expresionRegular);
        Matcher matcher = patron.matcher(texto);
        if (matcher.find()) {
            nombreVariable = texto.substring(matcher.start() + 1, matcher.end() - 1);
        }
        return nombreVariable;
    }

    /**
     * Formador de la correcta expresion regular, con la funcion indexOf
     * compruebo que el caracter pertenezca a la cadena, ese es el indicador
     * que utilizo para hacer los if-else if.No puedo hacer andar el replace,
     * no me deja.Asi dice la API public String replace(char oldChar, char newChar)
     * @param seudoEregular
     * @return expRegular
     */
    public String expresionRegularCorrecta(String seudoEregular) {
        String expRegular = seudoEregular;
        if (seudoEregular.indexOf("/?") != -1) {
            expRegular = seudoEregular.replace("/?", "[^\\s^\\t^\\n^\\r^\\.^\\;^\\:^\\_^\\-]");
        } else if (seudoEregular.indexOf("/*") != -1) {
            expRegular = seudoEregular.replace("/*", "[^\\t^\\n^\\r^\\.^\\;^\\:^\\_^\\-]*");
        } else if (seudoEregular.indexOf("/%") != -1) {
            char caracter = seudoEregular.split("/%")[1].charAt(0);
            expRegular = seudoEregular.replace("/%" + caracter, "[^" + caracter + "]");
        } else if (seudoEregular.indexOf("/#") != -1) {
            String secuencia = seudoEregular.split("/#")[1];
            expRegular = seudoEregular.replace("/#" + secuencia + "/#", "[^(" + secuencia + ")]*");
        } else if (seudoEregular.indexOf("/!") != -1) {
            String secuencia = seudoEregular.split("/!")[1];
            expRegular = seudoEregular.replace("/!" + secuencia + "/!", "[.*&&[^" + secuencia + "]]*");
        } else if (seudoEregular.indexOf("//") != -1) {
            expRegular = seudoEregular.replace("//", "/");
        }
        expRegular = expRegular.replace("\n", "");
        return expRegular;
    }

    /**
     * Verifica la posicion del cursor.Ya que si el mismo se encuentra en el
     * final del texto utilizado para realizar la prueba, esta funcionabilidad
     * no realizirá nada.
     * @return boolan
     */
    public boolean cursorEnFinDeTexto() {
        int posicionCursor = documentoActual.getCursorPosition();
        int sizeTexto = documentoActual.getTexto().length();
        if (sizeTexto == posicionCursor) {
            return true;
        } else {
            return false;
        }
    }

    public ScriptorPpal getAplicacion() {
        return aplicacion;
    }

    public void setAplicacion(ScriptorPpal aplicacion) {
        this.aplicacion = aplicacion;
    }

    public DocumentoDeTexto getDocumentoActual() {
        return documentoActual;
    }

    public void setDocumentoActual(DocumentoDeTexto documentoActual) {
        this.documentoActual = documentoActual;
    }

    public Vector getSeleccionadas() {
        return this.seleccionadas;
    }

    public int getSeleccion() {
        return this.seleccion;
    }

    public void aumentarSeleccion() {
        this.seleccion++;
    }
}
