import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

import com.sun.opengl.util.BufferUtil;

public class ShaderTest {

    private static final int points = 500;
    private static final int lines = 200;
    private static final float mouseAccel = 0.01f;
    private static final int steps = 50;
    private static final float phaseInc = (float) -(2 * Math.PI / steps);

    private static float scale = 1.0f;
    private static float xTranslate = 0.0f;
    private static float yTranslate = 0.0f;
    private static float xRotate = 0.0f;
    private static float yRotate = 0.0f;
    
    private static float xSource = 3.5f;
    private static float ySource = 0.5f;

    private static AtomicInteger frameCount = new AtomicInteger();
    
    private static List<FloatBuffer> verticesList = new ArrayList<FloatBuffer>();
    private static List<FloatBuffer> colorsList = new ArrayList<FloatBuffer>();

    private static FloatBuffer vertices = null;
    private static FloatBuffer colors = null;
    
    private static void createDataSet() {
        System.out.println("Creating simulated data");
        for (int i = 0; i < steps; i++) {
            
            float phase = phaseInc*i;
            float x, y, xDif, yDif, squareDist, amplitude;
            
            // 3 components (x, y, z) per vertex, 4 vertices per quad
            FloatBuffer vertices = BufferUtil.newFloatBuffer(points*lines*3*4);
            // 3 components (red, green, blue) per color, 4 colors per quad
            FloatBuffer colors = BufferUtil.newFloatBuffer(points*lines*3*4);
            for (int line = 0; line < lines; line++) {
                for (int point = 0; point < points; point++) {

                    x = point / 100.0f;
                    y = line / 100.0f;
                    xDif = xSource - x;
                    yDif = ySource - y;
                    squareDist = xDif*xDif + yDif*yDif;
                    amplitude = (float) Math.cos(squareDist + phase) / (10 * squareDist + 1);
                    vertices.put(x);
                    vertices.put(y);
                    vertices.put(amplitude);
                    colors.put(0.5f + amplitude);
                    colors.put(0.5f - amplitude * 10);
                    colors.put(0.5f - amplitude);
                    
                    x = (point + 1) / 100.0f;
                    y = line / 100.0f;
                    xDif = xSource - x;
                    yDif = ySource - y;
                    squareDist = xDif*xDif + yDif*yDif;
                    amplitude = (float) Math.cos(squareDist + phase) / (10 * squareDist + 1);
                    vertices.put(x);
                    vertices.put(y);
                    vertices.put(amplitude);
                    colors.put(0.5f + amplitude);
                    colors.put(0.5f - amplitude * 10);
                    colors.put(0.5f - amplitude);
                    
                    x = (point + 1) / 100.0f;
                    y = (line + 1) / 100.0f;
                    xDif = xSource - x;
                    yDif = ySource - y;
                    squareDist = xDif*xDif + yDif*yDif;
                    amplitude = (float) Math.cos(squareDist + phase) / (10 * squareDist + 1);
                    vertices.put(x);
                    vertices.put(y);
                    vertices.put(amplitude);
                    colors.put(0.5f + amplitude);
                    colors.put(0.5f - amplitude * 10);
                    colors.put(0.5f - amplitude);
                    
                    
                    x = point / 100.0f;
                    y = (line + 1) / 100.0f;
                    xDif = xSource - x;
                    yDif = ySource - y;
                    squareDist = xDif*xDif + yDif*yDif;
                    amplitude = (float) Math.cos(squareDist + phase) / (10 * squareDist + 1);
                    vertices.put(x);
                    vertices.put(y);
                    vertices.put(amplitude);
                    colors.put(0.5f + amplitude);
                    colors.put(0.5f - amplitude * 10);
                    colors.put(0.5f - amplitude);
                }
            }
            
            verticesList.add(vertices);
            colorsList.add(colors);
            System.out.print(".");
        }
        System.out.println();
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
            gl2.glTranslatef(0.0f, 0.0f, -5.0f);
            gl2.glRotatef(xRotate, 1, 0, 0);
            gl2.glRotatef(yRotate, 0, 1, 0);
            gl2.glScalef(scale, scale, scale);
            
            gl2.glLineWidth(5);
            gl2.glBegin(GL.GL_LINES);
            gl2.glColor3f(1.0f, 1.0f, 1.0f);
            gl2.glVertex3f(0.0f, 0.0f, 0.0f);
            gl2.glVertex3f(1.0f, 0.0f, 0.0f);
            gl2.glVertex3f(0.0f, 0.0f, 0.0f);
            gl2.glVertex3f(0.0f, 1.0f, 0.0f);
            gl2.glVertex3f(0.0f, 0.0f, 0.0f);
            gl2.glVertex3f(0.0f, 0.0f, 1.0f);
            gl2.glEnd();

            vertices.rewind();
            colors.rewind();
            
            gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            gl2.glEnableClientState(GL2.GL_COLOR_ARRAY);

            gl2.glVertexPointer(3, GL.GL_FLOAT, 0, vertices);
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
        createDataSet();
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
                    frameCount.incrementAndGet();
                    vertices = verticesList.get(frameCount.get() % steps);
                    colors = colorsList.get(frameCount.get() % steps);
                    canvas.display();
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
