package email;

import commands.CommandParser;
import commands.CommandExecutor;
import commands.CommandResult;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;
import java.util.Properties;

/**
 * Clase para leer correos electrónicos desde el servidor
 */
public class EmailReader {
    
    private Store store;
    private Folder inbox;
    
    /**
     * Configura la conexión al servidor de correo
     * Usa POP3 puerto 110 (como telnet) sin SSL/TLS
     */
    private Properties getMailProperties() {
        Properties props = new Properties();
        props.put("mail.store.protocol", "pop3");
        props.put("mail.pop3.host", EmailConfig.IMAP_HOST);
        props.put("mail.pop3.port", "110");
        props.put("mail.pop3.auth", "false"); // Desactivar auth automático
        props.put("mail.pop3.starttls.enable", "false");
        props.put("mail.pop3.ssl.enable", "false");
        props.put("mail.pop3.socketFactory.fallback", "true");
        props.put("mail.pop3.connectiontimeout", "10000");
        props.put("mail.pop3.timeout", "10000");
        return props;
    }
    
    /**
     * Conecta al servidor de correo usando POP3 simple
     */
    private void conectar() throws MessagingException {
        Properties props = getMailProperties();
        Session session = Session.getInstance(props, null);
        session.setDebug(true); // Activar debug para ver qué pasa
        
        store = session.getStore("pop3");
        
        // Conectar con usuario sin dominio (grupo08sc en lugar de grupo08sc@tecnoweb.org.bo)
        String username = EmailConfig.EMAIL_ADDRESS.split("@")[0]; // "grupo09sc"
        store.connect(EmailConfig.IMAP_HOST, 110, username, EmailConfig.EMAIL_PASSWORD);
        
        inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
    }
    
    /**
     * Desconecta del servidor de correo
     */
    private void desconectar() {
        try {
            if (inbox != null && inbox.isOpen()) {
                inbox.close(true); // true = aplicar cambios (eliminar mensajes marcados)
            }
            if (store != null && store.isConnected()) {
                store.close();
            }
        } catch (MessagingException e) {
            System.err.println("Error al desconectar: " + e.getMessage());
        }
    }
    
