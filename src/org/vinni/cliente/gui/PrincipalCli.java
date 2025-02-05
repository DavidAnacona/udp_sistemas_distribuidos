package org.vinni.cliente.gui;

import org.vinni.dto.MiDatagrama;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * author: Vinni 2024
 */
public class PrincipalCli extends JFrame {

    private final int PORT = 12345;

    /**
     * Creates new form Principal1
     */
    public PrincipalCli() {
        initComponents();

        this.btEnviar.setEnabled(true);
        this.mensajesTxt.setEditable(false);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        this.setTitle("Cliente ");
        jLabel1 = new JLabel();
        jScrollPane1 = new JScrollPane();
        mensajesTxt = new JTextArea();
        mensajeTxt = new JTextField();
        jLabel2 = new JLabel();
        btEnviar = new JButton();
        btCargarArchivo = new JButton();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(204, 0, 0));
        jLabel1.setText("CLIENTE UDP : LUING");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(110, 10, 250, 17);

        mensajesTxt.setColumns(20);
        mensajesTxt.setRows(5);

        jScrollPane1.setViewportView(mensajesTxt);

        getContentPane().add(jScrollPane1);
        jScrollPane1.setBounds(30, 210, 410, 110);

        mensajeTxt.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        getContentPane().add(mensajeTxt);
        mensajeTxt.setBounds(40, 120, 350, 30);

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        jLabel2.setText("Mensaje:");
        getContentPane().add(jLabel2);
        jLabel2.setBounds(20, 90, 120, 30);

        btEnviar.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        btEnviar.setText("Enviar");
        btEnviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btEnviarActionPerformed(evt);
            }
        });
        getContentPane().add(btEnviar);
        btEnviar.setBounds(327, 160, 120, 27);

        btCargarArchivo.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        btCargarArchivo.setText("Cargar Archivo");
        btCargarArchivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCargarArchivoActionPerformed(evt);
            }
        });
        getContentPane().add(btCargarArchivo);
        btCargarArchivo.setBounds(180, 160, 140, 27);

        setSize(new java.awt.Dimension(491, 375));
        setLocationRelativeTo(null);
    }

    private void btEnviarActionPerformed(java.awt.event.ActionEvent evt) {
        this.enviarMensaje();
    }

    private void btCargarArchivoActionPerformed(java.awt.event.ActionEvent evt) {
        this.enviarArchivo();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PrincipalCli().setVisible(true);
            }
        });
    }

    private JButton btEnviar;
    private JButton btCargarArchivo;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JScrollPane jScrollPane1;
    private JTextArea mensajesTxt;
    private JTextField mensajeTxt;

    /**
     * Enviar un mensaje de texto al servidor
     */
    private void enviarMensaje() {
        String ip = "127.0.0.1";
        String mensaje = mensajeTxt.getText();
        if (mensaje.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay mensaje para enviar");
            return;
        }

        DatagramPacket mensajeDG = MiDatagrama.crearDataG(ip, PORT, mensaje);
        try {
            DatagramSocket canal = new DatagramSocket();
            // Enviar mensaje al servidor
            canal.send(mensajeDG);
            mensajesTxt.append("Mensaje enviado: " + mensaje + "\n");

            // Preparar para recibir la respuesta del servidor
            byte[] buffer = new byte[1000];
            DatagramPacket respuesta = new DatagramPacket(buffer, buffer.length);

            // Esperar la respuesta del servidor
            canal.receive(respuesta);

            // Obtener el mensaje del servidor
            String mensajeRespuesta = new String(respuesta.getData(), 0, respuesta.getLength()).trim();

            // Mostrar la respuesta en la pantalla
            mensajesTxt.append("Mensaje recibido desde el servidor: " + mensajeRespuesta + "\n");

            // Cerrar el socket
            canal.close();

        } catch (SocketException ex) {
            Logger.getLogger(PrincipalCli.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PrincipalCli.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Seleccionar y enviar un archivo al servidor
     */
    private void enviarArchivo() {
        String ip = "127.0.0.1";
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            try {
                DatagramSocket socket = new DatagramSocket();

                // Notificar al servidor que se va a enviar un archivo
                String mensajeInicial = "FILE:" + archivo.getName();
                DatagramPacket mensajeDG = MiDatagrama.crearDataG(ip, PORT, mensajeInicial);
                socket.send(mensajeDG);
                mensajesTxt.append("Enviando archivo: " + archivo.getName() + "\n");

                // Enviar el archivo en bloques
                FileInputStream fis = new FileInputStream(archivo);
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    DatagramPacket archivoPacket = new DatagramPacket(buffer, bytesRead, InetAddress.getByName(ip), PORT);
                    socket.send(archivoPacket);
                }

                fis.close();
                mensajesTxt.append("Archivo enviado correctamente.\n");

                // Recibir confirmación del servidor
                byte[] bufferRespuesta = new byte[1000];
                DatagramPacket respuesta = new DatagramPacket(bufferRespuesta, bufferRespuesta.length);
                socket.receive(respuesta);

                String mensajeRespuesta = new String(respuesta.getData(), 0, respuesta.getLength()).trim();
                mensajesTxt.append("Respuesta del servidor: " + mensajeRespuesta + "\n");

                socket.close();

            } catch (SocketException ex) {
                Logger.getLogger(PrincipalCli.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PrincipalCli.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            mensajesTxt.append("No se seleccionó ningún archivo.\n");
        }
    }
}
