package com.example.NotariaPro.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    public void enviarTurno(String emailDestino, String numeroTurno, String nombre,
                             String tipoRegistro, String tipoPersona) {
        try {
            String tramite = switch (tipoRegistro) {
                case "RAPIDO"     -> "Autenticaciones / Firma";
                case "INTERMEDIO" -> "Registros Civiles";
                case "COMPLEJO"   -> "Escrituras / Sucesiones";
                default           -> tipoRegistro;
            };

            String prioridad = switch (tipoPersona) {
                case "PRIORITARIO"  -> "Alta (Embarazo / Discapacidad)";
                case "ADULTO_MAYOR" -> "Media (Adulto Mayor)";
                default             -> "Regular";
            };

            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(emailDestino);
            helper.setSubject("🏛️ Notaría Pro — Tu turno es " + numeroTurno);

            String cuerpo = """
                    <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto;
                                border: 1px solid #ddd; border-radius: 10px; overflow: hidden;">

                      <div style="background: #1F4E79; padding: 20px; text-align: center;">
                        <h1 style="color: white; margin: 0;">🏛️ Notaría Pro</h1>
                        <p style="color: #cde; margin: 5px 0 0;">Sistema Inteligente de Turnos</p>
                      </div>

                      <div style="padding: 30px;">
                        <p style="font-size: 16px;">Hola, <strong>%s</strong> 👋</p>
                        <p>Tu turno ha sido registrado exitosamente.</p>

                        <div style="background: #1F4E79; border-radius: 8px; padding: 20px;
                                    text-align: center; margin: 20px 0;">
                          <p style="color: #cde; margin: 0; font-size: 13px;">CÓDIGO DE TURNO</p>
                          <h1 style="color: white; font-size: 48px; margin: 5px 0;">%s</h1>
                        </div>

                        <table style="width: 100%%; border-collapse: collapse;">
                          <tr>
                            <td style="padding: 10px; background: #f5f5f5; border-radius: 6px;
                                        width: 48%%; margin-right: 4%%;">
                              <p style="margin: 0; color: #888; font-size: 12px;">📋 Trámite</p>
                              <p style="margin: 4px 0 0; font-weight: bold;">%s</p>
                            </td>
                            <td style="width: 4%%"></td>
                            <td style="padding: 10px; background: #f5f5f5; border-radius: 6px; width: 48%%;">
                              <p style="margin: 0; color: #888; font-size: 12px;">⭐ Prioridad</p>
                              <p style="margin: 4px 0 0; font-weight: bold;">%s</p>
                            </td>
                          </tr>
                        </table>

                        <p style="margin-top: 24px; color: #555;">
                          Por favor permanezca atento a las pantallas de la sala de espera.
                          Lo llamaremos cuando sea su turno.
                        </p>
                        <p style="color: #555;">¡Gracias por usar Notaría Pro! ✅</p>
                      </div>

                      <div style="background: #f0f0f0; padding: 12px; text-align: center;">
                        <p style="margin: 0; color: #999; font-size: 12px;">
                          Este es un mensaje automático, por favor no responda.
                        </p>
                      </div>
                    </div>
                    """.formatted(nombre, numeroTurno, tramite, prioridad);

            helper.setText(cuerpo, true); // true = HTML

            mailSender.send(mensaje);
            System.out.println("✅ Email enviado a " + emailDestino + " — turno " + numeroTurno);

        } catch (Exception e) {
            System.err.println("⚠️  Email no enviado a " + emailDestino + ": " + e.getMessage());
        }
    }
}
