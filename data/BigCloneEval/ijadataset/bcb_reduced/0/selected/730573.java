package persistencia;

import java.io.*;
import javax.swing.*;

public class DirectAccessFile extends RegisterFile {

    protected DirectAccessFileSorter sorter;

    /**
     * Crea un manejador para el archivo, asociando al mismo el nombre "newfile.dat " a 
     * modo de file descriptor. Abre el archivo en disco asociado con ese file descriptor, 
     * en el directorio default, y en modo "rw" (tal como se se usa en la clase 
     * RandomAccessFile). Si ocurre un problema al intentar abrir el archivo, la aplicación
     * finalizará.
     * @param obj un objeto de la clase base para el archivo.
     */
    public DirectAccessFile() {
        super("newfile.dat", "rw");
        sorter = new QuickSort(this);
    }

    /**
     * Crea un manejador para el archivo, asociando al mismo un nombre a modo de file 
     * descriptor. Abre el archivo en disco asociado con ese file descriptor, en el modo 
     * indicado por el segundo par�metro. El modo de apertura es tal y como se usa en la clase 
     * RandomAccessFile: "r" para sólo lectura y "rw" para lectura y grabación. Si el modo de
     * apertura enviado es null, o no es "r" ni "rw", se asumirá "r". Si la referencia "nombre"
     * es null, o si la cadena "nombre" es una cadena vacía o un blanco, el nombre del archivo
     * se asumirá como "newfile.dat". Si ocurre un problema al intentar abrir el archivo, la
     * aplicación finalizara.
     * @param nombre es el nombre físico y ruta del archivo a crear / abrir.
     * @param modo es el modo de apertura del archivo.
     */
    public DirectAccessFile(String nombre, String modo) {
        super(nombre, modo);
        sorter = new QuickSort(this);
    }

    /**
     * Crea un manejador para el archivo, asociando al mismo un file descriptor. Abre el archivo 
     * en disco asociado con ese file descriptor, en el modo indicado por el segundo parámetro.
     * El modo de apertura es tal y como se usa en la clase RandomAccessFile: "r" para sólo lectura
     * y "rw" para lectura y grabación. Si el modo de apertura enviado es null, o no es "r" ni "rw",
     * se asumirá "r". Si la referencia file es null, el nombre del archivo se asumirá como "newfile.dat".
     * Si ocurre un problema al intentar abrir el archivo, la aplicación finalizará.
     * @param file es el pathname del archivo a crear.
     * @param modo es el modo de apertura del archivo.
     */
    public DirectAccessFile(File file, String modo) {
        super(file, modo);
        sorter = new QuickSort(this);
    }

    /**
     *  Establece al objeto rfs como el ordenador del archivo actual. Ese objeto implementa algún
     *  método de ordenamiento a través de su método sort(). Seguimos el patrón Strategy.
     *  @param rfs el objeto que contiene el algoritmo de ordenamiento a usar, en su método sort().
     */
    public void setSorter(DirectAccessFileSorter rfs) {
        if (rfs == null) rfs = new QuickSort(this);
        sorter = rfs;
    }

    /**
     * Ordena el archivo utilizando el algoritmo QuickSort, o bien el algoritmo que implemente el 
     * objeto ordenador establecido con el método setSorter().
     */
    public void sort() {
        sorter.sort();
    }

    /**
     * Crea y retorna un iterador para el archivo. El método retorna null si el archivo
     * no tiene una clase base asociada.
     * @return un iterador para el archivo.
     */
    public RegisterFileIterator createIterator() {
        if (clase == null) return null;
        return new SequentialIterator();
    }

