import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main extends JFrame implements Runnable, KeyListener, MouseListener {

    private Image dbImage;

    private Graphics dbg;

    private Image doubleBuffer;

    private static final int WIDTH = 1024;

    private static final int HEIGHT = 768;

    private int ALT = 50;

    private Banner[] bannerPantallaPrincipal;

    private Banner[] bannerOpciones;

    private Banner[] banner3;

    private boolean juegonuevo;

    private Image imgCaminaIzq;

    private Image imgParadoIzq;

    private Image imgSaltaIzq;

    private Image imgCaminaDer;

    private Image imgParadoDer;

    private Image imgSaltaDer;

    private ImageIcon nivel1;

    private ImageIcon nivel2;

    private ImageIcon nivel3;

    private ImageIcon nivel4;

    private ImageIcon nivel5;

    private ImageIcon nivel6;

    private ImageIcon nivel7;

    private ImageIcon nivel8;

    private ImageIcon nivel9;

    private ImageIcon nivel10;

    private ImageIcon pausado;

    private ImageIcon bandera;

    private ImageIcon shade;

    private ImageIcon fin;

    private final int speed = 14;

    private Nivel nivelActual;

    private Jugador player;

    private boolean pause;

    private long pausedTime;

    private long tiempo;

    private long dt;

    private long tiempoguardao;

    private long tiempoparaguardar;

    private String tiempoString;

    private String nivelDeJuego;

    private rayoJugador[] rayo;

    private int opcion = 0;

    private int opcion2 = 0;

    private int salto;

    private int adelante;

    private int atras;

    private int disparo;

    private int tiemponivel;

    private int numeronivel;

    private boolean juego_empezado;

    private boolean en_menu_principal;

    private boolean aviso_teclas;

    private boolean sin_asignar;

    private boolean mostrar_nivel;

    private boolean juegocargado;

    private int numero_acargar;

    private SoundClip principal;

    private SoundClip saltar;

    private boolean reproduciendo;

    private boolean seacabo;

    public Main() {
        loadBanners();
        pintaPantallaPrincipal();
        cargaJugador();
        cargaNivel("nivel1.txt", "Images/Level1/wallpaper.png");
        nivelActual.moverNivel(0);
        pause = false;
        addMouseListener(this);
        addKeyListener(this);
        juegonuevo = false;
        tiempo = System.currentTimeMillis();
        pausedTime = 0;
        dt = 0;
        juego_empezado = false;
        en_menu_principal = true;
        aviso_teclas = false;
        sin_asignar = true;
        mostrar_nivel = false;
        numeronivel = 1;
        tiempoguardao = 0;
        reproduciendo = false;
        seacabo = false;
        Thread th = new Thread(this);
        th.start();
    }

    /**
     * Metodo actualiza
     * metodo que lleva el tiempo del juego y movimiento del rayo.
     */
    public void actualiza() {
        tiempoString = "" + getTiempo();
        if ((player.getPosXDer() > Constantes.window_width - bandera.getIconWidth()) && !nivelActual.puedoMoverDerecha()) {
            numeronivel++;
            if (numeronivel < 7) {
                String nivel = "nivel" + numeronivel + ".txt";
                String path = "images/Level1/wallpaper.png";
                cargaNivel(nivel, path);
                tiemponivel = (int) (System.currentTimeMillis() / 1000);
                mostrar_nivel = false;
                player.setPuntaje(player.getPuntaje() + 10);
            } else {
                seacabo = true;
            }
        }
        for (int i = 0; i < rayo.length; i++) {
            if (rayo[i].getMoviendo()) {
                rayo[i].moverRayo(player);
            }
            if (rayo[i].getRayoX() > 1029 || rayo[i].getRayoX() < -5) {
                rayo[i].desaparecer();
            }
        }
        nivelActual.seguirRayosWilly();
        nivelActual.checaColisionRayosWillySuelo();
    }

    /**
    * MEtodo run()
    * Metodo que checa toda la interaccion entre objetos del entorno del
    * juego.
    */
    public void run() {
        while (true) {
            if (!pause && player.getVidas() > 0) {
                actualiza();
                if (juegonuevo == true) {
                    nivelActual.checaColisionJugador(player);
                    for (int i = 0; i < rayo.length; i++) {
                        nivelActual.checaColisionRayo(rayo[i]);
                        nivelActual.checaColisionRayoEnemigos(rayo[i], player);
                    }
                    nivelActual.checaColisionEnemigos(player);
                    nivelActual.checaColisionChocolates(player);
                    nivelActual.moverEnemigos(player);
                    nivelActual.mueveTimmy();
                    nivelActual.mueveWilly();
                    nivelActual.checaColisionRayosWillyJugador(player);
                    nivelActual.checaColisionMonedas(player);
                    player.movimiento();
                    if (player.isOutDown()) {
                        nivelActual.resetLevel();
                        nivelActual.resetLimitesNivel();
                        for (int i = 0; i < rayo.length; i++) {
                            rayo[i].desaparecer();
                        }
                        player.resetPlayer(Constantes.posicion_jugador_x, Constantes.posicion_jugador_y);
                    }
                    if (player.getPosXIzq() <= Constantes.limite_izquierda) {
                        if (nivelActual.puedoMoverIzquierda()) {
                            player.mueveJugadorLimite(2);
                            nivelActual.moverNivel(2);
                            nivelActual.scrollEnemigos(2);
                        }
                    } else if (player.getPosXIzq() >= Constantes.limite_derecha) {
                        if (nivelActual.puedoMoverDerecha()) {
                            player.mueveJugadorLimite(-2);
                            nivelActual.moverNivel(-2);
                            nivelActual.scrollEnemigos(-2);
                        }
                    }
                    repaint();
                }
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                System.out.println("Error en " + ex.toString());
            }
        }
    }

    /**
    * Metodo cargaNivel
    * Se encarga de cargar el String con el nombre del archivo que 
    * contiene los caracteres del nivel de juego actual y el wall paper.
    * @param nivel que es el nombre del archivo.
    */
    public void cargaNivel(String nivelDeJuego, String pathImagen) {
        nivelActual = new NivelUno(this, this, Toolkit.getDefaultToolkit().getImage(pathImagen), nivelDeJuego);
        player.resetPlayer(Constantes.posicion_jugador_x, Constantes.posicion_jugador_y);
        mostrar_nivel = false;
        repaint();
    }

    public void cambiaNivel(String newlevel, String pathImagen) {
        NivelUno cargador = new NivelUno(this, this, Toolkit.getDefaultToolkit().getImage(pathImagen), newlevel);
        nivelActual = cargador;
        player.resetPlayer(Constantes.posicion_jugador_x, Constantes.posicion_jugador_y);
        repaint();
    }

    /**
     * Metodo <I>getTiempo</I> 
     * En este metodo se maneja el reloj del juego.
     * @return String con el tiempo a imprimir
     */
    public String getTiempo() {
        String time = "";
        if (juego_empezado && !pause) {
            tiempoparaguardar = System.currentTimeMillis() - tiempo - pausedTime;
            if ((System.currentTimeMillis() - tiempo - pausedTime + tiempoguardao) / 60000 < 10) {
                time += "0" + ((System.currentTimeMillis() - tiempo - pausedTime + tiempoguardao) / 60000) + ":";
            } else {
                time += (System.currentTimeMillis() - tiempo - pausedTime + tiempoguardao) / 60000 + ":";
            }
            if ((System.currentTimeMillis() - this.tiempo - pausedTime + tiempoguardao) / 1000 % 60 < 10) {
                time += "0" + (System.currentTimeMillis() - this.tiempo - pausedTime + tiempoguardao) / 1000 % 60;
            } else {
                time += "" + ((+System.currentTimeMillis() - this.tiempo - pausedTime + tiempoguardao) / 1000) % 60;
            }
            return time;
        } else if (juego_empezado && pause) {
            return time;
        } else {
            tiempo = System.currentTimeMillis();
            return "";
        }
    }

    /**
     * Metodo loadBanners()
     * Se encarga de cargar los botones y appliances del juego
     * 
     */
    public void loadBanners() {
        bannerPantallaPrincipal = new Banner[5];
        bannerOpciones = new Banner[8];
        banner3 = new Banner[4];
        bannerPantallaPrincipal[0] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/header.png"), this);
        bannerPantallaPrincipal[1] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/juegonuevo.png"), this);
        bannerPantallaPrincipal[2] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/cargarjuego.png"), this);
        bannerPantallaPrincipal[3] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/opciones.png"), this);
        bannerPantallaPrincipal[4] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/ayuda.png"), this);
        bannerOpciones[0] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/configteclas.png"), this);
        bannerOpciones[1] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/atras.png"), this);
        bannerOpciones[2] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/salto.png"), this);
        bannerOpciones[3] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/adelante.png"), this);
        bannerOpciones[4] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/volver.png"), this);
        bannerOpciones[5] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/opciones.png"), this);
        bannerOpciones[6] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/disparo.png"), this);
        bannerOpciones[7] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/teclea.png"), this);
        banner3[0] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/controles.png"), this);
        banner3[1] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/controles2.png"), this);
        banner3[2] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/ayuda.png"), this);
        banner3[3] = new Banner(0, 0, Toolkit.getDefaultToolkit().getImage("images/Jimmy/peq.gif"), this);
        nivel1 = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Images/numeros/nivel1.gif"));
        nivel2 = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Images/numeros/nivel2.gif"));
        nivel3 = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Images/numeros/nivel3.gif"));
        nivel4 = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Images/numeros/nivel4.gif"));
        nivel5 = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Images/numeros/nivel5.gif"));
        nivel6 = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Images/numeros/nivel6.gif"));
        nivel7 = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Images/numeros/nivel7.gif"));
        nivel8 = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Images/numeros/nivel8.gif"));
        nivel9 = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Images/numeros/nivel9.gif"));
        nivel10 = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Images/numeros/nivel10.gif"));
        pausado = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Images/numeros/pausado.png"));
        bandera = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Images/calzones.png"));
        fin = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Images/numeros/FIN.png"));
        shade = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Images/numeros/shade.png"));
    }

    /**
    * Metodo pintaPantallaPrincipal()
    * metodo que pinta la pantalla principal.
    */
    private void pintaPantallaPrincipal() {
        for (int i = 0; i <= 4; i++) {
            bannerPantallaPrincipal[i].setPosX((WIDTH / 2 - bannerPantallaPrincipal[i].getAncho() / 2));
            bannerPantallaPrincipal[i].setPosY((HEIGHT / 5) * (i));
        }
    }

    /**
    * Metodo pintaPantallaOpciones()
    * metodo que pinta la pantalla de opciones
    */
    private void pintaPantallaOpciones() {
        System.out.println("MEtodo Pantalla Opciones sigue vacio");
    }

    /**
     * Metodo cargaJugador()
     * Metodo que: inicializa, lee imagenes, 
     * y carga rayo que dispara el jugador
     */
    private void cargaJugador() {
        player = new Jugador(100, 200, this);
        imgParadoDer = Toolkit.getDefaultToolkit().getImage("Images/Jimmy/ParadoDerecha.gif");
        imgCaminaDer = Toolkit.getDefaultToolkit().getImage("Images/Jimmy/CaminaDerecha.gif");
        imgSaltaDer = Toolkit.getDefaultToolkit().getImage("Images/Jimmy/SaltaDerecha.gif");
        imgParadoIzq = Toolkit.getDefaultToolkit().getImage("Images/Jimmy/ParadoIzquierda.gif");
        imgCaminaIzq = Toolkit.getDefaultToolkit().getImage("Images/Jimmy/CaminaIzquierda.gif");
        imgSaltaIzq = Toolkit.getDefaultToolkit().getImage("Images/Jimmy/SaltaIzquierda.gif");
        player.setImagenes(imgParadoDer, imgCaminaDer, imgSaltaDer, imgParadoIzq, imgCaminaIzq, imgSaltaIzq);
        rayo = new rayoJugador[3];
        for (int i = 0; i < rayo.length; i++) {
            rayo[i] = new rayoJugador(-10, -10, Toolkit.getDefaultToolkit().getImage("images/rayo.png"), this);
        }
    }

    /**
     * Metodo KeyPressed()
     * Metodo que checa si se ha presionado una Tecla
     * @param e
     */
    public void keyPressed(KeyEvent e) {
        if (player.getVidas() > 0) {
            if (e.getKeyCode() == e.VK_ESCAPE) {
                tiemponivel = (int) (System.currentTimeMillis() / 1000);
                juegonuevo = false;
                opcion = 0;
                juego_empezado = false;
                en_menu_principal = true;
                repaint();
            }
            aviso_teclas = false;
            if (sin_asignar) {
                atras = e.VK_LEFT;
                salto = e.VK_A;
                adelante = e.VK_RIGHT;
                disparo = e.VK_S;
                sin_asignar = false;
            }
            if (opcion2 == 1) {
                atras = e.getKeyCode();
                opcion2 = 5;
                repaint();
            }
            if (opcion2 == 2) {
                salto = e.getKeyCode();
                opcion2 = 5;
                repaint();
            }
            if (opcion2 == 3) {
                adelante = e.getKeyCode();
                opcion2 = 5;
                repaint();
            }
            if (opcion2 == 4) {
                disparo = e.getKeyCode();
                opcion2 = 5;
                repaint();
            }
            if (opcion2 == 0) {
                if (e.getKeyCode() == e.VK_LEFT) {
                    player.setCaminaIzq(true);
                }
                if (e.getKeyCode() == e.VK_RIGHT) {
                    player.setCaminaDer(true);
                }
                if (e.getKeyCode() == e.VK_A) {
                    player.setSalta(true);
                }
                if (e.getKeyCode() == e.VK_S) {
                    for (int i = 0; i < rayo.length; i++) {
                        if (rayo[i].getMoviendo() == false) {
                            rayo[i].setMoviendo(true);
                            rayo[i].setRayoX(player.getPosXDer());
                            rayo[i].setRayoY(player.getPosYAba() - Constantes.jugador_height / 2);
                            break;
                        }
                    }
                }
            } else {
                if (e.getKeyCode() == atras) {
                    player.setCaminaIzq(true);
                }
                if (e.getKeyCode() == adelante) {
                    player.setCaminaDer(true);
                }
                if (e.getKeyCode() == salto) {
                    player.setSalta(true);
                }
                if (e.getKeyCode() == disparo) {
                    for (int i = 0; i < rayo.length; i++) {
                        if (rayo[i].getMoviendo() == false) {
                            rayo[i].setMoviendo(true);
                            rayo[i].setRayoX(player.getPosXDer());
                            rayo[i].setRayoY(player.getPosYAba() - Constantes.jugador_height / 2);
                            break;
                        }
                    }
                }
            }
            if (e.getKeyCode() == e.VK_P) {
                if (!pause) {
                    pause = true;
                    dt = System.currentTimeMillis();
                } else {
                    pausedTime += (System.currentTimeMillis() - dt);
                    dt = 0;
                    pause = false;
                }
            }
            if (e.getKeyCode() == e.VK_G) {
                if (!pause) {
                    pause = true;
                    dt = System.currentTimeMillis();
                } else {
                    pausedTime += (System.currentTimeMillis() - dt);
                    dt = 0;
                    pause = false;
                }
                try {
                    save();
                } catch (IOException m) {
                }
            }
        }
        if (player.getVidas() == 0) {
            if (e.getKeyCode() == e.VK_Y) {
                cargaNivel("nivel1.txt", "Images/Level1/wallpaper.png");
                juegonuevo = true;
                opcion = 1;
                juego_empezado = true;
                en_menu_principal = false;
                player.setVidas(3);
                numeronivel = 1;
            }
            if (e.getKeyCode() == e.VK_N) {
                System.exit(1);
            }
        }
    }

    /**
     * Metodo keyReleased
     * Metodoq ue checa si se ha soltado una tecla.
     * @param e
     */
    public void keyReleased(KeyEvent e) {
        if (player.getVidas() > 0) {
            if (opcion2 == 0) {
                if (e.getKeyCode() == e.VK_LEFT) {
                    player.setCaminaIzq(false);
                }
                if (e.getKeyCode() == e.VK_RIGHT) {
                    player.setCaminaDer(false);
                }
                if (e.getKeyCode() == e.VK_A) {
                    player.setSalta(false);
                }
            } else {
                if (e.getKeyCode() == atras) {
                    player.setCaminaIzq(false);
                }
                if (e.getKeyCode() == adelante) {
                    player.setCaminaDer(false);
                }
                if (e.getKeyCode() == salto) {
                    player.setSalta(false);
                }
            }
        }
    }

    /**
     * Metodo keyTyped()
     * No tiene nada.
     * @param arg0
     */
    public void keyTyped(KeyEvent arg0) {
    }

    /**
     * MEtodo mouseClicked
     * No tiene nada
     * @param arg0
     */
    public void mouseClicked(MouseEvent arg0) {
    }

    /**
     * MEtodo mouseEntered
     * No tiene nada
     * @param arg0
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * MEtodo mouseExited
     * No tiene nada
     * @param arg0
     */
    public void mouseExited(MouseEvent arg0) {
    }

    /**
     * MEtodo mousePressed
     * Contiene las acciones del mouse maneja la interaccion mouse Banner
     * @param arg0
     */
    public void mousePressed(MouseEvent e) {
        if (bannerPantallaPrincipal[1].sobreImagen(e)) {
            if (en_menu_principal) {
                juegonuevo = true;
                opcion = 1;
                juego_empezado = true;
                en_menu_principal = false;
                tiemponivel = (int) (System.currentTimeMillis() / 1000);
            }
        }
        if (bannerPantallaPrincipal[2].sobreImagen(e)) {
            if (en_menu_principal) {
                try {
                    open();
                } catch (IOException m) {
                }
            }
        }
        if (bannerPantallaPrincipal[3].sobreImagen(e)) {
            if (en_menu_principal) {
                bannerOpciones[0].setPosX(WIDTH / 2 - bannerOpciones[0].getAncho() / 2);
                bannerOpciones[0].setPosY(HEIGHT / 3 - bannerOpciones[0].getAlto());
                bannerOpciones[1].setPosX(30);
                bannerOpciones[1].setPosY(HEIGHT / 2 - bannerOpciones[1].getAlto() + 70);
                bannerOpciones[2].setPosX(WIDTH / 2 - bannerOpciones[2].getAncho() / 2);
                bannerOpciones[2].setPosY(HEIGHT / 2 - bannerOpciones[2].getAlto());
                bannerOpciones[3].setPosX(WIDTH - bannerOpciones[3].getAncho() - 30);
                bannerOpciones[3].setPosY(HEIGHT / 2 - bannerOpciones[3].getAlto() + 70);
                bannerOpciones[4].setPosX(WIDTH - bannerOpciones[4].getAncho() - 50);
                bannerOpciones[4].setPosY(HEIGHT - bannerOpciones[4].getAlto() - 50);
                bannerOpciones[5].setPosX(WIDTH - bannerOpciones[5].getAncho() - 10);
                bannerOpciones[5].setPosY(30);
                bannerOpciones[6].setPosX(WIDTH / 2 - bannerOpciones[6].getAncho() / 2);
                bannerOpciones[6].setPosY(HEIGHT / 2 - bannerOpciones[3].getAlto() + 70);
                bannerOpciones[7].setPosX(WIDTH / 2 - bannerOpciones[7].getAncho() / 2);
                bannerOpciones[7].setPosY(HEIGHT / 2 - bannerOpciones[3].getAlto() / 2);
                opcion = 3;
                en_menu_principal = false;
                repaint();
            }
        }
        if (bannerPantallaPrincipal[4].sobreImagen(e)) {
            if (en_menu_principal) {
                bannerOpciones[4].setPosX(WIDTH - bannerOpciones[4].getAncho() - 50);
                bannerOpciones[4].setPosY(HEIGHT - bannerOpciones[4].getAlto() - 50);
                banner3[0].setPosX(WIDTH - banner3[0].getAncho() - 30);
                banner3[0].setPosY(HEIGHT / 2 - banner3[0].getAlto() / 2);
                banner3[1].setPosX(30);
                banner3[1].setPosY(HEIGHT / 2);
                banner3[2].setPosX(WIDTH - bannerPantallaPrincipal[3].getAncho() - 10);
                banner3[2].setPosY(30);
                banner3[3].setPosX(400);
                banner3[3].setPosY(120);
                opcion = 4;
                en_menu_principal = false;
                repaint();
            }
        }
        if (bannerOpciones[4].sobreImagen(e)) {
            opcion = 0;
            en_menu_principal = true;
            repaint();
        }
        if (bannerOpciones[1].sobreImagen(e)) {
            opcion2 = 1;
            aviso_teclas = true;
            repaint();
        }
        if (bannerOpciones[2].sobreImagen(e)) {
            opcion2 = 2;
            aviso_teclas = true;
            repaint();
        }
        if (bannerOpciones[3].sobreImagen(e)) {
            opcion2 = 3;
            aviso_teclas = true;
            repaint();
        }
        if (bannerOpciones[6].sobreImagen(e)) {
            opcion2 = 4;
            aviso_teclas = true;
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }

    public void paint(Graphics g) {
        update(g);
    }

    public void update(Graphics g) {
        Dimension size = getSize();
        if (doubleBuffer == null || doubleBuffer.getWidth(this) != size.width || doubleBuffer.getHeight(this) != size.height) {
            doubleBuffer = createImage(size.width, size.height);
        }
        if (doubleBuffer != null) {
            Graphics g2 = doubleBuffer.getGraphics();
            paint1(g2);
            g2.dispose();
            g.drawImage(doubleBuffer, 0, 0, null);
        } else {
            paint1(g);
        }
    }

    /**
     * Metodo <I>paint1</I> sobrescrito de la clase <code>Applet</code>,
     * heredado de la clase Container.<P>
     * En este metodo se dibuja la imagen con la posicion actualizada,
     * ademas que cuando la imagen es cargada te despliega una advertencia.
     * @param g es el <code>objeto grafico</code> usado para dibujar.
     */
    public void paint1(Graphics g) {
        if (juegonuevo == false) {
            g.clearRect(0, 0, WIDTH, HEIGHT);
            g.drawImage(Toolkit.getDefaultToolkit().getImage("images/fondoInicio.png"), 0, 0, this);
            if (opcion == 0) {
                for (int i = 0; i < 5; i++) {
                    bannerPantallaPrincipal[i].paintBanner(g);
                }
            }
            if (opcion == 3) {
                for (int i = 0; i < 7; i++) {
                    bannerOpciones[i].paintBanner(g);
                }
                if (opcion2 == 1 && aviso_teclas == true) {
                    g.clearRect(0, 0, WIDTH, HEIGHT);
                    bannerOpciones[7].paintBanner(g);
                }
                if (opcion2 == 2 && aviso_teclas == true) {
                    g.clearRect(0, 0, WIDTH, HEIGHT);
                    bannerOpciones[7].paintBanner(g);
                }
                if (opcion2 == 3 && aviso_teclas == true) {
                    g.clearRect(0, 0, WIDTH, HEIGHT);
                    bannerOpciones[7].paintBanner(g);
                }
                if (opcion2 == 4 && aviso_teclas == true) {
                    g.clearRect(0, 0, WIDTH, HEIGHT);
                    bannerOpciones[7].paintBanner(g);
                }
            }
            if (opcion == 4) {
                g.drawString("Objetivo del juego:", 50, 70);
                g.drawString("Llegar a la meta sin tocar a los enemigos", 50, 90);
                g.drawString("Teclas por default:", 50, 140);
                g.drawString("Saltar: Tecla A", 50, 170);
                g.drawString("Atras:Flecha izquierda", 50, 190);
                g.drawString("Adelante: Flecha derecha", 50, 210);
                g.drawString("Disparar: Tecla S ", 50, 230);
                g.drawString("Enemigos:", 300, 140);
                g.drawString("Durante el juego aparceran estos singulares personajes", 300, 160);
                g.drawString("los cuales debes de esquivar ya que si te tocan perderas", 300, 180);
                g.drawString("una vida y volveras al comienzo del nivel", 300, 200);
                bannerOpciones[4].paintBanner(g);
                for (int i = 0; i < 4; i++) {
                    banner3[i].paintBanner(g);
                }
            }
        } else {
            if (!seacabo) {
                g.clearRect(0, 0, WIDTH, HEIGHT);
                nivelActual.pintarNivel(g);
                g.setColor(Color.black);
                if (player.getVidas() > 0) {
                    drawClock(g, getTiempo(), 30, 50);
                    drawVidas(g, Constantes.window_width - 90, 65);
                    drawPuntaje(g, Constantes.window_width - 90, 95);
                }
                player.pintaJugador(g);
                mostrarNivel(g);
                for (int i = 0; i < rayo.length; i++) {
                    rayo[i].pintarRayo(g);
                }
                if (pause == true) {
                    g.drawImage(pausado.getImage(), WIDTH / 2 - pausado.getIconWidth() / 2, HEIGHT / 2 - pausado.getIconHeight() / 2, this);
                }
                if (!nivelActual.puedoMoverDerecha()) {
                    g.drawImage(bandera.getImage(), Constantes.window_width - bandera.getIconWidth(), Constantes.window_height - (Constantes.bandera_Y + bandera.getIconHeight()), this);
                }
                repaint();
            } else {
                g.drawImage(shade.getImage(), 0, 0, this);
                g.drawImage(fin.getImage(), Constantes.window_width / 2 - fin.getIconWidth() / 2, Constantes.window_height / 2 - fin.getIconHeight() / 2, this);
                repaint();
            }
        }
    }

    /**
     * Metodo pintaReloj()
     * Metodo que se encarga de cambiar el String de Reloj a 
     * un reloj con imagenes
     * 
     * @param g Grafico
     * @param time String del tiempo
     * @param posX posicion x del reloj
     * @param posY posicion Y del reloj
     */
    public void drawClock(Graphics g, String time, int posX, int posY) {
        char caracter;
        int acumuladoDer = posX;
        g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/tiempo.png"), acumuladoDer, posY, null);
        acumuladoDer += 172;
        for (int i = 0; i < 5; i++) {
            if (!pause) {
                caracter = time.charAt(i);
                switch(caracter) {
                    case '0':
                        g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/0.png"), acumuladoDer, posY + 17, null);
                        acumuladoDer += 41;
                        break;
                    case '1':
                        g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/1.png"), acumuladoDer, posY + 17, null);
                        acumuladoDer += 41;
                        break;
                    case '2':
                        g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/2.png"), acumuladoDer, posY + 17, null);
                        acumuladoDer += 41;
                        break;
                    case '3':
                        g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/3.png"), acumuladoDer, posY + 17, null);
                        acumuladoDer += 41;
                        break;
                    case '4':
                        g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/4.png"), acumuladoDer, posY + 17, null);
                        acumuladoDer += 41;
                        break;
                    case '5':
                        g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/5.png"), acumuladoDer, posY + 17, null);
                        acumuladoDer += 41;
                        break;
                    case '6':
                        g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/6.png"), acumuladoDer, posY + 17, null);
                        acumuladoDer += 41;
                        break;
                    case '7':
                        g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/7.png"), acumuladoDer, posY + 17, null);
                        acumuladoDer += 41;
                        break;
                    case '8':
                        g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/8.png"), acumuladoDer, posY + 17, null);
                        acumuladoDer += 41;
                        break;
                    case '9':
                        g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/9.png"), acumuladoDer, posY + 17, null);
                        acumuladoDer += 41;
                        break;
                    case ':':
                        g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/punkte.png"), acumuladoDer, posY + 27, null);
                        acumuladoDer += 14;
                        break;
                    default:
                        ;
                        break;
                }
            }
        }
    }

    /**
     * Metodo drawVidas()
     * Metodo que pinta el numero de puntaje en una posicion dada
     * 
     * @param g     grafico
     * @param posX  posicion en x
     * @param posY  posicion en y
     */
    public void drawVidas(Graphics g, int posX, int posY) {
        String vidas;
        char caracter;
        int acumuladoDer = posX;
        vidas = "";
        if (this.player.getVidas() < 10) {
            vidas = "00" + this.player.getVidas();
        } else {
            if (this.player.getVidas() < 99) {
                vidas = "0" + this.player.getVidas();
            }
        }
        for (int i = vidas.length() - 1; i >= 0; i--) {
            caracter = vidas.charAt(i);
            switch(caracter) {
                case '0':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/0ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '1':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/1ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '2':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/2ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '3':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/3ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '4':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/4ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '5':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/5ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '6':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/6ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '7':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/7ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '8':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/8ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '9':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/9ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                default:
                    ;
                    break;
            }
        }
        g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/x.png"), acumuladoDer, posY, null);
        acumuladoDer -= 16;
        g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/Jimmy/Peq.gif"), acumuladoDer, posY, null);
    }

    /**
     * Metodo drawPuntaje()
     * Metodo que pinta el numero de puntaje en una posicion dada
     * 
     * @param g     grafico
     * @param posX  posicion en x
     * @param posY  posicion en y
     */
    public void drawPuntaje(Graphics g, int posX, int posY) {
        String puntaje;
        char caracter;
        int acumuladoDer = posX;
        puntaje = "";
        if (this.player.getPuntaje() < 10) {
            puntaje = "00" + this.player.getPuntaje();
        } else {
            if (this.player.getPuntaje() < 99) {
                puntaje = "0" + this.player.getPuntaje();
            }
        }
        for (int i = puntaje.length() - 1; i >= 0; i--) {
            caracter = puntaje.charAt(i);
            switch(caracter) {
                case '0':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/0ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '1':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/1ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '2':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/2ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '3':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/3ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '4':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/4ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '5':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/5ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '6':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/6ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '7':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/7ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '8':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/8ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                case '9':
                    g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/9ch.png"), acumuladoDer, posY, null);
                    acumuladoDer -= 16;
                    break;
                default:
                    ;
                    break;
            }
        }
        g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/numeros/x.png"), acumuladoDer, posY, null);
        acumuladoDer -= (Toolkit.getDefaultToolkit().getImage("Images/candy.png").getWidth(rootPane) + 5);
        g.drawImage(Toolkit.getDefaultToolkit().getImage("Images/candy.png"), acumuladoDer, posY, null);
    }

    public void mostrarNivel(Graphics g) {
        int tiempoacomparar = (int) (System.currentTimeMillis() / 1000) - tiemponivel;
        try {
            if ((tiempoacomparar < 3 && !mostrar_nivel)) {
                switch(numeronivel) {
                    case 1:
                        {
                            g.drawImage(nivel1.getImage(), WIDTH / 2 - nivel1.getIconWidth() / 2, HEIGHT / 2 - nivel1.getIconHeight() / 2, this);
                            break;
                        }
                    case 2:
                        {
                            g.drawImage(nivel2.getImage(), WIDTH / 2 - nivel1.getIconWidth() / 2, HEIGHT / 2 - nivel1.getIconHeight() / 2, this);
                            break;
                        }
                    case 3:
                        {
                            g.drawImage(nivel3.getImage(), WIDTH / 2 - nivel1.getIconWidth() / 2, HEIGHT / 2 - nivel1.getIconHeight() / 2, this);
                            break;
                        }
                    case 4:
                        {
                            g.drawImage(nivel4.getImage(), WIDTH / 2 - nivel1.getIconWidth() / 2, HEIGHT / 2 - nivel1.getIconHeight() / 2, this);
                            break;
                        }
                    case 5:
                        {
                            g.drawImage(nivel5.getImage(), WIDTH / 2 - nivel1.getIconWidth() / 2, HEIGHT / 2 - nivel1.getIconHeight() / 2, this);
                            break;
                        }
                    case 6:
                        {
                            g.drawImage(nivel6.getImage(), WIDTH / 2 - nivel1.getIconWidth() / 2, HEIGHT / 2 - nivel1.getIconHeight() / 2, this);
                            break;
                        }
                    case 7:
                        {
                            g.drawImage(nivel7.getImage(), WIDTH / 2 - nivel1.getIconWidth() / 2, HEIGHT / 2 - nivel1.getIconHeight() / 2, this);
                            break;
                        }
                    case 8:
                        {
                            g.drawImage(nivel8.getImage(), WIDTH / 2 - nivel1.getIconWidth() / 2, HEIGHT / 2 - nivel1.getIconHeight() / 2, this);
                            break;
                        }
                    case 9:
                        {
                            g.drawImage(nivel9.getImage(), WIDTH / 2 - nivel1.getIconWidth() / 2, HEIGHT / 2 - nivel1.getIconHeight() / 2, this);
                            break;
                        }
                    case 10:
                        {
                            g.drawImage(nivel10.getImage(), WIDTH / 2 - nivel1.getIconWidth() / 2, HEIGHT / 2 - nivel1.getIconHeight() / 2, this);
                            break;
                        }
                }
            } else {
                mostrar_nivel = true;
            }
        } catch (StringIndexOutOfBoundsException e) {
        }
    }

    /**
     * Metodo principal
     *
     * @param args es un arreglo de tipo <code>String</code> de linea de comandos
     */
    public static void main(String[] args) {
        Main juego = new Main();
        juego.setSize(WIDTH, HEIGHT);
        juego.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        juego.setVisible(true);
        juego.setResizable(false);
    }

    public static void score() throws IOException {
        PrintWriter fileOut = new PrintWriter(new FileWriter("highscore.txt"));
        fileOut.close();
    }

    /**
     * Metodo save()
     * Guarda el juego actual en curso.
     * @throws java.io.IOException
     */
    public void save() throws IOException {
        PrintWriter fileOut = new PrintWriter(new FileWriter(saveFile()));
        fileOut.println(numeronivel);
        fileOut.println(tiempoparaguardar);
        fileOut.println(player.getVidas());
        fileOut.println(player.getPuntaje());
        fileOut.close();
    }

    /**
     * Metodo open()
     * metodo que abre un archivo dado de acuerdo con getFile()
     * @throws java.io.IOException
     */
    public void open() throws IOException {
        if (getFile() != null) {
            Scanner fileIn = new Scanner(getFile());
            String linea = "";
            linea = fileIn.nextLine();
            numero_acargar = Integer.parseInt(linea);
            cargaNivel("nivel" + numero_acargar + ".txt", "Images/Level1/wallpaper.png");
            numeronivel = numero_acargar;
            linea = fileIn.nextLine();
            tiempoguardao = Integer.parseInt(linea);
            linea = fileIn.nextLine();
            player.setVidas(Integer.parseInt(linea));
            linea = fileIn.nextLine();
            player.setPuntaje(Integer.parseInt(linea));
            tiemponivel = (int) (System.currentTimeMillis() / 1000);
            juegonuevo = true;
            opcion = 1;
            juego_empezado = true;
            en_menu_principal = false;
        }
    }

    /**
     * Metodo getFile()
     * Metodo que permite al usuario utilizar JFileChooser para que el 
     * usuario utilice un archivo que se encuentra en su carpeta.
     * @return
     */
    private File getFile() {
        boolean cancelado = false;
        String path = System.getProperty("user.dir");
        JFileChooser fileChooser = new JFileChooser(path);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) {
            cancelado = true;
        }
        File fileName;
        fileName = fileChooser.getSelectedFile();
        if (((fileName == null) || (fileName.getName().equals(""))) && cancelado == false) {
            JOptionPane.showMessageDialog(this, "Invalid File Name", "Invalid File Name", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        return fileName;
    }

    private File saveFile() {
        String path = System.getProperty("user.dir");
        JFileChooser fc = new JFileChooser(new File(path));
        fc.showSaveDialog(this);
        File selFile = fc.getSelectedFile();
        if (!selFile.getName().contains(".txt")) {
            System.out.println("no le puso");
        }
        JOptionPane.showMessageDialog(this, "Juego Guardado Exitosamente", "Guardar como...", JOptionPane.INFORMATION_MESSAGE);
        return selFile;
    }
}
