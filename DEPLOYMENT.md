# Despliegue de la API (Vercel vs Alternativas)

Vercel es una plataforma excelente, pero está diseñada principalmente para **aplicaciones frontend** (Next.js, React, Vue) y **funciones serverless** (Node.js, Python, Go).

**No se recomienda ni es viable** desplegar una aplicación tradicional de Spring Boot en Vercel por las siguientes razones:

1. **Arquitectura Serverless vs Servidor de larga duración:** Vercel mata los procesos tras responder a la petición. Spring Boot requiere un servidor embebido (Tomcat) que se mantenga ejecutándose continuamente.
2. **Límites de tiempo de ejecución:** Vercel impone límites estrictos (ej. 10 a 60 segundos por función), lo que rompe las conexiones prolongadas y procesos en segundo plano.
3. **Tiempo de arranque (Cold Start):** La JVM de Java tarda unos segundos en arrancar. Si se adaptara como función serverless, la primera petición sería extremadamente lenta.
4. **Soporte oficial:** Vercel no tiene soporte nativo para Java en sus funciones.

---

## La Mejor Alternativa: Usar Plataformas basadas en Docker

Dado que este repositorio ya incluye un `Dockerfile` bien configurado, la forma más rápida, robusta y gratuita (o de muy bajo costo) de desplegar esta aplicación es utilizar plataformas de tipo Container-as-a-Service.

A continuación, te indico cómo desplegarlo usando tu `Dockerfile` existente en las plataformas más populares:

### 1. Railway (Recomendado)
Railway detectará automáticamente tu `Dockerfile` y desplegará la aplicación sin configuración adicional.
1. Ve a [Railway.app](https://railway.app/) y haz login con GitHub.
2. Haz clic en **"New Project"** -> **"Deploy from GitHub repo"**.
3. Selecciona tu repositorio.
4. Railway usará tu `Dockerfile` para construir (usando Maven) y desplegar la app de Java 17.
5. **Base de Datos:** Puedes provisionar un servicio de MySQL directamente dentro del mismo proyecto de Railway.
6. **Variables de Entorno:** Añade tus credenciales en la pestaña "Variables" del servicio de tu API (ej. `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, y tu configuración `SMTP_*`).

### 2. Render
Render es otra excelente opción gratuita/económica para contenedores Docker.
1. Crea una cuenta en [Render.com](https://render.com/).
2. Haz clic en **"New"** -> **"Web Service"**.
3. Conecta este repositorio de GitHub.
4. En el apartado de "Runtime", selecciona **"Docker"** (detectará el `Dockerfile` de forma automática).
5. Configura las variables de entorno requeridas en el panel de configuración y despliega.

### 3. Koyeb
1. Inicia sesión en [Koyeb.com](https://www.koyeb.com/).
2. Selecciona **"Create Web Service"**.
3. Conecta GitHub y selecciona este repositorio.
4. En los ajustes de despliegue, asegúrate de configurar el puerto en `8080` (que es el que expone tu `Dockerfile`).
5. Configura tus "Environment variables" y haz clic en desplegar.

---

## ¿Cómo integro esto con mi Frontend alojado en Vercel?

Si tienes un cliente web (React, Angular, Next.js) que **sí** está alojado en Vercel y quieres conectarlo a esta API sin sufrir problemas de CORS, puedes configurar tu frontend en Vercel para que haga de proxy hacia tu backend alojado en Railway/Render.

Para ello, crea o edita el archivo `vercel.json` **en la raíz de tu proyecto frontend**:

```json
{
  "rewrites": [
    {
      "source": "/api/:path*",
      "destination": "https://tu-backend.up.railway.app/api/:path*"
    }
  ]
}
```

De este modo, tu frontend en Vercel realizará llamadas a `https://tu-dominio-vercel.com/api/...` y Vercel se encargará de redirigirlas transparentemente a tu Spring Boot.
