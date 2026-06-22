# Deploy ShelfAware

ShelfAware has two deployable applications:

- `frontend/`: a Vite SPA hosted on Vercel
- repository root: a Spring Boot API hosted as a Docker web service

The API also needs a persistent PostgreSQL database. The walkthrough below uses Neon for PostgreSQL and Render for the API, while Vercel serves the frontend.

## 1. Push the repository

1. Push the project to a GitHub repository.
2. Confirm the default branch contains `Dockerfile`, `pom.xml`, `frontend/`, and `frontend/vercel.json`.
3. Do not commit real `.env` files or deployment secrets.

## 2. Create PostgreSQL on Neon

1. Create a Neon account and a new project at <https://console.neon.tech>.
2. Open the project's **Connect** dialog and select a Java/JDBC connection.
3. Record the host, database, username, and password.
4. Build the JDBC URL in this form:

   ```text
   jdbc:postgresql://YOUR_NEON_HOST/YOUR_DATABASE?sslmode=require
   ```

Neon's general connection guide is at <https://neon.com/docs/connect/connect-from-any-app>.

## 3. Deploy the Spring Boot API on Render

1. In the Render dashboard, choose **New > Web Service** and connect the GitHub repository.
2. Select **Docker** as the runtime. Keep the repository root as the root directory; Render will use the existing `Dockerfile`.
3. Set the health check path to `/api/health`.
4. Add these environment variables:

   ```text
   SPRING_PROFILES_ACTIVE=postgres
   DATABASE_URL=jdbc:postgresql://YOUR_NEON_HOST/YOUR_DATABASE?sslmode=require
   DATABASE_USERNAME=YOUR_NEON_USERNAME
   DATABASE_PASSWORD=YOUR_NEON_PASSWORD
   JWT_SECRET=A_RANDOM_SECRET_AT_LEAST_32_CHARACTERS_LONG
   JWT_ISSUER=shelfaware-production
   JWT_EXPIRATION_MINUTES=120
   OPEN_LIBRARY_BASE_URL=https://openlibrary.org
   DEMO_MODE_ENABLED=true
   CORS_ALLOWED_ORIGIN_PATTERNS=https://*.vercel.app
   ```

5. Generate `JWT_SECRET` locally with a password manager or `openssl rand -base64 48`.
6. Deploy the service and wait for `/api/health` to return `{"status":"ok"}`.
7. Copy the Render service URL, for example `https://shelfaware-api.onrender.com`.

Render's Docker deployment documentation is at <https://render.com/docs/docker>.

Flyway runs automatically during startup and creates or upgrades the database schema. Do not run migrations manually.

## 4. Deploy the frontend on Vercel

1. In Vercel, choose **Add New > Project** and import the same GitHub repository.
2. Set **Root Directory** to `frontend`.
3. Select the **Vite** framework preset. The expected build command is `npm run build` and output directory is `dist`.
4. Add this environment variable for Production, Preview, and Development:

   ```text
   VITE_API_BASE_URL=https://YOUR-RENDER-SERVICE.onrender.com
   ```

5. Deploy the project.
6. Open the generated `https://YOUR-PROJECT.vercel.app` URL and select **Explore the live demo**.

The included `frontend/vercel.json` sends direct visits such as `/insights` and `/books/1` back through the React router. Vercel's Vite and environment-variable references are at <https://vercel.com/docs/frameworks/frontend/vite> and <https://vercel.com/docs/projects/environment-variables>.

## 5. Tighten production CORS

After Vercel assigns the final domain, change the Render variable to include only the domains you use:

```text
CORS_ALLOWED_ORIGIN_PATTERNS=https://YOUR-PROJECT.vercel.app,https://YOUR-CUSTOM-DOMAIN.com
```

Redeploy the API after changing this value. Use `https://*.vercel.app` only when you want every Vercel preview deployment to call the production API.

## 6. Verify the deployment

1. Visit the API health URL.
2. Open the Vercel site in a private browser window.
3. Click **Explore the live demo** and confirm the Journey dashboard contains goals, a streak, active reading progress, and recent sessions.
4. Update reading progress, add a review, change a shelf status, and reload the page.
5. Open `/insights` directly in a new tab to verify the SPA rewrite.
6. Register a separate account and confirm its data is isolated from the demo profile.

Each demo click creates an isolated sample account. Demo accounts older than 24 hours are deleted when the next demo account is created.

## 7. Optional custom domains

1. Add the frontend domain in Vercel and follow its DNS instructions.
2. Add an API domain in Render if desired.
3. Update `VITE_API_BASE_URL` when the API domain changes.
4. Add the final frontend domain to `CORS_ALLOWED_ORIGIN_PATTERNS` and redeploy both services.
