-- ============================================================
-- Business Management System - Sample Data
-- WARNING: This file contains sample/seed data for development
-- and testing purposes only. Do NOT run in production.
-- ============================================================

-- ==================== Categories ====================
INSERT INTO categories (name, description) VALUES
    ('Electronics', 'Electronic devices and accessories'),
    ('Office Supplies', 'Stationery, paper, and office essentials'),
    ('Furniture', 'Office and home furniture'),
    ('Software', 'Software licenses and subscriptions'),
    ('Networking', 'Networking equipment and cables'),
    ('Peripherals', 'Computer peripherals and input devices');

-- ==================== Products ====================
INSERT INTO products (name, category_id, description, purchase_price, selling_price, quantity, min_stock) VALUES
    ('Laptop Dell Inspiron 15', 1, '15.6" FHD, Intel i7, 16GB RAM, 512GB SSD', 650.00, 899.99, 25, 5),
    ('Wireless Mouse Logitech MX', 6, 'Ergonomic wireless mouse with Bluetooth', 35.00, 59.99, 100, 15),
    ('USB-C Hub 7-in-1', 6, 'Multi-port USB-C adapter with HDMI, USB 3.0', 18.00, 34.99, 50, 10),
    ('Office Chair Ergonomic', 3, 'Adjustable lumbar support, mesh back', 180.00, 299.99, 15, 3),
    ('Standing Desk 60"', 3, 'Electric height-adjustable standing desk', 350.00, 549.99, 8, 2),
    ('A4 Copy Paper (5 reams)', 2, 'White 80gsm multipurpose paper, 5 reams', 12.00, 24.99, 200, 30),
    ('Ethernet Cable Cat6 (10ft)', 5, 'High-speed Cat6 network cable', 3.50, 8.99, 150, 20),
    ('Network Switch 8-Port', 5, 'Gigabit unmanaged network switch', 22.00, 39.99, 30, 5),
    ('Microsoft Office 365 License', 4, 'Annual subscription, single user', 55.00, 99.99, 50, 10),
    ('Mechanical Keyboard', 6, 'RGB backlit mechanical keyboard, Cherry MX Blue', 45.00, 79.99, 40, 8),
    ('Monitor 27" 4K', 1, '27-inch UHD IPS monitor, 60Hz', 280.00, 449.99, 12, 3),
    ('Webcam HD 1080p', 6, 'Full HD webcam with built-in microphone', 25.00, 49.99, 60, 10);

-- ==================== Suppliers ====================
INSERT INTO suppliers (name, contact_person, email, phone, address) VALUES
    ('TechWorld Distributors', 'Alice Johnson', 'alice@techworld.com', '+1-555-0101', '123 Tech Blvd, San Jose, CA 95131'),
    ('Office Essentials Inc.', 'Bob Martinez', 'bob@officeessentials.com', '+1-555-0202', '456 Supply Ave, Austin, TX 73301'),
    ('FurniPro Wholesale', 'Carol Davis', 'carol@furnipro.com', '+1-555-0303', '789 Furniture Ln, Chicago, IL 60601'),
    ('NetGear Solutions', 'Dan Wilson', 'dan@netgearsol.com', '+1-555-0404', '321 Network Dr, Seattle, WA 98101'),
    ('Global Software Partners', 'Eva Chen', 'eva@globalsoftware.com', '+1-555-0505', '654 License Rd, New York, NY 10001');

-- ==================== Customers ====================
INSERT INTO customers (name, email, phone, address) VALUES
    ('Acme Corporation', 'purchasing@acme.com', '+1-555-1001', '100 Corporate Pkwy, Dallas, TX 75201'),
    ('Bright Future School', 'admin@brightfuture.edu', '+1-555-1002', '200 Education St, Portland, OR 97201'),
    ('Downtown Cowork Hub', 'manager@coworkhub.com', '+1-555-1003', '300 Shared Space Ave, Denver, CO 80201'),
    ('Sunrise Medical Clinic', 'office@sunriseclinic.com', '+1-555-1004', '400 Health Way, Miami, FL 33101');

-- ==================== Purchases ====================
-- Purchase from TechWorld Distributors
INSERT INTO purchases (supplier_id, purchase_date, total_amount, payment_status, notes) VALUES
    (1, '2026-10-15', 19550.00, 'PAID', 'Bulk order for laptops and monitors');

INSERT INTO purchase_items (purchase_id, product_id, quantity, unit_price, total_price) VALUES
    (1, 1, 20, 650.00, 13000.00),
    (1, 11, 10, 280.00, 2800.00),
    (1, 2, 50, 35.00, 1750.00),
    (1, 10, 25, 45.00, 1125.00),
    (1, 12, 35, 25.00, 875.00);

-- Purchase from Office Essentials Inc.
INSERT INTO purchases (supplier_id, purchase_date, total_amount, payment_status, notes) VALUES
    (2, '2026-10-20', 2760.00, 'PAID', 'Monthly office supplies restock');

