/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.chatautomatizado;

import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
/**
 *
 * @author tonny
 */
public class ChatAutomatizado {

    public static void main(String[] args) {
        // Aplicar el tema moderno ANTES de crear la interfaz
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf()); // o FlatDarkLaf() si quieres modo oscuro
        } catch (Exception ex) {
            System.err.println("Error al inicializar FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen();
            splash.mostrarYContinuar(() -> {
                VentanaChat chat = new VentanaChat();
                chat.setVisible(true);
            });
        });
    }
}
