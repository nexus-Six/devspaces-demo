package org.acme;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Fake Jira issue page for the Dev Spaces demo story:
 * developer opens a ticket, clicks the Dev Spaces link, and lands in a
 * ready-to-code workspace with the matching branch / JDK / tooling.
 */
@Path("/jira")
public class JiraTicketResource {

    @ConfigProperty(name = "demo.devspaces.base-url")
    String devSpacesBaseUrl;

    @ConfigProperty(name = "demo.github.repo")
    String githubRepo;

    @ConfigProperty(name = "demo.devspaces.branch", defaultValue = "v1")
    String branch;

    /** JDK major version required by the target branch (differs from main on purpose). */
    @ConfigProperty(name = "demo.devspaces.jdk", defaultValue = "17")
    String jdkVersion;

    @ConfigProperty(name = "demo.devspaces.devfile-path", defaultValue = "devfile.yaml")
    String devfilePath;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String ticket() {
        String repoTreeUrl = trimSlash(githubRepo) + "/tree/" + branch;
        String rawDevfileUrl = toRawGithubUrl(githubRepo, branch, devfilePath);
        // Factory URL that opens Dev Spaces against the branch (uses in-repo devfile).
        String factoryFromBranch = trimSlash(devSpacesBaseUrl) + "/#" + repoTreeUrl;
        // Alternate: open directly from the raw devfile URL (explicit checkoutFrom in that file).
        String factoryFromDevfile = trimSlash(devSpacesBaseUrl) + "/#" + rawDevfileUrl;

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1" />
              <title>[SUMMIT-1847] Shop checkout error popup — Jira</title>
              <style>
                :root {
                  --jira-blue: #0c66e4;
                  --jira-blue-hover: #0055cc;
                  --jira-navy: #1d7afc;
                  --bg: #f7f8f9;
                  --surface: #ffffff;
                  --border: #dfe1e6;
                  --text: #172b4d;
                  --muted: #626f86;
                  --subtle: #f1f2f4;
                  --green: #22a06b;
                  --orange: #f15b50;
                  --yellow-bg: #fff7d6;
                  --yellow-border: #f5cd47;
                }
                * { box-sizing: border-box; }
                body {
                  margin: 0;
                  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", sans-serif;
                  background: var(--bg);
                  color: var(--text);
                  line-height: 1.45;
                }
                .topbar {
                  display: flex;
                  align-items: center;
                  gap: 1rem;
                  padding: 0 1.25rem;
                  height: 56px;
                  background: #1d2125;
                  color: #fff;
                }
                .logo {
                  display: inline-flex;
                  align-items: center;
                  gap: 0.55rem;
                  font-weight: 700;
                  letter-spacing: 0.02em;
                }
                .logo-mark {
                  width: 28px;
                  height: 28px;
                  border-radius: 6px;
                  background: linear-gradient(135deg, #2684ff, #0052cc);
                  display: grid;
                  place-items: center;
                  font-size: 0.85rem;
                  font-weight: 800;
                }
                .topbar nav {
                  display: flex;
                  gap: 1.1rem;
                  color: #c7d1db;
                  font-size: 0.92rem;
                }
                .topbar nav span { cursor: default; }
                .layout {
                  max-width: 1100px;
                  margin: 1.25rem auto 3rem;
                  padding: 0 1.25rem;
                  display: grid;
                  grid-template-columns: 1fr 300px;
                  gap: 1.25rem;
                }
                @media (max-width: 900px) {
                  .layout { grid-template-columns: 1fr; }
                }
                .breadcrumb {
                  grid-column: 1 / -1;
                  color: var(--muted);
                  font-size: 0.85rem;
                }
                .breadcrumb strong { color: var(--jira-blue); }
                .main, .side {
                  background: var(--surface);
                  border: 1px solid var(--border);
                  border-radius: 8px;
                }
                .main { padding: 1.35rem 1.5rem 1.75rem; }
                .issue-key {
                  color: var(--muted);
                  font-size: 0.85rem;
                  font-weight: 600;
                  margin-bottom: 0.35rem;
                }
                h1 {
                  margin: 0 0 1rem;
                  font-size: 1.55rem;
                  font-weight: 600;
                  letter-spacing: -0.01em;
                }
                .actions {
                  display: flex;
                  flex-wrap: wrap;
                  gap: 0.6rem;
                  margin-bottom: 1.35rem;
                }
                .btn {
                  display: inline-flex;
                  align-items: center;
                  gap: 0.45rem;
                  border-radius: 3px;
                  padding: 0.55rem 0.9rem;
                  font-size: 0.9rem;
                  font-weight: 600;
                  text-decoration: none;
                  border: 1px solid transparent;
                  cursor: pointer;
                }
                .btn-primary {
                  background: var(--jira-blue);
                  color: #fff;
                }
                .btn-primary:hover { background: var(--jira-blue-hover); }
                .btn-secondary {
                  background: var(--subtle);
                  color: var(--text);
                  border-color: var(--border);
                }
                .section-title {
                  font-size: 0.78rem;
                  font-weight: 700;
                  text-transform: uppercase;
                  letter-spacing: 0.04em;
                  color: var(--muted);
                  margin: 1.4rem 0 0.55rem;
                }
                .description {
                  background: var(--subtle);
                  border-radius: 6px;
                  padding: 1rem 1.1rem;
                  color: var(--text);
                }
                .description p { margin: 0 0 0.75rem; }
                .description p:last-child { margin-bottom: 0; }
                .description ul { margin: 0.4rem 0 0.75rem 1.1rem; padding: 0; }
                .description code, .mono {
                  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
                  font-size: 0.86em;
                  background: #ebecf0;
                  padding: 0.1rem 0.35rem;
                  border-radius: 3px;
                }
                .callout {
                  margin-top: 1rem;
                  border: 1px solid var(--yellow-border);
                  background: var(--yellow-bg);
                  border-radius: 6px;
                  padding: 0.9rem 1rem;
                }
                .callout strong { display: block; margin-bottom: 0.35rem; }
                .devspaces-cta {
                  margin-top: 1rem;
                  border: 1px solid #b3d4ff;
                  background: #e9f2ff;
                  border-radius: 8px;
                  padding: 1rem 1.1rem;
                  display: flex;
                  flex-wrap: wrap;
                  align-items: center;
                  justify-content: space-between;
                  gap: 0.85rem;
                }
                .devspaces-cta p {
                  margin: 0;
                  color: var(--text);
                  max-width: 34rem;
                }
                .devspaces-cta .hint {
                  display: block;
                  margin-top: 0.25rem;
                  color: var(--muted);
                  font-size: 0.82rem;
                }
                .activity {
                  border-top: 1px solid var(--border);
                  margin-top: 1.5rem;
                  padding-top: 1rem;
                }
                .comment {
                  display: grid;
                  grid-template-columns: 36px 1fr;
                  gap: 0.75rem;
                  margin-top: 0.9rem;
                }
                .avatar {
                  width: 36px;
                  height: 36px;
                  border-radius: 50%%;
                  background: #6554c0;
                  color: #fff;
                  display: grid;
                  place-items: center;
                  font-weight: 700;
                  font-size: 0.8rem;
                }
                .comment-body {
                  background: var(--subtle);
                  border-radius: 6px;
                  padding: 0.75rem 0.9rem;
                }
                .comment-meta {
                  font-size: 0.8rem;
                  color: var(--muted);
                  margin-bottom: 0.35rem;
                }
                .comment-body a {
                  color: var(--jira-blue);
                  word-break: break-all;
                }
                .side { padding: 1rem 1.1rem 1.25rem; }
                .field { margin-bottom: 0.95rem; }
                .field label {
                  display: block;
                  font-size: 0.75rem;
                  font-weight: 700;
                  color: var(--muted);
                  text-transform: uppercase;
                  letter-spacing: 0.03em;
                  margin-bottom: 0.25rem;
                }
                .pill {
                  display: inline-flex;
                  align-items: center;
                  gap: 0.35rem;
                  padding: 0.2rem 0.55rem;
                  border-radius: 3px;
                  font-size: 0.82rem;
                  font-weight: 600;
                  background: var(--subtle);
                }
                .pill.todo::before {
                  content: "";
                  width: 8px;
                  height: 8px;
                  border-radius: 50%%;
                  background: var(--muted);
                }
                .pill.high { color: var(--orange); background: #ffedeb; }
                .people {
                  display: flex;
                  align-items: center;
                  gap: 0.45rem;
                  font-size: 0.9rem;
                }
                .people .mini {
                  width: 24px;
                  height: 24px;
                  border-radius: 50%%;
                  background: #00a3bf;
                  color: #fff;
                  display: grid;
                  place-items: center;
                  font-size: 0.65rem;
                  font-weight: 700;
                }
                .footer-note {
                  grid-column: 1 / -1;
                  font-size: 0.8rem;
                  color: var(--muted);
                  text-align: center;
                }
                .footer-note code { font-size: 0.78em; }
              </style>
            </head>
            <body>
              <header class="topbar">
                <div class="logo">
                  <span class="logo-mark">J</span>
                  <span>Jira SoftWare</span>
                </div>
                <nav>
                  <span>Your work</span>
                  <span>Projects</span>
                  <span>Filters</span>
                  <span>Dashboards</span>
                </nav>
              </header>

              <div class="layout">
                <div class="breadcrumb">
                  <strong>SUMMIT</strong> / Software Delivery / <strong>SUMMIT-1847</strong>
                </div>

                <article class="main">
                  <div class="issue-key">SUMMIT-1847 · Bug</div>
                  <h1>Shop checkout fails with error popup (Quarkus Merch demo)</h1>

                  <div class="actions">
                    <a class="btn btn-primary" href="%s" target="_blank" rel="noopener">
                      ▷ Open in OpenShift Dev Spaces
                    </a>
                    <span class="btn btn-secondary">Start sprint</span>
                    <span class="btn btn-secondary">Log work</span>
                    <span class="btn btn-secondary">···</span>
                  </div>

                  <div class="section-title">Description</div>
                  <div class="description">
                    <p>
                      The <a href="/shop/">Quarkus Merch</a> shop lets customers add the
                      <strong>“Quarkus rocks”</strong> tee to the cart, but
                      <strong>Checkout</strong> always shows an error popup
                      (<code>Payment gateway returned HTTP 500</code>).
                      Reproduce on the maintenance branch — not on <code>main</code> (2.x / JDK 21).
                      That line still runs on <strong>OpenJDK %s</strong>, which most laptops no longer have installed.
                    </p>
                    <ul>
                      <li>Reproduce: open <code>/shop/</code> → Add to cart → Checkout</li>
                      <li>Fix: in <code>META-INF/resources/shop/app.js</code>, set <code>CHECKOUT_BUG = false</code></li>
                      <li>Affects version: <code>1.4.0-SNAPSHOT</code> · branch <code>%s</code> · OpenJDK %s</li>
                    </ul>
                    <p>
                      Please reproduce and patch in a cloud workspace so you do not need to juggle
                      multiple JDKs / Quarkus toolchains on your laptop.
                    </p>
                  </div>

                  <div class="devspaces-cta">
                    <p>
                      <strong>Ready-to-code workspace</strong>
                      Clicking the button opens OpenShift Dev Spaces with branch
                      <code>%s</code>, OpenJDK %s, Maven, and Java/Quarkus IDE extensions —
                      loaded from this repo’s <code>%s</code>.
                      <span class="hint">Factory URL targets the Git branch so the matching in-repo devfile (and JDK image) is applied automatically.</span>
                    </p>
                    <a class="btn btn-primary" href="%s" target="_blank" rel="noopener">
                      Open Dev Spaces workspace
                    </a>
                  </div>

                  <div class="callout">
                    <strong>Demo tip</strong>
                    Set <code>DEMO_DEVSPACES_BASE_URL</code> in local <code>.env</code>
                    and update <code>demo.github.repo</code> in <code>application.properties</code>
                    before presenting.
                  </div>

                  <div class="section-title">Acceptance criteria</div>
                  <div class="description">
                    <ul>
                      <li>Workspace opens from the Jira link with <code>%s</code> checked out</li>
                      <li>Container JDK matches %s (see app landing page badge) — different from <code>main</code> / JDK 21</li>
                      <li>Checkout on <code>/shop/</code> succeeds after flipping <code>CHECKOUT_BUG</code> to <code>false</code></li>
                      <li>Verified with <code>./mvnw quarkus:dev</code> (or <code>quarkus dev</code>)</li>
                    </ul>
                  </div>

                  <div class="activity">
                    <div class="section-title" style="margin-top:0">Activity</div>
                    <div class="comment">
                      <div class="avatar">AL</div>
                      <div class="comment-body">
                        <div class="comment-meta"><strong>Alex Lead</strong> · yesterday</div>
                        <p style="margin:0 0 0.5rem">
                          Dropped a Dev Spaces factory link so anyone on-call can jump straight into the
                          1.4 / JDK %s environment — no laptop setup.
                        </p>
                        <p style="margin:0" class="mono">
                          Branch factory:<br />
                          <a href="%s">%s</a>
                        </p>
                        <p style="margin:0.65rem 0 0" class="mono">
                          Raw devfile factory (alternate):<br />
                          <a href="%s">%s</a>
                        </p>
                      </div>
                    </div>
                  </div>
                </article>

                <aside class="side">
                  <div class="field">
                    <label>Status</label>
                    <span class="pill todo">To Do</span>
                  </div>
                  <div class="field">
                    <label>Priority</label>
                    <span class="pill high">▲ High</span>
                  </div>
                  <div class="field">
                    <label>Assignee</label>
                    <div class="people"><span class="mini">DV</span> Unassigned → You</div>
                  </div>
                  <div class="field">
                    <label>Reporter</label>
                    <div class="people"><span class="mini" style="background:#ff5630">AL</span> Alex Lead</div>
                  </div>
                  <div class="field">
                    <label>Affects versions</label>
                    <div>1.4.0</div>
                  </div>
                  <div class="field">
                    <label>Fix versions</label>
                    <div>1.4.1</div>
                  </div>
                  <div class="field">
                    <label>Components</label>
                    <div>rest-service</div>
                  </div>
                  <div class="field">
                    <label>Labels</label>
                    <div><span class="pill">devspaces</span> <span class="pill">hot-fix</span></div>
                  </div>
                  <div class="field">
                    <label>Environment</label>
                    <div>OpenJDK %s · Quarkus · branch <code>%s</code></div>
                  </div>
                </aside>

                <p class="footer-note">
                  Demo mock · not affiliated with Atlassian ·
                  <a href="/shop/" style="color:var(--jira-blue)">Open shop</a>
                  ·
                  <a href="/" style="color:var(--jira-blue)">Back to app</a>
                </p>
              </div>
            </body>
            </html>
            """.formatted(
                factoryFromBranch, // primary CTA href
                jdkVersion,        // description: maintenance JDK
                branch,            // affects: branch
                jdkVersion,        // affects: OpenJDK
                branch,            // CTA branch
                jdkVersion,        // CTA OpenJDK
                devfilePath,       // CTA devfile name
                factoryFromBranch, // secondary CTA href
                branch,            // acceptance: checkout
                jdkVersion,        // acceptance: container JDK
                jdkVersion,        // comment: JDK environment
                factoryFromBranch, // comment: branch factory href
                factoryFromBranch, // comment: branch factory text
                factoryFromDevfile,// comment: raw devfile href
                factoryFromDevfile,// comment: raw devfile text
                jdkVersion,        // sidebar: OpenJDK
                branch             // sidebar: branch
            );
    }

    private static String trimSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    /**
     * Converts https://github.com/org/repo → https://raw.githubusercontent.com/org/repo/{branch}/{path}
     */
    private static String toRawGithubUrl(String repoUrl, String branch, String path) {
        String cleaned = trimSlash(repoUrl)
                .replace("https://github.com/", "https://raw.githubusercontent.com/")
                .replace("http://github.com/", "https://raw.githubusercontent.com/");
        if (cleaned.endsWith(".git")) {
            cleaned = cleaned.substring(0, cleaned.length() - 4);
        }
        return cleaned + "/" + branch + "/" + path;
    }
}
