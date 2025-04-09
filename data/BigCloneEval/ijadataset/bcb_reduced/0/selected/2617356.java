package com.googlecode.boringengine;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SoundSystem {

    private static SourceDataLine musicLine;

    private static HashMap<String, Clip> sfx;

    private static boolean musicWorks = true, isPlaying, isPaused;

    private static GameMusicEmu gme;

    private static Pointer music, tempMusic;

    private static int track;

    private static GameLoader loader;

    public static void init() {
        {
            String prop = System.getProperty("jna.library.path");
            if (prop == null) prop = "native"; else prop = "native" + File.pathSeparator + prop;
            System.setProperty("jna.library.path", prop);
        }
        try {
            gme = (GameMusicEmu) Native.loadLibrary("gme", GameMusicEmu.class);
        } catch (UnsatisfiedLinkError e) {
            Log.warn("Couldn't load libgme: %s", e.getMessage());
            musicWorks = false;
        }
        sfx = new HashMap<String, Clip>();
        AudioFormat af = new AudioFormat(44100, 16, 2, true, false);
        try {
            musicLine = AudioSystem.getSourceDataLine(af);
            musicLine.open(af);
            musicLine.start();
        } catch (LineUnavailableException ex) {
            Log.warn("Couldn't open an audio output stream: %s", ex.getMessage());
            musicWorks = false;
        }
        if (!musicWorks) {
            if (gme != null) gme = null;
            if (musicLine != null) musicLine = null;
        }
    }

    public static void playSound(String fileName) {
        Clip sound = null;
        if (sfx.containsKey(fileName)) {
            sound = sfx.get(fileName);
        } else {
            try {
                sound = AudioSystem.getClip();
                sound.open(AudioSystem.getAudioInputStream(loader.getFile(fileName)));
            } catch (Exception ex) {
                Log.warn("Error loading file: %s", ex);
            }
            sfx.put(fileName, sound);
        }
        if (!GameSettings.soundEnabled || sound == null) return;
        sound.start();
    }

    public static void gameLoaded(GameLoader l) {
        loader = l;
        sfx.clear();
        if (music == tempMusic) tempMusic = null;
        if (music != null) {
            gme.gme_delete(music);
            music = null;
        }
        if (tempMusic != null) {
            gme.gme_delete(tempMusic);
            tempMusic = null;
        }
        track = 0;
    }

    private static Pointer openFile(String fileName) {
        Log.debug("Loading %s...", fileName);
        if (music != null) {
            gme.gme_delete(music);
            music = null;
        }
        InputStream in = loader.getFile(fileName);
        if (in == null) {
            Log.error("Couldn't find %s", fileName);
            return null;
        }
        byte[] file;
        try {
            file = new byte[in.available()];
            int offset = 0;
            int readLen = 0;
            while (offset < file.length) {
                readLen = in.read(file, offset, file.length - offset);
                if (readLen == -1) {
                    Log.error("Reached end of file while expecting more bytes");
                    break;
                }
                offset += readLen;
            }
        } catch (IOException ex) {
            Log.error("Couldn't open %s for reading: %s", fileName, ex.getMessage());
            return null;
        }
        ByteBuffer fileBuf = ByteBuffer.wrap(file);
        PointerByReference musicPointer = new PointerByReference();
        String err = gme.gme_open_data(fileBuf, new NativeLong(file.length), musicPointer, 44100);
        if (err != null) {
            Log.error("libgme error: %s", err);
            return null;
        }
        return musicPointer.getValue();
    }

    public static void changeTrack(int tack) {
        if (music == null) return;
        track = tack;
        gme.gme_start_track(music, track - 1);
        musicLine.flush();
    }

    public static void play() {
        if (isPlaying) return;
        isPlaying = true;
        isPaused = false;
    }

    public static void pause() {
        if (isPaused || !isPlaying) return;
        isPlaying = false;
        isPaused = true;
    }

    public static void stop() {
        if (!isPlaying && !isPaused) return;
        isPlaying = false;
        isPaused = false;
        musicLine.flush();
    }

    public static void setSpeed(double speed) {
        if (music == null) return;
        gme.gme_set_tempo(music, speed);
    }

    public static void update() {
        if (!GameSettings.musicEnabled || !isPlaying) return;
        Pointer mu = music;
        if (tempMusic != null) mu = tempMusic;
        if (mu == null) return;
        int avail = musicLine.available();
        ByteBuffer buf = ByteBuffer.allocate(avail);
        gme.gme_play(music, avail / 2, buf);
        musicLine.write(buf.array(), 0, avail);
    }

    public static void openMusic(String fileName) {
        music = openFile(fileName);
    }

    public static void playTempMusic(int trak) {
        if (music == null) return;
        tempMusic = music;
        gme.gme_start_track(tempMusic, trak - 1);
        musicLine.flush();
    }

    public static void playTempMusic(String fileName, int trak) {
        tempMusic = openFile(fileName);
        if (tempMusic == null) return;
        gme.gme_start_track(tempMusic, trak - 1);
        musicLine.flush();
    }

    public static void endTempMusic() {
        if (tempMusic == null) return;
        if (tempMusic != music) {
            gme.gme_delete(tempMusic);
        }
        tempMusic = null;
        if (music == null) return;
        gme.gme_start_track(music, track - 1);
        musicLine.flush();
    }

    private interface GameMusicEmu extends Library {

        String gme_open_data(Buffer data, NativeLong size, PointerByReference out, int sample_rate);

        String gme_start_track(Pointer me, int index);

        String gme_play(Pointer me, int count, Buffer out);

        void gme_delete(Pointer me);

        void gme_set_tempo(Pointer me, double tempo);

        void gme_ignore_silence(Pointer me, boolean ignore);
    }
}
