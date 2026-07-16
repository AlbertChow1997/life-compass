-- MySQL dump 10.13  Distrib 8.3.0, for Win64 (x86_64)
--
-- Host: localhost    Database: lifecompass
-- ------------------------------------------------------
-- Server version	8.3.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `tb_ai_faq`
--

LOCK TABLES `tb_ai_faq` WRITE;
/*!40000 ALTER TABLE `tb_ai_faq` DISABLE KEYS */;
INSERT INTO `tb_ai_faq` (`id`, `question`, `answer`, `category`, `keywords`, `status`, `create_time`, `update_time`) VALUES (1,'How do I create an account?','You can create a LifeCompass account using your email address, Irish mobile number, or Google account.','Account','account,register,sign up,email,google,phone',1,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(2,'How do I search for places in Dublin?','You can use the search bar and filter by city, category, rating, price level, and student-friendly options.','Search','search,Dublin,filter,category,rating',1,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(3,'How do vouchers work?','Some businesses offer vouchers or student discounts. You can claim an available voucher and use it before the expiry date.','Voucher','voucher,discount,student discount,claim',1,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(4,'Can I write a review?','Yes. After logging in, you can write a review, upload images, and give a rating from 1 to 5 stars.','Review','review,rating,stars,images',1,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(5,'Can I save a business?','Yes. You can save your favourite businesses and view them later in your saved places list.','Saved Places','save,favourite,business,place',1,'2026-07-15 10:36:46','2026-07-15 10:36:46');
/*!40000 ALTER TABLE `tb_ai_faq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `tb_business`
--

LOCK TABLES `tb_business` WRITE;
/*!40000 ALTER TABLE `tb_business` DISABLE KEYS */;
INSERT INTO `tb_business` (`id`, `category_id`, `name`, `cuisine_type`, `image_url`, `images`, `description`, `address`, `area`, `city`, `county`, `eircode`, `latitude`, `longitude`, `phone`, `email`, `website`, `google_map_url`, `opening_hours`, `average_price`, `price_level`, `rating`, `review_count`, `liked_count`, `saved_count`, `student_friendly`, `student_discount`, `vegetarian_friendly`, `halal_available`, `takeaway_available`, `booking_required`, `status`, `create_time`, `update_time`) VALUES (1,2,'Docklands Study Cafe','Coffee and Brunch','/imgs/business/docklands-cafe.jpg',NULL,'A quiet cafe near the Dublin Docklands area, suitable for students and remote workers.','Mayor Street Lower','Dublin 1','Dublin','County Dublin','D01 F2X9',53.3498053,-6.2409853,'+35318000001','hello@docklandsstudycafe.ie','https://example.com/docklands-study-cafe','https://maps.google.com','Mon-Fri 08:00-18:00; Sat-Sun 09:00-17:00',12.50,'€€',4.6,128,0,0,1,1,1,0,1,0,1,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(2,1,'Temple Bar Irish Kitchen','Irish Food','/imgs/business/temple-bar-kitchen.jpg',NULL,'Traditional Irish dishes located near Temple Bar, popular with tourists.','18 Temple Bar','Temple Bar','Dublin','County Dublin','D02 XW24',53.3456000,-6.2642000,'+35318000002','booking@templebaririshkitchen.ie','https://example.com/temple-bar-irish-kitchen','https://maps.google.com','Daily 12:00-22:00',28.00,'€€€',4.4,216,0,0,0,0,1,0,0,1,1,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(3,1,'Galway Bay Seafood House','Seafood','/imgs/business/galway-seafood.jpg',NULL,'Fresh seafood restaurant close to Galway city centre.','Quay Street','City Centre','Galway','County Galway','H91 AB12',53.2707000,-9.0568000,'+35391000003','info@galwaybayseafood.ie','https://example.com/galway-bay-seafood-house','https://maps.google.com','Tue-Sun 12:00-21:30',32.00,'€€€',4.7,189,0,0,0,0,1,0,0,1,1,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(4,1,'Cork Asian Street Food','Asian Food','/imgs/business/cork-asian-food.jpg',NULL,'Affordable Asian street food restaurant in Cork city centre.','Patrick Street','City Centre','Cork','County Cork','T12 CD34',51.8985000,-8.4756000,'+35321000004','hello@corkasianstreetfood.ie','https://example.com/cork-asian-street-food','https://maps.google.com','Mon-Sun 11:30-21:00',15.00,'€€',4.5,95,0,0,1,1,1,1,1,0,1,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(5,5,'Liffey Fitness Club','Gym','/imgs/business/liffey-fitness.jpg',NULL,'Modern gym with flexible memberships and student plans.','North Wall Quay','Dublin 1','Dublin','County Dublin','D01 GH56',53.3481000,-6.2455000,'+35318000005','support@liffeyfitness.ie','https://example.com/liffey-fitness-club','https://maps.google.com','Mon-Fri 06:00-22:00; Sat-Sun 08:00-20:00',45.00,'€€',4.3,74,0,0,1,1,0,0,0,0,1,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(6,4,'Dublin City Barber','Barber','/imgs/business/dublin-city-barber.jpg',NULL,'Local barber shop offering haircuts, beard trim and student discounts.','Parnell Street','Dublin 1','Dublin','County Dublin','D01 IJ78',53.3509000,-6.2651000,'+35318000006','contact@dublincitybarber.ie','https://example.com/dublin-city-barber','https://maps.google.com','Mon-Sat 10:00-19:00',22.00,'€€',4.5,62,0,0,1,1,0,0,0,0,1,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(7,7,'Cliffs of Moher Visitor Stop','Tourist Attraction','/imgs/business/cliffs-moher.jpg',NULL,'Visitor information and local guide recommendations for the Cliffs of Moher.','Liscannor','Cliffs of Moher','Liscannor','County Clare','V95 KN9T',52.9715000,-9.4309000,'+35365000007','info@moherstops.ie','https://example.com/cliffs-of-moher-visitor-stop','https://maps.google.com','Daily 09:00-17:00',10.00,'€',4.8,350,0,0,1,0,0,0,0,0,1,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(8,6,'NCI Student Lunch Spot','Student Food','/imgs/business/nci-lunch.jpg',NULL,'Budget-friendly lunch spot near the National College of Ireland.','Mayor Square','Dublin 1','Dublin','County Dublin','D01 K6W2',53.3493000,-6.2427000,'+35318000008','team@ncistudentlunch.ie','https://example.com/nci-student-lunch-spot','https://maps.google.com','Mon-Fri 10:30-16:00',9.50,'€',4.4,88,0,0,1,1,1,1,1,0,1,'2026-07-15 10:36:46','2026-07-15 10:36:46');
/*!40000 ALTER TABLE `tb_business` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `tb_business_category`
--

LOCK TABLES `tb_business_category` WRITE;
/*!40000 ALTER TABLE `tb_business_category` DISABLE KEYS */;
INSERT INTO `tb_business_category` (`id`, `name`, `icon`, `description`, `sort_order`, `create_time`, `update_time`) VALUES (1,'Restaurants','/icons/restaurants.png','Local restaurants, Irish food, Asian food and international cuisine',1,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(2,'Cafes','/icons/cafes.png','Coffee shops, study cafes and brunch places',2,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(3,'Bars and Pubs','/icons/pubs.png','Irish pubs, cocktail bars and nightlife places',3,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(4,'Beauty and Hair','/icons/beauty.png','Hair salons, barbers, nail salons and beauty services',4,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(5,'Fitness and Gyms','/icons/fitness.png','Gyms, yoga studios, sports clubs and fitness centres',5,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(6,'Student Services','/icons/student.png','Student-friendly services near colleges and universities',6,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(7,'Tourist Attractions','/icons/attractions.png','Museums, landmarks and places to visit in Ireland',7,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(8,'Shopping','/icons/shopping.png','Shopping centres, local stores and lifestyle shops',8,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(9,'Healthcare','/icons/healthcare.png','Clinics, pharmacies and healthcare-related services',9,'2026-07-15 10:36:46','2026-07-15 10:36:46'),(10,'Accommodation','/icons/accommodation.png','Student accommodation, hostels and short-stay places',10,'2026-07-15 10:36:46','2026-07-15 10:36:46');
/*!40000 ALTER TABLE `tb_business_category` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-16 22:29:24
