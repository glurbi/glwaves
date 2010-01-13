import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.FloatBuffer;
import java.util.concurrent.atomic.AtomicLong;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

import com.sun.opengl.util.BufferUtil;

public class ShaderTest {

    private static final int points = 1000;
    private static final int lines = 100;
    private static final float mouseAccel = 0.01f;

    private volatile static float scale = 1.0f;
    private volatile static float xTranslate = 0.0f;
    private volatile static float yTranslate = 0.0f;
    private volatile static float xRotate = 0.0f;
    private volatile static float yRotate = 0.0f;
    
    private static FloatBuffer vertices = createVertices();
    private static FloatBuffer colors = createColors();

    private static AtomicLong frameCount = new AtomicLong();
    
    private static FloatBuffer createVertices() {
        FloatBuffer buf = BufferUtil.newFloatBuffer(points*lines*2*4*4);
        for (int line = 0; line < lines; line++) {
            for (int point = 0; point < points; point++) {
                buf.put(point / 100.0f);
                buf.put(line / 100.0f);
                buf.put((point + 1) / 100.0f);
                buf.put(line / 100.0f);
                buf.put((point + 1) / 100.0f);
                buf.put((line + 1) / 100.0f);
                buf.put(point / 100.0f);
                buf.put((line + 1) / 100.0f);
            }
        }
        return buf;
    }
    
    private static FloatBuffer createColors() {
        FloatBuffer buf = BufferUtil.newFloatBuffer(points*lines*3*4*4);
        vertices.rewind();
        for (int i = 0; i < lines * points * 4; i++) {
            float x = vertices.get();
            float y = vertices.get();
            buf.put(0.5f + (float) Math.cos(x) / 2);
            buf.put(0.5f + (float) Math.sin(y) / 2);
            buf.put(0.5f);
        }
        return buf;
    }

    private static class MyMouseListener extends MouseAdapter {
        
        private int lastX;
        private int lastY;
        
        public void mouseWheelMoved(MouseWheelEvent e) {
            int rotation = e.getWheelRotation();
            if (rotation > 0) {
                scale /= rotation * 2;
            } else if (rotation < 0) {
                scale *= -rotation * 2;
            }
            System.out.println("zoomFactor=" + scale);
        }

        public void mousePressed(MouseEvent e) {
            lastX = e.getX();
            lastY = e.getY();
        }
        
        public void mouseDragged(MouseEvent e) {
            int difX = e.getX() - lastX;
            int difY = e.getY() - lastY;
            lastX = e.getX();
            lastY = e.getY();
            if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
                xTranslate += mouseAccel * difX / scale;
                yTranslate -= mouseAccel * difY / scale;
            } else if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
                xRotate += difY;
                yRotate += difX;
            }
        }
        
    };
    
    public static class MyEventListener implements GLEventListener {

        public void init(GLAutoDrawable drawable) {
            GL gl = drawable.getGL();
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        }

        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        }

        public void display(GLAutoDrawable drawable) {
            GL2 gl2 = (GL2) drawable.getGL();

            gl2.glMatrixMode(GL2.GL_PROJECTION);
            gl2.glLoadIdentity();
            gl2.glFrustum(-0.5f, 0.5f, -0.5, 0.5f, 1.0f, 100.0f);
            
            gl2.glMatrixMode(GL2.GL_MODELVIEW);
            gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            
            gl2.glLoadIdentity();
            gl2.glTranslatef(xTranslate, yTranslate, 0.0f);
            gl2.glTranslatef(-1.5f, -0.0f, -5.0f);
            gl2.glRotatef(xRotate, 1, 0, 0);
            gl2.glRotatef(yRotate, 0, 1, 0);
            gl2.glScalef(scale, scale, scale);
            
            gl2.glBegin(GL.GL_TRIANGLES);
            gl2.glColor3f(1.0f, 1.0f, 1.0f);
            gl2.glVertex3f(0.0f, 1.0f, 0.0f);
            gl2.glVertex3f(-1.0f, -1.0f, 0.0f);
            gl2.glVertex3f(1.0f, -1.0f, 0.0f);
            gl2.glEnd();

            gl2.glTranslatef(1.5f, 0.0f, 0.0f);

            vertices.rewind();
            colors.rewind();
            
            gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            gl2.glEnableClientState(GL2.GL_COLOR_ARRAY);

            gl2.glVertexPointer(2, GL.GL_FLOAT, 0, vertices);
            gl2.glColorPointer(3, GL.GL_FLOAT, 0, colors);
            gl2.glDrawArrays(GL2.GL_QUADS, 0, lines*points*4);

            gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);
            gl2.glDisableClientState(GL2.GL_COLOR_ARRAY);
            
            
            gl2.glFlush();
        }

        public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
        }

        public void dispose(GLAutoDrawable drawable) {
        }

    }

    
    public static void main(String[] args) {
        final long t0 = System.currentTimeMillis();
        Frame frame = new Frame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        final GLCanvas canvas = new GLCanvas();
        final GLEventListener myEventListener = new MyEventListener();
        MyMouseListener myMouseListener = new MyMouseListener();
        canvas.addGLEventListener(myEventListener);
        canvas.addMouseListener(myMouseListener);
        canvas.addMouseWheelListener(myMouseListener);
        canvas.addMouseMotionListener(myMouseListener);
        frame.add(canvas);
        frame.setSize(300, 300);
        frame.setVisible(true);
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    canvas.display();
                    frameCount.incrementAndGet();
                    Thread.yield();
                }
            }
        }).start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                long totalTime = System.currentTimeMillis() - t0;
                System.out.println(totalTime + " ms");
                System.out.println(frameCount.get() + " frames");
                System.out.println(1.0 * frameCount.get() / totalTime * 1000);
            }
        });
    }

}
