public class DataManagementAllInOne {


    static final class Customer implements Comparable<Customer> {
        private final UUID id;
        private String name;
        private String email;
        private int loyaltyPoints;

        public Customer(UUID id, String name, String email, int loyaltyPoints) {
            this.id = Objects.requireNonNull(id);
            setName(name);
            setEmail(email);
            this.loyaltyPoints = loyaltyPoints;
        }

        public UUID getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public int getLoyaltyPoints() { return loyaltyPoints; }

        public void setName(String name) {
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Customer.name must not be blank");
            this.name = name.trim();
        }

        public void setEmail(String email) {
            if (email == null || email.isBlank()) throw new IllegalArgumentException("Customer.email must not be blank");
            this.email = email.trim();
        }

        public void setLoyaltyPoints(int loyaltyPoints) {
            if (loyaltyPoints < 0) throw new IllegalArgumentException("loyaltyPoints must be >= 0");
            this.loyaltyPoints = loyaltyPoints;
        }

        @Override
        public int compareTo(Customer o) {
            int c = this.name.compareToIgnoreCase(o.name);
            if (c != 0) return c;
            c = this.email.compareToIgnoreCase(o.email);
            if (c != 0) return c;
            return this.id.compareTo(o.id);
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Customer)) return false;
            return id.equals(((Customer) o).id);
        }
        @Override public int hashCode() { return id.hashCode(); }

        @Override public String toString() {
            return "Customer{id=" + id + ", name='" + name + "', email='" + email + "', points=" + loyaltyPoints + "}";
        }
    }

    static final class Product implements Comparable<Product> {
        private final UUID id;
        private String name;
        private String category;
        private double price;

        public Product(UUID id, String name, String category, double price) {
            this.id = Objects.requireNonNull(id);
            setName(name);
            setCategory(category);
            setPrice(price);
        }

        public UUID getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public double getPrice() { return price; }

        public void setName(String name) {
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Product.name must not be blank");
            this.name = name.trim();
        }

        public void setCategory(String category) {
            if (category == null || category.isBlank()) throw new IllegalArgumentException("Product.category must not be blank");
            this.category = category.trim();
        }

        public void setPrice(double price) {
            if (price < 0) throw new IllegalArgumentException("Product.price must be >= 0");
            this.price = price;
        }

        @Override
        public int compareTo(Product o) {
            int c = this.name.compareToIgnoreCase(o.name);
            if (c != 0) return c;
            c = this.category.compareToIgnoreCase(o.category);
            if (c != 0) return c;
            c = Double.compare(this.price, o.price);
            if (c != 0) return c;
            return this.id.compareTo(o.id);
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Product)) return false;
            return id.equals(((Product) o).id);
        }
        @Override public int hashCode() { return id.hashCode(); }

        @Override public String toString() {
            return "Product{id=" + id + ", name='" + name + "', category='" + category + "', price=" + price + "}";
        }
    }

    enum OrderStatus { NEW, PAID, SHIPPED, CANCELLED }

    static final class OrderItem {
        private final Product product;
        private int qty;

        public OrderItem(Product product, int qty) {
            this.product = Objects.requireNonNull(product);
            setQty(qty);
        }

        public Product getProduct() { return product; }
        public int getQty() { return qty; }
        public void setQty(int qty) {
            if (qty <= 0) throw new IllegalArgumentException("OrderItem.qty must be > 0");
            this.qty = qty;
        }

        public double lineTotal() {
            return product.getPrice() * qty;
        }

        @Override public String toString() {
            return "OrderItem{product=" + product.getName() + ", qty=" + qty + ", total=" + lineTotal() + "}";
        }
    }

    static final class Order {
        private final UUID id;
        private final UUID customerId;
        private final LocalDateTime createdAt;
        private OrderStatus status;
        private final LinkedList<OrderItem> items = new LinkedList<>();

        public Order(UUID id, UUID customerId, LocalDateTime createdAt) {
            this.id = Objects.requireNonNull(id);
            this.customerId = Objects.requireNonNull(customerId);
            this.createdAt = Objects.requireNonNull(createdAt);
            this.status = OrderStatus.NEW;
        }

        public UUID getId() { return id; }
        public UUID getCustomerId() { return customerId; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = Objects.requireNonNull(status); }
        public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }

        public void addItem(Product p, int qty) {
            items.add(new OrderItem(p, qty));
        }

        public void removeItemByProductId(UUID productId) {
            items.removeIf(it -> it.getProduct().getId().equals(productId));
        }

        public double total() {
            return items.stream().mapToDouble(OrderItem::lineTotal).sum();
        }

        @Override public String toString() {
            return "Order{id=" + id + ", customerId=" + customerId + ", status=" + status +
                    ", items=" + items.size() + ", total=" + total() + ", createdAt=" + createdAt + "}";
        }
    }

    static final class DataStore {
         private final HashMap<UUID, Customer> customersById = new HashMap<>();
        private final HashMap<UUID, Product> productsById = new HashMap<>();
        private final HashMap<UUID, Order> ordersById = new HashMap<>();

        private final HashSet<String> customerEmails = new HashSet<>();
        private final HashSet<String> productCategories = new HashSet<>();

         private final TreeSet<Customer> customersSorted = new TreeSet<>(); 
        private final TreeSet<Product> productsSorted = new TreeSet<>();   

        private final TreeMap<LocalDateTime, UUID> ordersByTime = new TreeMap<>();

        public Customer createCustomer(String name, String email, int points) {
            if (customerEmails.contains(email)) throw new IllegalArgumentException("Email already exists: " + email);
            Customer c = new Customer(UUID.randomUUID(), name, email, points);
            customersById.put(c.getId(), c);
            customerEmails.add(c.getEmail());
            customersSorted.add(c);
            return c;
        }

        public Optional<Customer> readCustomer(UUID id) {
            return Optional.ofNullable(customersById.get(id));
        }

        public Customer updateCustomer(UUID id, Optional<String> newName, Optional<String> newEmail, Optional<Integer> newPoints) {
            Customer c = customersById.get(id);
            if (c == null) throw new NoSuchElementException("Customer not found: " + id);

            customersSorted.remove(c);
            customerEmails.remove(c.getEmail());

            newName.ifPresent(c::setName);
            newEmail.ifPresent(em -> {
                if (!em.equals(c.getEmail()) && customerEmails.contains(em)) {
                    throw new IllegalArgumentException("Email already exists: " + em);
                }
                c.setEmail(em);
            });
            newPoints.ifPresent(c::setLoyaltyPoints);

            customerEmails.add(c.getEmail());
            customersSorted.add(c);
            return c;
        }

        public boolean deleteCustomer(UUID id) {
            Customer c = customersById.remove(id);
            if (c == null) return false;
            customerEmails.remove(c.getEmail());
            customersSorted.remove(c);

            List<UUID> toDeleteOrders = ordersById.values().stream()
                    .filter(o -> o.getCustomerId().equals(id))
                    .map(Order::getId)
                    .toList();
            toDeleteOrders.forEach(this::deleteOrder);

            return true;
        }

        public List<Customer> listCustomersArrayList() {
            return new ArrayList<>(customersById.values());
        }

        public SortedSet<Customer> listCustomersTreeSetView() {
            return Collections.unmodifiableSortedSet(customersSorted);
        }

        public Product createProduct(String name, String category, double price) {
            Product p = new Product(UUID.randomUUID(), name, category, price);
            productsById.put(p.getId(), p);
            productCategories.add(p.getCategory());
            productsSorted.add(p);
            return p;
        }

        public Optional<Product> readProduct(UUID id) {
            return Optional.ofNullable(productsById.get(id));
        }

        public Product updateProduct(UUID id, Optional<String> newName, Optional<String> newCategory, Optional<Double> newPrice) {
            Product p = productsById.get(id);
            if (p == null) throw new NoSuchElementException("Product not found: " + id);

            productsSorted.remove(p);
            String oldCategory = p.getCategory();

            newName.ifPresent(p::setName);
            newCategory.ifPresent(p::setCategory);
            newPrice.ifPresent(p::setPrice);

            productsSorted.add(p);
            productCategories.add(p.getCategory());

           if (!oldCategory.equals(p.getCategory())) {
                recomputeCategories();
            }
            return p;
        }

        public boolean deleteProduct(UUID id) {
            Product p = productsById.remove(id);
            if (p == null) return false;

            productsSorted.remove(p);
            recomputeCategories();

            ordersById.values().forEach(o -> o.removeItemByProductId(id));
            return true;
        }

        public List<Product> listProductsArrayList() {
            return new ArrayList<>(productsById.values());
        }

        public SortedSet<Product> listProductsTreeSetView() {
            return Collections.unmodifiableSortedSet(productsSorted);
        }

        public Set<String> listCategoriesHashSetView() {
            return Collections.unmodifiableSet(productCategories);
        }

        private void recomputeCategories() {
            productCategories.clear();
            productsById.values().forEach(pp -> productCategories.add(pp.getCategory()));
        }

       public Order createOrder(UUID customerId) {
            if (!customersById.containsKey(customerId)) throw new NoSuchElementException("Customer not found: " + customerId);
            Order o = new Order(UUID.randomUUID(), customerId, LocalDateTime.now());
            ordersById.put(o.getId(), o);
            ordersByTime.put(o.getCreatedAt(), o.getId());
            return o;
        }

        public Optional<Order> readOrder(UUID id) {
            return Optional.ofNullable(ordersById.get(id));
        }

        public Order addItemToOrder(UUID orderId, UUID productId, int qty) {
            Order o = ordersById.get(orderId);
            if (o == null) throw new NoSuchElementException("Order not found: " + orderId);

            Product p = productsById.get(productId);
            if (p == null) throw new NoSuchElementException("Product not found: " + productId);

            o.addItem(p, qty);
            return o;
        }

        public Order updateOrderStatus(UUID orderId, OrderStatus status) {
            Order o = ordersById.get(orderId);
            if (o == null) throw new NoSuchElementException("Order not found: " + orderId);
            o.setStatus(status);
            return o;
        }

        public boolean deleteOrder(UUID id) {
            Order o = ordersById.remove(id);
            if (o == null) return false;
            ordersByTime.remove(o.getCreatedAt());
            return true;
        }

        public List<Order> listOrdersArrayList() {
            return new ArrayList<>(ordersById.values());
        }

        public NavigableMap<LocalDateTime, UUID> listOrdersTreeMapView() {
            return Collections.unmodifiableNavigableMap(ordersByTime);
        }
    }

   
    static final class Sorters {
        static final Comparator<Product> PRODUCT_BY_PRICE_DESC_THEN_NAME_THEN_CATEGORY =
                Comparator.comparingDouble(Product::getPrice).reversed()
                        .thenComparing(Product::getName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Product::getCategory, String.CASE_INSENSITIVE_ORDER);

        static final Comparator<Customer> CUSTOMER_BY_POINTS_DESC_THEN_NAME =
                Comparator.comparingInt(Customer::getLoyaltyPoints).reversed()
                        .thenComparing(Customer::getName, String.CASE_INSENSITIVE_ORDER);
    }

   
    static final class Analytics {

        static List<String> expensiveProductNames(List<Product> products, double minPrice) {
            return products.stream()
                    .filter(p -> p.getPrice() >= minPrice)
                    .map(Product::getName)
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
        }

       static Map<String, Double> avgPriceByCategory(List<Product> products) {
            return products.stream()
                    .collect(Collectors.groupingBy(
                            Product::getCategory,
                            Collectors.averagingDouble(Product::getPrice)
                    ));
        }

       static double totalRevenue(List<Order> orders) {
            return orders.stream()
                    .map(Order::total)
                    .reduce(0.0, Double::sum);
        }

        static String safeCustomerEmail(Optional<Customer> customerOpt) {
            return customerOpt
                    .map(Customer::getEmail)
                    .filter(em -> em.contains("@"))
                    .orElse("NO_EMAIL");
        }

        static Optional<Customer> topCustomer(List<Customer> customers) {
            return customers.stream().max(Comparator.comparingInt(Customer::getLoyaltyPoints));
        }
    }

    static final class Perf {
        static final class Result {
            final String name;
            final long nanos;
            Result(String name, long nanos) { this.name = name; this.nanos = nanos; }
        }

        static long timeNanos(Runnable r) {
            long t0 = System.nanoTime();
            r.run();
            return System.nanoTime() - t0;
        }

         static void runExperiments() {
            System.out.println("\n ЕКСПЕРИМЕНТИ ПРОДУКТИВНОСТІ ");
            System.out.println(" Методика:");
            System.out.println("- Генеруємо великий набір ключів/елементів (N).");
            System.out.println("- Робимо прогрів (warm-up), щоб JVM/JIT оптимізував код.");
            System.out.println("- Порівнюємо операції add / contains / iterate для різних колекцій.");
            System.out.println("- Час вимірюємо через System.nanoTime().\n");

            final int N = 200_000;
            List<Integer> data = new ArrayList<>(N);
            ThreadLocalRandom rnd = ThreadLocalRandom.current();
            for (int i = 0; i < N; i++) data.add(rnd.nextInt(N * 10));

            ArrayList<Integer> arrayList = new ArrayList<>();
            LinkedList<Integer> linkedList = new LinkedList<>();
            HashSet<Integer> hashSet = new HashSet<>();
            TreeSet<Integer> treeSet = new TreeSet<>();
            HashMap<Integer, Integer> hashMap = new HashMap<>();
            TreeMap<Integer, Integer> treeMap = new TreeMap<>();

             for (int w = 0; w < 2; w++) {
                arrayList.clear(); linkedList.clear(); hashSet.clear(); treeSet.clear(); hashMap.clear(); treeMap.clear();
                for (Integer x : data) {
                    arrayList.add(x);
                    linkedList.add(x);
                    hashSet.add(x);
                    treeSet.add(x);
                    hashMap.put(x, x);
                    treeMap.put(x, x);
                }
                int probe = data.get(N / 2);
                arrayList.contains(probe);
                linkedList.contains(probe);
                hashSet.contains(probe);
                treeSet.contains(probe);
                hashMap.containsKey(probe);
                treeMap.containsKey(probe);
                long sum = 0;
                for (Integer x : arrayList) sum += x;
                for (Integer x : linkedList) sum += x;
                for (Integer x : hashSet) sum += x;
                for (Integer x : treeSet) sum += x;
                if (sum == -1) System.out.println("ignore");
            }

             List<Result> results = new ArrayList<>();

            results.add(new Result("ArrayList.add(N)", timeNanos(() -> {
                arrayList.clear();
                for (Integer x : data) arrayList.add(x);
            })));

            results.add(new Result("LinkedList.add(N)", timeNanos(() -> {
                linkedList.clear();
                for (Integer x : data) linkedList.add(x);
            })));

            results.add(new Result("HashSet.add(N)", timeNanos(() -> {
                hashSet.clear();
                for (Integer x : data) hashSet.add(x);
            })));

            results.add(new Result("TreeSet.add(N)", timeNanos(() -> {
                treeSet.clear();
                for (Integer x : data) treeSet.add(x);
            })));

            results.add(new Result("HashMap.put(N)", timeNanos(() -> {
                hashMap.clear();
                for (Integer x : data) hashMap.put(x, x);
            })));

            results.add(new Result("TreeMap.put(N)", timeNanos(() -> {
                treeMap.clear();
                for (Integer x : data) treeMap.put(x, x);
            })));

            final int probes = 50_000;
            List<Integer> probeKeys = new ArrayList<>(probes);
            for (int i = 0; i < probes; i++) probeKeys.add(data.get(rnd.nextInt(N)));

            results.add(new Result("ArrayList.contains(M)", timeNanos(() -> {
                int found = 0;
                for (Integer k : probeKeys) if (arrayList.contains(k)) found++;
                if (found == -1) System.out.println("ignore");
            })));

            results.add(new Result("LinkedList.contains(M)", timeNanos(() -> {
                int found = 0;
                for (Integer k : probeKeys) if (linkedList.contains(k)) found++;
                if (found == -1) System.out.println("ignore");
            })));

            results.add(new Result("HashSet.contains(M)", timeNanos(() -> {
                int found = 0;
                for (Integer k : probeKeys) if (hashSet.contains(k)) found++;
                if (found == -1) System.out.println("ignore");
            })));

            results.add(new Result("TreeSet.contains(M)", timeNanos(() -> {
                int found = 0;
                for (Integer k : probeKeys) if (treeSet.contains(k)) found++;
                if (found == -1) System.out.println("ignore");
            })));

            results.add(new Result("HashMap.containsKey(M)", timeNanos(() -> {
                int found = 0;
                for (Integer k : probeKeys) if (hashMap.containsKey(k)) found++;
                if (found == -1) System.out.println("ignore");
            })));

            results.add(new Result("TreeMap.containsKey(M)", timeNanos(() -> {
                int found = 0;
                for (Integer k : probeKeys) if (treeMap.containsKey(k)) found++;
                if (found == -1) System.out.println("ignore");
            })));

            results.add(new Result("ArrayList.iterate(sum)", timeNanos(() -> {
                long sum = 0;
                for (Integer x : arrayList) sum += x;
                if (sum == -1) System.out.println("ignore");
            })));

            results.add(new Result("LinkedList.iterate(sum)", timeNanos(() -> {
                long sum = 0;
                for (Integer x : linkedList) sum += x;
                if (sum == -1) System.out.println("ignore");
            })));

            results.sort(Comparator.comparingLong(r -> r.nanos));

            System.out.println("Результати (чим менше — тим краще):");
            for (Result r : results) {
                System.out.printf(" - %-26s : %.3f ms%n", r.name, r.nanos / 1_000_000.0);
            }

            System.out.println("\n Висновки (типові, можуть трохи відрізнятись на твоєму ПК):");
            System.out.println("- HashMap/HashSet зазвичай найшвидші для пошуку contains/containsKey (O(1) average).");
            System.out.println("- TreeMap/TreeSet повільніші за Hash*, але тримають елементи відсортованими (O(logN)).");
            System.out.println("- ArrayList швидкий для ітерації та доступу за індексом, але contains(M) може бути повільним (O(N)).");
            System.out.println("- LinkedList часто програє ArrayList у contains та ітерації через погану cache-locality.");
            System.out.println("=======================================================================\n ");
        }
    }

   
    public static void main(String[] args) {
        System.out.println("=== Застосунок управління даними (Customers / Products / Orders) ===");

        DataStore db = new DataStore();

        Customer c1 = db.createCustomer("Олена", "olena@mail.com", 120);
        Customer c2 = db.createCustomer("Андрій", "andrii@mail.com", 45);
        Customer c3 = db.createCustomer("Марія", "maria@mail.com", 200);

        System.out.println("\n[Customers created]");
        db.listCustomersArrayList().forEach(System.out::println);

        db.updateCustomer(c2.getId(), Optional.of("Андрій К."), Optional.empty(), Optional.of(60));

        System.out.println("\n[Read customer safely with Optional]");
        String emailSafe = Analytics.safeCustomerEmail(db.readCustomer(c2.getId()));
        System.out.println("Email safe: " + emailSafe);

        Product p1 = db.createProduct("Ноутбук", "Electronics", 32000);
        Product p2 = db.createProduct("Мишка", "Electronics", 700);
        Product p3 = db.createProduct("Кава", "Food", 250);
        Product p4 = db.createProduct("Навушники", "Electronics", 2200);
        Product p5 = db.createProduct("Чай", "Food", 180);

        System.out.println("\n[Products created]");
        db.listProductsArrayList().forEach(System.out::println);

        Order o1 = db.createOrder(c1.getId());
        db.addItemToOrder(o1.getId(), p1.getId(), 1);
        db.addItemToOrder(o1.getId(), p2.getId(), 2);
        db.updateOrderStatus(o1.getId(), OrderStatus.PAID);

        Order o2 = db.createOrder(c3.getId());
        db.addItemToOrder(o2.getId(), p3.getId(), 3);
        db.addItemToOrder(o2.getId(), p5.getId(), 2);

        System.out.println("\n[Orders created]");
        db.listOrdersArrayList().forEach(System.out::println);

        System.out.println("\n[Customers TreeSet (Comparable sort by name/email/id)]");
        db.listCustomersTreeSetView().forEach(System.out::println);

       System.out.println("\n[Customers sorted by points desc, then name]");
        List<Customer> customersForSort = db.listCustomersArrayList();
        customersForSort.sort(Sorters.CUSTOMER_BY_POINTS_DESC_THEN_NAME);
        customersForSort.forEach(System.out::println);

        System.out.println("\n[Products sorted by price desc, then name, then category]");
        List<Product> productsForSort = db.listProductsArrayList();
        productsForSort.sort(Sorters.PRODUCT_BY_PRICE_DESC_THEN_NAME_THEN_CATEGORY);
        productsForSort.forEach(System.out::println);

        System.out.println("\n[Streams: expensive product names >= 2000]");
        System.out.println(Analytics.expensiveProductNames(db.listProductsArrayList(), 2000));

        System.out.println("\n[Streams: avg price by category]");
        System.out.println(Analytics.avgPriceByCategory(db.listProductsArrayList()));

       System.out.println("\n[Streams: total revenue from orders]");
        System.out.println("Total revenue = " + Analytics.totalRevenue(db.listOrdersArrayList()));

        System.out.println("\n[Optional: top customer by points]");
        System.out.println(Analytics.topCustomer(db.listCustomersArrayList()).map(Customer::toString).orElse("NO_CUSTOMERS"));

         System.out.println("\n[Orders TreeMap view: createdAt -> orderId (sorted keys)]");
        db.listOrdersTreeMapView().forEach((k, v) -> System.out.println(k + " -> " + v));

        System.out.println("\n[Delete product (removes from orders too)] " + p2.getName());
        db.deleteProduct(p2.getId());
        db.readOrder(o1.getId()).ifPresent(System.out::println);

        Perf.runExperiments();

        System.out.println("=== Done ===");
    }
}