    /**
     * Prueba la conexión al servidor de correo
     */
    public boolean testConnection() {
        try {
            conectar();
            desconectar();
            return true;
        } catch (MessagingException e) {
            System.err.println("Error de conexión email: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Procesa todos los correos no leídos
     */
    public void procesarCorreosPendientes() {
        try {
            conectar();
            
            // Buscar correos no leídos
            Message[] mensajes = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            
            if (mensajes.length == 0) {
                System.out.println("   No hay correos nuevos.");
                return;
            }
            
            System.out.println("   📬 " + mensajes.length + " correo(s) nuevo(s) encontrado(s).");
            
            for (Message mensaje : mensajes) {
                procesarMensaje(mensaje);
            }
            
        } catch (MessagingException e) {
            System.err.println("❌ Error al procesar correos: " + e.getMessage());
            e.printStackTrace();
        } finally {
            desconectar();
        }
    }
    
    /**
     * Procesa un mensaje individual
     */
    private void procesarMensaje(Message mensaje) {
        try {
            // Obtener información del correo
            String remitente = obtenerRemitente(mensaje);
            String asunto = mensaje.getSubject();
            
            System.out.println("\n   ┌─────────────────────────────────────────────");
            System.out.println("   │ 📧 Nuevo correo recibido");
            System.out.println("   ├─────────────────────────────────────────────");
            System.out.println("   │ De:      " + remitente);
            System.out.println("   │ Asunto:  " + asunto);
            System.out.println("   └─────────────────────────────────────────────");
            
            // Parsear el comando del asunto
            CommandParser parser = new CommandParser(asunto);
            
            if (!parser.isValid()) {
                System.out.println("   ❌ Comando inválido: " + parser.getError());
                enviarRespuestaError(remitente, asunto, parser.getError());
                marcarComoLeido(mensaje);
                return;
            }
            
            // Ejecutar el comando
            System.out.println("   ⚙️  Ejecutando comando: " + parser.getComando());
            CommandExecutor executor = new CommandExecutor();
            CommandResult resultado = executor.ejecutar(parser);
            
            // Enviar respuesta
            if (resultado.isExito()) {
                System.out.println("   ✅ Comando ejecutado exitosamente");
                enviarRespuestaExito(remitente, asunto, resultado);
            } else {
                System.out.println("   ❌ Error al ejecutar comando: " + resultado.getMensaje());
                enviarRespuestaError(remitente, asunto, resultado.getMensaje());
            }
            
            // Marcar como leído
            marcarComoLeido(mensaje);
            
        } catch (Exception e) {
            System.err.println("   ❌ Error procesando mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Obtiene el remitente del mensaje
     */
    private String obtenerRemitente(Message mensaje) throws MessagingException {
        Address[] from = mensaje.getFrom();
        if (from != null && from.length > 0) {
            InternetAddress address = (InternetAddress) from[0];
            return address.getAddress();
        }
        return "desconocido@unknown.com";
    }
    
    /**
     * Marca un mensaje como leído
     */
    private void marcarComoLeido(Message mensaje) throws MessagingException {
        // POP3 no soporta SEEN flag, eliminamos el mensaje del servidor
        mensaje.setFlag(Flags.Flag.DELETED, true);
    }
    
    /**
     * Envía respuesta de éxito
     */
    private void enviarRespuestaExito(String destinatario, String asuntoOriginal, CommandResult resultado) {
        EmailSender sender = new EmailSender();
        
        String asunto = "RE: " + asuntoOriginal + " - ✅ ÉXITO";
        
        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("╔════════════════════════════════════════════════════════════╗\n");
        cuerpo.append("║           SISTEMA TRANS VELASCO - GRUPO09SC               ║\n");
        cuerpo.append("║                  OPERACIÓN EXITOSA                         ║\n");
        cuerpo.append("╚════════════════════════════════════════════════════════════╝\n\n");
        cuerpo.append("✅ Estado: ÉXITO\n\n");
        cuerpo.append("📋 Comando ejecutado: ").append(asuntoOriginal).append("\n\n");
        cuerpo.append("📊 Resultado:\n");
        cuerpo.append("────────────────────────────────────────────────────────────\n");
        cuerpo.append(resultado.getDatos()).append("\n");
        cuerpo.append("────────────────────────────────────────────────────────────\n\n");
        cuerpo.append("💬 Mensaje: ").append(resultado.getMensaje()).append("\n\n");
        cuerpo.append("🕐 Fecha y hora: ").append(java.time.LocalDateTime.now()).append("\n\n");
        cuerpo.append("──────────────────────────────────────────────────────────\n");
        cuerpo.append("Sistema de Seguimiento de Paquetes - Trans Velasco\n");
        cuerpo.append("Email: grupo09sc@tecnoweb.org.bo\n");
        
        sender.enviarCorreo(destinatario, asunto, cuerpo.toString());
    }
    
    /**
     * Envía respuesta de error
     */
    private void enviarRespuestaError(String destinatario, String asuntoOriginal, String mensajeError) {
        EmailSender sender = new EmailSender();
        
        String asunto = "RE: " + asuntoOriginal + " - ❌ ERROR";
        
        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("╔════════════════════════════════════════════════════════════╗\n");
        cuerpo.append("║           SISTEMA TRANS VELASCO - GRUPO09SC               ║\n");
        cuerpo.append("║                      ERROR                                 ║\n");
        cuerpo.append("╚════════════════════════════════════════════════════════════╝\n\n");
        cuerpo.append("❌ Estado: ERROR\n\n");
        cuerpo.append("📋 Comando recibido: ").append(asuntoOriginal).append("\n\n");
        cuerpo.append("⚠️  Error detectado:\n");
        cuerpo.append("────────────────────────────────────────────────────────────\n");
        cuerpo.append(mensajeError).append("\n");
        cuerpo.append("────────────────────────────────────────────────────────────\n\n");
        cuerpo.append("📖 Formato correcto de comandos:\n\n");
        cuerpo.append("  COMANDO[\"param1\",\"param2\",...]\n\n");
        cuerpo.append("Ejemplos:\n");
        cuerpo.append("  • LISTUSU[\"*\"] - Listar todos los usuarios\n");
        cuerpo.append("  • LISTPAQ[\"EN_TRANSITO\"] - Listar paquetes en tránsito\n");
        cuerpo.append("  • INSUSU[\"ci\",\"nombre\",\"apellido\",...] - Insertar usuario\n\n");
        cuerpo.append("🕐 Fecha y hora: ").append(java.time.LocalDateTime.now()).append("\n\n");
        cuerpo.append("──────────────────────────────────────────────────────────\n");
        cuerpo.append("Sistema de Seguimiento de Paquetes - Trans Velasco\n");
        cuerpo.append("Email: grupo09sc@tecnoweb.org.bo\n");
        
        sender.enviarCorreo(destinatario, asunto, cuerpo.toString());
    }
}
