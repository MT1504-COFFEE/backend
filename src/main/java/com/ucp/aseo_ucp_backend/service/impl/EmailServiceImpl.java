package com.ucp.aseo_ucp_backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucp.aseo_ucp_backend.entity.Incident;
import com.ucp.aseo_ucp_backend.entity.User;
import com.ucp.aseo_ucp_backend.repository.UserRepository;
import com.ucp.aseo_ucp_backend.service.EmailService;

import lombok.RequiredArgsConstructor;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Value("${MAILGUN_DOMAIN}")
    private String mailgunDomain;

    @Value("${MAILGUN_API_KEY}")
    private String mailgunApiKey;


    @Async
    @Override
    public void sendNewIncidentNotification(Incident incident) {
        try {
            List<User> admins = userRepository.findAllByRole(User.Role.admin);
            if (admins.isEmpty()) {
                System.err.println("ALERTA: No se encontraron administradores para notificar el incidente.");
                return;
            }

            // --- INICIO DE LA CORRECCIÓN ---
            // NO unimos los correos. Se enviará uno por uno.
            // String adminEmails = admins.stream()... // LÍNEA ELIMINADA

            String bathroomName = incident.getBathroom().getName();
            String buildingName = incident.getBathroom().getBuilding() != null ?
                                  incident.getBathroom().getBuilding().getName() : "N/A";
            String reportedBy = incident.getReportedBy().getFullName();

            // El HTML se crea una sola vez
            String htmlContent = String.format(
                "<html><body>" +
                "<h2>Se ha reportado un nuevo incidente:</h2>" +
                "<p><strong>Título:</strong> %s</p>" +
                "<p><strong>Prioridad:</strong> %s</p>" +
                "<p><strong>Reportado por:</strong> %s</p>" +
                "<p><strong>Edificio:</strong> %s</p>" +
                "<p><strong>Baño:</strong> %s</p>" +
                "<p><strong>Descripción:</strong> %s</p>" +
                "<p>Por favor, ingrese al panel de administración para asignarlo.</p>" +
                "</body></html>",
                incident.getTitle(),
                incident.getPriority().name(),
                reportedBy,
                buildingName,
                bathroomName,
                incident.getDescription()
            );

            // Iteramos sobre cada admin y enviamos un correo individual
            for (User admin : admins) {
                if (admin.getEmail() != null && !admin.getEmail().isEmpty()) {
                    System.out.println("Enviando notificación de incidente a admin: " + admin.getEmail());
                    sendMailgunMessage(
                        "alertas@" + mailgunDomain,
                        admin.getEmail(), // Se envía solo a este admin
                        "¡Nuevo Incidente Reportado! - " + incident.getTitle(),
                        htmlContent
                    );
                }
            }
            // --- FIN DE LA CORRECCIÓN ---

        } catch (Exception e) {
            System.err.println("Error FATAL al enviar correo de 'nuevo incidente'. El incidente SÍ se guardó.");
            System.err.println("Revisa la configuración de Mailgun (API Key y Dominio).");
            System.err.println("Error de correo: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Async
    @Override
    public void sendIncidentAssignmentNotification(Incident incident, User assignedUser) {
        if (assignedUser.getEmail() == null) {
            System.err.println("Error: El usuario " + assignedUser.getFullName() + " no tiene email para notificar.");
            return;
        }

        try {
            String bathroomName = incident.getBathroom().getName();
            String buildingName = incident.getBathroom().getBuilding() != null ?
                                  incident.getBathroom().getBuilding().getName() : "N/A";
            String floor = incident.getBathroom().getFloor() != null ?
                           String.valueOf(incident.getBathroom().getFloor().getFloorNumber()) : "N/A";
            String time = incident.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm"));
            String firstPhotoUrl = getFirstPhotoUrl(incident.getPhotos());

            String htmlContent = String.format(
                "<html><body>" +
                "<h2>Hola %s, se te ha asignado un nuevo incidente:</h2>" +
                "<p><strong>Título:</strong> %s</p>" +
                "<p><strong>Prioridad:</strong> %s</p>" +
                "<p><strong>Reportado el:</strong> %s</p>" +
                "<hr>" +
                "<h3>Detalles de Ubicación:</h3>" +
                "<p><strong>Edificio:</strong> %s</p>" +
                "<p><strong>Piso:</strong> %s</p>" +
                "<p><strong>Baño:</strong> %s</p>" +
                "<hr>" +
                "<h3>Descripción del Problema:</h3>" +
                "<p>%s</p>" +
                "%s" + // Bloque de imagen
                "<p>Por favor, atiende este incidente y márcalo como resuelto en la aplicación cuando termines.</p>" +
                "</body></html>",
                assignedUser.getFullName(),
                incident.getTitle(),
                incident.getPriority().name(),
                time,
                buildingName,
                floor,
                bathroomName,
                incident.getDescription(),
                firstPhotoUrl.isEmpty() ? "<p><em>No se adjuntó imagen.</em></p>" :
                "<p><strong>Evidencia:</strong><br/><img src='" + firstPhotoUrl + "' alt='Evidencia del incidente' style='max-width: 400px; height: auto;' /></p>"
            );

            sendMailgunMessage(
                "asignaciones@" + mailgunDomain,
                assignedUser.getEmail(),
                "Nueva Tarea Asignada: " + incident.getTitle(),
                htmlContent
            );

        } catch (Exception e) {
            System.err.println("Error FATAL al enviar correo de 'asignación'. El incidente SÍ se asignó.");
            System.err.println("Error de correo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void sendPasswordResetLink(User user, String token) {
        try {
            String frontendUrl = System.getenv().getOrDefault("FRONTEND_URL", "http://localhost:3000");
            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            String htmlContent = String.format(
                "<html><body>" +
                "<h2>Hola %s,</h2>" +
                "<p>Solicitaste restablecer tu contraseña para AseoUCP.</p>" +
                "<p>Haz clic en el siguiente enlace para crear una nueva contraseña:</p>" +
                "<a href=\"%s\" style=\"background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;\">Restablecer Contraseña</a>" +
                "<p>Si no solicitaste esto, ignora este correo.</p>" +
                "<p>El enlace expirará en 1 hora.</p>" +
                "</body></html>",
                user.getFullName(),
                resetUrl
            );

            sendMailgunMessage(
                "soporte@" + mailgunDomain,
                user.getEmail(),
                "Restablece tu contraseña de AseoUCP",
                htmlContent
            );
        } catch (Exception e) {
            System.err.println("Error FATAL al enviar correo de 'restablecer contraseña'.");
            System.err.println("Error de correo: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void sendMailgunMessage(String from, String to, String subject, String html) {
        String apiUrl = "https://api.mailgun.net/v3/" + mailgunDomain + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth("api", mailgunApiKey);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("from", from);
        map.add("to", to);
        map.add("subject", subject);
        map.add("html", html);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Fallo al enviar correo: " + response.getBody());
        }

        System.out.println("Correo enviado exitosamente a: " + to);
    }

    private String getFirstPhotoUrl(String photosJson) {
        if (photosJson == null || photosJson.isEmpty() || photosJson.equals("[]")) {
            return "";
        }
        try {
            List<String> photoUrls = objectMapper.readValue(photosJson, new TypeReference<List<String>>() {});
            if (!photoUrls.isEmpty()) {
                return photoUrls.get(0);
            }
        } catch (Exception e) {
            System.err.println("Error al parsear JSON de fotos para email: " + e.getMessage());
        }
        return "";
    }
}