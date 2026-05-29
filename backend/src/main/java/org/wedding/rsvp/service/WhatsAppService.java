package org.wedding.rsvp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.wedding.rsvp.entity.GuestEntity;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WhatsAppService {

    @Value("${app.whatsapp.enabled}")
    private boolean enabled;

    @Value("${app.whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${app.whatsapp.access-token}")
    private String accessToken;

    @Value("${app.whatsapp.template-name}")
    private String templateName;

    @Value("${app.whatsapp.language-code}")
    private String languageCode;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendInvite(GuestEntity guest, String inviteUrl) {
        if (!enabled) {
            return;
        }

        if (guest.getPhone() == null || guest.getPhone().isBlank()) {
            throw new IllegalArgumentException("Telefono invitato mancante");
        }

        if (phoneNumberId == null || phoneNumberId.isBlank()
                || accessToken == null || accessToken.isBlank()
                || templateName == null || templateName.isBlank()) {
            throw new IllegalStateException("Configurazione WhatsApp Cloud API incompleta");
        }

        String to = guest.getPhone().replaceAll("[^0-9]", "");
        String url = "https://graph.facebook.com/v20.0/" + phoneNumberId + "/messages";

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "template",
                "template", Map.of(
                        "name", templateName,
                        "language", Map.of("code", languageCode),
                        "components", new Object[]{
                                Map.of(
                                        "type", "body",
                                        "parameters", new Object[]{
                                                Map.of("type", "text", "text", guest.getName()),
                                                Map.of("type", "text", "text", inviteUrl)
                                        }
                                )
                        }
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
    }
}
