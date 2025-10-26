-- =============================================
-- ALOTRA MILK TEA - SAMPLE DATA
-- =============================================
-- Insert sample data for testing
-- =============================================

USE AloTraDB;
GO

-- =============================================
-- 1. INSERT CATEGORIES
-- =============================================
INSERT INTO categories (name, slug, description, display_order) VALUES
('Classic Milk Tea', 'classic-milk-tea', 'Traditional milk tea with classic flavors', 1),
('Green Tea Series', 'green-tea-series', 'Premium green tea and matcha drinks', 2),
('Fruit Tea', 'fruit-tea', 'Refreshing fruit-infused tea drinks', 3),
('Premium Collection', 'premium-collection', 'Exclusive premium beverages', 4),
('Seasonal Specials', 'seasonal-specials', 'Limited time seasonal offerings', 5);
GO

-- =============================================
-- 2. INSERT TAGS
-- =============================================
INSERT INTO tags (name, slug) VALUES
('Bestseller', 'bestseller'),
('New', 'new'),
('Premium', 'premium'),
('Vegan', 'vegan'),
('Antioxidant', 'antioxidant'),
('Low Calorie', 'low-calorie'),
('Sustainable', 'sustainable'),
('Classic', 'classic');
GO

-- =============================================
-- 3. INSERT INGREDIENTS
-- =============================================
INSERT INTO ingredients (name, description) VALUES
('Ceylon Black Tea', 'Premium black tea from Sri Lanka'),
('Fresh Milk', 'Locally sourced fresh milk'),
('Brown Sugar Pearls', 'Handmade brown sugar boba pearls'),
('Natural Sweetener', 'Organic cane sugar'),
('Japanese Matcha', 'Ceremonial grade matcha from Uji, Japan'),
('Oat Milk', 'Creamy plant-based milk alternative'),
('Honey Pearls', 'Sweet honey-flavored pearls'),
('Mango Puree', 'Fresh mango fruit puree'),
('Passion Fruit', 'Tropical passion fruit pulp'),
('Coconut Milk', 'Rich creamy coconut milk'),
('Taro Powder', 'Natural taro root powder'),
('Cheese Foam', 'Creamy salted cheese topping');
GO

-- =============================================
-- 4. INSERT ALLERGENS
-- =============================================
INSERT INTO allergens (name, description) VALUES
('Dairy', 'Contains milk or milk products'),
('Nuts', 'Contains tree nuts or peanuts'),
('Soy', 'Contains soy or soy products'),
('Gluten', 'Contains wheat or gluten');
GO

