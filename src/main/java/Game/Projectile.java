package Game;

import com.jogamp.opengl.*;

class Projectile {
    private float x, y;
    private float speed;

    public Projectile(float x, float y, float speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
    }

    public void update() {
        y += speed;
    }

    public float getX() { return x; }
    public float getY() { return y; }

    public void draw(GL2 gl) {
        if (speed > 0) { // Projectile du joueur
            // Laser énergétique bleu-vert
            gl.glBegin(GL2.GL_QUADS);
            // Corps principal du laser
            gl.glColor3f(0.0f, 1.0f, 0.8f); // Bleu-vert clair
            gl.glVertex2f(x - 0.01f, y - 0.03f);
            gl.glVertex2f(x + 0.01f, y - 0.03f);
            gl.glVertex2f(x + 0.01f, y + 0.03f);
            gl.glVertex2f(x - 0.01f, y + 0.03f);
            gl.glEnd();

            // Effet de brillance au centre
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor3f(1.0f, 1.0f, 1.0f); // Blanc brillant
            gl.glVertex2f(x - 0.003f, y - 0.02f);
            gl.glVertex2f(x + 0.003f, y - 0.02f);
            gl.glVertex2f(x + 0.003f, y + 0.02f);
            gl.glVertex2f(x - 0.003f, y + 0.02f);
            gl.glEnd();

        } else { // Projectile des aliens
            // Boule d'énergie rouge
            gl.glBegin(GL2.GL_TRIANGLES);
            // Noyau central
            gl.glColor3f(1.0f, 0.0f, 0.0f); // Rouge vif
            gl.glVertex2f(x - 0.015f, y - 0.015f);
            gl.glVertex2f(x + 0.015f, y - 0.015f);
            gl.glVertex2f(x, y + 0.015f);

            gl.glVertex2f(x - 0.015f, y + 0.015f);
            gl.glVertex2f(x + 0.015f, y + 0.015f);
            gl.glVertex2f(x, y - 0.015f);
            gl.glEnd();

            // Points de brillance
            gl.glPointSize(2.0f);
            gl.glBegin(GL2.GL_POINTS);
            gl.glColor3f(1.0f, 0.8f, 0.8f); // Rose clair
            gl.glVertex2f(x, y);
            gl.glEnd();
            gl.glPointSize(1.0f);
        }
    }
}