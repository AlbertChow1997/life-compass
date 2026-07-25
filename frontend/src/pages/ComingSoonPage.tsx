import { Link, useSearchParams } from 'react-router-dom'

type FeatureKey = 'takeaway' | 'movies'

const FEATURES: Record<FeatureKey, { emoji: string; title: string; description: string }> = {
  takeaway: {
    emoji: '🍱',
    title: 'Takeaway ordering',
    description: "Order food directly from a shop's page after reading reviews.",
  },
  movies: {
    emoji: '🎬',
    title: 'Movie & entertainment reviews',
    description: 'Review cinemas, gigs, and other entertainment venues alongside shops.',
  },
}

/**
 * Static placeholder for the two lower-priority extensions raised in the
 * original Requirements Spec (a takeaway ordering entrance, and a movie/
 * entertainment review category) — deliberately not built, just an honest
 * "not yet" landing page linked from SecondaryNav's Takeaway/Movies links.
 * Shows only the one feature the visitor actually clicked (via `?feature=`),
 * not both at once.
 */
export default function ComingSoonPage() {
  const [searchParams] = useSearchParams()
  const featureKey = searchParams.get('feature')
  const feature = featureKey === 'takeaway' || featureKey === 'movies' ? FEATURES[featureKey] : null

  return (
    <section className="page">
      <div className="hero">
        <h1>{feature ? feature.title : 'More is on the way'}</h1>
        <p>{feature ? feature.description : "This part of LifeCompass isn't built yet."}</p>
      </div>
      <div className="grid">
        <div className="card">
          <div className="card-body">
            {feature && <h3>{feature.emoji}</h3>}
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
