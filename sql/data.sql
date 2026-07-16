-- =============================================================================
-- LifeCompass — Seed Data (Irish market, English)
-- Run after schema.sql:  mysql -u root -p lifecompass < sql/data.sql
--
-- Images referenced below are downloaded into src/main/resources/static/images/
-- and served by Spring Boot's default static handler at /images/**.
--
-- Credential accounts use BCrypt. The seed hash below is BCrypt("password").
--   admin@lifecompass.ie / password              (ADMIN)
--   olivia@templebar.ie / password                (MERCHANT — owns shop 1)
--   sean@corkroast.ie / password                   (MERCHANT — owns shop 5)
--   fiona@brazenhead.ie / password                 (MERCHANT — owns shop 8)
--   niall@eddierockets.ie / password                (MERCHANT — owns shop 11)
-- Regular users normally sign in via Google or SMS, so they have no password.
-- =============================================================================

USE `lifecompass`;

-- --- users -------------------------------------------------------------------
INSERT INTO `user` (`id`, `phone`, `email`, `google_id`, `password`, `nick_name`, `icon`, `role`, `status`) VALUES
(1,  NULL,            'admin@lifecompass.ie',    NULL,                     '$2a$10$1C8qXV5QSSfC6NXt0uisMOtNmxopEX0Y06qTAGaI3aCYWWNbBUjM6', 'Site Admin',                 '', 'ADMIN',    1),
(2,  NULL,            'olivia@templebar.ie',     NULL,                     '$2a$10$1C8qXV5QSSfC6NXt0uisMOtNmxopEX0Y06qTAGaI3aCYWWNbBUjM6', 'Olivia (Temple Bar Bistro)', '', 'MERCHANT', 1),
(3,  NULL,            'sean@corkroast.ie',       NULL,                     '$2a$10$1C8qXV5QSSfC6NXt0uisMOtNmxopEX0Y06qTAGaI3aCYWWNbBUjM6', 'Sean (Cork Roast House)',    '', 'MERCHANT', 1),
(4,  NULL,            'fiona@brazenhead.ie',     NULL,                     '$2a$10$1C8qXV5QSSfC6NXt0uisMOtNmxopEX0Y06qTAGaI3aCYWWNbBUjM6', 'Fiona (The Brazen Head)',    '', 'MERCHANT', 1),
(5,  NULL,            'niall@eddierockets.ie',   NULL,                     '$2a$10$1C8qXV5QSSfC6NXt0uisMOtNmxopEX0Y06qTAGaI3aCYWWNbBUjM6', 'Niall (Eddie Rockets)',      '', 'MERCHANT', 1),
(6,  '+353851234567', NULL,                      NULL,                     NULL, 'Aoife M.',   '', 'USER', 1),
(7,  NULL,            'liam.ryan@gmail.com',     'google-sub-100200300',   NULL, 'Liam Ryan',  '', 'USER', 1),
(8,  '+353867654321', NULL,                      NULL,                     NULL, 'Niamh B.',   '', 'USER', 1),
(9,  NULL,            'sinead.oconnor@gmail.com', 'google-sub-100200301',  NULL, 'Sinead OC.', '', 'USER', 1),
(10, '+353851112222', NULL,                      NULL,                     NULL, 'Cormac D.',  '', 'USER', 1),
(11, NULL,            'roisin.kelly@gmail.com',  'google-sub-100200302',   NULL, 'Roisin K.',  '', 'USER', 1),
(12, '+353867778888', NULL,                      NULL,                     NULL, 'Declan F.',  '', 'USER', 1),
(13, NULL,            'aisling.murphy@gmail.com', 'google-sub-100200303',  NULL, 'Aisling M.', '', 'USER', 1);

-- --- shop categories ---------------------------------------------------------
INSERT INTO `shop_type` (`id`, `name`, `icon`, `sort`) VALUES
(1, 'Restaurant',  '', 1),
(2, 'Cafe',        '', 2),
(3, 'Pub & Bar',   '', 3),
(4, 'Fast Food',   '', 4),
(5, 'Bakery',      '', 5),
(6, 'Cinema',      '', 6),
(7, 'Live Music',  '', 7),
(8, 'Nightclub',   '', 8);

