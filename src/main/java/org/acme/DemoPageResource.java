package org.acme;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class DemoPageResource {

    @ConfigProperty(name = "quarkus.application.version")
    String appVersion;

    @ConfigProperty(name = "quarkus.application.name", defaultValue = "rest-service")
    String appName;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String index() {
        String javaVersion = System.getProperty("java.version", "unknown");
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1" />
              <title>%s — Dev Spaces Demo</title>
              <style>
                :root {
                  --bg: #0b1220;
                  --card: #141e33;
                  --accent: #ff6b35;
                  --accent-2: #3dd6c6;
                  --text: #f4f7fb;
                  --muted: #9db0c9;
                }
                * { box-sizing: border-box; }
                body {
                  margin: 0;
                  min-height: 100vh;
                  font-family: "Segoe UI", system-ui, sans-serif;
                  color: var(--text);
                  background:
                    radial-gradient(circle at 15%% 20%%, #1d3a6d 0%%, transparent 40%%),
                    radial-gradient(circle at 85%% 10%%, #4a1d40 0%%, transparent 35%%),
                    radial-gradient(circle at 70%% 80%%, #0f3d3a 0%%, transparent 40%%),
                    var(--bg);
                  display: grid;
                  place-items: center;
                  padding: 2rem;
                }
                .card {
                  width: min(640px, 100%%);
                  background: color-mix(in srgb, var(--card) 88%%, transparent);
                  border: 1px solid color-mix(in srgb, var(--accent-2) 35%%, transparent);
                  border-radius: 24px;
                  padding: 2.5rem;
                  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.45);
                  backdrop-filter: blur(8px);
                  text-align: center;
                }
                .emoji {
                  font-size: 3.5rem;
                  line-height: 1;
                  margin-bottom: 0.75rem;
                  animation: float 3s ease-in-out infinite;
                }
                @keyframes float {
                  0%%, 100%% { transform: translateY(0); }
                  50%% { transform: translateY(-10px); }
                }
                h1 {
                  margin: 0 0 0.5rem;
                  font-size: clamp(1.8rem, 4vw, 2.4rem);
                  letter-spacing: -0.02em;
                }
                .tagline {
                  color: var(--muted);
                  margin: 0 0 1.75rem;
                  font-size: 1.05rem;
                }
                .badges {
                  display: flex;
                  flex-wrap: wrap;
                  gap: 0.75rem;
                  justify-content: center;
                  margin-bottom: 1.75rem;
                }
                .badge {
                  display: inline-flex;
                  align-items: center;
                  gap: 0.4rem;
                  padding: 0.55rem 0.95rem;
                  border-radius: 999px;
                  font-weight: 700;
                  font-size: 0.95rem;
                  letter-spacing: 0.01em;
                }
                .badge.version {
                  background: var(--accent);
                  color: #1a0b05;
                }
                .badge.java {
                  background: color-mix(in srgb, var(--accent-2) 25%%, transparent);
                  border: 1px solid var(--accent-2);
                  color: var(--accent-2);
                }
                .panel {
                  background: rgba(255, 255, 255, 0.04);
                  border-radius: 16px;
                  padding: 1.1rem 1.25rem;
                  color: var(--muted);
                  line-height: 1.5;
                }
                code {
                  color: var(--accent-2);
                  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
                  font-size: 0.9em;
                }
                a {
                  color: var(--accent);
                }
              </style>
            </head>
            <body>
              <main class="card">
                <div class="emoji" aria-hidden="true">🚀☕</div>
                <h1>Quarkus Mission Control</h1>
                <p class="tagline">
                  A tiny demo app for OpenShift Dev Spaces — fix bugs in the cloud, not on your laptop.
                </p>
                <div class="badges">
                  <span class="badge version">App version %s</span>
                  <span class="badge java">Running on Java %s</span>
                </div>
                <div class="panel">
                  Hello from <strong>%s</strong>!<br />
                  Try the classic endpoint at <a href="/hello"><code>/hello</code></a>.<br />
                  Demo the onboarding story: open the fake
                  <a href="/jira"><code>Jira ticket</code></a> and click
                  <em>Open in OpenShift Dev Spaces</em>.
                </div>
              </main>
            </body>
            </html>
            """.formatted(appName, appVersion, javaVersion, appName);
    }
}
