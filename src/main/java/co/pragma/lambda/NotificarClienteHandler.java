package co.pragma.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.ses.SesClient;

import java.math.BigDecimal;

public class NotificarClienteHandler implements RequestHandler<SQSEvent, Void> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final SesEmailSender sesEmailSender;

    private static final String EMAIL_BODY_TEMPLATE = """
        Hola %s,

        %s

        📌 Detalles de tu solicitud:
           - Ref.: %s
           - Monto: $%,.2f
           - Tasa: %,.2f
           - Estado: %s

        Gracias por confiar en nosotros. Si tienes dudas, no dudes en contactarnos.

        Atentamente,
        El equipo de CrediYa
        """;

    public NotificarClienteHandler() {
        String fromEmail = "santiagovelezsuarez@gmail.com";
        this.sesEmailSender = new SesEmailSender(SesClient.create(), fromEmail);
    }

    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        sqsEvent.getRecords().forEach(msg -> {
            try {
                EstadoSolicitudEvent solicitud = mapper.readValue(msg.getBody(), EstadoSolicitudEvent.class);

                String subject = "📢 Actualización de tu solicitud de crédito";
                BigDecimal monto = solicitud.monto();

                String estadoMensaje;
                switch (solicitud.estado().toUpperCase()) {
                    case "APROBADA" -> estadoMensaje = "¡Felicidades! 🎉 Tu solicitud de crédito ha sido APROBADA.";
                    case "RECHAZADA" -> estadoMensaje = "Lamentamos informarte que tu solicitud de crédito ha sido RECHAZADA.";
                    default -> estadoMensaje = "Tu solicitud de crédito ha sido actualizada al estado: " + solicitud.estado();
                }

                String body = EMAIL_BODY_TEMPLATE.formatted(
                        solicitud.nombreCliente() != null ? solicitud.nombreCliente() : "Cliente",
                        estadoMensaje,
                        solicitud.codigoSolicitud(),
                        monto != null ? monto : BigDecimal.ZERO,
                        solicitud.tasaInteres() != null ? solicitud.tasaInteres() : BigDecimal.ZERO,
                        solicitud.estado()
                );

                sesEmailSender.enviarCorreo(solicitud.emailCliente(), subject, body);

                context.getLogger().log("Correo enviado a " + solicitud.emailCliente());

            } catch (Exception e) {
                context.getLogger().log("Error procesando mensaje: " + e.getMessage());
            }
        });
        return null;
    }
}