-- --- shops (20, across Dublin / Cork / Galway / Limerick / Kilkenny) --------
INSERT INTO `shop` (`id`, `name`, `type_id`, `owner_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`) VALUES
(1,  'Temple Bar Bistro',    1, 2,    '/images/shops/1.jpg',  'Dublin',    '12 Temple Bar, Dublin 2',           -6.2635, 53.3455, 3500, 128, 0, 0, '12:00-23:00'),
(2,  'Galway Bay Seafood',   1, NULL, '/images/shops/2.jpg',  'Galway',    '3 Quay St, Galway',                 -9.0530, 53.2700, 4200, 59,  0, 0, '12:00-22:00'),
(3,  'The Shelbourne Grill', 1, NULL, '/images/shops/3.jpg',  'Dublin',    '27 St Stephen''s Green, Dublin 2',   -6.2591, 53.3381, 5500, 41,  0, 0, '17:00-22:30'),
(4,  'Limerick Steakhouse',  1, NULL, '/images/shops/4.jpg',  'Limerick',  '8 O''Connell St, Limerick',          -8.6231, 52.6638, 4800, 33,  0, 0, '17:00-22:00'),
(5,  'Cork Roast House',     2, 3,    '/images/shops/5.jpg',  'Cork',      '5 Oliver Plunkett St, Cork',         -8.4720, 51.8975, 1200, 76,  0, 0, '08:00-18:00'),
(6,  'Bewley''s Cafe',       2, NULL, '/images/shops/6.jpg',  'Dublin',    '78 Grafton St, Dublin 2',            -6.2601, 53.3415, 1800, 260, 0, 0, '08:00-20:00'),
(7,  'Kilkenny Coffee Co',   2, NULL, '/images/shops/7.jpg',  'Kilkenny',  '14 High St, Kilkenny',               -7.2561, 52.6541, 1400, 88,  0, 0, '08:00-17:30'),
(8,  'The Brazen Head',      3, 4,    '/images/shops/8.jpg',  'Dublin',    '20 Lower Bridge St, Dublin 8',       -6.2770, 53.3447, 2500, 402, 0, 0, '11:00-00:30'),
(9,  'Tigh Neachtain',       3, NULL, '/images/shops/9.jpg',  'Galway',    '17 Cross St, Galway',                -9.0568, 53.2712, 2200, 190, 0, 0, '11:00-23:30'),
(10, 'The Long Valley',      3, NULL, '/images/shops/10.jpg', 'Cork',      '10 Winthrop St, Cork',               -8.4744, 51.8981, 2000, 145, 0, 0, '12:00-23:00'),
(11, 'Eddie Rockets',        4, 5,    '/images/shops/11.jpg', 'Dublin',    '48 Grafton St, Dublin 2',            -6.2600, 53.3410, 1500, 890, 0, 0, '10:00-23:00'),
(12, 'Supermac''s',          4, NULL, '/images/shops/12.jpg', 'Galway',    '1 Eyre Square, Galway',              -9.0490, 53.2745, 1000, 720, 0, 0, '09:00-01:00'),
(13, 'The Bakehouse',        5, NULL, '/images/shops/13.jpg', 'Cork',      '2 Washington St, Cork',              -8.4790, 51.8965, 900,  180, 0, 0, '07:30-17:00'),
(14, 'Cooke''s Bakery',      5, NULL, '/images/shops/14.jpg', 'Dublin',    '31 Wicklow St, Dublin 2',            -6.2618, 53.3427, 850,  210, 0, 0, '07:00-18:00'),
(15, 'Light House Cinema',   6, NULL, '/images/shops/15.jpg', 'Dublin',    'Market Square, Smithfield, Dublin 7', -6.2779, 53.3477, 1300, 310, 0, 0, '11:00-23:00'),
(16, 'Omniplex Cork',        6, NULL, '/images/shops/16.jpg', 'Cork',      'Mahon Point, Cork',                  -8.4319, 51.8837, 1200, 405, 0, 0, '11:00-23:30'),
(17, 'Roisin Dubh',          7, NULL, '/images/shops/17.jpg', 'Galway',    'Dominick St Upper, Galway',          -9.0560, 53.2705, 2000, 140, 0, 0, '17:00-02:00'),
(18, 'Whelan''s',            7, NULL, '/images/shops/18.jpg', 'Dublin',    '25 Wexford St, Dublin 2',            -6.2649, 53.3336, 2200, 265, 0, 0, '17:00-02:30'),
(19, 'Coppers',               8, NULL, '/images/shops/19.jpg', 'Dublin',    '30 Harcourt St, Dublin 2',           -6.2635, 53.3345, 1000, 520, 0, 0, '23:00-03:00'),
(20, 'Cyprus Avenue',        8, NULL, '/images/shops/20.jpg', 'Cork',      'Caroline St, Cork',                  -8.4738, 51.8977, 1500, 95,  0, 0, '21:00-02:30');

