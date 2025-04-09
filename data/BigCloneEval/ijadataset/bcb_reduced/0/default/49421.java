import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class WeiLiYu extends Thread implements ActionListener {

    static WeiLiYuStrings ws;

    static Fish[] fish;

    static HashSet swimming = new HashSet();

    static int num_chars;

    static Image[] chars;

    static String[] chars_pinyin;

    static int[] chars_tones;

    static String[] chars_unicode;

    static HashMap char_to_location = new HashMap();

    static int[] correctc;

    static int[] incorrectc;

    static int miss;

    static long start_time;

    static long stop_duration;

    static int frames;

    static int frame_duration_avg;

    static int unfed_fish;

    static Image[] pinyin;

    static Image[] tones;

    static int[] p_offset;

    static Image[] s_pinyin;

    static Image[] s_tones;

    static int[] s_p_offset;

    static Image[] fish_food;

    static Image[][] fish_anim;

    static Image background;

    static Image congrats;

    static Image hungry;

    static Image hungry_cloud;

    static Graphics main;

    static JLabel screen;

    static JProgressBar prog;

    static JLayeredPane layeredPane;

    static JPanel retry_panel;

    static JLabel accuracy;

    static JFrame gamewind;

    static JButton save_list;

    static BufferedImage offscreen_buffer;

    static Graphics2D graphics;

    static SyllableTree stree = new SyllableTree();

    static FontRenderer fr;

    static SettingFrame sf;

    static File default_dir = null;

    static int[] chars_to_study;

    static int num_chars_to_study;

    static int cur_buff = 0;

    static double[] theta_table;

    private void drawSprite(Image sprite, int x, int y) {
        AffineTransform at = new AffineTransform(1, 0, 0, 1, x, y);
        graphics.drawImage(sprite, at, null);
    }

    private void drawSprite(Image sprite, int x, int y, double t, int rx, int ry) {
        AffineTransform at = new AffineTransform(1, 0, 0, 1, x, y);
        at.rotate(t, rx, ry);
        graphics.drawImage(sprite, at, null);
    }

    private void drawPinyinString(Image[] pinyin, Image[] tones, int[] p_offset, int space, int bx, int by, String s, int tone) {
        int width = 0;
        int tonemark_char = -1;
        for (int i = 0; i < s.length(); i++) {
            int c = s.charAt(i);
            if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' || c == 'v') {
                if (tonemark_char == -1 || s.charAt(tonemark_char) == 'i' || s.charAt(tonemark_char) == 'u') tonemark_char = i;
            }
            c -= 'a';
            width += pinyin[c].getWidth(null) + space;
        }
        bx = bx - width / 2;
        for (int i = 0; i < s.length(); i++) {
            int c = s.charAt(i);
            if (c == 'i' && tonemark_char == i && tone != 5) c = 26; else c -= 'a';
            drawSprite(pinyin[c], bx, by + 1 - pinyin[c].getHeight(null) + p_offset[c]);
            if (tonemark_char == i && tone != 5) {
                drawSprite(tones[tone - 1], bx + pinyin[c].getWidth(null) / 2 - tones[tone - 1].getWidth(null) / 2, by - pinyin[c].getHeight(null) - tones[tone - 1].getHeight(null) + p_offset[c] - 1);
            }
            bx += pinyin[c].getWidth(null) + space;
        }
    }

    public void swap_buffers() {
        if (Settings.MOVIE_MODE) {
            try {
                String framestr = Integer.toString(frames);
                ImageIO.write(offscreen_buffer, "png", new File("0000000000".substring(framestr.length()) + framestr + "_WLYMOVIE.png"));
            } catch (Exception e) {
                System.err.println("MOVIE MODE: Exception writing image...");
            }
        }
        main.drawImage(offscreen_buffer, 0, 0, null);
    }

    public void permanent_swap() {
        screen.setIcon(new ImageIcon(offscreen_buffer));
        cur_buff = 1 - cur_buff;
    }

    public void drawPChar(int i, int x, int y) {
        drawSprite(chars[i], x - chars[i].getWidth(null) / 2, y + 4);
        drawPinyinString(s_pinyin, s_tones, s_p_offset, 1, x, y - 4, chars_pinyin[i], chars_tones[i]);
    }

    public void run() {
        screen.setIcon(null);
        KeyStates.key_typed = 0;
        stop_duration = (int) (Settings.GAME_DURATION * 60000);
        System.out.println("Will stop in ~" + stop_duration / 1000 + " seconds.");
        frames = 0;
        unfed_fish = 0;
        frame_duration_avg = 0;
        long frame_start;
        long frame_duration = 0;
        long add_every = Settings.FISH_ADD_TIME;
        long last_added_time = -add_every;
        long game_time = 0;
        LetterNode cur_letter = stree.root;
        String cur_string = "";
        int cur_tone = 5;
        start_time = System.currentTimeMillis();
        for (; ; ) {
            frame_start = System.currentTimeMillis();
            drawSprite(background, 0, 0);
            boolean hasActiveFish = false;
            for (int i = 0; i < fish.length; i++) {
                if (fish[i].active && fish[i].draw_food) {
                    Image f = fish_food[fish[i].food_frame];
                    drawSprite(f, fish[i].food_x - f.getWidth(null) / 2, fish[i].food_y - f.getHeight(null) / 2);
                }
            }
            if (frame_duration > 100) frame_duration = 100;
            if (Settings.MOVIE_MODE) frame_duration = 40;
            game_time += frame_duration;
            for (int i = 0; i < fish.length; i++) {
                if (fish[i].active) {
                    hasActiveFish = true;
                    int f = fish[i].current_frame;
                    drawSprite(fish[i].anim[f], fish[i].current_x, fish[i].current_y);
                    drawSprite(fish[i].character, fish[i].current_x + Settings.CHAR_OFFSET_X, fish[i].current_y + Settings.CHAR_OFFSET_Y, theta_table[f], Settings.CHAR_ROT_AXIS_X, Settings.CHAR_ROT_AXIS_Y);
                    fish[i].updateLocation(frame_duration);
                    if (fish[i].current_y + Settings.FISH_HEIGHT - fish[i].food_y > Settings.EAT_LOCATION) fish[i].draw_food = false;
                    if (fish[i].current_y > Settings.SCREEN_HEIGHT) {
                        fish[i].active = false;
                        if (!fish[i].fed) {
                            String pronounce = fish[i].pinyin + fish[i].tone;
                            swimming.remove(pronounce);
                            incorrectc[fish[i].char_num]++;
                            unfed_fish--;
                        }
                    }
                } else if ((game_time - last_added_time > add_every || unfed_fish == 0) && game_time < stop_duration) {
                    int c = (int) (Math.random() * num_chars);
                    String pronounce = chars_pinyin[c] + chars_tones[c];
                    if (!swimming.contains(pronounce)) {
                        int speed = (int) (Math.random() * (Settings.FISH_MAX_SPEED - Settings.FISH_MIN_SPEED)) + Settings.FISH_MIN_SPEED;
                        int randx = (int) (Math.random() * (Settings.SCREEN_WIDTH - Settings.FISH_WIDTH));
                        boolean ok_to_add = true;
                        for (int j = 0; j < fish.length; j++) {
                            if (fish[j].active && Math.abs(fish[j].current_x - randx) <= Settings.FISH_WIDTH) {
                                long time_until_bottom = fish[j].getTimeUntil(Settings.SCREEN_HEIGHT);
                                int y_loc = Fish.getYLocationAt(speed, -Settings.FISH_HEIGHT, time_until_bottom) + Settings.FISH_HEIGHT;
                                if (fish[j].current_y <= Settings.FISH_START_SPACE || y_loc >= Settings.SCREEN_HEIGHT) {
                                    ok_to_add = false;
                                    break;
                                }
                            }
                        }
                        if (ok_to_add) {
                            swimming.add(pronounce);
                            last_added_time = game_time;
                            fish[i].activate(fish_anim[(int) (Math.random() * 7)], (int) (Math.random() * 25), speed, randx, -Settings.FISH_HEIGHT, c, chars[c], chars_pinyin[c], chars_tones[c]);
                            unfed_fish++;
                        }
                    }
                }
            }
            if (!hasActiveFish && game_time > stop_duration) {
                System.out.println("END GAME");
                int overall_i = 0;
                int overall_c = 0;
                chars_to_study = new int[10];
                num_chars_to_study = 0;
                int cur = 0;
                for (int i = 0; i < 1024; i++) {
                    overall_i += incorrectc[i];
                    overall_c += correctc[i];
                    if (incorrectc[i] != 0) {
                        double total = incorrectc[i] + correctc[i];
                        int rate = (int) (correctc[i] / total * 100);
                        System.out.println("Character #" + i + " (" + chars_pinyin[i] + chars_tones[i] + "): correct pinyin " + rate + "%");
                        if (num_chars_to_study != chars_to_study.length) {
                            chars_to_study[num_chars_to_study] = i;
                            num_chars_to_study++;
                        } else if (correctc[i] == 0) {
                            chars_to_study[cur] = i;
                            cur++;
                            if (cur == chars_to_study.length) cur = 0;
                        }
                    }
                }
                double total = overall_i + miss + overall_c;
                if (total != 0) {
                    int rate = (int) (overall_c / total * 100);
                    System.out.println("Overall: " + rate + "% correct.");
                    accuracy.setText(WeiLiYu.ws.overall_accuracy() + rate + "%");
                }
                if (num_chars_to_study == 0) {
                    drawSprite(congrats, 0, 0);
                    save_list.setEnabled(false);
                } else {
                    save_list.setEnabled(true);
                    drawSprite(hungry_cloud, Settings.SCREEN_WIDTH / 2 - hungry_cloud.getWidth(null) / 2, Settings.SCREEN_HEIGHT / 2 - hungry_cloud.getHeight(null) / 2);
                    drawSprite(hungry, Settings.SCREEN_WIDTH / 2 - hungry.getWidth(null) / 2, Settings.SCREEN_HEIGHT / 2 - hungry.getHeight(null) / 2 - 82);
                    if (num_chars_to_study == 1) {
                        drawPChar(chars_to_study[0], Settings.SCREEN_WIDTH / 2, Settings.SCREEN_HEIGHT / 2);
                    } else {
                        int chars_row1 = (num_chars_to_study + 1) / 2;
                        int chars_row2;
                        if (num_chars_to_study % 2 == 0) chars_row2 = chars_row1; else chars_row2 = chars_row1 - 1;
                        int draw_at_x;
                        draw_at_x = Settings.SCREEN_WIDTH / 2 - (Settings.STUDY_CHAR_WIDTH / 2 * (chars_row1 - 1));
                        for (int i = 0; i < chars_row1; i++) {
                            drawPChar(chars_to_study[i], draw_at_x, Settings.STUDY_ROW1_Y);
                            draw_at_x += Settings.STUDY_CHAR_WIDTH;
                        }
                        draw_at_x = Settings.SCREEN_WIDTH / 2 - (Settings.STUDY_CHAR_WIDTH / 2 * (chars_row2 - 1));
                        for (int i = chars_row1; i < chars_row1 + chars_row2; i++) {
                            drawPChar(chars_to_study[i], draw_at_x, Settings.STUDY_ROW2_Y);
                            draw_at_x += Settings.STUDY_CHAR_WIDTH;
                        }
                    }
                }
                permanent_swap();
                retry_panel.setVisible(true);
                System.out.println("Game end. Average frame duration: " + frame_duration_avg / frames + "ms");
                return;
            }
            if (KeyStates.key_typed != 0) {
                if (KeyStates.key_typed == '\n') {
                    if (cur_string.length() != 0) {
                        boolean correct = false;
                        for (int i = 0; i < fish.length; i++) {
                            if (fish[i].active && !fish[i].fed && fish[i].pinyin.equals(cur_string) && fish[i].tone == cur_tone) {
                                unfed_fish--;
                                fish[i].fed = true;
                                fish[i].draw_food = true;
                                fish[i].food_x = fish[i].current_x + Settings.FOOD_SPAWN_OFFSET_X;
                                fish[i].food_y = fish[i].start_food_y = fish[i].current_y + Settings.FOOD_SPAWN_OFFSET_Y;
                                if (fish[i].food_y < Settings.SCREEN_HEIGHT) {
                                    fish[i].food_y += (int) (Math.random() * (Settings.SCREEN_HEIGHT - fish[i].food_y));
                                }
                                fish[i].start_y = fish[i].current_y;
                                fish[i].start_frame = fish[i].current_frame;
                                fish[i].time = 0;
                                fish[i].speed = Settings.FISH_EAT_SPEED;
                                String pronounce = fish[i].pinyin + fish[i].tone;
                                swimming.remove(pronounce);
                                correctc[fish[i].char_num]++;
                                correct = true;
                                break;
                            }
                        }
                        if (!correct) {
                            miss++;
                        }
                        cur_string = "";
                        cur_tone = 5;
                        cur_letter = stree.root;
                    }
                } else if (KeyStates.key_typed == '\b') {
                    if (cur_string.length() != 0) {
                        if (cur_tone != 5) cur_tone = 5; else {
                            cur_string = cur_string.substring(0, cur_string.length() - 1);
                            cur_letter = cur_letter.parent;
                        }
                    }
                } else if (KeyStates.key_typed >= '1' && KeyStates.key_typed <= '5') {
                    if (cur_string.length() != 0) {
                        cur_tone = KeyStates.key_typed - '1' + 1;
                    }
                } else {
                    if (cur_letter.next[KeyStates.key_typed - 'a'] != null) {
                        cur_string += KeyStates.key_typed;
                        cur_tone = 5;
                        cur_letter = cur_letter.next[KeyStates.key_typed - 'a'];
                    }
                }
                KeyStates.key_typed = 0;
            }
            drawPinyinString(pinyin, tones, p_offset, 3, Settings.SCREEN_WIDTH / 2, Settings.SCREEN_HEIGHT - Settings.PINYIN_OFFSET_Y, cur_string, cur_tone);
            swap_buffers();
            frame_duration = System.currentTimeMillis() - frame_start;
            frame_duration_avg += frame_duration;
            frames++;
            if (frame_duration < 30 && !Settings.MOVIE_MODE) {
                try {
                    sleep(30 - frame_duration);
                } catch (Exception e) {
                }
            }
            frame_duration = System.currentTimeMillis() - frame_start;
        }
    }

    public static URL fromJar(String filename) {
        URL u = WeiLiYu.class.getResource(filename);
        return u;
    }

    public static boolean addCharWithPinyin(String name, String character) {
        char t_char = name.charAt(name.length() - 1);
        int t;
        String py;
        if (Character.isLetter(t_char)) {
            t = 5;
            py = name;
        } else {
            t = Integer.parseInt(name.substring(name.length() - 1, name.length()));
            py = name.substring(0, name.length() - 1);
        }
        if (t < 1 || t > 5) {
            return false;
        }
        if (!stree.contains(py)) {
            return false;
        }
        Integer J = (Integer) char_to_location.get(character);
        if (J != null) {
            int j = J.intValue();
            if (j != -1) {
                if (!chars_pinyin[j].equals(py) || chars_tones[j] != t) {
                    if (num_chars >= 1) {
                        System.err.println("Character conflict with " + character + " (" + chars_pinyin[j] + chars_tones[j] + " / " + py + t + ")");
                        num_chars--;
                        chars[j] = chars[num_chars];
                        chars_pinyin[j] = chars_pinyin[num_chars];
                        chars_tones[j] = chars_tones[num_chars];
                        chars_unicode[j] = chars_unicode[num_chars];
                    }
                    char_to_location.put(character, new Integer(-1));
                }
            }
            return true;
        }
        int i;
        if (num_chars == chars.length) {
            i = (int) (Math.random() * num_chars);
        } else {
            i = num_chars;
            num_chars++;
        }
        BufferedImage img = fr.getBufferedImage(character);
        chars[i] = img;
        chars_pinyin[i] = py;
        chars_tones[i] = t;
        chars_unicode[i] = character;
        char_to_location.put(character, new Integer(i));
        return true;
    }

    public static void addChar(String name, Image img) {
        int t = Integer.parseInt(name.substring(name.length() - 10, name.length() - 9));
        String py = name.substring(0, name.length() - 10);
        int i;
        if (num_chars == chars.length) {
            i = (int) (Math.random() * num_chars);
        } else {
            i = num_chars;
            num_chars++;
        }
        chars[i] = img;
        chars_pinyin[i] = py;
        chars_tones[i] = t;
    }

    public static boolean try_to_load_txt(String filename) {
        return load_chars_from_txt(filename, "UTF8") || load_chars_from_txt(filename, "UTF-16") || load_chars_from_txt(filename, "UTF-16BE") || load_chars_from_txt(filename, "UTF-16LE") || load_chars_from_txt(filename, "Big5") || load_chars_from_txt(filename, "GB2312") || load_chars_from_txt(filename, "GBK") || load_chars_from_txt(filename, "Big5_HKSCS") || load_chars_from_txt(filename, "EUC_CN") || load_chars_from_txt(filename, "EUC_TW") || load_chars_from_txt(filename, "ISO2022CN") || load_chars_from_txt(filename, "Cp935") || load_chars_from_txt(filename, "Cp937") || load_chars_from_txt(filename, "Cp948") || load_chars_from_txt(filename, "Cp950") || load_chars_from_txt(filename, "Cp964") || load_chars_from_txt(filename, "MS936") || load_chars_from_txt(filename, "MS950");
    }

    public static boolean load_chars_from_txt(String filename, String encoding) {
        int old_num_chars = num_chars;
        boolean added_chars = false;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), encoding));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() >= 1 && line.charAt(0) == 65279) {
                    line = line.substring(1);
                }
                StringTokenizer st = new StringTokenizer(line);
                int num_tok = st.countTokens();
                if (num_tok == 2) {
                    added_chars = true;
                    String pinyin = st.nextToken().toLowerCase();
                    String character = st.nextToken();
                    if (character.length() != 1) {
                        return false;
                    }
                    int zhchar = character.charAt(0);
                    if (zhchar < 0x2E80) {
                        return false;
                    }
                    if (zhchar > 0x2FDF && zhchar < 0x3200) {
                        return false;
                    }
                    if (zhchar > 0x32FF && zhchar < 0x3400) {
                        return false;
                    }
                    if (zhchar > 0x4DBF && zhchar < 0x4E00) {
                        return false;
                    }
                    if (zhchar > 0x9FFF && zhchar < 0xF900) {
                        return false;
                    }
                    if (zhchar > 0xFAFF && zhchar < 0x20000) {
                        return false;
                    }
                    if (zhchar > 0x2A6DF && zhchar < 0x2F800) {
                        return false;
                    }
                    if (zhchar > 0x2FA1F) {
                        return false;
                    }
                    boolean res = addCharWithPinyin(pinyin, character);
                    if (res == false) {
                        return false;
                    }
                } else if (num_tok != 0) {
                    return false;
                }
            }
            if (old_num_chars == num_chars && !added_chars) {
                return false;
            }
            System.err.println("Succeeded in loading \"" + filename + "\", encoding detected as " + encoding + ".");
            return true;
        } catch (Exception e) {
            num_chars = old_num_chars;
            return false;
        }
    }

    public static void updateProg() {
        prog.setValue(prog.getValue() + 1);
    }

    public static void doneProg() {
        prog.setValue(prog.getMaximum());
        prog.setString(WeiLiYu.ws.done());
    }

    public static void init_game() {
        correctc = new int[1024];
        incorrectc = new int[1024];
        chars = new Image[1024];
        chars_pinyin = new String[1024];
        chars_tones = new int[1024];
        chars_unicode = new String[1024];
        prog.setString(WeiLiYu.ws.loading_graphics());
        prog.setString(WeiLiYu.ws.loading_screens());
        background = new ImageIcon(fromJar("data/background.png")).getImage();
        updateProg();
        congrats = new ImageIcon(fromJar(WeiLiYu.ws.congrats_image())).getImage();
        updateProg();
        hungry_cloud = new ImageIcon(fromJar("data/hcloud.png")).getImage();
        updateProg();
        hungry = new ImageIcon(fromJar(WeiLiYu.ws.hungry_image())).getImage();
        updateProg();
        prog.setString(WeiLiYu.ws.loading_sprites());
        fish_food = new Image[64];
        for (int i = 0; i < 64; i++) {
            fish_food[i] = new ImageIcon(fromJar("data/food/food" + i + ".png")).getImage();
            updateProg();
        }
        prog.setString(WeiLiYu.ws.loading_pinyin_fonts());
        tones = new Image[4];
        s_tones = new Image[4];
        for (int i = 1; i <= 4; i++) {
            tones[i - 1] = new ImageIcon(fromJar("data/pinyin/" + i + ".png")).getImage();
            updateProg();
            s_tones[i - 1] = new ImageIcon(fromJar("data/pinyin_small/" + i + ".png")).getImage();
            updateProg();
        }
        pinyin = new Image[27];
        s_pinyin = new Image[27];
        for (char i = 'a'; i <= 'z'; i++) {
            pinyin[i - 'a'] = new ImageIcon(fromJar("data/pinyin/" + i + ".png")).getImage();
            updateProg();
            s_pinyin[i - 'a'] = new ImageIcon(fromJar("data/pinyin_small/" + i + ".png")).getImage();
            updateProg();
        }
        pinyin[26] = new ImageIcon(fromJar("data/pinyin/i_nodot.png")).getImage();
        updateProg();
        s_pinyin[26] = new ImageIcon(fromJar("data/pinyin_small/i_nodot.png")).getImage();
        updateProg();
        fish_anim = new Image[7][];
        for (int i = 0; i < 7; i++) {
            prog.setString(WeiLiYu.ws.loading_fish() + (i + 1) + "/7...");
            fish_anim[i] = new Image[25];
            for (int f = 0; f < 25; f++) {
                fish_anim[i][f] = new ImageIcon(fromJar("data/fish/" + i + "/" + f + ".png")).getImage();
                updateProg();
            }
        }
        prog.setString(WeiLiYu.ws.loading_tables());
        offscreen_buffer = new BufferedImage(Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);
        graphics = offscreen_buffer.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        theta_table = new double[25];
        theta_table[0] = 0.188;
        theta_table[1] = 0.159;
        theta_table[2] = 0.122;
        theta_table[3] = 0.085;
        theta_table[4] = 0.047;
        theta_table[5] = 0.007;
        theta_table[6] = -0.031;
        theta_table[7] = -0.070;
        theta_table[8] = -0.105;
        theta_table[9] = -0.139;
        theta_table[10] = -0.168;
        theta_table[11] = -0.188;
        theta_table[12] = -0.192;
        theta_table[13] = -0.171;
        theta_table[14] = -0.142;
        theta_table[15] = -0.109;
        theta_table[16] = -0.071;
        theta_table[17] = -0.032;
        theta_table[18] = 0.005;
        theta_table[19] = 0.045;
        theta_table[20] = 0.084;
        theta_table[21] = 0.120;
        theta_table[22] = 0.152;
        theta_table[23] = 0.180;
        theta_table[24] = 0.197;
        p_offset = new int[27];
        p_offset['g' - 'a'] = 11;
        p_offset['i' - 'a'] = -1;
        p_offset['j' - 'a'] = 11;
        p_offset['p' - 'a'] = 12;
        p_offset['q' - 'a'] = 12;
        p_offset['y' - 'a'] = 11;
        p_offset['z' - 'a'] = -1;
        p_offset[26] = -1;
        s_p_offset = new int[27];
        s_p_offset['g' - 'a'] = 3;
        s_p_offset['j' - 'a'] = 3;
        s_p_offset['p' - 'a'] = 3;
        s_p_offset['q' - 'a'] = 3;
        s_p_offset['y' - 'a'] = 3;
        prog.setString(WeiLiYu.ws.loading_chinese());
        fr = new FontRenderer(WeiLiYu.class.getResourceAsStream("data/ukai.ttf"), Settings.CHAR_SIZE, 0);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(WeiLiYu.class.getResourceAsStream("data/syllables")));
            String line;
            while ((line = br.readLine()) != null) {
                stree.addSyllable(line);
            }
        } catch (IOException e) {
        }
        fish = new Fish[8];
        for (int i = 0; i < fish.length; i++) {
            fish[i] = new Fish();
        }
        doneProg();
    }

    public static void show_settings() {
        screen.setIcon(new ImageIcon(fromJar("data/splash.png")));
        sf.show();
    }

    public static void setChinese(String chinesesample) {
        Font[] allfonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        Font zhfont = null;
        System.out.println("Searching for Chinese fonts...");
        for (int j = 0; j < allfonts.length; j++) {
            if (allfonts[j].canDisplayUpTo(chinesesample) == -1) {
                zhfont = new Font(allfonts[j].getFontName(), Font.PLAIN, 12);
            }
        }
        System.out.println("Setting default font to Chinese...");
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) UIManager.put(key, zhfont);
        }
    }

    public static void main(String[] args) {
        String default_lang = Locale.getDefault().getLanguage();
        if (default_lang.equals(Locale.GERMAN.getLanguage())) ws = new DeutschWS(); else if (default_lang.equals(Locale.SIMPLIFIED_CHINESE.getLanguage())) {
            ws = new ZhongWenJianTiZiWS();
            setChinese("喂鲤鱼");
        } else if (default_lang.equals(Locale.TRADITIONAL_CHINESE.getLanguage())) {
            ws = new ZhongWenFanTiZiWS();
            setChinese("喂鯉魚");
        } else ws = new EnglishWS();
        boolean show_settings_flag = true;
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Could not set look-and-feel... Oh well.");
        }
        gamewind = new JFrame(WeiLiYu.ws.window_title());
        try {
            gamewind.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } catch (Exception e) {
            gamewind.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        gamewind.setResizable(false);
        screen = new JLabel();
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT));
        gamewind.getContentPane().add(layeredPane);
        layeredPane.add(screen, new Integer(0));
        screen.setBounds(0, 0, Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT);
        screen.setIcon(new ImageIcon(fromJar("data/splash.png")));
        prog = new JProgressBar(0, 408);
        prog.setValue(0);
        prog.setStringPainted(true);
        layeredPane.add(prog, new Integer(1));
        prog.setBounds(3, 3, 400, 16);
        ActionListener al = new WeiLiYu();
        accuracy = new JLabel();
        JButton ng = new JButton(WeiLiYu.ws.new_game());
        ng.addActionListener(al);
        ng.setActionCommand("newgame");
        JButton retry = new JButton(WeiLiYu.ws.try_again());
        retry.addActionListener(al);
        retry.setActionCommand("retry");
        save_list = new JButton(WeiLiYu.ws.save_missed());
        save_list.addActionListener(al);
        save_list.setActionCommand("savemissed");
        JButton exit = new JButton(WeiLiYu.ws.exit());
        exit.addActionListener(al);
        exit.setActionCommand("exit");
        JPanel buttonpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonpanel.add(ng);
        buttonpanel.add(retry);
        buttonpanel.add(save_list);
        buttonpanel.add(exit);
        JPanel retry_container_panel = new JPanel(new BorderLayout());
        retry_container_panel.setBounds(0, 0, Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT);
        retry_container_panel.setOpaque(false);
        retry_panel = new JPanel(new BorderLayout());
        retry_panel.add(accuracy, BorderLayout.WEST);
        retry_panel.add(buttonpanel, BorderLayout.EAST);
        retry_panel.setVisible(false);
        retry_container_panel.add(retry_panel, BorderLayout.SOUTH);
        layeredPane.add(retry_container_panel, new Integer(1));
        gamewind.pack();
        main = screen.getGraphics();
        gamewind.addKeyListener(new KeyStates());
        screen.addKeyListener(new KeyStates());
        gamewind.setLocationRelativeTo(null);
        gamewind.setFocusable(true);
        gamewind.setVisible(true);
        System.out.println("Splash shown... starting load...");
        long splash_shown = System.currentTimeMillis();
        init_game();
        if (args.length >= 1) {
            boolean allow_arg = true;
            for (int i = 0; i < args.length; i++) {
                try {
                    if (allow_arg && args[i].length() > 8 && "--speed=".equals(args[i].substring(0, 8))) {
                        String arg = args[i].substring(8);
                        int n = Integer.parseInt(arg);
                        if (n < 30) throw new Exception();
                        if (n > 90) throw new Exception();
                        Settings.DEFAULT_SPEED = n;
                    } else if (allow_arg && args[i].length() > 11 && "--duration=".equals(args[i].substring(0, 11))) {
                        String arg = args[i].substring(11);
                        double n = Double.parseDouble(arg);
                        if (n < 0.08) throw new Exception();
                        if (n > 30) throw new Exception();
                        Settings.GAME_DURATION = n;
                    } else if (allow_arg && args[i].length() > 6 && "--dir=".equals(args[i].substring(0, 6))) {
                        String arg = args[i].substring(6);
                        default_dir = new File(arg);
                        if (default_dir == null) {
                            throw new Exception();
                        }
                        if (!default_dir.exists()) {
                            System.err.println("ARG ERROR: Directory does not exist.");
                            throw new Exception();
                        }
                        if (!default_dir.isDirectory()) {
                            System.err.println("ARG ERROR: Not a directory.");
                            throw new Exception();
                        }
                    } else if (allow_arg && args[i].equals("--record")) {
                        Settings.MOVIE_MODE = true;
                    } else if (allow_arg && args[i].equals("--")) {
                        allow_arg = false;
                    } else {
                        String arg = args[i];
                        if (!try_to_load_txt(arg)) {
                            System.err.println("Not a valid .wly file or does not exist: \"" + arg + "\" (see character library creation documentation)");
                            throw new Exception();
                        }
                        show_settings_flag = false;
                    }
                } catch (Exception e) {
                    System.err.println("Problem with argument: " + args[i]);
                    System.err.println("Further errors will not be reported. See documentation.");
                    System.exit(1);
                }
            }
        }
        if (!show_settings_flag && num_chars == 0) {
            System.err.println("No characters loaded! This is probably because of character conflicts.");
            System.err.println("Problem with argument given list of character libraries.");
            System.err.println("Further errors will not be reported. See documentation.");
            System.exit(1);
        }
        layeredPane.remove(layeredPane.getIndexOf(prog));
        layeredPane.repaint(0, 3, 3, 400, 16);
        long duration = System.currentTimeMillis() - splash_shown;
        System.out.println("Load finished in " + duration / 1000 + " secs.");
        sf = new SettingFrame(gamewind, new WeiLiYu(), default_dir, show_settings_flag);
    }

    public static void doAction(String a) {
        if ("start".equals(a)) {
            boolean ok = true;
            File[] f;
            try {
                f = sf.chooser.getSelectedFiles();
            } catch (NullPointerException e) {
                f = new File[0];
            }
            for (int i = 0; i < f.length; i++) {
                if (!f[i].exists()) {
                    JOptionPane.showMessageDialog(sf, WeiLiYu.ws.file_dne(f[i].getName()), WeiLiYu.ws.error_dialog_title(), JOptionPane.ERROR_MESSAGE);
                    ok = false;
                }
            }
            if (f.length == 0 && num_chars == 0) {
                JOptionPane.showMessageDialog(sf, WeiLiYu.ws.must_select_wly(), WeiLiYu.ws.error_dialog_title(), JOptionPane.ERROR_MESSAGE);
                ok = false;
            } else if (sf.time_model.getNumber().doubleValue() < 0.08) {
                JOptionPane.showMessageDialog(sf, WeiLiYu.ws.duration_too_small(), WeiLiYu.ws.error_dialog_title(), JOptionPane.ERROR_MESSAGE);
                ok = false;
            } else if (ok) {
                sf.hide();
                num_chars = 0;
                char_to_location.clear();
                for (int i = 0; i < f.length; i++) {
                    String filename = f[i].getAbsolutePath();
                    if (!try_to_load_txt(filename)) {
                        sf.show();
                        JOptionPane.showMessageDialog(sf, WeiLiYu.ws.could_not_load_wly(filename), WeiLiYu.ws.error_dialog_title(), JOptionPane.ERROR_MESSAGE);
                        ok = false;
                        break;
                    }
                }
                if (ok && num_chars == 0) {
                    sf.show();
                    JOptionPane.showMessageDialog(sf, WeiLiYu.ws.no_chars_loaded(), WeiLiYu.ws.error_dialog_title(), JOptionPane.ERROR_MESSAGE);
                    ok = false;
                }
                if (ok) {
                    Settings.GAME_DURATION = sf.time_model.getNumber().doubleValue();
                    Settings.FISH_MIN_SPEED = (int) sf.speed.getValue();
                    Settings.FISH_MAX_SPEED = (int) (2 * sf.speed.getValue()) + 6;
                    Settings.FISH_ADD_TIME = 400 * Settings.SCREEN_HEIGHT / Settings.FISH_MIN_SPEED;
                    new WeiLiYu().start();
                }
            }
        } else if ("exit".equals(a)) {
            System.exit(0);
        } else if ("retry".equals(a)) {
            for (int i = 0; i < 1024; i++) {
                incorrectc[i] = 0;
                correctc[i] = 0;
            }
            retry_panel.setVisible(false);
            new WeiLiYu().start();
        } else if ("newgame".equals(a)) {
            for (int i = 0; i < 1024; i++) {
                incorrectc[i] = 0;
                correctc[i] = 0;
            }
            retry_panel.setVisible(false);
            show_settings();
        } else if ("savemissed".equals(a)) {
            JFileChooser save = new JFileChooser();
            save.setDialogTitle(WeiLiYu.ws.save_list_title());
            for (; ; ) {
                int retval = save.showSaveDialog(gamewind);
                if (retval == JFileChooser.APPROVE_OPTION) {
                    File outf = save.getSelectedFile();
                    boolean ok_to_write = true;
                    if (outf.exists()) {
                        int choice = JOptionPane.showConfirmDialog(gamewind, WeiLiYu.ws.file_already_exist(outf.toString()), WeiLiYu.ws.error_dialog_title(), JOptionPane.YES_NO_CANCEL_OPTION);
                        if (choice == JOptionPane.CANCEL_OPTION) break;
                        if (choice != JOptionPane.YES_OPTION) ok_to_write = false;
                    }
                    if (ok_to_write) {
                        try {
                            PrintWriter ps = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outf), "UTF8"));
                            for (int i = 0; i < num_chars_to_study; i++) {
                                int c = chars_to_study[i];
                                ps.println(chars_pinyin[c] + chars_tones[c] + "\t" + chars_unicode[c]);
                            }
                            ps.close();
                            JOptionPane.showMessageDialog(gamewind, WeiLiYu.ws.saved_list_to(outf.toString()));
                            break;
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(gamewind, WeiLiYu.ws.error_occured_saving(e.getMessage()), WeiLiYu.ws.error_dialog_title(), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else break;
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        doAction(e.getActionCommand());
    }
}
