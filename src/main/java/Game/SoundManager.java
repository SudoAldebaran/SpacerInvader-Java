package Game;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static Map<String, Clip> soundClips = new HashMap<>();
    private static boolean initialized = false;

    public static void loadSounds() {
        if (initialized) return;
        String currentDir = System.getProperty("user.dir");
        String soundPath = currentDir + File.separator + "sounds" + File.separator;

        // Précharger tous les sons au démarrage
        preloadSound("shoot", soundPath + "shoot.wav");
        preloadSound("explosion", soundPath + "explosion.wav");
        preloadSound("gameover", soundPath + "gameover.wav");
        preloadSound("victory", soundPath + "victory.wav");
        preloadSound("hit", soundPath + "hit.wav");

        initialized = true;
    }

    private static void preloadSound(String name, String path) {
        try {
            File soundFile = new File(path);
            if (!soundFile.exists()) {
                System.err.println("Son non trouvé: " + path);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            soundClips.put(name, clip);
            audioIn.close();
        } catch (Exception e) {
            System.err.println("Erreur chargement son " + name + ": " + e.getMessage());
        }
    }

    public static void playSound(String name) {
        try {
            Clip clip = soundClips.get(name);
            if (clip != null) {
                // Si le son joue déjà, on le stoppe
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.setFramePosition(0);
                // Jouer le son dans un thread séparé pour éviter les blocages
                new Thread(() -> clip.start()).start();
            }
        } catch (Exception e) {
            // Ignorer les erreurs de son pour ne pas perturber le jeu
        }
    }

    public static void cleanup() {
        for (Clip clip : soundClips.values()) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.close();
        }
        soundClips.clear();
        initialized = false;
    }
}