    /**
     * Lee y retorna el objeto contenido en la posición index del archivo, considerando que el
     * índice del primer registro es 0(cero). Si el archivo está vacío, retorna null. Si index
     * es mayor a la cantidad de registros del archivo, retorna null. Si index es menor a cero, 
     * la aplicación terminará. El método retorna el objeto físicamente almacenado en la posición
     * index, SIN CHEQUEAR si el registro que lo contiene está marcado como borrado o no. Si se
     * desea certeza respecto de recuperar siempre objetos válidos (no marcados como borrados)
     * deberáa ejecutarse previamente el método clean() para eliminar los registros invalidados.
     * @param index el nemero relativo de registro que se quiere acceder.
     * @return el objeto grabado en la posición index, o null si la operación no puede hacerse.
     */
    public Grabable get(long index) {
        if (clase == null) return null;
        if (index >= count()) return null;
        seekRegister(index);
        Register r = this.read();
        return r.getData();
    }

    /**
     * Graba el objeto referido por obj en la posición index del archivo, considerando que el
     * índice del primer registro es 0(cero). El registro que estuviera ubicado en la posición
     * index será reemplazado. Si la referencia obj es null, el método no hace nada. Tampoco
     * hará nada si el objeto obj no es compatible con la clase base del archivo. Si index es
     * negativo, la aplicación terminará.
     * @param index el número relativo de registro que se quiere acceder.
     * @param obj el objeto que se quiere grabar en la posición index.
     */
    public void set(long index, Grabable obj) {
        if (getMode().equals("r")) return;
        if (!isOk(obj)) return;
        if (clase == null) writeClassName(obj);
        Register r = new Register(obj);
        seekRegister(index);
        this.write(r);
    }

    /**
     * Ubica el file pointer en la posición de inicio del registro número index. El método
     * no hace nada si el archivo no tiene una clase base asociada. Si hay algún problema de
     * IO al intentar el posicionamiento, la aplicación terminará. Si index es mayor a la
     * cantidad de registros del archivo, el posicionamiento se realiza de todos modos, dejando 
     * el file pointer apuntando más allá del final del archivo.
     * @param index número relativo del registro que se quiere acceder, contando desde el
     * principio del archivo.
     * @return true si el posicionamiento pudo realizarse.
     */
    public boolean seekRegister(long index) {
        if (clase == null) return false;
        int tr = (new Register(clase)).sizeOf();
        seekByte(tr * index + marca);
        return true;
    }

    /**
     * Devuelve el número relativo de registro en el cual está posicionado el
     * archivo en este momento. Si el archivo no tiene una clase base asociada,
     * retornará -1.
     * @return el número de registro de posicionamiento actual, o -1 si no hay
     * clase base asociada al archivo.
     */
    public long registerPos() {
        if (clase == null) return -1;
        long p = bytePos() - marca;
        int tr = (new Register(clase)).sizeOf();
        return p / tr;
    }

    /**
     * Devuelve la cantidad de registros que contiene el archivo. Este número
     * incluye los registros marcados como borrados. Si se desea el numero de
     * registros válidos, deberá limpiar el contenido del archivo invocando
     * previamente a clean().
     * @return el total de registros del archivo.
     */
    public long count() {
        if (clase == null) return 0;
        long t = length();
        t -= marca;
        return t / (new Register(clase)).sizeOf();
    }

    /**
     * Busca un registro en el archivo. Retorna -1 si el registro no se encuentra, o el 
     * número de registro que corresponde a la primera ocurrencia del registro en disco si
     * se encontraba en el archivo. En general, el retorno de -1 significa que el registro 
     * no fue encontrado, por cualquier causa. El file pointer sale apuntando al mismo byte 
     * donde estaba cuando empezó la operación.
     * @param obj el objeto a buscar en el archivo.
     * @return el número relativo de la primera ocurrencia del registro en el archivo, si
     * existe, o -1 si no existe.
     */
    public long search(Grabable obj) {
        if (clase == null) return -1;
        if (!isOk(obj)) return -1;
        long pos = -1;
        try {
            long actual = bytePos();
            rewind();
            while (!eof()) {
                Register reg = this.read();
                if (reg.getData().equals(obj) && reg.getState() == Register.ACTIVO) {
                    long p = bytePos() - reg.sizeOf();
                    p = p - marca;
                    pos = p / reg.sizeOf();
                    break;
                }
            }
            seekByte(actual);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al buscar el registro: " + e.getMessage());
            System.exit(1);
        }
        return pos;
    }

