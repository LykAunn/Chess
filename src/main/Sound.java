package main;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class Sound {
    Clip[] clips = new Clip[10];
    URL soundURL[] = new URL[10];

    public Sound() {
        soundURL[0] = getClass().getResource("/sound/start.wav");
        soundURL[1] = getClass().getResource("/sound/move.wav");
        soundURL[2] = getClass().getResource("/sound/move2.wav");
        soundURL[3] = getClass().getResource("/sound/capture1.wav");
        soundURL[4] = getClass().getResource("/sound/capture2.wav");
        soundURL[5] = getClass().getResource("/sound/castling.wav");
        soundURL[6] = getClass().getResource("/sound/castling2.wav");
        soundURL[7] = getClass().getResource("/sound/checked.wav");
        soundURL[8] = getClass().getResource("/sound/checked2.wav");
        soundURL[9] = getClass().getResource("/sound/checkmate.wav");

        for (int i = 0; i < 10; i++) {
            setFile(i);
        }
    }

    public void setFile(int i) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clips[i] = AudioSystem.getClip();
            clips[i].open(ais);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play(int i) {
        if (clips[i] != null) {
            clips[i].setFramePosition(0); // rewind
            clips[i].start();
        }
    }

    public void loop(int i) {

        clips[i].loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop(int i) {

        clips[i].stop();
    }
}
