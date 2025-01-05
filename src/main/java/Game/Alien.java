package Game;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;
import javax.swing.JFrame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

// Classe pour représenter un alien
public class Alien {
    private float x, y;
    private int health;

    public Alien(float x, float y) {
        this.x = x;
        this.y = y;
        this.health = 1;
    }

    public void hit() {
        health--;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public float getX() { return x; }
    public float getY() { return y; }

    public void moveHorizontal(float amount) {
        x += amount;
    }

    public void moveDown(float amount) {
        y -= amount;
    }

    // Dans la classe Alien, remplacez la méthode draw par :
// Dans la classe Alien, remplacez la méthode draw par :
    public void draw(GL2 gl) {
        // Corps principal
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glColor3f(0.8f, 0.2f, 0.2f);  // Rouge foncé

        // Tête
        gl.glVertex2f(x - 0.04f, y);
        gl.glVertex2f(x + 0.04f, y);
        gl.glVertex2f(x, y + 0.04f);

        // Antennes
        gl.glVertex2f(x - 0.03f, y + 0.02f);
        gl.glVertex2f(x - 0.02f, y + 0.02f);
        gl.glVertex2f(x - 0.02f, y + 0.06f);

        gl.glVertex2f(x + 0.03f, y + 0.02f);
        gl.glVertex2f(x + 0.02f, y + 0.02f);
        gl.glVertex2f(x + 0.02f, y + 0.06f);
        gl.glEnd();

        // Corps
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor3f(1.0f, 0.2f, 0.2f);  // Rouge plus vif
        gl.glVertex2f(x - 0.04f, y - 0.04f);
        gl.glVertex2f(x + 0.04f, y - 0.04f);
        gl.glVertex2f(x + 0.04f, y);
        gl.glVertex2f(x - 0.04f, y);

        // Yeux
        gl.glColor3f(1.0f, 1.0f, 1.0f);  // Blanc
        gl.glVertex2f(x - 0.02f, y + 0.01f);
        gl.glVertex2f(x - 0.01f, y + 0.01f);
        gl.glVertex2f(x - 0.01f, y + 0.02f);
        gl.glVertex2f(x - 0.02f, y + 0.02f);

        gl.glVertex2f(x + 0.01f, y + 0.01f);
        gl.glVertex2f(x + 0.02f, y + 0.01f);
        gl.glVertex2f(x + 0.02f, y + 0.02f);
        gl.glVertex2f(x + 0.01f, y + 0.02f);
        gl.glEnd();

        // Tentacules
        gl.glBegin(GL2.GL_LINES);
        gl.glColor3f(0.8f, 0.2f, 0.2f);
        for(int i = -3; i <= 3; i++) {
            gl.glVertex2f(x + (i * 0.01f), y - 0.04f);
            gl.glVertex2f(x + (i * 0.01f), y - 0.06f);
        }
        gl.glEnd();
    }
}