    /**
     * Busca un registro en el archivo mediante b�squeda binaria. El archivo SE SUPONE 
     * ORDENADO de acuerdo a las convenciones de compareTo(), de menor a mayor, y efectúa
     * la búsqueda de acuerdo al mismo método de comparación. Retorna -1 si el registro no
     * se encuentra, o el número de registro que corresponde a la primera ocurrencia encontrada
     * del registro si se encontraba en el archivo. En general, el retorno de -1 significa que 
     * el registro no fue encontrado, por cualquier causa. El file pointer sale apuntando al mismo 
     * byte donde estaba cuando empezó la operación.
     * @param obj el objeto a buscar en el archivo.
     * @return el número relativo de la primera ocurrencia encontrada del registro en el archivo, si
     * existe, o -1 si no existe.
     */
    public long binarySearch(Grabable obj) {
        if (clase == null) return -1;
        if (!isOk(obj)) return -1;
        long pos = -1;
        try {
            long actual = bytePos();
            long n = count();
            long izq = 0, der = n - 1;
            while (izq <= der && pos == -1) {
                long c = (izq + der) / 2;
                seekRegister(c);
                Register reg = this.read();
                if (reg.getData().equals(obj) && reg.getState() == Register.ACTIVO) {
                    long p = bytePos() - reg.sizeOf();
                    p = p - marca;
                    pos = p / reg.sizeOf();
                    break;
                } else {
                    if (obj.compareTo(reg.getData()) > 0) izq = c + 1; else der = c - 1;
                }
            }
            seekByte(actual);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al buscar el registro: " + e.getMessage());
            System.exit(1);
        }
        return pos;
    }

    /**
     * Busca un registro en el archivo. Retorna true si el registro se encuentra, o false si no. 
     * El file pointer sale apuntando al mismo byte donde estaba cuando empezó la operación.
     * @param obj el objeto a buscar en el archivo.
     * @return true si el archivo contiene un objeto igual a obj.
     */
    public boolean contains(Grabable obj) {
        return (this.search(obj) != -1);
    }

    /**
     * Busca un registro en el archivo. Retorna una copia del registro que estaba en el archivo
     * si es que el archivo conten�a uno igual al que entra como parámetro. Si no lo encuentra,
     * retorna null. El file pointer sale apuntando al mismo byte donde estaba cuando empezó la
     * operación.
     * @param obj el objeto a buscar en el archivo.
     * @return una referencia al registro encontrado, o null si no se encontró.
     */
    public Grabable find(Grabable obj) {
        long d = this.search(obj);
        return (d != -1) ? this.get(d) : null;
    }

    /**
     * Agrega un registro en el archivo, controlando que no haya repetición. El archivo debe
     * estar abierto en modo de grabación. El archivo vuelve abierto, y posicionado en el byte
     * siguiente a aquel en el cual terminó la grabación.
     * @param obj el objeto a agregar.
     * @return true si fue posible agregar el registro - false si no fue posible.
     */
    public boolean add(Grabable obj) {
        if (getMode().equals("r")) return false;
        if (!isOk(obj)) return false;
        if (clase == null) writeClassName(obj);
        if (search(obj) == -1) {
            goFinal();
            return this.write(new Register(obj));
        }
        return false;
    }

    /**
     * Agrega un registro en el archivo, sin controlar repetición. El archivo debe
     * estar abierto en modo de grabación. El archivo vuelve abierto y posicionado al
     * final.
     * @param obj el registro a agregar. 
     * @return true si fue posible agregar el registro - false si no fue posible.
     */
    public boolean append(Grabable obj) {
        if (getMode().equals("r")) return false;
        if (!isOk(obj)) return false;
        if (clase == null) writeClassName(obj);
        goFinal();
        return this.write(new Register(obj));
    }

