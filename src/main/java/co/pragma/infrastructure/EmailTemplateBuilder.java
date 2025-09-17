package co.pragma.infrastructure;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class EmailTemplateBuilder {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    public static String buildResultadoTemplate(String decision, List<String[]> plan) {
        StringBuilder html = new StringBuilder();

        html.append("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f5f5f5;
                    }
                    .email-container {
                        background: white;
                        border-radius: 10px;
                        box-shadow: 0 4px 15px rgba(0,0,0,0.1);
                        overflow: hidden;
                    }
                    .header {
                        padding: 30px;
                        text-align: center;
                        color: white;
                    }
                    .header.approved {
                        background: linear-gradient(135deg, #4CAF50, #45a049);
                    }
                    .header.rejected {
                        background: linear-gradient(135deg, #f44336, #d32f2f);
                    }
                    .logo {
                        font-size: 2em;
                        font-weight: bold;
                        margin-bottom: 10px;
                    }
                    .title {
                        font-size: 1.4em;
                        margin-bottom: 10px;
                    }
                    .content {
                        padding: 30px;
                    }
                    .status-badge {
                        display: inline-block;
                        padding: 10px 20px;
                        border-radius: 25px;
                        font-weight: bold;
                        margin: 15px 0;
                        text-transform: uppercase;
                        letter-spacing: 1px;
                    }
                    .approved { background: #4CAF50; color: white; }
                    .rejected { background: #f44336; color: white; }
                    .plan-table {
                        width: 100%;
                        border-collapse: collapse;
                        margin: 25px 0;
                        border-radius: 8px;
                        overflow: hidden;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .plan-table th {
                        background: #667eea;
                        color: white;
                        padding: 12px;
                        text-align: center;
                        font-weight: 600;
                    }
                    .plan-table td {
                        padding: 10px 12px;
                        text-align: center;
                        border-bottom: 1px solid #eee;
                    }
                    .plan-table tr:nth-child(even) { background-color: #f9f9f9; }
                    .plan-table tr:hover { background-color: #f0f8ff; }
                    .footer {
                        background: #f8f9fa;
                        padding: 25px;
                        text-align: center;
                        border-top: 1px solid #dee2e6;
                        color: #666;
                    }
                    .contact-info {
                        margin-top: 15px;
                        padding: 15px;
                        background: #e8f5e8;
                        border-radius: 8px;
                        border-left: 4px solid #4CAF50;
                    }
                    @media (max-width: 600px) {
                        body { padding: 10px; }
                        .content, .footer { padding: 20px; }
                        .plan-table { font-size: 0.9em; }
                        .plan-table th, .plan-table td { padding: 8px 6px; }
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
            """);

        if ("APROBADA".equals(decision)) {
            html.append("""
                <div class="header approved">
                    <div class="logo">💳 CrediYa</div>
                    <div class="title">¡Felicitaciones! 🎉</div>
                    <div>Tu solicitud de crédito ha sido aprobada</div>
                </div>
                """);
        } else {
            html.append("""
                <div class="header rejected">
                    <div class="logo">💳 CrediYa</div>
                    <div class="title">Respuesta a tu solicitud</div>
                    <div>Resultado de tu solicitud de crédito</div>
                </div>
                """);
        }

        html.append("<div class=\"content\">");

        if ("APROBADA".equals(decision)) {
            html.append("""
                <div class="status-badge approved">✓ APROBADA</div>
                <p>Nos complace informarte que tu solicitud de crédito ha sido <strong>aprobada exitosamente</strong>.</p>
                """);

            if (plan != null && !plan.isEmpty()) {
                html.append("""
                    <h3 style="color: #4CAF50; margin-top: 25px;">📊 Plan de Pagos</h3>
                    <table class="plan-table">
                        <thead>
                            <tr>
                                <th>Mes</th>
                                <th>Cuota</th>
                                <th>Capital</th>
                                <th>Interés</th>
                                <th>Saldo</th>
                            </tr>
                        </thead>
                        <tbody>
                    """);

                for (String[] fila : plan) {
                    html.append("<tr>");
                    for (int i = 0; i < fila.length; i++) {
                        html.append("<td>");
                        if (i == 0) {
                            html.append("<strong>").append(fila[i]).append("</strong>");
                        } else {
                            try {
                                double valor = Double.parseDouble(fila[i].replace(",", ""));
                                html.append(CURRENCY_FORMAT.format(valor));
                            } catch (NumberFormatException e) {
                                html.append(fila[i]);
                            }
                        }
                        html.append("</td>");
                    }
                    html.append("</tr>");
                }

                html.append("""
                        </tbody>
                    </table>
                    """);
            }


        } else {
            html.append("""
                <div class="status-badge rejected">✗ NO APROBADA</div>
                <p>Lamentamos informarte que en esta ocasión <strong>no hemos podido aprobar</strong> tu solicitud de crédito.</p>                
                """
            );
        }

        html.append("</div>");

        html.append("""
            <div class="footer">
                <p><strong>Gracias por confiar en CrediYa</strong></p>
                <p>Si tienes dudas, no dudes en contactarnos.</p>
                <p style="margin-top: 20px;"><strong>Atentamente,<br>El equipo de CrediYa</strong></p>                
            </div>
            """
        );

        html.append("""
                </div>
            </body>
            </html>
            """
        );

        return html.toString();
    }
}