import { useLocation } from 'react-router-dom'
import Banner from './Banner'

/**
 * Renders the Ireland banner only on the pages that want it (Shops, Posts),
 * between TopBar and SecondaryNav. Wrapped in the same `.container` box
 * model as the page content below, so its rounded-card width lines up with
 * everything else instead of running edge-to-edge like the two nav bars.
 */
export default function BannerSlot() {
  const { pathname } = useLocation()
  if (pathname !== '/' && pathname !== '/posts') return null
  return (
    <div className="container banner-shell">
      <Banner />
    </div>
  )
}