    /**
     * Borra un registro del archivo. El archivo debe estar abierto en modo de grabación.
     * El registro se marca como borrado, aunque sigue físicamente ocupando lugar en el
     * archivo. El archivo vuelve abierto y posicionado
     * en el byte siguiente a aquel en el cual termina el registro marcado.
     * @param obj el registro a buscar y borrar.
     * @return true si fue posible borrar el registro - false si no fue posible.
     */
    public boolean remove(Grabable obj) {
        if (clase == null || getMode().equals("r")) return false;
        if (!isOk(obj)) return false;
        boolean resp = false;
        long pos = search(obj);
        if (pos != -1) {
            seekRegister(pos);
            Register reg = this.read();
            reg.setState(Register.BORRADO);
            seekRegister(pos);
            resp = this.write(reg);
        }
        return resp;
    }

    /**
     * Modifica un registro en el archivo. Reemplaza el registro en una posición dada,
     * cambiando sus datos por otros tomados como parámetro. El objeto que viene como
     * parámetro se busca en el archivo, y si se encuentra entonces el que estaba en el
     * disco es reemplazado por el que entró a modo de parámetro. El archivo debe estar
     * abierto en modo de grabación. El archivo vuelve abierto y posicionado en el byte
     * siguiente a aquel en el que termina el registro modificado.
     * @param obj el objeto con los nuevos datos.
     * @return true si fue posible modificar el registro - false si no fue posible
     */
    public boolean update(Grabable obj) {
        if (clase == null || getMode().equals("r")) return false;
        if (!isOk(obj)) return false;
        long pos = search(obj);
        if (pos != -1) {
            seekRegister(pos);
            return this.write(new Register(obj));
        }
        return false;
    }

    /**
     * Elimina físicamente los registros que estuvieran marcados como borrados. El archivo
     * queda limpio, sólo con registros activos (no marcados como borrados).
     */
    public void clean() {
        if (clase == null || getMode().equals("r")) return;
        try {
            DirectAccessFile temp = new DirectAccessFile("temporal.dat", "rw");
            temp.maestro.setLength(0);
            temp.writeClassName(clase);
            this.rewind();
            while (!this.eof()) {
                Register reg = this.read();
                if (reg.getState() == Register.ACTIVO) temp.write(reg);
            }
            this.close();
            temp.close();
            this.delete();
            temp.rename(this);
            maestro = new RandomAccessFile(fd, apertura);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al limpiar el archivo: " + e.getMessage());
            System.exit(1);
        }
    }

    private class SequentialIterator implements RegisterFileIterator {

        private int currentIndex;

        /**
            * Crea un iterador posicionado en el registro número 0(cero). El
            * constructor lanza un proceso de limpieza del archivo (invoca al
            * método clean() de la clase contenedora)
            */
        private SequentialIterator() {
            clean();
            currentIndex = 0;
        }

        /**
            * Vuelve el iterador al primer registro del archivo.
            */
        public void first() {
            currentIndex = 0;
        }

        /**
            * Avanza el iterador al índice del siguiente registro en el
            * archivo, si es que esto es posible. De lo contrario, el 
            * iterador se mantiene en la misma posición anterior.
            */
        public void next() {
            if (currentIndex < count()) {
                currentIndex++;
            }
        }

        /**
            * Determina si es posible avanzar el iterador al siguiente
            * registro (return true) o no (return false).
            * @return true si es posible avanzar al siguiente registro.
            */
        public boolean hasNext() {
            return (currentIndex < count());
        }

        /**
            * Retorna el objeto actualmente apuntado por el iterador. Si el archivo está
            * vacío, retorna null. Si ocurre un problema de IO o de instanciación, la
            * aplicaci�n terminará.
            * @return el objeto actual del iterador o null si el archivo está vacío.
            */
        public Grabable current() {
            if (clase == null) return null;
            seekRegister(currentIndex);
            Register r = read();
            return r.getData();
        }
    }
}
