# 🏛️ Notaría Pro — Sistema Inteligente de Turnos

Sistema web de gestión de turnos con prioridades para notarías, desarrollado con **Spring Boot 4**, **MySQL** y **Angular 21**. Incluye algoritmo de envejecimiento (aging), ratio de equidad 3:1 y notificación del turno por **WhatsApp**.

---

## 📋 Descripción

Notaría Pro permite a los ciudadanos registrar un turno en línea y recibir su número de turno por WhatsApp. El operador gestiona la atención desde un panel de control que aplica reglas inteligentes de prioridad para garantizar equidad en el servicio.

### Tipos de usuario atendidos
| Tipo | Prioridad | Puntos |
|------|-----------|--------|
| Embarazo / Discapacidad | Alta | 3 |
| Adulto Mayor (+62 años) | Media | 2 |
| Público General | Regular | 1 |

### Tipos de trámite
| Código | Trámite | Puntos |
|--------|---------|--------|
| A-xxx | Autenticaciones / Firmas | 3 |
| C-xxx | Registros Civiles / Permisos | 2 |
| E-xxx | Escrituras / Sucesiones / Hipotecas | 1 |

---

## 🧠 Algoritmo de Selección

### Fórmula de Peso Final (Aging)
```
Peso Final = (Peso Persona + Peso Trámite) + (Minutos de Espera / 10)
```

### Reglas aplicadas en orden
1. **Prioridad** — mayor peso final primero
2. **FIFO** — ante empate, el más antiguo primero
3. **Aging** — el tiempo de espera aumenta el peso automáticamente
4. **Ratio 3:1** — tras 3 turnos prioritarios consecutivos, se fuerza uno regular (anti-inanición)

---

## ⚙️ Requisitos Técnicos

- Java JDK 17+
- Maven 3.8+
- MySQL 5.7+ / XAMPP
- Node.js 18+ y Angular CLI 17+
- Cuenta Twilio (gratuita) para WhatsApp

---

## 🚀 Instalación y Configuración

### 1. Clonar el repositorio
```bash
git clone https://github.com/tu-usuario/notaria-pro.git
cd notaria-pro
```

### 2. Crear la base de datos
```sql
CREATE DATABASE notaria_pro;
```
Asegúrate de que MySQL esté corriendo (XAMPP → Start MySQL).

### 3. Configurar el backend
Edita `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/notaria_pro?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=

# Twilio WhatsApp
twilio.account-sid=TU_ACCOUNT_SID
twilio.auth-token=TU_AUTH_TOKEN
twilio.whatsapp-from=whatsapp:+14155238886
```

### 4. Cargar datos de prueba (opcional)
```bash
mysql -u root notaria_pro < seed_turnos.sql
```

### 5. Ejecutar el backend
```bash
mvn spring-boot:run
```
O desde **NetBeans**: clic derecho en el proyecto → **Run**.

El servidor inicia en: `http://localhost:8080`

### 6. Ejecutar el frontend
```bash
cd notaria-pro-frontend
npm install
ng serve
```
La aplicación inicia en: `http://localhost:4200`

---

## 📱 Configurar WhatsApp (Twilio Sandbox)

1. Crear cuenta gratuita en [twilio.com](https://www.twilio.com/try-twilio)
2. Ir a **Messaging → Try it out → Send a WhatsApp message**
3. Desde tu celular, enviar `join <palabra-clave>` al número `+1 415 523 8886`
4. Pegar **Account SID** y **Auth Token** en `application.properties`

> ⚠️ En modo sandbox, cada destinatario debe activar el número una vez.

---

## 🖥️ Pantallas del Sistema

| URL | Descripción |
|-----|-------------|
| `localhost:4200/registro` | Formulario de registro de turnos (3 pasos) |
| `localhost:4200/dashboard` | Panel del operador — llamar y finalizar turnos |
| `localhost:4200/pantalla` | Pantalla TV de sala de espera |
| `localhost:8080/swagger-ui.html` | Documentación de la API REST |

---

## 🔌 Endpoints REST

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/turnos` | Registrar nuevo turno |
| `POST` | `/api/v1/turnos/siguiente` | Llamar siguiente turno (algoritmo) |
| `PUT` | `/api/v1/turnos/{id}/finalizar` | Finalizar turno en atención |
| `GET` | `/api/v1/turnos/esperando` | Listar turnos en espera |
| `GET` | `/api/v1/turnos/en-atencion` | Turno actualmente en atención |
| `GET` | `/api/v1/turnos/recientes` | Últimos 3 turnos finalizados |
| `GET` | `/api/v1/turnos` | Listar todos los turnos |

---

## 🗄️ Estructura del Proyecto

```
notaria-pro/
├── src/main/java/com/example/NotariaPro/
│   ├── controller/      # TurnoController
│   ├── Service/         # TurnoService, WhatsAppService
│   ├── repository/      # TurnoRepository
│   ├── Entidad/         # Turno, TipoPersona, TipoRegistro, EstadoTurno
│   └── dto/             # TurnoRequest, TurnoResponse
├── src/main/resources/
│   └── application.properties
├── seed_turnos.sql       # 50 registros de prueba con aging
└── README.md

notaria-pro-frontend/
├── src/app/
│   ├── components/
│   │   ├── registro/    # Formulario 3 pasos
│   │   ├── dashboard/   # Panel operador
│   │   └── pantalla/    # Pantalla TV
│   ├── services/        # TurnoService (HTTP)
│   └── models/          # TurnoRequest, TurnoResponse
└── README.md
```

---

## 👥 Tecnologías Utilizadas

| Capa | Tecnología |
|------|-----------|
| Backend | Spring Boot 4.0.5 |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | MySQL 5.7 (XAMPP) |
| Validaciones | Jakarta Validation (@Valid) |
| Documentación | SpringDoc OpenAPI (Swagger) |
| Frontend | Angular 21 (Standalone) |
| Estilos | Tailwind CSS |
| Notificaciones | Twilio WhatsApp API |

---

## 👨‍💻 Autor

Proyecto académico — Ingeniería de Sistemas  
Universidad Antonio Mariño · Semestre VI · 2026
