-- =============================================
-- ALOTRA MILK TEA WEBSITE - SQL SERVER DATABASE
-- =============================================
-- Author: AloTra Development Team
-- Date: 2025
-- Description: Complete database schema for AloTra Milk Tea Chain
-- Technologies: Spring Boot + JPA + SQL Server + JWT + WebSocket + Cloudinary
-- =============================================

USE master;
GO

-- Drop database if exists
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'AloTraDB')
BEGIN
    ALTER DATABASE AloTraDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE AloTraDB;
END
GO

-- Create database
CREATE DATABASE AloTraDB;
GO

USE AloTraDB;
GO

-- =============================================
-- 1. USERS TABLE (Authentication with JWT)
-- =============================================
CREATE TABLE users (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    username NVARCHAR(50) NOT NULL UNIQUE,
    email NVARCHAR(100) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    phone NVARCHAR(20),
    avatar_url NVARCHAR(500), -- Cloudinary URL
    role NVARCHAR(20) NOT NULL DEFAULT 'USER', -- USER, ADMIN
    is_active BIT NOT NULL DEFAULT 1,
    email_verified BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    last_login DATETIME2,
    CONSTRAINT CHK_user_role CHECK (role IN ('USER', 'ADMIN'))
);
GO

-- =============================================
-- 2. REFRESH TOKENS TABLE (JWT)
-- =============================================
CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT NOT NULL,
    token NVARCHAR(500) NOT NULL UNIQUE,
    expiry_date DATETIME2 NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
GO

-- =============================================
-- 3. CATEGORIES TABLE
-- =============================================
CREATE TABLE categories (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) NOT NULL UNIQUE,
    slug NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(500),
    image_url NVARCHAR(500), -- Cloudinary URL
    display_order INT NOT NULL DEFAULT 0,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- =============================================
-- 4. PRODUCTS TABLE
-- =============================================
CREATE TABLE products (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    category_id BIGINT NOT NULL,
    name NVARCHAR(200) NOT NULL,
    slug NVARCHAR(200) NOT NULL UNIQUE,
    description NVARCHAR(1000),
    long_description NVARCHAR(MAX),
    price DECIMAL(10,2) NOT NULL,
    image_url NVARCHAR(500), -- Main image from Cloudinary
    calories INT,
    rating DECIMAL(3,2) DEFAULT 0,
    review_count INT DEFAULT 0,
    is_vegan BIT DEFAULT 0,
    is_sustainable BIT DEFAULT 0,
    is_bestseller BIT DEFAULT 0,
    is_active BIT NOT NULL DEFAULT 1,
    stock_quantity INT DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT CHK_price_positive CHECK (price >= 0),
    CONSTRAINT CHK_rating_range CHECK (rating >= 0 AND rating <= 5)
);
GO

