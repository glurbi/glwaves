import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

public class ShaderTest {

    private static int points = 1000;
    private static int lines = 100;

    private static class Vertice {
    	public Vertice(float x, float y, float red, float green, float blue) {
    		this.x = x; this.y = y; this.red = red; this.green = green; this.blue = blue;
    	}
    	public final float x;
    	public final float y;
    	public final float red;
    	public final float green;
    	public final float blue;
    }
    
    private static class Triangle {
    	public Triangle(Vertice v1, Vertice v2, Vertice v3) {
    		this.v1 = v1; this.v2 = v2; this.v3 = v3;
    	}
    	public final Vertice v1;
    	public final Vertice v2;
    	public final Vertice v3;
    }
    
    private static List<Triangle> triangles = createTriangles();
    
    private static List<Triangle> createTriangles() {
    	ArrayList<Triangle> result = new ArrayList<Triangle>();
        for (int line = 1; line < lines; line++) {
            for (int point = 1; point < points; point++) {
            	float x1 = point / 100.0f;
            	float x2 = (point+1) / 100.0f;
            	float x3 = point / 100.0f;
            	float x4 = (point+1) / 100.0f;
            	float y1 = line / 100.0f;
            	float y2 = line / 100.0f;
            	float y3 = (line+1) / 100.0f;
            	float y4 = (line+1) / 100.0f;
            	Vertice v1 = new Vertice(x1, y1, 0.5f + (float) Math.cos(x1) / 2, 0.5f + (float) Math.sin(y1) / 2, 0.5f);
            	Vertice v2 = new Vertice(x2, y2, 0.5f + (float) Math.cos(x2) / 2, 0.5f + (float) Math.sin(y2) / 2, 0.5f);
            	Vertice v3 = new Vertice(x3, y3, 0.5f + (float) Math.cos(x3) / 2, 0.5f + (float) Math.sin(y3) / 2, 0.5f);
            	Vertice v4 = new Vertice(x4, y4, 0.5f + (float) Math.cos(x4) / 2, 0.5f + (float) Math.sin(y4) / 2, 0.5f);
            	result.add(new Triangle(v1, v2, v3));
            	result.add(new Triangle(v2, v3, v4));
            }
        }
    	return result;
    }
    
    private volatile static float zoomFactor = 1.0f;

    public static void main(String[] args) {
        final long t0 = System.currentTimeMillis();
        final AtomicLong frameCount = new AtomicLong();
        Frame frame = new Frame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        final GLCanvas canvas = new GLCanvas();
        final GLEventListener myEventListener = new MyEventListener();
        canvas.addGLEventListener(myEventListener);
        frame.add(canvas);
        frame.setSize(300, 300);
        frame.setVisible(true);
        MouseAdapter myMouseAdapter = new MouseAdapter() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                int rotation = e.getWheelRotation();
                if (rotation > 0) {
                    zoomFactor *= rotation * 2;
                } else if (rotation < 0) {
                    zoomFactor /= -rotation * 2;
                }
                System.out.println("zoomFactor=" + zoomFactor);
            }
            public void mouseDragged(MouseEvent e) {
            }
        };
        frame.addMouseWheelListener(myMouseAdapter);
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
            gl2.glFrustum(-0.5f * zoomFactor, 0.5f * zoomFactor, -0.5f * zoomFactor / 2, 0.5f * zoomFactor / 2, 1.0f,
                    10.0f);
            gl2.glMatrixMode(GL2.GL_MODELVIEW);

            gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            gl2.glLoadIdentity();
            gl2.glTranslatef(0.0f, -0.0f, -4.0f);
            gl2.glBegin(GL.GL_TRIANGLES);

            for (Triangle triangle : triangles) {
            	gl2.glVertex3f(triangle.v1.x, triangle.v1.y, 0.0f);
            	gl2.glColor3f(triangle.v1.red, triangle.v1.green, triangle.v1.blue);
            	gl2.glVertex3f(triangle.v2.x, triangle.v2.y, 0.0f);
            	gl2.glColor3f(triangle.v2.red, triangle.v2.green, triangle.v1.blue);
            	gl2.glVertex3f(triangle.v3.x, triangle.v3.y, 0.0f);
            	gl2.glColor3f(triangle.v3.red, triangle.v3.green, triangle.v1.blue);
            }

            gl2.glEnd();
            gl2.glFlush();
        }

        public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
        }

        public void dispose(GLAutoDrawable drawable) {
        }

    }

}