-- --- shop ratings (2 per shop; shop.comments/score are recomputed below) ----
INSERT INTO `shop_rating` (`shop_id`, `user_id`, `score`, `content`) VALUES
(1,  6,  5, 'Lovely spot in the heart of Temple Bar. Great seafood chowder.'),
(1,  7,  4, 'Good food, a bit busy on weekends.'),
(2,  8,  5, 'Best seafood chowder in Galway.'),
(2,  9,  4, 'Great view, slightly pricey.'),
(3,  10, 5, 'Excellent steak, great service.'),
(3,  11, 4, 'Lovely atmosphere for a special occasion.'),
(4,  12, 4, 'Solid steak, friendly staff.'),
(4,  13, 3, 'Good but service was a bit slow.'),
(5,  6,  5, 'Perfect flat white and a cosy corner to work from.'),
(5,  8,  5, 'My favourite coffee spot in Cork.'),
(6,  7,  4, 'Classic Dublin institution, great scones.'),
(6,  9,  4, 'Historic spot, always reliable.'),
(7,  10, 5, 'Best coffee in Kilkenny, hands down.'),
(7,  12, 4, 'Nice and quiet, good for working.'),
(8,  6,  5, 'Oldest pub in Dublin, brilliant trad music.'),
(8,  11, 5, 'Great pint, great craic.'),
(9,  13, 5, 'Authentic Galway pub with a proper snug.'),
(9,  7,  4, 'Lovely traditional seating and atmosphere.'),
(10, 8,  4, 'Old-school Cork charm.'),
(10, 9,  5, 'Fantastic toasted sandwiches with the pint.'),
(11, 10, 4, 'Good late-night burgers.'),
(11, 6,  3, 'Busy on weekends, worth the wait though.'),
(12, 11, 4, 'Quick and tasty, exactly what you need.'),
(12, 12, 4, 'Great chip butty.'),
(13, 13, 5, 'Best sourdough in Cork.'),
(13, 7,  5, 'Fresh pastries every morning, never disappoints.'),
(14, 9,  4, 'Lovely brown bread.'),
(14, 10, 4, 'Great scones, friendly staff.'),
(15, 6,  5, 'Great indie film selection.'),
(15, 11, 4, 'Comfortable seats, good bar too.'),
(16, 12, 4, 'Good for families, plenty of screens.'),
(16, 13, 3, 'A bit pricey for snacks.'),
(17, 7,  5, 'Class live music every night, a Galway must-visit.'),
(17, 8,  5, 'Best trad sessions in the west of Ireland.'),
(18, 9,  5, 'Best live venue in Dublin, hands down.'),
(18, 10, 4, 'Great sound system and lineup.'),
(19, 11, 3, 'Very busy, classic Dublin nightlife.'),
(19, 12, 4, 'Fun if you like the craic.'),
(20, 13, 4, 'Great local acts, good sound.'),
(20, 6,  4, 'Good atmosphere, reasonably priced drinks.');

-- Derive shop.comments/score from the ratings just inserted, instead of
-- hand-computing averages (guarantees consistency).
UPDATE `shop` s
JOIN (
    SELECT `shop_id`, COUNT(*) AS cnt, ROUND(AVG(`score`) * 10) AS avg_score
    FROM `shop_rating`
    GROUP BY `shop_id`
) r ON r.`shop_id` = s.`id`
SET s.`comments` = r.cnt, s.`score` = r.avg_score;