-- =============================================
-- 5. PRODUCT IMAGES TABLE (Cloudinary)
-- =============================================
CREATE TABLE product_images (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    product_id BIGINT NOT NULL,
    image_url NVARCHAR(500) NOT NULL, -- Cloudinary URL
    cloudinary_public_id NVARCHAR(200), -- For deletion
    display_order INT NOT NULL DEFAULT 0,
    is_primary BIT DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
GO

-- =============================================
-- 6. TAGS TABLE
-- =============================================
CREATE TABLE tags (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(50) NOT NULL UNIQUE,
    slug NVARCHAR(50) NOT NULL UNIQUE,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- =============================================
-- 7. PRODUCT TAGS (Many-to-Many)
-- =============================================
CREATE TABLE product_tags (
    product_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (product_id, tag_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);
GO

-- =============================================
-- 8. INGREDIENTS TABLE
-- =============================================
CREATE TABLE ingredients (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(500),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- =============================================
-- 9. PRODUCT INGREDIENTS (Many-to-Many)
-- =============================================
CREATE TABLE product_ingredients (
    product_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    PRIMARY KEY (product_id, ingredient_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE CASCADE
);
GO

-- =============================================
-- 10. ALLERGENS TABLE
-- =============================================
CREATE TABLE allergens (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(500),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- =============================================
-- 11. PRODUCT ALLERGENS (Many-to-Many)
-- =============================================
CREATE TABLE product_allergens (
    product_id BIGINT NOT NULL,
    allergen_id BIGINT NOT NULL,
    PRIMARY KEY (product_id, allergen_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (allergen_id) REFERENCES allergens(id) ON DELETE CASCADE
);
GO

-- =============================================
-- 12. NUTRITION FACTS TABLE
-- =============================================
CREATE TABLE nutrition_facts (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    product_id BIGINT NOT NULL UNIQUE,
    calories INT NOT NULL,
    protein NVARCHAR(20),
    carbs NVARCHAR(20),
    fat NVARCHAR(20),
    sugar NVARCHAR(20),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
GO

-- =============================================
-- 13. ADDRESSES TABLE
-- =============================================
CREATE TABLE addresses (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    phone NVARCHAR(20) NOT NULL,
    address_line1 NVARCHAR(200) NOT NULL,
    address_line2 NVARCHAR(200),
    city NVARCHAR(100) NOT NULL,
    state NVARCHAR(100),
    postal_code NVARCHAR(20),
    country NVARCHAR(100) NOT NULL DEFAULT 'Vietnam',
    is_default BIT DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
GO

-- =============================================
-- 14. COUPONS TABLE
-- =============================================
CREATE TABLE coupons (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    code NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(500),
    discount_type NVARCHAR(20) NOT NULL, -- PERCENTAGE, FIXED
    discount_value DECIMAL(10,2) NOT NULL,
    min_order_amount DECIMAL(10,2) DEFAULT 0,
    max_discount_amount DECIMAL(10,2),
    usage_limit INT,
    used_count INT DEFAULT 0,
    start_date DATETIME2 NOT NULL,
    end_date DATETIME2 NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT CHK_discount_type CHECK (discount_type IN ('PERCENTAGE', 'FIXED')),
    CONSTRAINT CHK_discount_value CHECK (discount_value >= 0)
);
GO

-- =============================================
-- 15. ORDERS TABLE
-- =============================================
CREATE TABLE orders (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT NOT NULL,
    order_number NVARCHAR(50) NOT NULL UNIQUE,
    status NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
    subtotal DECIMAL(10,2) NOT NULL,
    shipping_fee DECIMAL(10,2) DEFAULT 0,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL,
    coupon_id BIGINT,
    shipping_address_id BIGINT NOT NULL,
    payment_method NVARCHAR(50) NOT NULL,
    payment_status NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes NVARCHAR(1000),
    estimated_delivery DATETIME2,
    delivered_at DATETIME2,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id),
    FOREIGN KEY (shipping_address_id) REFERENCES addresses(id),
    CONSTRAINT CHK_order_status CHECK (status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'SHIPPING', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT CHK_payment_status CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED'))
);
GO

-- =============================================
-- 16. ORDER ITEMS TABLE
-- =============================================
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name NVARCHAR(200) NOT NULL, -- Store name at order time
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL, -- Store price at order time
    subtotal DECIMAL(10,2) NOT NULL,
    customizations NVARCHAR(MAX), -- JSON string for customizations
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT CHK_quantity_positive CHECK (quantity > 0)
);
GO

-- =============================================
-- 17. CART TABLE
-- =============================================
CREATE TABLE cart (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
GO

-- =============================================
-- 18. CART ITEMS TABLE
-- =============================================
CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    customizations NVARCHAR(MAX), -- JSON string for customizations
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (cart_id) REFERENCES cart(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT CHK_cart_quantity_positive CHECK (quantity > 0)
);
GO

-- =============================================
-- 19. REVIEWS TABLE
-- =============================================
CREATE TABLE reviews (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT,
    rating INT NOT NULL,
    title NVARCHAR(200),
    comment NVARCHAR(2000),
    is_verified_purchase BIT DEFAULT 0,
    helpful_count INT DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT CHK_review_rating CHECK (rating >= 1 AND rating <= 5)
);
GO

-- =============================================
-- 20. BLOGS TABLE
-- =============================================
CREATE TABLE blogs (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    author_id BIGINT NOT NULL,
    title NVARCHAR(300) NOT NULL,
    slug NVARCHAR(300) NOT NULL UNIQUE,
    excerpt NVARCHAR(500),
    content NVARCHAR(MAX) NOT NULL,
    featured_image NVARCHAR(500), -- Cloudinary URL
    status NVARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    view_count INT DEFAULT 0,
    published_at DATETIME2,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (author_id) REFERENCES users(id),
    CONSTRAINT CHK_blog_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);
GO

-- =============================================
-- 21. PROMOTIONS TABLE
-- =============================================
CREATE TABLE promotions (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    title NVARCHAR(200) NOT NULL,
    slug NVARCHAR(200) NOT NULL UNIQUE,
    description NVARCHAR(1000),
    banner_image NVARCHAR(500), -- Cloudinary URL
    discount_type NVARCHAR(20) NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    start_date DATETIME2 NOT NULL,
    end_date DATETIME2 NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT CHK_promotion_discount_type CHECK (discount_type IN ('PERCENTAGE', 'FIXED'))
);
GO

-- =============================================
-- 22. NOTIFICATIONS TABLE (for WebSocket)
-- =============================================
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT NOT NULL,
    type NVARCHAR(50) NOT NULL, -- ORDER_STATUS, PROMOTION, SYSTEM
    title NVARCHAR(200) NOT NULL,
    message NVARCHAR(1000) NOT NULL,
    link NVARCHAR(500),
    is_read BIT DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT CHK_notification_type CHECK (type IN ('ORDER_STATUS', 'PROMOTION', 'SYSTEM', 'REVIEW'))
);
GO

-- =============================================
-- INDEXES for Performance
-- =============================================

-- Users
CREATE INDEX IDX_users_email ON users(email);
CREATE INDEX IDX_users_username ON users(username);
CREATE INDEX IDX_users_role ON users(role);

-- Products
CREATE INDEX IDX_products_category ON products(category_id);
CREATE INDEX IDX_products_slug ON products(slug);
CREATE INDEX IDX_products_is_active ON products(is_active);
CREATE INDEX IDX_products_rating ON products(rating);

-- Orders
CREATE INDEX IDX_orders_user ON orders(user_id);
CREATE INDEX IDX_orders_status ON orders(status);
CREATE INDEX IDX_orders_created ON orders(created_at);
CREATE INDEX IDX_orders_number ON orders(order_number);

-- Reviews
CREATE INDEX IDX_reviews_product ON reviews(product_id);
CREATE INDEX IDX_reviews_user ON reviews(user_id);

-- Notifications
CREATE INDEX IDX_notifications_user ON notifications(user_id);
CREATE INDEX IDX_notifications_read ON notifications(is_read);

GO

PRINT 'Database schema created successfully!';
PRINT 'Total tables: 22';
PRINT 'Database: AloTraDB';
GO
