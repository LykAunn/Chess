package main;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class Sound {
    Clip clip;
    URL soundURL[] = new URL[10];

    public Sound() {
        soundURL[0] = getClass().getResource("/sound/start.wav");
        soundURL[1] = getClass().getResource("/sound/move.wav");
        soundURL[2] = getClass().getResource("/sound/move2.wav");
        soundURL[3] = getClass().getResource("/sound/capture1.wav");
        soundURL[4] = getClass().getResource("/sound/capture2.wav");
        soundURL[5] = getClass().getResource("/sound/castling1.wav");
        soundURL[6] = getClass().getResource("/sound/castling2.wav");
        soundURL[7] = getClass().getResource("/sound/checked1.wav");
        soundURL[8] = getClass().getResource("/sound/checked2.wav");
        soundURL[9] = getClass().getResource("/sound/checkmate.wav");
    }

    public void setFile(int i) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);

        } catch(Exception e) {

        }
    }

    public void play() {

        clip.start();
    }

    public void loop() {

        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {

        clip.stop();
    }
}
