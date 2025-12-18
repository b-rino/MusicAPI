# 🏨 MusicAPI – Backend REST API

MusicAPI er et REST API bygget med fokus på arkitektur, sikkerhed, DevOps-automatisering. Projektet er udviklet som backend-only og deployet via CI/CD til en VPS med Docker og Caddy.

## 🚀 Teknologier

- **Javalin** – Letvægts Java web framework med indbygget rollebaseret adgangskontrol via `RouteRole`
- **Hibernate** – ORM til effektiv databasehåndtering med PostgreSQL
- **Lombok** – Reduktion af boilerplate via annoteringer som `@Getter`, `@Builder`, `@AllArgsConstructor`
- **JWT (nimbus-jose-jwt)** – Token-baseret autentificering
- **RestAssured + Hamcrest** – Integrationstest med præcise assertions
- **Docker + GitHub Actions + Watchtower** – CI/CD pipeline med automatisk container-opdatering
- **Caddy** – Reverse proxy med automatisk HTTPS og routing


## 🔐 Fokuspunkter i opgaven

- **Rolle-baseret autorisation** via Javalins `RouteRoles`
- **Token-baseret autentificering** via `JWT`
- **Ekstern API** `Deezer` som integreres i eget API
- **Global exception handling** med strukturerede JSON-fejlbeskeder
- **Global logging** af både requests og responses, inkl. maskering af følsomme felter
- **CI/CD pipeline** der automatisk bygger og deployer


## 🧭 Arkitekturoversigt

MusicAPI følger en klassisk lagdelt struktur:

- **Controller** – Modtager og validerer requests
- **Service** – Indeholder forretningslogik og adgangskontrol
- **DAO (Hibernate)** – Håndterer databaseoperationer

JWT-token verificeres i middleware og adgang håndhæves via `RouteRole`.


## 🌐 Endpoints og adgang

MusicAPI er live og tilgængelig via følgende base-URL:

**https://music.brino.dk/api/v1**

> Alle beskyttede endpoints kræver `Authorization: Bearer <token>` i headeren.  
> Ruteoversigt: [music.brino.dk/api/v1/routes](https://music.brino.dk/api/v1/routes)

---

### 🔑 Autentificering

| Metode | Endpoint       | Beskrivelse                                               | Rolle  |
|--------|----------------|-----------------------------------------------------------|--------|
| POST   | `/login`       | Login med brugernavn og adgangskode. Returnerer JWT-token | Alle   |
| POST   | `/register`    | Opret ny bruger. Returnerer succesbesked                  | Alle   |
| GET    | `/healthcheck` | Simpel status-check af API                                | Alle   |

---

### 🎵 Sang & søgning

| Metode | Endpoint                      | Beskrivelse                           | Rolle  |
|--------|-------------------------------|---------------------------------------|--------|
| GET    | `/songs/search?query=...`     | Søg efter sange via eksternt API      | Alle   |
| GET    | `/songs`                      | Hent alle sange i systemet            | Admin  |

---

### 📁 Playlists

| Metode | Endpoint                                 | Beskrivelse                          | Rolle  |
|--------|------------------------------------------|--------------------------------------|--------|
| POST   | `/playlists`                             | Opret ny playlist                    | User   |
| GET    | `/playlists`                             | Hent alle brugerens playlister       | User   |
| PUT    | `/playlists/{id}`                        | Opdater playlist-navn                | User   |
| DELETE | `/playlists/{id}`                        | Slet brugerens playlist              | User   |
| POST   | `/playlists/{id}/songs`                  | Tilføj sang til playlist             | User   |
| GET    | `/playlists/{id}/songs`                  | Hent sange i playlist                | User   |
| DELETE | `/playlists/{playlistId}/songs/{songId}` | Fjern sang fra playlist              | User   |

---

### 🛡️ Rolle & brugeradministration

| Metode | Endpoint                          | Beskrivelse                          | Rolle  |
|--------|-----------------------------------|--------------------------------------|--------|
| GET    | `/admin/users`                    | Hent alle brugere                    | Admin  |
| DELETE | `/admin/users/{username}`         | Slet bruger                          | Admin  |
| PATCH  | `/users/{username}/role`          | Tildel rolle til bruger              | Admin  |

---

### 🧭 Ruteoversigt

| Metode | Endpoint   | Beskrivelse                     | Rolle  |
|--------|------------|---------------------------------|--------|
| GET    | `/routes`  | Hent oversigt over alle ruter   | Alle   |



## 📦 Deployment Flow

1. Push til `main` trigger GitHub Actions
2. Docker image bygges og pushes til Docker Hub
3. Watchtower på droplet detekterer nyt image og opdaterer container
4. Caddy reverse proxy håndterer HTTPS og routing


## 🧪 Test og kvalitet

MusicAPI er testet med **RestAssured** og **Hamcrest** som integrationstests, der validerer både funktionalitet og fejlhåndtering. Testene er skrevet med fokus på klarhed, robusthed og reviewer-venlighed, og dækker både succesfulde kald og negative scenarier.

### ✅ Dækkede områder

- **Autentificering og token-flow**
    - Login og token-generering
    - Token-validering: gyldig, udløbet, forkert signatur, malformeret og manglende token
    - Adgang til beskyttede endpoints med gyldig token
    - Fejl ved adgang med slettet bruger

- **Registrering**
    - Gyldig brugeroprettelse
    - Duplikat-brugernavn og tomme felter

- **Rollebaseret adgang**
    - `User` og `Admin` adgang til endpoints
    - Fejl ved adgang til endpoints uden korrekt rolle
    - Autorisationsfejl med korrekte statuskoder(401, 403)

- **Admin endpoints**
    - Hentning og sletning af brugere
    - Fejl ved sletning af sig selv eller ikke-eksisterende brugere
    - Tildeling af roller og håndtering af ugyldige roller

- **Playlist endpoints**
    - Oprettelse, opdatering og sletning af playlister
    - Tilføjelse og fjernelse af sange
    - Hentning af brugerens playlister og tilknyttede sange
    - Fejl ved manglende navn, dubletter og adgang til andres playlister

- **Ekstern søgning**
    - Søgning efter sange via eksternt API
    - Fejl ved manglende query-parameter

- **Fejlhåndtering**
    - Global exception handler med struktureret JSON-output
    - Autorisationsfejl med korrekte statuskoder (`400`, `401`, `403`, `404`)

## 📚 JSON-struktur på fejlmeddelelser (exceptions)
`````
{
  "error": "Access Denied",
  "message": "You do not own this playlist",
  "path": "/api/v1/playlists/2/songs",
  "method": "GET"
}
`````````


## 🔗 Projektlink

- Live API: [music.brino.dk](https://music.brino.dk/api/v1/routes)
- GitHub-repo: [github.com/b-rino/MusicAPI](https://github.com/b-rino/MusicAPI)
- Portfolio: [brino.dk](https://brino.dk)


## ⚠️ Disclaimer

Dette projekt er et backend-demo og indeholder ingen følsomme data.  
Alle brugere, tokens og credentials er testdata og kun til udviklingsformål.  
Secrets som `SECRET_KEY` og databaseadgang håndteres via miljøvariabler og er ikke inkluderet i koden eller repository.  
Live API på `music.brino.dk` er beskyttet og rate-limited, og bør kun bruges til test og demonstration.