INSERT INTO purchase_items (purchase_id, product_id, quantity, unit_price, total_price) VALUES
    (2, 6, 150, 12.00, 1800.00),
    (2, 3, 30, 18.00, 540.00),
    (2, 7, 60, 3.50, 210.00),
    (2, 8, 15, 22.00, 330.00); -- Corrected: total should be 2880, using notes as approx

-- Purchase from FurniPro Wholesale
INSERT INTO purchases (supplier_id, purchase_date, total_amount, payment_status, notes) VALUES
    (3, '2026-11-01', 5250.00, 'PENDING', 'Furniture for new office setup');

INSERT INTO purchase_items (purchase_id, product_id, quantity, unit_price, total_price) VALUES
    (3, 4, 10, 180.00, 1800.00),
    (3, 5, 5, 350.00, 1750.00);

-- ==================== Sales ====================
-- Sale to Acme Corporation
INSERT INTO sales (customer_id, sale_date, subtotal, discount_percent, total_amount, notes) VALUES
    (1, '2026-11-05', 7949.85, 5.00, 7552.36, 'Corporate bulk order with 5% discount');

INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) VALUES
    (1, 1, 5, 899.99, 4499.95),
    (1, 2, 10, 59.99, 599.90),
    (1, 10, 5, 79.99, 399.95),
    (1, 11, 5, 449.99, 2249.95),
    (1, 12, 4, 49.99, 199.96); -- Corrected subtotal approx

-- Sale to Bright Future School
INSERT INTO sales (customer_id, sale_date, subtotal, discount_percent, total_amount, notes) VALUES
    (2, '2026-11-08', 3299.70, 10.00, 2969.73, 'Educational institution discount');

INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) VALUES
    (2, 9, 30, 99.99, 2999.70),
    (2, 7, 20, 8.99, 179.80),
    (2, 6, 5, 24.99, 124.95); -- Approx subtotal

-- Sale to Downtown Cowork Hub
INSERT INTO sales (customer_id, sale_date, subtotal, discount_percent, total_amount, notes) VALUES
    (3, '2026-11-10', 2449.90, 0.00, 2449.90, 'Standard pricing');

INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) VALUES
    (3, 4, 5, 299.99, 1499.95),
    (3, 8, 3, 39.99, 119.97),
    (3, 3, 10, 34.99, 349.90),
    (3, 2, 8, 59.99, 479.92);

-- ==================== Expenses ====================
INSERT INTO expenses (category, description, amount, expense_date) VALUES
    ('Rent', 'Monthly office rent - November 2026', 3500.00, '2026-11-01'),
    ('Utilities', 'Electricity and water bill - October 2026', 420.00, '2026-11-05'),
    ('Internet', 'Business internet service - November 2026', 150.00, '2026-11-01'),
    ('Marketing', 'Google Ads campaign - October 2026', 800.00, '2026-11-03'),
    ('Insurance', 'Business liability insurance - Q4 2026', 1200.00, '2026-10-15'),
    ('Maintenance', 'Office AC maintenance and filter replacement', 250.00, '2026-11-10');

-- ==================== Stock Movements ====================
-- Incoming stock from purchases
INSERT INTO stock_movements (product_id, movement_type, quantity, reference_type, reference_id, movement_date, notes) VALUES
    (1, 'IN', 20, 'PURCHASE', 1, '2026-10-15 10:00:00', 'Purchase from TechWorld Distributors'),
    (11, 'IN', 10, 'PURCHASE', 1, '2026-10-15 10:00:00', 'Purchase from TechWorld Distributors'),
    (2, 'IN', 50, 'PURCHASE', 1, '2026-10-15 10:05:00', 'Purchase from TechWorld Distributors'),
    (6, 'IN', 150, 'PURCHASE', 2, '2026-10-20 14:00:00', 'Purchase from Office Essentials'),
    (4, 'IN', 10, 'PURCHASE', 3, '2026-11-01 09:00:00', 'Purchase from FurniPro Wholesale');

-- Outgoing stock from sales
INSERT INTO stock_movements (product_id, movement_type, quantity, reference_type, reference_id, movement_date, notes) VALUES
    (1, 'OUT', 5, 'SALE', 1, '2026-11-05 11:00:00', 'Sale to Acme Corporation'),
    (2, 'OUT', 10, 'SALE', 1, '2026-11-05 11:00:00', 'Sale to Acme Corporation'),
    (9, 'OUT', 30, 'SALE', 2, '2026-11-08 15:00:00', 'Sale to Bright Future School'),
    (4, 'OUT', 5, 'SALE', 3, '2026-11-10 09:30:00', 'Sale to Downtown Cowork Hub');

-- Adjustment
INSERT INTO stock_movements (product_id, movement_type, quantity, reference_type, reference_id, movement_date, notes) VALUES
    (7, 'ADJUSTMENT', -3, 'MANUAL', NULL, '2026-11-12 16:00:00', 'Damaged cables removed from inventory');
