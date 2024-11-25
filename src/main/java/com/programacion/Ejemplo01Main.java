package com.programacion;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Ejemplo01Main {

    private static long window;
    static int textureID;

    static void run() {
        System.out.println("LWJGL " + Version.getVersion());

        init();

        loop();
    }

    static void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Create the window
        window = glfwCreateWindow(Params.WIDTH, Params.HEIGHT, "Deber Fractales - Juan Estrella", NULL, NULL);

        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            glViewport(0, 0, width, height);
        });

        {
            // centrar ventana
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - Params.WIDTH) / 2,
                    (vidmode.height() - Params.HEIGHT) / 2
            );
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        GL.createCapabilities();

        String version = glGetString(GL_VERSION);
        String vendor = glGetString(GL_VENDOR);
        String renderer = glGetString(GL_RENDERER);

        System.out.println("OpenGL version: " + version);
        System.out.println("OpenGL vendor: " + vendor);
        System.out.println("OpenGL renderer: " + renderer);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(-1, 1, -1, 1, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glEnable(GL_TEXTURE_2D);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        initTexteures();
    }

    static void initTexteures() {
        textureID = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textureID);

        glTexImage2D(GL_TEXTURE_2D,
                0,
                GL_RGBA8,
                Params.WIDTH, Params.HEIGHT, 0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                NULL
        );

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    static void loop() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f); //color de fondo negro

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            paint();

            glfwSwapBuffers(window);

            glfwPollEvents();
        }
    }


    //--------------------------------------------------------------------------------
    static int pixel_buffer[] = new int[Params.WIDTH*Params.HEIGHT]; // size= WIDTHxHEIGHT

    static void mandelbrotCpu() {
        int iteracionesMaximas = 2;
        double radioEscape = 2;

        int colorDentro = 0x00000000; // Negro
        int colorFuera = 0x00FFFFFF;  // Blanco

        for (int px = 0; px < Params.WIDTH; px++) {
            for (int py = 0; py < Params.HEIGHT; py++) {
                int iteracion = 0;

                // zn = x + y*i       -> zn   es número complejo de la forma z = a + b*i
                // zn+1 = (zn)^2 + c  -> zn+1 es número complejo de la forma zn+1 = (a + b*i)^2 + (a0 + b0*i), donde c es otro complejo.
                // c = x0 + y0*i

                // zn = x + y*i, donde z0 = 0
                double x = 0;
                double y = 0;

                // Mapear coordenadas del píxel al plano complejo para obtener "c"
                // c = x0 + y0*i
                double x0 = map(px, 0, Params.WIDTH, -2.0, 1.0);
                double y0 = map(py, 0, Params.HEIGHT, -1.5, 1.5);

                // si ( ||zn|| <= 4         y  i < N ), i.e.:
                // si ( ||x^2 + y^2|| <= 4  y  i < N )
                while (x * x + y * y <= radioEscape * radioEscape && iteracion < iteracionesMaximas) {
                    // zn+1 = (zn)^2 + c
                    // zn+1 = (x + y*i)^2 + c
                    // zn+1 = (x^2 + 2*x*y*i + (y*i)^2) + c
                    // zn+1 = (x^2 - y^2) + (2*x*y)*i + c    ya que (y*i)^2 = -y^2 por i^2 = -1
                    // zn+1 = (x^2 - y^2) + (2*x*y)*i + (x0 + y0*i)
                    // zn+1 = (x^2 - y^2 + x0) + (2*x*y + y0)*i

                    // La parte real de zn+1 es: x^2 - y^2 + x0
                    // La parte imaginaria de zn+1 es: 2*x*y + y0

                    // uso temporales para hacer los calculos sin sobrescribir las variables previo a usarlas
                    // en la formula
                    double xtemp = x * x - y * y + x0;
                    double ytemp = 2 * x * y + y0;
                    //Luego de aplicar, ya actualizo
                    x = xtemp;
                    y = ytemp;
                    iteracion++;
                }

                // Determinar color basado en el número de iteraciones
                // para acceder al pixel correcto en el vector, el índice = fila x ancho x columna
                if (iteracion < iteracionesMaximas) {
                    pixel_buffer[py * Params.WIDTH + px] = colorFuera;
                } else {
                    pixel_buffer[py * Params.WIDTH + px] = colorDentro;
                }
            }
        }

        // Dibujar los píxeles como textura
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, Params.WIDTH, Params.HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixel_buffer);

        glBegin(GL_QUADS);
        {
            glTexCoord2f(0, 1);
            glVertex3f(-1, -1, 0);

            glTexCoord2f(0, 0);
            glVertex3f(-1, 1, 0);

            glTexCoord2f(1, 0);
            glVertex3f(1, 1, 0);

            glTexCoord2f(1, 1);
            glVertex3f(1, -1, 0);
        }
        glEnd();
    }

    // Función para mapear valores de un rango a otro
    static double map(int valor, int inicio1, int fin1, double inicio2, double fin2) {
        return inicio2 + (fin2 - inicio2) * ((double) valor - inicio1) / (fin1 - inicio1);
    }


    //--------------------------------------------------------------------------------

    static void paint() {
        mandelbrotCpu();

//        glBegin(GL_TRIANGLES);
//        {
//            glVertex2d(-1, -1);
//            glVertex2d(0, 0);
//            glVertex2d(0, -1);
//        }
//        glEnd();
    }

    public static void main(String[] args) {
        run();
    }
}
