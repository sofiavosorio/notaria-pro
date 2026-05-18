package com.example.NotariaPro.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.whatsapp-from}")
    private String whatsappFrom;

    public void enviarTurno(String telefono, String numeroTurno, String nombre,
                             String tipoRegistro, String tipoPersona) {
        try {
            Twilio.init(accountSid, authToken);

            String tramite = switch (tipoRegistro) {
                case "RAPIDO"     -> "Autenticaciones / Firma";
                case "INTERMEDIO" -> "Registros Civiles";
                case "COMPLEJO"   -> "Escrituras / Sucesiones";
                default           -> tipoRegistro;
            };

            String prioridad = switch (tipoPersona) {
                case "PRIORITARIO"  -> "Alta (Embarazo/Discapacidad)";
                case "ADULTO_MAYOR" -> "Media (Adulto Mayor)";
                default             -> "Regular";
            };

            // Número colombiano: si el usuario ingresó 10 dígitos, agregamos +57
            String destino = telefono.startsWith("+") ? telefono : "+57" + telefono;

            String mensaje = String.format(
                "🏛️ *Notaría Pro* — Turno Asignado\n\n" +
                "Hola *%s*, su turno ha sido registrado exitosamente.\n\n" +
                "📋 *Número de turno:* %s\n" +
                "📁 *Trámite:* %s\n" +
                "⭐ *Prioridad:* %s\n\n" +
                "Por favor permanezca atento a las pantallas de la sala de espera.\n" +
                "Gracias por usar Notaría Pro. ✅",
                nombre, numeroTurno, tramite, prioridad
            );

            Message.creator(
                new PhoneNumber("whatsapp:" + destino),
                new PhoneNumber(whatsappFrom),
                mensaje
            ).create();

        } catch (Exception e) {
            // No interrumpir el flujo si WhatsApp falla
            System.err.println("⚠️  WhatsApp no enviado a " + telefono + ": " + e.getMessage());
        }
    }
}
