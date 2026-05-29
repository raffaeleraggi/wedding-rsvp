package org.wedding.rsvp.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wedding.rsvp.entity.GuestEntity;
import org.wedding.rsvp.entity.RsvpStatus;
import org.wedding.rsvp.repository.GuestRepository;

import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestPdfService {

    private final GuestRepository guestRepository;

    public byte[] exportGuestsPdf() {
        List<GuestEntity> guests = guestRepository.findAll().stream().sorted(Comparator
                .comparing(GuestEntity::getSurname,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(GuestEntity::getName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))).toList();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font font16 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font font14 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);

            Paragraph title = new Paragraph("Lista invitati matrimonio", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 2, 2, 2});

            addHeader(table, "Nome");
            addHeader(table, "Cognome");
            addHeader(table, "Stato");
            addHeader(table, "Persone aggiuntive");

            for (GuestEntity guest : guests) {
                table.addCell(value(guest.getName()));
                table.addCell(value(guest.getSurname()));
                table.addCell(guest.getStatus() != null ? guest.getStatus().name() : "");
                table.addCell(guest.getAdditionalGuests() != null ? guest.getAdditionalGuests().toString() : "1");
            }

            table.setSpacingAfter(20);
            document.add(table);

            List<GuestEntity> confermati = guests.stream()
                    .filter(g -> g.getStatus().equals(RsvpStatus.CONFERMATO)).toList();

            long additionalGuests = guestRepository.sumAdditionalGuestsByStatus(RsvpStatus.CONFERMATO);

            Paragraph totalConfirmed = new Paragraph("Totale confermati: "+String.valueOf(confermati.size() + additionalGuests), font16);

            totalConfirmed.setAlignment(Element.ALIGN_RIGHT);

            document.add(totalConfirmed);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Errore durante export PDF invitati", e);
        }
    }

    private void addHeader(PdfPTable table, String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Phrase phrase = new Phrase(text, font);
        table.addCell(phrase);
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}