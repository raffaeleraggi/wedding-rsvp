# Wedding RSVP Starter

MVP per partecipazioni di nozze digitali.

## Struttura

```txt
wedding-rsvp-starter/
 ├── backend/   Spring Boot + SQLite
 ├── frontend/  React + Vite
 └── docker-compose.yml
```

## Backend

Requisiti:
- Java 21
- Maven oppure Maven Wrapper

Avvio:

```powershell
cd backend
mvn spring-boot:run
```

Se hai Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

API principali:

```txt
GET  /api/admin/stats
GET  /api/admin/guests
POST /api/admin/guests
POST /api/admin/guests/import-csv
POST /api/admin/guests/{id}/send-whatsapp
GET  /api/invite/{token}
POST /api/invite/{token}/reply
```

## SQLite

Il file viene creato automaticamente:

```txt
backend/wedding-rsvp.db
```

Aprilo con DBeaver usando connessione SQLite.

## CSV import

File esempio:

```txt
backend/guests-example.csv
```

Colonne:

```csv
name,surname,email,phone,numberOfPeople
```

## Frontend

```powershell
cd frontend
npm install
npm run dev
```

Pagine:

```txt
http://localhost:5173/admin
http://localhost:5173/i/{token}
```

## Docker Compose

```powershell
docker compose up --build
```

Frontend:

```txt
http://localhost:5173/admin
```

Backend:

```txt
http://localhost:8080
```

## WhatsApp

Di default `WHATSAPP_ENABLED=false`.

In questa modalità il sistema genera link WhatsApp cliccabili:

```txt
https://wa.me/...
```

Per l'invio automatico reale con WhatsApp Cloud API devi configurare:

```txt
WHATSAPP_ENABLED=true
WHATSAPP_PHONE_NUMBER_ID=...
WHATSAPP_ACCESS_TOKEN=...
WHATSAPP_TEMPLATE_NAME=...
WHATSAPP_LANGUAGE_CODE=it
```

Nota: WhatsApp Business richiede template approvati per invii proattivi.
