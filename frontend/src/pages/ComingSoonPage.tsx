import { Link } from 'react-router-dom'

/**
 * Static placeholder for the two lower-priority extensions raised in the
 * original Requirements Spec (a takeaway ordering entrance, and a movie/
 * entertainment review category) — deliberately not built, just an honest
 * "not yet" landing page linked from the homepage.
 */
export default function ComingSoonPage() {
  return (
    <section className="page">
      <div className="hero">
        <h1>More is on the way</h1>
        <p>A couple of extensions are planned but not built yet:</p>
      </div>
      <div className="grid">
        <div className="card">
          <div className="card-body">
            <h3>🍱 Takeaway ordering</h3>
            <p className="muted">Order food directly from a shop's page after reading reviews.</p>
            <p>
              <strong>To be implemented.</strong>
            </p>
          </div>
        </div>
        <div className="card">
          <div className="card-body">
            <h3>🎬 Movie &amp; entertainment reviews</h3>
            <p className="muted">Review cinemas, gigs, and other entertainment venues alongside shops.</p>
            <p>
              <strong>To be implemented.</strong>
            </p>
          </div>
        </div>
      </div>
      <p>
        <Link className="back-link" to="/">
          ← Back to shops
        </Link>
      </p>
    </section>
  )
}