-- =============================================
-- 5. INSERT USERS (Password: password123)
-- Note: In real app, use BCrypt hashed passwords
-- =============================================
INSERT INTO users (username, email, password_hash, full_name, phone, role, email_verified) VALUES
('admin', 'admin@alotra.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Admin User', '0901234567', 'ADMIN', 1),
('johndoe', 'john@example.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'John Doe', '0909876543', 'USER', 1),
('janedoe', 'jane@example.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Jane Doe', '0908765432', 'USER', 1);
GO

-- =============================================
-- 6. INSERT PRODUCTS
-- =============================================
INSERT INTO products (category_id, name, slug, description, long_description, price, calories, rating, review_count, is_vegan, is_sustainable, is_bestseller, stock_quantity) VALUES
(1, 'Classic Milk Tea', 'classic-milk-tea', 'Our signature black tea with creamy milk and brown sugar pearls', 
'Experience the perfect blend of traditional Ceylon black tea with rich, creamy milk and hand-made brown sugar pearls. This classic combination has been our bestseller since day one, offering a comforting taste that brings back childhood memories.', 
4.50, 180, 4.8, 156, 0, 1, 1, 100),

(2, 'Matcha Supreme', 'matcha-supreme', 'Premium Japanese matcha with oat milk and honey pearls',
'Sourced directly from Uji, Japan, our ceremonial-grade matcha delivers an authentic, rich umami flavor with natural sweetness. Paired with creamy oat milk and delicate honey pearls, this drink offers a perfect balance of tradition and innovation.',
5.90, 150, 4.9, 203, 1, 1, 1, 80),

(3, 'Tropical Passion', 'tropical-passion', 'Mango and passion fruit blend with coconut milk',
'A refreshing tropical escape in every sip! Sweet mango puree meets tangy passion fruit, balanced with smooth coconut milk and chewy pearls. Perfect for hot summer days or when you need a vacation in a cup.',
5.20, 160, 4.7, 98, 1, 1, 0, 120),

(1, 'Brown Sugar Boba', 'brown-sugar-boba', 'Rich brown sugar syrup with fresh milk and signature pearls',
'Watch the mesmerizing tiger stripes form as brown sugar cascades down the cup! Our signature drink features thick brown sugar syrup, creamy fresh milk, and perfectly cooked chewy boba pearls.',
6.50, 220, 4.9, 312, 0, 1, 1, 90),

(4, 'Taro Cloud', 'taro-cloud', 'Purple taro milk tea topped with fluffy cheese foam',
'A Instagram-worthy drink that tastes as good as it looks! Natural taro root creates a beautiful purple hue, topped with our signature salted cheese foam that adds a savory-sweet complexity.',
6.90, 200, 4.6, 87, 0, 0, 0, 60);
GO

-- =============================================
-- 7. INSERT PRODUCT TAGS
-- =============================================
INSERT INTO product_tags (product_id, tag_id) VALUES
(1, 1), (1, 8), -- Classic Milk Tea: Bestseller, Classic
(2, 1), (2, 3), (2, 4), (2, 5), -- Matcha: Bestseller, Premium, Vegan, Antioxidant
(3, 4), (3, 6), (3, 7), -- Tropical: Vegan, Low Calorie, Sustainable
(4, 1), (4, 8), -- Brown Sugar: Bestseller, Classic
(5, 2), (5, 3); -- Taro: New, Premium
GO

-- =============================================
-- 8. INSERT PRODUCT INGREDIENTS
-- =============================================
-- Classic Milk Tea
INSERT INTO product_ingredients (product_id, ingredient_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4);

-- Matcha Supreme
INSERT INTO product_ingredients (product_id, ingredient_id) VALUES
(2, 5), (2, 6), (2, 7);

-- Tropical Passion
INSERT INTO product_ingredients (product_id, ingredient_id) VALUES
(3, 8), (3, 9), (3, 10);

-- Brown Sugar Boba
INSERT INTO product_ingredients (product_id, ingredient_id) VALUES
(4, 2), (4, 3);

-- Taro Cloud
INSERT INTO product_ingredients (product_id, ingredient_id) VALUES
(5, 11), (5, 2), (5, 12);
GO

-- =============================================
-- 9. INSERT PRODUCT ALLERGENS
-- =============================================
INSERT INTO product_allergens (product_id, allergen_id) VALUES
(1, 1), -- Classic: Dairy
(4, 1), -- Brown Sugar: Dairy
(5, 1); -- Taro: Dairy
GO

-- =============================================
-- 10. INSERT NUTRITION FACTS
-- =============================================
INSERT INTO nutrition_facts (product_id, calories, protein, carbs, fat, sugar) VALUES
(1, 180, '8g', '24g', '6g', '18g'),
(2, 150, '6g', '20g', '5g', '12g'),
(3, 160, '2g', '38g', '1g', '32g'),
(4, 220, '9g', '32g', '7g', '26g'),
(5, 200, '7g', '28g', '8g', '20g');
GO

-- =============================================
-- 11. INSERT COUPONS
-- =============================================
INSERT INTO coupons (code, description, discount_type, discount_value, min_order_amount, start_date, end_date) VALUES
('WELCOME10', '10% off your first order', 'PERCENTAGE', 10, 0, GETDATE(), DATEADD(YEAR, 1, GETDATE())),
('FIRST20', '20% off for new customers', 'PERCENTAGE', 20, 15, GETDATE(), DATEADD(YEAR, 1, GETDATE())),
('STUDENT15', '15% student discount', 'PERCENTAGE', 15, 0, GETDATE(), DATEADD(YEAR, 1, GETDATE())),
('SAVE5', '$5 off orders over $25', 'FIXED', 5, 25, GETDATE(), DATEADD(YEAR, 1, GETDATE()));
GO

-- =============================================
-- 12. INSERT ADDRESSES
-- =============================================
INSERT INTO addresses (user_id, full_name, phone, address_line1, city, state, postal_code, is_default) VALUES
(2, 'John Doe', '0909876543', '123 Nguyen Hue Street', 'Ho Chi Minh City', 'Ho Chi Minh', '700000', 1),
(3, 'Jane Doe', '0908765432', '456 Le Loi Boulevard', 'Ho Chi Minh City', 'Ho Chi Minh', '700000', 1);
GO

-- =============================================
-- 13. INSERT PROMOTIONS
-- =============================================
INSERT INTO promotions (title, slug, description, discount_type, discount_value, start_date, end_date) VALUES
('Grand Opening Sale', 'grand-opening-sale', 'Celebrate our grand opening with 30% off all drinks!', 'PERCENTAGE', 30, GETDATE(), DATEADD(MONTH, 1, GETDATE())),
('Buy 2 Get 1 Free', 'buy-2-get-1-free', 'Purchase any 2 drinks and get the 3rd one free!', 'PERCENTAGE', 33, GETDATE(), DATEADD(MONTH, 2, GETDATE()));
GO

-- =============================================
-- 14. INSERT BLOGS
-- =============================================
INSERT INTO blogs (author_id, title, slug, excerpt, content, status, published_at) VALUES
(1, 'The Art of Making Perfect Milk Tea', 'art-of-making-perfect-milk-tea', 
'Discover the secrets behind creating the perfect cup of milk tea', 
'<h2>Introduction</h2><p>Making perfect milk tea is both an art and a science. In this comprehensive guide, we will explore the techniques and ingredients that make our milk tea special...</p>',
'PUBLISHED', GETDATE()),

(1, 'Health Benefits of Matcha', 'health-benefits-of-matcha',
'Learn why matcha is more than just a delicious beverage',
'<h2>Why Matcha?</h2><p>Matcha is packed with antioxidants and provides numerous health benefits. From boosting metabolism to improving focus, matcha is a superfood in a cup...</p>',
'PUBLISHED', GETDATE());
GO

PRINT 'Sample data inserted successfully!';
PRINT '- 5 Categories';
PRINT '- 8 Tags';
PRINT '- 12 Ingredients';
PRINT '- 4 Allergens';
PRINT '- 3 Users (admin@alotra.com, john@example.com, jane@example.com)';
PRINT '- 5 Products';
PRINT '- 4 Coupons';
PRINT '- 2 Promotions';
PRINT '- 2 Blogs';
GO
