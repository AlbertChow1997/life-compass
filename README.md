# LifeCompass

A local entertainment & dining directory for the Irish market — users browse and rate
shops, buy vouchers, and post recommendations; merchants manage their own vouchers;
admins moderate content. Backend is Spring Boot, frontend is React 19 + Vite. All UI
text and stored data are in English.

## Contents

- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [Database schema](#database-schema)
- [Features](#features)
- [Business rules worth knowing](#business-rules-worth-knowing)
- [Authentication](#authentication)
- [API reference](#api-reference)
- [Running it locally](#running-it-locally)
- [Known gaps](#known-gaps)

## Tech stack

| Layer | Choice |
|---|---|
| Backend | Spring Boot 3.5, Java 17 |
| Persistence | MySQL 8 via MyBatis-Plus, Redis (SMS codes, caching) |
| Auth | Stateless JWT (jjwt), Google ID token verification, Twilio SMS, BCrypt |
| Frontend | React 19, TypeScript, Vite, React Router, Axios |
| Images | Uploaded via multipart to a local static/external directory, served by Spring |

## Project structure

### Backend (`src/main/java/com/albertchow/lifecompass/`)

Organized by domain, not by layer — each feature area owns its controller(s), service(s),
and DTOs together:

```
auth/        Login (Google, SMS, email+password), registration, current-user endpoint
shop/        Shop browsing/search, admin shop CRUD, shop ratings, shop follows
blog/        Posts, comments, likes, admin moderation
voucher/     Public voucher browsing/purchase, merchant voucher management
user/        The "personal center": stats, profile, and every "my X" list
support/     The FAQ/keyword support widget + admin FAQ & message management
upload/      Image upload endpoint + static file serving config
security/    JWT issuing/parsing, Spring Security config, Google/Twilio integration
common/      Result envelope, shared enums, exception types + global handler
entity/      MyBatis-Plus entities (one per table)
mapper/      MyBatis-Plus mapper interfaces (one per entity, plus a few custom @Select queries)
config/      MyBatis-Plus setup, auto-fill for create_time/update_time
```

### Frontend (`frontend/src/`)

```
pages/        One component per route
components/   Shared UI: Navbar, UserMenu (avatar dropdown), Banner, SupportWidget,
              PostComments, RequireAuth/RequireRole (route guards)
context/      AuthContext — the logged-in user's session state
api/client.ts Shared Axios instance (attaches the JWT, unwraps the Result envelope)
hooks/        useGoogleIdentityScript — lazy-loads Google's Sign-In script
types.ts      TypeScript interfaces mirroring the backend entities/DTOs
format.ts     Small display helpers (money formatting, star rating, first image)
```

## Database schema

All tables live in `sql/schema.sql` (fresh install) and are seeded by `sql/data.sql`.

| Table | Purpose |
|---|---|
| `user` | Accounts — regular users, merchants, admins. Any role can have a password; regular users can also sign in via Google or SMS. |
| `shop_type` | Business categories (Restaurant, Pub & Bar, Cinema, ...). |
| `shop` | Business listings. Money in euro cents; `score` is average rating × 10. |
| `shop_rating` | A history of star ratings (1–5) per shop — see [rating limits](#business-rules-worth-knowing). |
| `blog` | User posts, optionally linking a shop. |
| `blog_comment` | Comments on a post (flat, with optional `parent_id`/`answer_id` for threading). |
| `blog_like` | Who liked which post — also the source data for experience points. |
| `voucher` | Purchasable coupons for a shop. `type` 0 = regular, 1 = limited/stock-controlled. |
| `voucher_order` | A user's purchase of a voucher. |
| `follow` | User-to-user follows (social, not shop follows). |
| `shop_follow` | A user following/saving a shop. |
| `support_faq` | Admin-managed keyword → auto-reply entries for the support widget. |
| `support_message` | Every question asked via the support widget, matched or not. |

## Features

Mapped to the original requirements, plus what was added along the way:

1. **Login** — Google (ID token verification), Twilio SMS (code sent, verified against
   Redis), or email+password (any role, via self-registration or a seeded account).
2. **Browse shops** — paginated-free list, filterable by category, searchable by name.
3. **Rate shops** — 1–5 stars + optional text, capped to prevent spam (see below).
4. **Posts** — title/content/optional photo/optional linked shop; likeable and
   commentable; a "Following" filter shows only posts from users you follow.
5. **Search** — by name and/or category on the shop list.
6. **Buy vouchers** — purchase creates a `voucher_order`; limited-stock vouchers use an
   atomic SQL decrement so concurrent purchases can't oversell.
7. **Merchant voucher management** — a merchant creates vouchers for shops they own and
   toggles them on/off shelf.
8. **Admin moderation** — feature/delete posts, delete comments.

Built on top of the original scope:

- **Personal center** — edit profile + avatar, view/delete own reviews, and lists of
  your posts, comments, liked posts, followed shops, and voucher orders.
- **Experience points & PRO badge** — see [business rules](#business-rules-worth-knowing).
- **User-to-user follow** — follow another user from their post; a "Following" tab
  filters the feed. (There's no standalone "browse people" page — following currently
  only happens from a post you're already looking at.)
- **Support widget** — a floating chat bubble (bottom-right, works signed out) that
  matches questions against admin-managed keyword→answer entries and logs every
  question asked, matched or not, for admins to review. This is the seam where an AI
  responder would plug in later.
- **Image upload** — for post photos and user avatars.

## Business rules worth knowing

These aren't obvious from the code alone unless you're looking for them:

- **Experience points** are *derived*, not stored — recomputed on every read from the
  `blog`, `blog_comment`, and `blog_like` tables, each **capped per calendar day**
  before summing, so no single day of activity can inflate the score:
  - Posts: 10 XP each, max 3 counted per day (30 XP/day cap)
  - Comments: 2 XP each, max 10 counted per day (20 XP/day cap)
  - Likes received: 1 XP each, max 20 counted per day (20 XP/day cap)
  - **500 XP** unlocks the colorful "PRO" badge in the profile dropdown; below that,
    the badge is shown greyed out.
- **Shop rating limits** — a user can rate at most **50 shops per calendar month**, and
  must wait **30 days** before rating the *same* shop again. Ratings are a history (not
  one row upserted per shop), so a shop's score/comment count is the live average of
  every rating row for it — deleting or adding a rating recomputes it immediately.
- **Money** is always stored as integer **euro cents** (e.g. €12.50 → `1250`) to avoid
  float rounding issues.
- **`shop.score`** is the average star rating **× 10** (e.g. `46` = 4.6★), also to avoid
  floats in the DB.
- **Voucher stock** for limited vouchers is decremented atomically in SQL
  (`UPDATE ... SET stock = stock - 1 WHERE stock > 0`) so two simultaneous purchases
  can't both succeed when only one item is left.

## Authentication

Stateless JWT — no server-side session. The token encodes `{ userId, role }` and is
sent as `Authorization: Bearer <token>`. Roles are `USER`, `MERCHANT`, `ADMIN`.

| Method | Who can use it | Notes |
|---|---|---|
| Google | Any visitor | Verifies the ID token against Google's public keys; creates/links a `USER` account. Needs `GOOGLE_CLIENT_ID`. |
| SMS | Any visitor | 6-digit code, 5-minute TTL in Redis; creates a `USER` account. Without Twilio configured, the code is logged to the console instead of texted. |
| Email + password | Any role | Self-registration picks `USER` or `MERCHANT` (never `ADMIN` — admin accounts are seeded, not self-service). |

`GET /api/auth/config` tells the frontend whether SMS is actually wired up to Twilio, so
the login page can show accurate copy instead of always claiming a text will be sent.

## API reference

All responses use a uniform envelope: `{ success, errorMsg, data, total }`. Endpoints
marked **public** need no token; **auth** means any logged-in role; specific roles are
named where the route is restricted.

### Auth — `/api/auth`

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/config` | public | Whether SMS is live (vs. dev-mode logging). |
| POST | `/google` | public | Login with a Google ID token. |
| POST | `/sms/code` | public | Send a 6-digit SMS verification code. |
| POST | `/sms/login` | public | Verify the code and log in. |
| POST | `/login` | public | Email + password login. |
| POST | `/register` | public | Self-registration (`USER` or `MERCHANT`). |
| GET | `/me` | auth | The current user's profile. |

### Shops — `/api/shop`

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/` | public | List/search shops (`?typeId=&name=`). |
| GET | `/{id}` | public | Shop detail. |
| GET | `/{id}/follow` | public | Follow status (`false` if signed out). |
| POST | `/{id}/follow` | auth | Follow a shop. |
| DELETE | `/{id}/follow` | auth | Unfollow a shop. |
| GET | `/{shopId}/ratings` | public | List a shop's ratings. |
| POST | `/{shopId}/ratings` | auth | Submit a rating (subject to the monthly/cooldown limits). |

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/api/shop-type` | public | List business categories. |
| POST | `/api/admin/shop` | ADMIN | Create a shop listing. |
| PUT | `/api/admin/shop/{id}` | ADMIN | Update a shop listing. |

### Posts — `/api/blog`

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/` | public | List posts (`?featured=true`, `?followedOnly=true` needs auth). |
| GET | `/{id}` | public | Post detail. |
| POST | `/` | auth | Create a post. |
| POST | `/{id}/like` | auth | Toggle like. |
| GET | `/{blogId}/comments` | public | List comments on a post. |
| POST | `/{blogId}/comments` | auth | Add a comment. |

| Method | Path | Access | Description |
|---|---|---|---|
| PUT | `/api/admin/blog/{id}/feature?featured=` | ADMIN | Feature/unfeature a post. |
| DELETE | `/api/admin/blog/{id}` | ADMIN | Soft-delete a post. |
| DELETE | `/api/admin/blog/comments/{commentId}` | ADMIN | Soft-delete a comment. |

### Vouchers — `/api/voucher`

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/` | public | List on-shelf vouchers (`?shopId=`). |
| GET | `/{id}` | public | Voucher detail. |
| POST | `/{id}/purchase` | auth | Buy a voucher. |

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/api/merchant/voucher` | MERCHANT | List vouchers for shops you own. |
| POST | `/api/merchant/voucher` | MERCHANT | Create a voucher (must own the shop). |
| PUT | `/api/merchant/voucher/{id}/shelf?onShelf=` | MERCHANT | Toggle on/off shelf. |

### Support — `/api/support`

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/ask` | public | Ask a question; matched against admin keywords. |

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/api/admin/support/faq` | ADMIN | List FAQ entries. |
| POST | `/api/admin/support/faq` | ADMIN | Create an FAQ entry. |
| PUT | `/api/admin/support/faq/{id}` | ADMIN | Update an FAQ entry. |
| DELETE | `/api/admin/support/faq/{id}` | ADMIN | Delete an FAQ entry. |
| GET | `/api/admin/support/messages` | ADMIN | List questions customers have asked. |

### Personal center — `/api/user` (all require auth)

| Method | Path | Description |
|---|---|---|
| GET | `/stats` | Following/follower counts, experience, PRO threshold. |
| PUT | `/profile` | Update nickname/city/avatar. |
| GET | `/shops` | Shops you follow. |
| GET | `/posts` | Posts you've written. |
| GET | `/comments` | Comments you've written. |
| GET | `/likes` | Posts you've liked. |
| GET | `/orders` | Your voucher purchase history. |
| GET | `/ratings` | Your shop reviews. |
| DELETE | `/ratings/{id}` | Delete one of your reviews. |
| POST | `/{targetUserId}/follow` | Follow another user. |
| DELETE | `/{targetUserId}/follow` | Unfollow. |
| GET | `/{targetUserId}/follow` | Follow status. |

### Uploads

| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/upload` | auth | Upload an image (JPEG/PNG/WEBP/GIF, ≤8MB); returns its URL. |

## Running it locally

**Prerequisites:** Java 17, Node 18+, MySQL 8, Redis (or Docker to run one).

1. **Database** — create the schema and seed data:
   ```
   mysql -u root -p < sql/schema.sql
   mysql -u root -p < sql/data.sql
   ```
2. **Redis** — `docker run -d --name lifecompass-redis -p 6379:6379 redis:latest`
   (or any local Redis instance on port 6379).
3. **Backend config** — copy `src/main/resources/application.yaml.example` to
   `application.yaml` (gitignored) and fill in your DB credentials, or just export
   them as environment variables (the file already reads every secret from one):
   `DB_USERNAME`, `DB_PASSWORD`, `LIFECOMPASS_JWT_SECRET`, and optionally
   `GOOGLE_CLIENT_ID` / `TWILIO_ACCOUNT_SID` / `TWILIO_AUTH_TOKEN` / `TWILIO_FROM_NUMBER`
   (leave the Twilio ones unset in dev — codes are logged instead of texted).
4. **Start the backend**: `./mvnw spring-boot:run` (serves on `:8080`).
5. **Start the frontend**: `cd frontend && npm install && npm run dev` (serves on
   `:5173`, proxies `/api`, `/images`, `/uploads` to the backend — see
   `frontend/vite.config.ts`).
6. For Google login, also create `frontend/.env.local` (see
   `frontend/.env.local.example`) with `VITE_GOOGLE_CLIENT_ID`.

**Seeded accounts** (see `sql/data.sql` for the full list): `admin@lifecompass.ie` /
`password` (ADMIN), `olivia@templebar.ie` / `password` (MERCHANT, owns shop 1).

## Known gaps

Documented honestly rather than silently left out:

- No page to browse/discover other users — following someone only happens from a post
  of theirs you're already viewing.
- The support widget only does keyword matching; the AI responder it's designed to
  make room for isn't built yet (`support_message` already logs everything an AI
  could later be trained/evaluated on).
- Google login requires a real OAuth Client ID and Twilio requires a real account to
  actually send SMS — both degrade gracefully without one (Google shows a "not
  configured" message; SMS logs the code to the console).
