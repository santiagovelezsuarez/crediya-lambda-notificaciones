package co.pragma.infrastructure;

import co.pragma.domain.EstadoSolicitudEvent;
import co.pragma.usecase.GenerarPlanPagosUseCase;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.ses.SesClient;
import java.util.List;

@Slf4j
public class NotificarClienteHandler implements RequestHandler<SQSEvent, Void> {

    private final ObjectMapper mapper;
    private final GenerarPlanPagosUseCase generarPlanPagos;
    private final SesEmailSender sesEmailSender;

    public NotificarClienteHandler() {
        this.mapper = new ObjectMapper();
        this.generarPlanPagos = new GenerarPlanPagosUseCase();
        this.sesEmailSender = new SesEmailSender(SesClient.create(), "santiagovelezsuarez@gmail.com");
    }

    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        log.info("Iniciando notificación de solicitud de prestamo");
        sqsEvent.getRecords().forEach(msg -> {
            try {
                EstadoSolicitudEvent solicitud = mapper.readValue(msg.getBody(), EstadoSolicitudEvent.class);

                log.info("Solicitud: {}, estado: {}", solicitud.codigoSolicitud(), solicitud.estado());

                List<String[]> plan = generarPlanPagos.execute(
                        solicitud.monto(),
                        solicitud.tasaInteres(),
                        solicitud.plazoEnMeses()
                );

                String subject = "📢 Actualización en tu solicitud de crédito";
                String html = EmailTemplateBuilder.buildResultadoTemplate(solicitud.estado(), plan);

                sesEmailSender.enviarCorreo(solicitud.emailCliente(), subject, html);

                log.info("Correo enviado a {}", solicitud.emailCliente());

            } catch (Exception e) {
                log.error("Error procesando mensaje: {}", e.getMessage());
            }
        });
        return null;
    }
}
