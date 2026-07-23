/** Shared brand banner (hero image + tagline) shown between the navbar and page content on Shops/Posts. Purely presentational, no props or state. */
export default function Banner() {
  return (
    <div className="site-banner">
      <img src="/images/banner/ireland.jpg" alt="" />
      <span className="site-banner-brand">◆ LifeCompass</span>
      <div className="site-banner-text">Enjoy your life in Ireland</div>
    </div>
  )
}
