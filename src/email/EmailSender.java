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
     */
    private Properties getMailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", EmailConfig.SMTP_HOST);
        props.put("mail.smtp.port", EmailConfig.SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", EmailConfig.SMTP_HOST);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        
        return props;
    }
    
    /**
     * Envía un correo electrónico
     */
    public boolean enviarCorreo(String destinatario, String asunto, String cuerpo) {
        try {
            Properties props = getMailProperties();
            
            // Crear sesión con autenticación
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        EmailConfig.EMAIL_ADDRESS, 
                        EmailConfig.EMAIL_PASSWORD
                    );
                }
            });
            
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
