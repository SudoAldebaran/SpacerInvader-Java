package Game;

import com.jogamp.opengl.GL2;

class Explosion {
    private float x, y;
    private int duration = 300; // Durée de l'explosion en frames augmentée

    public Explosion(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public boolean update() {
        duration--;
        return duration > 0;
    }

    public void draw(GL2 gl) {
        // Dessiner une explosion simple (étoile/éclair)
        // Couleurs plus vives avec variation selon la durée
        float intensity = (float)duration / 45.0f;
        gl.glColor3f(1.0f, intensity * 0.7f, 0.0f); // Orange vif à rouge

        // Lignes en étoile
        // Augmenter l'épaisseur des lignes
        gl.glLineWidth(3.0f);
        gl.glBegin(GL2.GL_LINES);
        float size = 0.08f; // Taille augmentée

        // Lignes horizontales et verticales
        gl.glVertex2f(x - size, y);
        gl.glVertex2f(x + size, y);
        gl.glVertex2f(x, y - size);
        gl.glVertex2f(x, y + size);

        // Lignes diagonales
        gl.glVertex2f(x - size/2, y - size/2);
        gl.glVertex2f(x + size/2, y + size/2);
        gl.glVertex2f(x - size/2, y + size/2);
        gl.glVertex2f(x + size/2, y - size/2);
        gl.glEnd();
    }
}