package com.econest.bootstrap;

import com.econest.entity.*;
import com.econest.repository.CategoryRepository;
import com.econest.repository.OrderRepository;
import com.econest.repository.ProductRepository;
import com.econest.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          ProductRepository productRepository,
                          OrderRepository orderRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedCategoriesAndProducts();
        seedSampleOrders();
    }

    private void seedUsers() {
        if (!userRepository.existsByEmail("admin@econest.com")) {
            User admin = new User(
                    "admin@econest.com",
                    passwordEncoder.encode("Admin@123"),
                    "EcoNest System Administrator",
                    "+1 (555) 019-2831",
                    Role.ROLE_ADMIN
            );
            userRepository.save(admin);
            log.info("Default admin account created: admin@econest.com");
        }

        if (!userRepository.existsByEmail("customer@econest.com")) {
            User customer = new User(
                    "customer@econest.com",
                    passwordEncoder.encode("Customer@123"),
                    "Emma Green",
                    "+1 (555) 018-9922",
                    Role.ROLE_CUSTOMER
            );
            userRepository.save(customer);
            log.info("Default customer account created: customer@econest.com");
        }
    }

    private void seedCategoriesAndProducts() {
        if (categoryRepository.count() > 0) {
            return;
        }

        Category reusables = new Category(
                "Reusable Essentials",
                "reusable-essentials",
                "Durable everyday essentials designed to replace single-use plastics and packaging forever.",
                "https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=800&q=80",
                1
        );

        Category kitchen = new Category(
                "Zero Waste Kitchen",
                "zero-waste-kitchen",
                "Natural, non-toxic kitchenware and pantry essentials for mindful cooking and sustainable food storage.",
                "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=800&q=80",
                2
        );

        Category personalCare = new Category(
                "Sustainable Personal Care",
                "personal-care",
                "Pure, plastic-free self-care and hygiene products made with ethically harvested botanicals.",
                "https://images.unsplash.com/photo-1607613009820-a29f7bb81c04?auto=format&fit=crop&w=800&q=80",
                3
        );

        Category stationery = new Category(
                "Eco Stationery & Office",
                "eco-stationery",
                "Tree-free notebooks, refillable pens, and recycled workspace supplies crafted for conscious thinkers.",
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=800&q=80",
                4
        );

        Category homeLiving = new Category(
                "Mindful Home & Living",
                "home-and-living",
                "Earth-friendly textiles, clean-burning candles, and natural home accessories for a peaceful sanctuary.",
                "https://images.unsplash.com/photo-1603006905003-be475563bc59?auto=format&fit=crop&w=800&q=80",
                5
        );

        categoryRepository.saveAll(Arrays.asList(reusables, kitchen, personalCare, stationery, homeLiving));

        List<Product> products = Arrays.asList(
                new Product(
                        "ECO-BOT-01",
                        "Triple-Insulated Stainless Steel Water Bottle (750ml)",
                        "triple-insulated-stainless-steel-water-bottle-750ml",
                        "Crafted from premium 18/8 food-grade stainless steel with double-wall vacuum insulation. Keeps beverages cold for 24 hours or piping hot for 12 hours. Featuring a leakproof sustainably harvested bamboo screw cap and comfortable carry loop.",
                        new BigDecimal("28.50"),
                        35,
                        "https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=800&q=80",
                        "Plastic-Free, Reusable, BPA-Free",
                        "18/8 Food-Grade Stainless Steel, Natural Bamboo",
                        "100% Recycled Kraft Box, Zero Plastic Tape",
                        "Ethically Manufactured",
                        reusables,
                        true
                ),
                new Product(
                        "ECO-BAG-02",
                        "Organic Heavyweight Cotton Canvas Tote Bag",
                        "organic-heavyweight-cotton-canvas-tote-bag",
                        "Reinforced stitching and durable 12oz GOTS-certified organic cotton canvas. Perfect for farmers markets, bookstore visits, or daily commutes. Includes an interior key pocket and extra-wide shoulder straps for comfort.",
                        new BigDecimal("16.00"),
                        50,
                        "https://images.unsplash.com/photo-1544816155-12df9643f363?auto=format&fit=crop&w=800&q=80",
                        "100% Organic, Plastic-Free, Biodegradable",
                        "GOTS-Certified Organic Cotton Canvas",
                        "Minimal Recycled Paper Band",
                        "Fair Trade Artisan Workshop",
                        reusables,
                        true
                ),
                new Product(
                        "ECO-BRU-03",
                        "Pure Bamboo Charcoal Toothbrush (Set of 4)",
                        "pure-bamboo-charcoal-toothbrush-set-of-4",
                        "Ergonomically shaped handles carved from rapidly renewable, water-neutral Moso bamboo. Bristles infused with activated charcoal for gentle plaque removal and natural whitening. Naturally antibacterial and 100% compostable handle.",
                        new BigDecimal("12.00"),
                        40,
                        "https://images.unsplash.com/photo-1607613009820-a29f7bb81c04?auto=format&fit=crop&w=800&q=80",
                        "Biodegradable, Compostable, Plastic-Free",
                        "Moso Bamboo, Castor Bean Oil Bristles",
                        "Compostable Unbleached Paper Box",
                        "Sustainable Forest Certified",
                        personalCare,
                        true
                ),
                new Product(
                        "ECO-BAR-04",
                        "Nourishing Solid Shampoo & Conditioner Bar Duo",
                        "nourishing-solid-shampoo-conditioner-bar-duo",
                        "Concentrated waterless hair care equivalent to 5 plastic shampoo bottles. Formulated with organic shea butter, French green clay, and cold-pressed argan oil. Gentle on scalp, safe for color-treated hair, and travel friendly.",
                        new BigDecimal("19.50"),
                        25,
                        "https://images.unsplash.com/photo-1585238342024-78d387f4a707?auto=format&fit=crop&w=800&q=80",
                        "Zero Waste, Vegan, Cruelty-Free, Plastic-Free",
                        "Raw Shea Butter, Argan Oil, Coconut Cleansing Base",
                        "Recycled Cardboard Tuck Box",
                        "Handcrafted in Small Batches",
                        personalCare,
                        true
                ),
                new Product(
                        "ECO-WRP-05",
                        "Natural Beeswax Food Wraps (Assorted 3-Pack)",
                        "natural-beeswax-food-wraps-assorted-3-pack",
                        "The natural alternative to plastic cling wrap. Made with organic cotton muslin infused with pure sustainably harvested beeswax, cold-pressed jojoba oil, and natural tree resin. Washable in cool water and reusable for up to a year.",
                        new BigDecimal("18.00"),
                        30,
                        "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=800&q=80",
                        "Plastic-Free, Reusable, Compostable",
                        "Organic Cotton, Beeswax, Jojoba Oil, Pine Resin",
                        "Recycled Paper Envelope",
                        "Handmade in Vermont, USA",
                        kitchen,
                        false
                ),
                new Product(
                        "ECO-BWL-06",
                        "Reclaimed Coconut Shell Bowls with Wooden Spoons (Pair)",
                        "reclaimed-coconut-shell-bowls-wooden-spoons-pair",
                        "Rescued from agricultural byproduct waste and sanded silky smooth by skilled artisans. Finished with virgin organic coconut oil polish. Ideal for smoothie bowls, fresh salads, ramen, and mindful dining.",
                        new BigDecimal("22.00"),
                        18,
                        "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=800&q=80",
                        "Upcycled, Biodegradable, Natural",
                        "Reclaimed Coconut Shells, Ebony Wood",
                        "Recycled Kraft Corrugated Wrap",
                        "Artisan Community Cooperative",
                        kitchen,
                        true
                ),
                new Product(
                        "ECO-JRN-07",
                        "Recycled Cotton Paper Hardcover Journal (A5)",
                        "recycled-cotton-paper-hardcover-journal-a5",
                        "Tree-free archival paper created from upcycled post-industrial textile remnants. 192 blank pages with a durable linen cloth spine and ribbon marker. Takes fountain pen ink without feathering or bleed-through.",
                        new BigDecimal("15.50"),
                        28,
                        "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=800&q=80",
                        "Tree-Free, Recycled, Upcycled",
                        "100% Upcycled Cotton Rag Paper, Linen Binding",
                        "Recycled Paper Wrap",
                        "Traditional Handmade Paper Mill",
                        stationery,
                        false
                ),
                new Product(
                        "ECO-PEN-08",
                        "Refillable Solid Brass Rollerball Pen",
                        "refillable-solid-brass-rollerball-pen",
                        "Precision-machined from solid untreated brass designed to develop a rich, personal patina over years of use. Ships with a reusable ink converter compatible with standard bottled fountain ink.",
                        new BigDecimal("24.00"),
                        15,
                        "https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?auto=format&fit=crop&w=800&q=80",
                        "Zero Waste, Lifetime Reusable, Plastic-Free",
                        "Solid Machined Brass, Ceramic Roller Tip",
                        "Natural Portuguese Cork Sleeve",
                        "Precision Tooling Studio",
                        stationery,
                        true
                ),
                new Product(
                        "ECO-CDL-09",
                        "Hand-Poured Soy Wax Scented Candle (Cedarwood & Sage)",
                        "hand-poured-soy-wax-candle-cedarwood-sage",
                        "Clean-burning 100% Midwest-grown soybean wax in a heavy amber glass jar with an untreated crackling wood wick. Scented exclusively with therapeutic-grade botanical essential oils. Burn time: 55 hours.",
                        new BigDecimal("26.00"),
                        22,
                        "https://images.unsplash.com/photo-1603006905003-be475563bc59?auto=format&fit=crop&w=800&q=80",
                        "Clean Burning, Vegan, Reusable Jar",
                        "Natural Soy Wax, Wood Wick, Pure Botanicals",
                        "Plastic-Free Unbleached Paper Box",
                        "Hand-poured in Oregon",
                        homeLiving,
                        true
                ),
                new Product(
                        "ECO-LIN-10",
                        "Stone-Washed Pure Flax Linen Kitchen Towels (Set of 3)",
                        "stone-washed-pure-flax-linen-kitchen-towels-set-of-3",
                        "Woven from high-grade European flax. Naturally antibacterial, lint-free, and three times more absorbent than conventional cotton. Softens beautifully with each wash and lasts for decades.",
                        new BigDecimal("29.00"),
                        16,
                        "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?auto=format&fit=crop&w=800&q=80",
                        "100% Flax Linen, Biodegradable, Natural",
                        "OEKO-TEX Certified European Flax",
                        "Tied with Natural Jute Twine",
                        "Heritage Weaver, Lithuania",
                        homeLiving,
                        false
                ),
                new Product(
                        "ECO-BTO-11",
                        "Stainless Steel Leakproof Bento Lunch Box (2-Tier)",
                        "stainless-steel-leakproof-bento-lunch-box-2-tier",
                        "Engineered for durable zero-waste packed lunches. Includes removable dividers and a leakproof silicone-gasket seal lid. Dishwasher safe, rustproof, and completely non-toxic.",
                        new BigDecimal("34.00"),
                        12,
                        "https://images.unsplash.com/photo-1590794056226-79ef3a8147e1?auto=format&fit=crop&w=800&q=80",
                        "Plastic-Free, Lifetime Reusable, BPA-Free",
                        "Food-Grade 304 Stainless Steel, Silicone Seal",
                        "100% Recycled Kraft Box",
                        "Responsible Manufacturing Partner",
                        kitchen,
                        true
                ),
                new Product(
                        "ECO-SCB-12",
                        "Natural Sisal & Coconut Fiber Kitchen Dish Scrubbers (Pack of 4)",
                        "natural-sisal-coconut-fiber-kitchen-dish-scrubbers-pack-of-4",
                        "Tough on grease and baked-on pots without scratching non-stick coatings. Made entirely from agave sisal and coconut husk fiber. 100% backyard compostable at the end of its life.",
                        new BigDecimal("9.50"),
                        6, // Low stock on purpose for testing admin alert
                        "https://images.unsplash.com/photo-1563453392212-326f5e854473?auto=format&fit=crop&w=800&q=80",
                        "100% Compostable, Biodegradable, Plant-Based",
                        "Agave Sisal, Coconut Husk, Cotton Thread",
                        "Compostable Paper Sleeve",
                        "Women-led Cooperative, Sri Lanka",
                        kitchen,
                        false
                ),
                new Product(
                        "ECO-GLS-13",
                        "Amber Glass Multi-Surface Cleaning Spray Bottle (500ml)",
                        "amber-glass-spray-bottle-500ml",
                        "Thick UV-blocking amber glass container fitted with an ergonomic adjustable trigger spray nozzle (mist/stream). Perfect for DIY eco cleaning solutions with vinegar, essential oils, and baking soda.",
                        new BigDecimal("14.00"),
                        20,
                        "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=800&q=80",
                        "Reusable, Recycled Glass, Refill Ready",
                        "Recycled Amber Glass, Silicone Base Protector",
                        "Pulp Molded Cushion",
                        "Crafted in USA",
                        homeLiving,
                        false
                ),
                new Product(
                        "ECO-SWB-14",
                        "Organic Bamboo Cotton Swabs (Box of 200)",
                        "organic-bamboo-cotton-swabs-box-of-200",
                        "Dual-ended pure organic cotton swabs with sturdy bamboo sticks. Replaces plastic ear swabs that commonly pollute coastal waters and ocean life. Soft and 100% biodegradable.",
                        new BigDecimal("6.50"),
                        35,
                        "https://images.unsplash.com/photo-1607613009820-a29f7bb81c04?auto=format&fit=crop&w=800&q=80",
                        "100% Compostable, Zero Plastic, Organic Cotton",
                        "Certified Organic Cotton, Renewable Bamboo",
                        "Slide-Open Recycled Paper Box",
                        "Ethical Farm Sourced",
                        personalCare,
                        false
                )
        );

        productRepository.saveAll(products);
        log.info("Successfully seeded 5 eco-friendly categories and 14 premium products.");
    }

    private void seedSampleOrders() {
        if (orderRepository.count() > 0) {
            return;
        }

        User customer = userRepository.findByEmail("customer@econest.com").orElse(null);
        Product bottle = productRepository.findBySku("ECO-BOT-01").orElse(null);
        Product bag = productRepository.findBySku("ECO-BAG-02").orElse(null);
        Product brush = productRepository.findBySku("ECO-BRU-03").orElse(null);

        if (customer != null && bottle != null && bag != null && brush != null) {
            // Sample Delivered Order
            Order order1 = new Order();
            order1.setOrderReference("ECO-2026-A10982");
            order1.setUser(customer);
            order1.setCustomerName("Emma Green");
            order1.setCustomerEmail("customer@econest.com");
            order1.setCustomerPhone("+1 (555) 018-9922");
            order1.setShippingAddress("742 Evergreen Terrace");
            order1.setCity("Portland");
            order1.setState("Oregon");
            order1.setPostalCode("97201");
            order1.setCountry("United States");
            order1.setStatus(OrderStatus.DELIVERED);
            order1.setPaymentMethod(PaymentMethod.MOCK_ONLINE);
            order1.setPaymentStatus(PaymentStatus.PAID);
            order1.setSubtotal(new BigDecimal("44.50"));
            order1.setShippingFee(new BigDecimal("4.99"));
            order1.setDiscountAmount(BigDecimal.ZERO);
            order1.setTotalAmount(new BigDecimal("51.72")); // includes tax

            OrderItem item1 = new OrderItem(order1, bottle, bottle.getName(), bottle.getSku(),
                    bottle.getPrice(), 1, bottle.getPrice());
            OrderItem item2 = new OrderItem(order1, bag, bag.getName(), bag.getSku(),
                    bag.getPrice(), 1, bag.getPrice());
            order1.addItem(item1);
            order1.addItem(item2);

            // Sample Processing Order
            Order order2 = new Order();
            order2.setOrderReference("ECO-2026-B88411");
            order2.setUser(customer);
            order2.setCustomerName("Emma Green");
            order2.setCustomerEmail("customer@econest.com");
            order2.setCustomerPhone("+1 (555) 018-9922");
            order2.setShippingAddress("742 Evergreen Terrace");
            order2.setCity("Portland");
            order2.setState("Oregon");
            order2.setPostalCode("97201");
            order2.setCountry("United States");
            order2.setStatus(OrderStatus.PROCESSING);
            order2.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
            order2.setPaymentStatus(PaymentStatus.PENDING);
            order2.setSubtotal(new BigDecimal("24.00"));
            order2.setShippingFee(new BigDecimal("4.99"));
            order2.setDiscountAmount(BigDecimal.ZERO);
            order2.setTotalAmount(new BigDecimal("30.19"));

            OrderItem item3 = new OrderItem(order2, brush, brush.getName(), brush.getSku(),
                    brush.getPrice(), 2, new BigDecimal("24.00"));
            order2.addItem(item3);

            orderRepository.saveAll(Arrays.asList(order1, order2));
            log.info("Successfully seeded 2 realistic demo orders for customer Emma Green.");
        }
    }
}
