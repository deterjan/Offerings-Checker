package sidemodules;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 *  Sound player that disables itself for given milliseconds after playing
 *  Make sure delay is longer than the length of the sound clip
 */

public class SoundPlayer extends Thread
{
    String fileName;
    int waitTime;
    boolean enabled;

    public SoundPlayer( String filename, int waitTime)
    {
        super();

        this.fileName = filename;
        this.waitTime = waitTime;
        enabled = true;
    }

    public void run() {
        if ( enabled)
        {
            try{
                // locating files is HARD
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(( getClass().getClassLoader().getResource( fileName)));
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            enabled = false;
            waitAndEnable();
        }
    }

    private void waitAndEnable()
    {
        new java.util.Timer().schedule(
                new java.util.TimerTask()
                {
                    @Override
                    public void run()
                    {
                        enabled = true;
                    }
                },
                waitTime
        );
    }
}
