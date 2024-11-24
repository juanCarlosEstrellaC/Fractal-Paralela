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
        window = glfwCreateWindow(Params.WIDTH, Params.HEIGHT, "Ejemplo 01!", NULL, NULL);

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
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

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

        int clColor = 0x00FF0000;

        for(int i=0;i<Params.WIDTH;i++) {
            for(int j=0;j<Params.HEIGHT;j++) {
                //clColor = new Random().nextInt() % 255;
                //pixel_buffer[j*Params.WIDTH+i] = clColor;

                clColor = i * 255 / Params.WIDTH;

                pixel_buffer[j*Params.WIDTH+i] = clColor;        //red
                //pixel_buffer[j*Params.WIDTH+i] = clColor << 8; //green
                //pixel_buffer[j*Params.WIDTH+i] = clColor << 16; //blue
            }
        }

        //dibujar
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D,
                0,
                GL_RGBA,
                Params.WIDTH, Params.HEIGHT, 0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                pixel_buffer);

        glBegin(GL_QUADS);
        {
            glTexCoord2f(0,1);
            glVertex3f(-1, -1, 0);

            glTexCoord2f(0,0);
            glVertex3f(-1, 1, 0);

            glTexCoord2f(1,0);
            glVertex3f(1, 1, 0);

            glTexCoord2f(1,1);
            glVertex3f(1, -1, 0);
        }
        glEnd();
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
