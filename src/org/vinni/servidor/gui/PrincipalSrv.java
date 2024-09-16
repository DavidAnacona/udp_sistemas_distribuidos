package org.vinni.servidor.gui;

import org.vinni.dto.MiDatagrama;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author: Vinni
 */
public class PrincipalSrv extends JFrame {

    private final int PORT = 12345;

    /**
     * Creates new form Principal1
     */
    public PrincipalSrv() {
        initComponents();
        this.mensajesTxt.setEditable(false);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        this.setTitle("Servidor ...");

        bIniciar = new JButton();
        jLabel1 = new JLabel();
        mensajesTxt = new JTextArea();
        jScrollPane1 = new JScrollPane();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        bIniciar.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bIniciar.setText("INICIAR SERVIDOR");
        bIniciar.addActionListener(evt -> bIniciarActionPerformed(evt));
        getContentPane().add(bIniciar);
        bIniciar.setBounds(150, 50, 250, 40);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(204, 0, 0));
        jLabel1.setText("SERVIDOR UDP : FERINK");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(150, 10, 160, 17);

        mensajesTxt.setColumns(25);
        mensajesTxt.setRows(5);

        jScrollPane1.setViewportView(mensajesTxt);

        getContentPane().add(jScrollPane1);
        jScrollPane1.setBounds(20, 150, 500, 120);

        setSize(new java.awt.Dimension(570, 320));
        setLocationRelativeTo(null);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new PrincipalSrv().setVisible(true));
    }

    private void bIniciarActionPerformed(java.awt.event.ActionEvent evt) {
        iniciar();
    }

    public void iniciar() {
        mensajesTxt.append("Servidor UDP iniciado en el puerto " + PORT + "\n");
        byte[] buf = new byte[4096]; // Aumentar el buffer para manejar archivos más grandes

        new Thread(() -> {
            DatagramPacket dp = null;
            try {
                DatagramSocket socketudp = new DatagramSocket(PORT);
                boolean inicio = true;
                this.bIniciar.setEnabled(false);

                while (inicio) {
                    mensajesTxt.append("Escuchando ...\n ");
                    dp = new DatagramPacket(buf, buf.length);
                    socketudp.receive(dp);

                    // Obtener el mensaje recibido y procesarlo
                    String elmensaje = new String(dp.getData(), 0, dp.getLength()).trim();
                    mensajesTxt.append("El mensaje recibido es: " + elmensaje + "\n");

                    // Si el mensaje es "Fin", cerrar el servidor
                    if (elmensaje.equalsIgnoreCase("Fin")) {
                        mensajesTxt.append("Cerrando el servidor...\n");
                        inicio = false;
                        socketudp.close();
                        break;
                    }

                    // Si el mensaje es un archivo, cargarlo en una ruta específica
                    if (elmensaje.startsWith("FILE:")) {
                        String[] parts = elmensaje.split(":");
                        String fileName = parts[1]; // Obtener el nombre del archivo
                        recibirArchivo(socketudp, dp.getAddress(), dp.getPort(), fileName);
                    } else {
                        // Enviar de vuelta al cliente el mensaje recibido
                        DatagramPacket mensajeServ = MiDatagrama.crearDataG(dp.getAddress().getHostAddress(),
                                dp.getPort(), elmensaje);
                        socketudp.send(mensajeServ);
                    }
                }

            } catch (SocketException ex) {
                Logger.getLogger(PrincipalSrv.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PrincipalSrv.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    }

    private void recibirArchivo(DatagramSocket socket, InetAddress address, int port, String fileName) {
        try {
            // Ruta donde se almacenarán los archivos recibidos
            String filePath = "C:/UDP_ARCHIVO/" + fileName;
            FileOutputStream fos = new FileOutputStream(filePath);

            // Recibir los datos del archivo en bloques
            byte[] buffer = new byte[4096];
            DatagramPacket archivoPacket = new DatagramPacket(buffer, buffer.length);
            boolean receivingFile = true;

            while (receivingFile) {
                socket.receive(archivoPacket);
                fos.write(archivoPacket.getData(), 0, archivoPacket.getLength());

                // Verificar si este es el último paquete (puedes usar una señal o tamaño)
                if (archivoPacket.getLength() < buffer.length) { // Indicador del último paquete
                    receivingFile = false;
                }
            }

            fos.close();
            mensajesTxt.append("Archivo guardado en: " + filePath + "\n");

            // Enviar confirmación al cliente
            String confirmacion = "Archivo " + fileName + " recibido y guardado correctamente.";
            DatagramPacket confirmacionPacket = MiDatagrama.crearDataG(address.getHostAddress(), port, confirmacion);
            socket.send(confirmacionPacket);

        } catch (IOException e) {
            mensajesTxt.append("Error al recibir el archivo: " + e.getMessage() + "\n");
        }
    }

    // Variables declaration - do not modify
    private JButton bIniciar;
    private JLabel jLabel1;
    private JTextArea mensajesTxt;
    private JScrollPane jScrollPane1;
}
