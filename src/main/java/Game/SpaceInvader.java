package Game;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.awt.TextRenderer;

import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SpaceInvader implements GLEventListener, KeyListener {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    // Ajout du TextRenderer pour afficher le texte
    private TextRenderer textRenderer;
    private TextRenderer bigTextRenderer;

    // Position du vaisseau joueur
    private float playerX = 0.0f;
    private float playerY = -0.8f;
    private int playerLives = 3;
    private boolean gameOver = false;
    private int score = 0;
    private int finalScore = 0;

    private boolean victory = false;

    // Liste des projectiles
    private List<Projectile> playerProjectiles = new ArrayList<>();
    private List<Projectile> alienProjectiles = new ArrayList<>();
    private List<Alien> aliens = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();

    // Variables pour le mouvement des aliens
    private float alienMoveSpeed = 0.0007f;
    private boolean aliensMovingRight = true;
    private float alienDropDistance = 0.1f;
    private long lastAlienUpdate = System.currentTimeMillis();
    private long lastAlienShot = System.currentTimeMillis();
    private static final long ALIEN_UPDATE_DELAY = 16;
    private static final long ALIEN_SHOT_DELAY = 3000; // Délai entre les tirs aliens (1 seconde)
    private Random random = new Random();

    public static void main(String[] args) {
        GLProfile glProfile = GLProfile.getDefault();
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);

        GLCanvas canvas = new GLCanvas(glCapabilities);
        SpaceInvader spaceInvader = new SpaceInvader();
        canvas.addGLEventListener(spaceInvader);
        canvas.addKeyListener(spaceInvader);
        canvas.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        JFrame frame = new JFrame("Space Invader");
        frame.getContentPane().add(canvas);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        Animator animator = new Animator(canvas);
        animator.start();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        // Créer les TextRenderer avec anti-aliasing
        textRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 16), true, true);
        bigTextRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 32), true, true);
        SoundManager.loadSounds();
        initGame();
    }

    private void initGame() {
        // Réinitialiser les variables du jeu
        playerX = 0.0f;
        playerY = -0.8f;
        playerLives = 3;
        gameOver = false;
        victory = false;
        score = 0;
        alienMoveSpeed = 0.0009f;
        aliensMovingRight = true;

        // Vider les listes
        aliens.clear();
        playerProjectiles.clear();
        alienProjectiles.clear();
        explosions.clear();

        // Recréer les aliens
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 8; col++) {
                float x = -0.8f + col * 0.2f;
                float y = 0.8f - row * 0.15f;
                aliens.add(new Alien(x, y));
            }
        }
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        if (gameOver) {
            displayGameOver(drawable);
            return;
        }

        // Afficher le score pendant le jeu
        textRenderer.beginRendering(WINDOW_WIDTH, WINDOW_HEIGHT);
        textRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        textRenderer.draw("Score: " + score, 10, WINDOW_HEIGHT - 30);
        textRenderer.endRendering();

        updateAliens();
        updateAlienShots();

        drawPlayer(gl);
        drawLives(gl);

        // Dessiner les projectiles du joueur
        Iterator<Projectile> playerProjectileIterator = playerProjectiles.iterator();
        while (playerProjectileIterator.hasNext()) {
            Projectile projectile = playerProjectileIterator.next();
            projectile.update();
            if (projectile.getY() > 1.0f) {
                playerProjectileIterator.remove();
            } else {
                projectile.draw(gl);
            }
        }

        // Dessiner les projectiles aliens
        Iterator<Projectile> alienProjectileIterator = alienProjectiles.iterator();
        while (alienProjectileIterator.hasNext()) {
            Projectile projectile = alienProjectileIterator.next();
            projectile.update();
            if (projectile.getY() < -1.0f) {
                alienProjectileIterator.remove();
            } else {
                projectile.draw(gl);
            }
        }

        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            if (!explosion.update()) {
                explosionIterator.remove();
            } else {
                explosion.draw(gl);
            }
        }

        // Dessiner les aliens
        for (Alien alien : aliens) {
            alien.draw(gl);
        }

        checkCollisions();
        checkGameOver();
    }

    private void displayGameOver(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        // Fond noir
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        bigTextRenderer.beginRendering(WINDOW_WIDTH, WINDOW_HEIGHT);

        if (victory) {
            // Afficher "VICTOIRE!" en vert
            bigTextRenderer.setColor(0.2f, 1.0f, 0.2f, 1.0f);
            String victoryText = "VICTOIRE!";
            Rectangle2D victoryBounds = bigTextRenderer.getBounds(victoryText);
            int victoryWidth = (int) victoryBounds.getWidth();
            bigTextRenderer.draw(victoryText,
                    (WINDOW_WIDTH - victoryWidth) / 2,
                    WINDOW_HEIGHT / 2 + 70);
        } else {
            // Afficher "GAME OVER" en rouge
            bigTextRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
            String gameOverText = "GAME OVER";
            Rectangle2D gameOverBounds = bigTextRenderer.getBounds(gameOverText);
            int gameOverWidth = (int) gameOverBounds.getWidth();
            bigTextRenderer.draw(gameOverText,
                    (WINDOW_WIDTH - gameOverWidth) / 2,
                    WINDOW_HEIGHT / 2 + 70);
        }

        // Score final
        String scoreText = "Score final : " + finalScore;
        Rectangle2D scoreBounds = bigTextRenderer.getBounds(scoreText);
        int scoreWidth = (int) scoreBounds.getWidth();
        bigTextRenderer.draw(scoreText,
                (WINDOW_WIDTH - scoreWidth) / 2,
                WINDOW_HEIGHT / 2);
        bigTextRenderer.endRendering();

        // Instructions pour recommencer
        textRenderer.beginRendering(WINDOW_WIDTH, WINDOW_HEIGHT);
        textRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        String restartText = "Appuyez sur ESPACE pour recommencer";
        Rectangle2D restartBounds = textRenderer.getBounds(restartText);
        int restartWidth = (int) restartBounds.getWidth();
        textRenderer.draw(restartText,
                (WINDOW_WIDTH - restartWidth) / 2,
                WINDOW_HEIGHT / 2 - 50);
        textRenderer.endRendering();
    }

    private void drawLives(GL2 gl) {
        // Pour chaque vie, dessiner un cœur
        for (int i = 0; i < playerLives; i++) {
            float x = -0.9f + (i * 0.15f); // Espacement augmenté pour éviter le chevauchement
            float y = -0.9f;
            float size = 0.05f; // Taille du cœur augmentée

            // Dessiner un cœur avec deux cercles et un triangle
            gl.glColor3f(1.0f, 0.2f, 0.2f); // Rouge pour le cœur

            // Cercle gauche du cœur
            gl.glBegin(GL2.GL_TRIANGLE_FAN);
            float centerLeftX = x - size/4;
            float centerLeftY = y + size/4;
            gl.glVertex2f(centerLeftX, centerLeftY); // Centre
            for (int angle = 0; angle <= 180; angle += 20) {
                float radian = (float) Math.toRadians(angle);
                gl.glVertex2f(
                        centerLeftX + (float) Math.cos(radian) * size/4,
                        centerLeftY + (float) Math.sin(radian) * size/4
                );
            }
            gl.glEnd();

            // Cercle droit du cœur
            gl.glBegin(GL2.GL_TRIANGLE_FAN);
            float centerRightX = x + size/4;
            float centerRightY = y + size/4;
            gl.glVertex2f(centerRightX, centerRightY); // Centre
            for (int angle = 0; angle <= 180; angle += 20) {
                float radian = (float) Math.toRadians(angle);
                gl.glVertex2f(
                        centerRightX + (float) Math.cos(radian) * size/4,
                        centerRightY + (float) Math.sin(radian) * size/4
                );
            }
            gl.glEnd();

            // Triangle pour la pointe du cœur
            gl.glBegin(GL2.GL_TRIANGLES);
            gl.glVertex2f(x - size/2, y + size/4); // Point gauche
            gl.glVertex2f(x + size/2, y + size/4); // Point droit
            gl.glVertex2f(x, y - size/2);          // Point du bas
            gl.glEnd();

            // Contour du cœur (optionnel, pour un meilleur rendu)
            gl.glColor3f(0.8f, 0.1f, 0.1f); // Rouge plus foncé pour le contour
            gl.glLineWidth(1.5f);
            gl.glBegin(GL2.GL_LINE_LOOP);
            // Dessiner le contour gauche
            for (int angle = 180; angle >= 0; angle -= 20) {
                float radian = (float) Math.toRadians(angle);
                gl.glVertex2f(
                        centerLeftX + (float) Math.cos(radian) * size/4,
                        centerLeftY + (float) Math.sin(radian) * size/4
                );
            }
            // Dessiner la pointe
            gl.glVertex2f(x, y - size/2);
            // Dessiner le contour droit
            for (int angle = 0; angle <= 180; angle += 20) {
                float radian = (float) Math.toRadians(angle);
                gl.glVertex2f(
                        centerRightX + (float) Math.cos(radian) * size/4,
                        centerRightY + (float) Math.sin(radian) * size/4
                );
            }
            gl.glEnd();
            gl.glLineWidth(1.0f);
        }
    }

    private void updateAlienShots() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAlienShot > ALIEN_SHOT_DELAY && !aliens.isEmpty()) {
            // Sélectionner un alien aléatoire pour tirer
            Alien shooter = aliens.get(random.nextInt(aliens.size()));
            alienProjectiles.add(new Projectile(shooter.getX(), shooter.getY(), -0.0001f)); // Tir vers le bas
            lastAlienShot = currentTime;
        }
    }

    private void updateAliens() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAlienUpdate < ALIEN_UPDATE_DELAY) {
            return;
        }
        lastAlienUpdate = currentTime;

        boolean needToDropAndReverse = false;
        float maxRight = -1.0f;
        float maxLeft = 1.0f;

        // Trouver les positions extrêmes des aliens
        for (Alien alien : aliens) {
            if (alien.getX() > maxRight) maxRight = alien.getX();
            if (alien.getX() < maxLeft) maxLeft = alien.getX();
        }

        // Vérifier si les aliens doivent changer de direction
        if (aliensMovingRight && maxRight + 0.04f >= 0.9f) {
            needToDropAndReverse = true;
        } else if (!aliensMovingRight && maxLeft - 0.04f <= -0.9f) {
            needToDropAndReverse = true;
        }

        // Mettre à jour la position de tous les aliens
        if (needToDropAndReverse) {
            // Descendre et changer de direction
            for (Alien alien : aliens) {
                alien.moveDown(alienDropDistance);
            }
            aliensMovingRight = !aliensMovingRight;
            alienMoveSpeed *= 1.1f; // Augmenter légèrement la vitesse
        } else {
            // Continuer dans la même direction
            float moveAmount = aliensMovingRight ? alienMoveSpeed : -alienMoveSpeed;
            for (Alien alien : aliens) {
                alien.moveHorizontal(moveAmount);
            }
        }
    }

    // Dans la classe SpaceInvader, remplacez la méthode drawPlayer par :
    private void drawPlayer(GL2 gl) {
        // Corps principal du vaisseau
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glColor3f(0.2f, 0.3f, 0.8f);  // Bleu foncé
        gl.glVertex2f(playerX - 0.05f, playerY);
        gl.glVertex2f(playerX + 0.05f, playerY);
        gl.glVertex2f(playerX, playerY + 0.08f);
        gl.glEnd();

        // Ailes
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor3f(0.3f, 0.4f, 0.9f);  // Bleu plus clair

        // Aile gauche
        gl.glVertex2f(playerX - 0.07f, playerY - 0.02f);
        gl.glVertex2f(playerX - 0.03f, playerY - 0.02f);
        gl.glVertex2f(playerX - 0.03f, playerY + 0.02f);
        gl.glVertex2f(playerX - 0.07f, playerY);

        // Aile droite
        gl.glVertex2f(playerX + 0.03f, playerY - 0.02f);
        gl.glVertex2f(playerX + 0.07f, playerY - 0.02f);
        gl.glVertex2f(playerX + 0.07f, playerY);
        gl.glVertex2f(playerX + 0.03f, playerY + 0.02f);
        gl.glEnd();

        // Détails du cockpit
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glColor3f(0.4f, 0.6f, 1.0f);  // Bleu très clair
        gl.glVertex2f(playerX - 0.02f, playerY + 0.02f);
        gl.glVertex2f(playerX + 0.02f, playerY + 0.02f);
        gl.glVertex2f(playerX, playerY + 0.06f);
        gl.glEnd();

        // Propulseurs
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor3f(0.9f, 0.5f, 0.1f);  // Orange
        gl.glVertex2f(playerX - 0.04f, playerY - 0.01f);
        gl.glVertex2f(playerX - 0.02f, playerY - 0.01f);
        gl.glVertex2f(playerX - 0.02f, playerY - 0.03f);
        gl.glVertex2f(playerX - 0.04f, playerY - 0.03f);

        gl.glVertex2f(playerX + 0.02f, playerY - 0.01f);
        gl.glVertex2f(playerX + 0.04f, playerY - 0.01f);
        gl.glVertex2f(playerX + 0.04f, playerY - 0.03f);
        gl.glVertex2f(playerX + 0.02f, playerY - 0.03f);
        gl.glEnd();
    }

    private void checkCollisions() {
        // Collisions projectiles joueur - aliens
        Iterator<Projectile> projectileIterator = playerProjectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile projectile = projectileIterator.next();
            Iterator<Alien> alienIterator = aliens.iterator();
            boolean collisionDetected = false;

            while (alienIterator.hasNext() && !collisionDetected) {
                Alien alien = alienIterator.next();
                if (isCollision(projectile, alien)) {
                    explosions.add(new Explosion(alien.getX(), alien.getY()));
                    alienIterator.remove();
                    projectileIterator.remove();
                    collisionDetected = true;
                    score += 100;
                    SoundManager.playSound("explosion");
                }
            }
        }

        // Collisions projectiles aliens - joueur
        Iterator<Projectile> alienProjectileIterator = alienProjectiles.iterator();
        while (alienProjectileIterator.hasNext()) {
            Projectile projectile = alienProjectileIterator.next();
            if (isPlayerHit(projectile)) {
                alienProjectileIterator.remove();
                playerLives--;
                SoundManager.playSound("hit");  // Ajout du son quand le joueur est touché
                System.out.println("Vies restantes : " + playerLives);
            }
        }
    }

    private boolean isPlayerHit(Projectile projectile) {
        float projectileLeft = projectile.getX() - 0.01f;
        float projectileRight = projectile.getX() + 0.01f;
        float projectileTop = projectile.getY() + 0.02f;
        float projectileBottom = projectile.getY() - 0.02f;

        float playerLeft = playerX - 0.05f;
        float playerRight = playerX + 0.05f;
        float playerTop = playerY + 0.1f;
        float playerBottom = playerY;

        return !(projectileRight < playerLeft ||
                projectileLeft > playerRight ||
                projectileTop < playerBottom ||
                projectileBottom > playerTop);
    }

    private void checkGameOver() {
        // Vérifier si le joueur n'a plus de vies
        if (playerLives <= 0) {
            victory = false;
            endGame();
        }

        // Vérifier si un alien a atteint le bas de l'écran
        for (Alien alien : aliens) {
            if (alien.getY() <= -0.7f) {
                victory = false;
                endGame();
                break;
            }
        }

        // Vérifier la victoire (tous les aliens détruits)
        if (aliens.isEmpty()) {
            victory = true;
            endGame();
        }
    }


    private void endGame() {
        gameOver = true;
        finalScore = score;
        SoundManager.playSound(victory ? "victory" : "gameover");
    }

    private boolean isCollision(Projectile projectile, Alien alien) {
        // Définir les hitbox
        float projectileLeft = projectile.getX() - 0.01f;
        float projectileRight = projectile.getX() + 0.01f;
        float projectileTop = projectile.getY() + 0.02f;
        float projectileBottom = projectile.getY() - 0.02f;

        float alienLeft = alien.getX() - 0.04f;
        float alienRight = alien.getX() + 0.04f;
        float alienTop = alien.getY() + 0.04f;
        float alienBottom = alien.getY() - 0.04f;

        // Vérifier si les rectangles se chevauchent
        return !(projectileRight < alienLeft ||
                projectileLeft > alienRight ||
                projectileTop < alienBottom ||
                projectileBottom > alienTop);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        SoundManager.cleanup();
    }

    // Gestion des touches
    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                initGame(); // Recommencer le jeu
            }
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if (playerX > -0.9f) playerX -= 0.05f;
                break;
            case KeyEvent.VK_RIGHT:
                if (playerX < 0.9f) playerX += 0.05f;
                break;
            case KeyEvent.VK_SPACE:
                playerProjectiles.add(new Projectile(playerX, playerY, 0.0007f));
                SoundManager.playSound("shoot");
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}