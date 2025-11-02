package email;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Clase para enviar correos electrónicos
 */
public class EmailSender {
    
    /**
     * Configura las propiedades del servidor SMTP
     * Usa puerto 25 sin TLS (como telnet)
     */
    private Properties getMailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", EmailConfig.SMTP_HOST);
        props.put("mail.smtp.port", "25");
        props.put("mail.smtp.auth", "false"); // Sin autenticación compleja
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.ssl.enable", "false");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        
        return props;
    }
    
    /**
     * Envía un correo electrónico usando SMTP simple
     */
    public boolean enviarCorreo(String destinatario, String asunto, String cuerpo) {
        try {
            Properties props = getMailProperties();
            
            // Crear sesión SIN autenticación
            Session session = Session.getInstance(props);
            session.setDebug(true); // Ver qué pasa
            
            // Crear mensaje
            Message mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(EmailConfig.EMAIL_ADDRESS));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            
            // Enviar
            Transport.send(mensaje);
            
            System.out.println("   📤 Respuesta enviada a: " + destinatario);
            return true;
            
        } catch (MessagingException e) {
            System.err.println("   ❌ Error al enviar correo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
