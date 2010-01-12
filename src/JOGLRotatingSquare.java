import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

import com.sun.opengl.util.Animator;

public class JOGLRotatingSquare {

    private static float angle = 0;
    private static final int SIZE = 160;

    JOGLRotatingSquare() {
        GLCanvas canvas = getGLCanvas();
        canvas.addGLEventListener(new RotatingSquareListener());
        Animator anim = new Animator(canvas);
        addCanvasToFrame(canvas, anim);
        anim.start();
    }

    private void addCanvasToFrame(GLCanvas canvas, final Animator anim) {
        Frame f = new Frame("JOGL Rotating Square");
        f.setSize(600, 400);
        f.add(canvas);
        f.setVisible(true);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                anim.stop();
                System.exit(0);
            }
        });
    }

    private GLCanvas getGLCanvas() {
        return new GLCanvas();
    }

    public static void main(String[] args) {
        new JOGLRotatingSquare();
    }

    private void drawRedCenteredSquare(GL2 gl) {
        gl.glColor3f(1, 0, 0);
        gl.glRecti(-SIZE / 2, -SIZE / 2, SIZE / 2, SIZE / 2);
        gl.glColor3f(0.0f, 0.0f, 0.0f);
    }

    class RotatingSquareListener implements GLEventListener {

        public void init(GLAutoDrawable drawable) {
            GL2 gl = (GL2) drawable.getGL();
            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // erasing color
            gl.glColor3f(0.0f, 0.0f, 0.0f); // drawing color
        }

        public void display(GLAutoDrawable drawable) {
            GL2 gl = (GL2) drawable.getGL();
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            drawRedCenteredSquare(gl);
            angle++;
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glRotatef(angle, 0, 0, 1);
        }

        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
            GL2 gl = (GL2) drawable.getGL();
            gl.glViewport(0, 0, width, height);
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glOrtho(-width, width, -height, height, -1, 1);
        }

        public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
        }

        public void dispose(GLAutoDrawable drawable) {
        }
    }
    
}
