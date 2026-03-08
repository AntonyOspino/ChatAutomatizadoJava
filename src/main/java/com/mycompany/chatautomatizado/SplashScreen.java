
package com.mycompany.chatautomatizado;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author tonny
 */
public class SplashScreen extends JWindow{
    public SplashScreen() {
        // Cargar el logo real desde la carpeta resources/imagenes
        java.net.URL imgURL = getClass().getResource("/imagenes/tu_logo.png");
        
        if (imgURL != null) {
            ImageIcon logo = new ImageIcon(imgURL);
            JLabel labelLogo = new JLabel(logo);
            add(labelLogo, BorderLayout.CENTER);
        } else {
            // Si no encuentra la imagen, pone un texto para que no se estrelle
            System.err.println("No se encontró la imagen del logo");
            add(new JLabel("Cargando Asistente...", SwingConstants.CENTER), BorderLayout.CENTER);
        }
        
        pack();
        setLocationRelativeTo(null); // Centrar en la pantalla
    }
    /*public SplashScreen() {
        // Texto temporal en lugar de imagen
        JLabel labelLogo = new JLabel("Cargando Asistente...", SwingConstants.CENTER);
        labelLogo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        add(labelLogo, BorderLayout.CENTER);
        
        // Hacer la ventana de carga un poco más grande para el texto
        setPreferredSize(new Dimension(400, 300));
        pack();
        setLocationRelativeTo(null); // Centrar en la pantalla
    }*/
    /*public SplashScreen() {
        // Aquí pones la ruta de tu logo
        ImageIcon logo = new ImageIcon(getClass().getResource("/imagenes/tu_logo.webp")); 
        JLabel labelLogo = new JLabel(logo);
        add(labelLogo, BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(null); // Centrar en la pantalla
    }*/
    
    public void mostrarYContinuar(Runnable onTerminar) {
        setVisible(true);
        // Usamos un hilo para no bloquear la aplicación
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Esperar 3 segundos
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setVisible(false);
            dispose();
            // Iniciar la ventana principal
            SwingUtilities.invokeLater(onTerminar);
        }).start();
    }
}