-- --- vouchers (one or more per shop) -----------------------------------------
INSERT INTO `voucher` (`id`, `shop_id`, `title`, `sub_title`, `rules`, `pay_value`, `actual_value`, `type`, `stock`, `status`, `begin_time`, `end_time`) VALUES
(1,  1,  'EUR 50 dinner for EUR 40',   'Two-course dinner for two',        'Valid Mon-Thu. Excludes public holidays.', 4000, 5000, 0, 0,   1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(2,  1,  'Limited: EUR 30 lunch deal', 'Weekday lunch, first 100 only',    'Valid 12:00-15:00. One per customer.',     2000, 3000, 1, 100, 1, '2026-07-01 00:00:00', '2026-08-31 23:59:59'),
(3,  2,  'EUR 15 off seafood platter', 'For two or more people',           'Booking required.',                        3500, 5000, 0, 0,   1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(4,  3,  'Steak night EUR 45',         'Includes a glass of house wine',    'Valid evenings only.',                     4500, 6000, 0, 0,   1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(5,  4,  'EUR 10 off any main',        'Minimum spend EUR 30',              'Dine-in only.',                            0,    1000, 0, 0,   1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(6,  5,  'Coffee & pastry EUR 5',      'Any coffee plus a pastry',          'Dine-in only.',                            500,  750,  0, 0,   1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(7,  5,  'Old promo (off-shelf)',      'Retired offer',                     'No longer available.',                     500,  700,  0, 0,   2, '2025-01-01 00:00:00', '2025-12-31 23:59:59'),
(8,  6,  'Afternoon tea for two',      'EUR 35 for two',                    'Booking recommended.',                     3500, 4500, 0, 0,   1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(9,  7,  'Buy one get one free latte', 'Weekdays before 11am',              'One redemption per visit.',                400,  800,  1, 50,  1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(10, 8,  'Trad night pint & a bite',   'Pint plus a toastie',               'Wed-Sun evenings.',                        1200, 1600, 0, 0,   1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(11, 9,  'Whiskey tasting flight',     '3 Irish whiskeys, guided',           'Over 18s only. ID required.',              1800, 2500, 1, 30,  1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(12, 10, 'Toasted sandwich & pint',    'Classic Cork combo',                'Lunchtime only.',                          1000, 1300, 0, 0,   1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(13, 11, 'Meal deal for EUR 8',        'Burger, fries and a drink',         'Eat-in or takeaway.',                      800,  1100, 0, 0,   1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(14, 12, '2-for-1 chip butty',         'Any two chip butties',              'Eat-in or takeaway.',                      500,  900,  0, 0,   1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(15, 13, 'Dozen pastries for EUR 12',  'Mixed dozen, while stocks last',    'Collection only, morning bake.',           1200, 1800, 1, 20,  1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(16, 14, 'Loaf & butter EUR 4',        'Fresh brown bread',                  'Daily bake.',                              400,  550,  0, 0,   1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(17, 15, '2-for-1 cinema tickets',     'Any weekday screening',              'Excludes premieres.',                      1000, 2000, 0, 0,   1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(18, 16, 'Family ticket bundle',       '2 adults + 2 kids, includes popcorn', 'Weekends and school holidays.',           3500, 5000, 1, 40,  1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(19, 17, 'Gig ticket + first drink',   'Any advertised gig night',           'Subject to availability.',                 1500, 2000, 0, 0,   1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(20, 18, 'Standing ticket discount',   'EUR 5 off any standing show',        'Excludes sold-out shows.',                 0,    500,  0, 0,   1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(21, 19, 'Free entry before 11pm',     'Skip the queue and the door charge', 'ID required. Dress code applies.',         0,    1000, 0, 0,   1, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(22, 20, 'Gig + late bar combo',       'Entry plus late bar access',         'Over 21s only.',                           1000, 1500, 0, 0,   1, '2026-01-01 00:00:00', '2026-12-31 23:59:59');

-- --- posts (blogs), several with images and/or a linked shop -----------------
INSERT INTO `blog` (`id`, `user_id`, `shop_id`, `title`, `images`, `content`, `liked`, `comments`, `featured`, `status`) VALUES
(1,  6,  1,    'A cosy dinner at Temple Bar Bistro',    '/images/posts/4.jpg', 'Went here on Saturday and the seafood chowder was unreal. Highly recommend booking ahead.', 24, 0, 1, 1),
(2,  7,  8,    'Trad night at The Brazen Head',         '/images/posts/2.jpg', 'Ireland''s oldest pub still delivers. Great pint and even better music.', 51, 0, 1, 1),
(3,  9,  NULL, 'My favourite cafes to work from',       '/images/posts/3.jpg', 'A quick roundup of Cork spots with good coffee and reliable wifi.', 12, 0, 0, 1),
(4,  8,  2,    'Best seafood in Galway',                '/images/posts/5.jpg', 'Galway Bay Seafood does not miss. The chowder alone is worth the trip west.', 33, 0, 0, 1),
(5,  10, 17,   'Live music weekend in Galway',          '/images/posts/7.jpg', 'Caught a trad session at Roisin Dubh — absolutely class atmosphere all night.', 40, 0, 1, 1),
(6,  11, 13,   'Cork''s best bakery, hands down',        '/images/posts/6.jpg', 'The Bakehouse sourdough is the real deal. Get there early before it sells out.', 19, 0, 0, 1),
(7,  12, 19,   'Big night out in Dublin',               '/images/posts/8.jpg', 'Coppers is exactly the chaos you''d expect and I loved every minute of it.', 27, 0, 0, 1),
(8,  13, NULL, 'Weekend guide: Dublin on a budget',      '/images/posts/1.jpg', 'Free walking tours, cheap pints before 9pm, and the best chipper on Grafton St.', 15, 0, 0, 1),
(9,  6,  6,    'Bewley''s never gets old',               '',                     'Still my go-to spot for scones and people-watching on Grafton St.', 8,  0, 0, 1),
(10, 9,  10,   'Sunday session at The Long Valley',      '',                     'Toasted sandwich and a pint, what else do you need on a Sunday.', 11, 0, 0, 1),
(11, 7,  15,   'Indie film night at Light House',        '',                     'Caught a brilliant Irish film here last week, great little cinema.', 6,  0, 0, 1),
(12, 13, NULL, 'First month in Dublin: what I''ve learned', '/images/posts/1.jpg', 'From bus routes to the best coffee spots, here''s what surprised me most.', 21, 0, 0, 1);

-- --- comments ------------------------------------------------------------
INSERT INTO `blog_comment` (`user_id`, `blog_id`, `parent_id`, `answer_id`, `content`) VALUES
(7,  1, 0, 0, 'Adding this to my list, thanks!'),
(8,  1, 0, 0, 'The chowder really is the best in Dublin.'),
(6,  2, 0, 0, 'Was there last week — spot on.'),
(9,  2, 0, 0, 'Best trad session in the city, agreed.'),
(6,  3, 0, 0, 'Great list, adding a couple of these to my rotation.'),
(10, 4, 0, 0, 'Been meaning to try this place, thanks for the writeup.'),
(11, 4, 0, 0, 'The view alone is worth it.'),
(7,  5, 0, 0, 'Roisin Dubh never disappoints.'),
(8,  5, 0, 0, 'Wish I was there!'),
(12, 6, 0, 0, 'Their sourdough is unreal, can confirm.'),
(13, 7, 0, 0, 'Classic Dublin night out.'),
(6,  8, 0, 0, 'Great tips, saving this for my next visit.'),
(9,  9, 0, 0, 'A Dublin institution for sure.'),
(11, 10, 0, 0, 'Simple pleasures.'),
(12, 11, 0, 0, 'Love this cinema, hidden gem.'),
(7,  12, 0, 0, 'Welcome to Dublin! Great list.');

UPDATE `blog` b
JOIN (
    SELECT `blog_id`, COUNT(*) AS cnt FROM `blog_comment` GROUP BY `blog_id`
) c ON c.`blog_id` = b.`id`
SET b.`comments` = c.cnt;

-- --- sample voucher orders -----------------------------------------------
INSERT INTO `voucher_order` (`user_id`, `voucher_id`, `pay_type`, `status`, `pay_time`) VALUES
(6, 1, 1, 2, '2026-07-10 19:30:00'),
(8, 6, 1, 2, '2026-07-12 09:15:00'),
(10, 17, 1, 2, '2026-07-13 20:00:00');
