/** Shared brand banner (hero image + tagline) shown between TopBar and SecondaryNav on Shops/Posts (see BannerSlot.tsx). The brand mark itself lives in TopBar, not here, so it isn't shown twice. Purely presentational, no props or state. */
export default function Banner() {
  return (
    <div className="site-banner">
      <img src="/images/banner/ireland.jpg" alt="" />
      <div className="site-banner-text">Enjoy your life in Ireland</div>
    </div>
  )
}
