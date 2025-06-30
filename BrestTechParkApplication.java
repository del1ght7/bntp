package com.bresttechpark;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

        import jakarta.persistence.*;
        import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SpringBootApplication
class BrestTechParkApplication {
    public static void main(String[] args) {
        SpringApplication.run(BrestTechParkApplication.class, args);
    }

    @Bean
    public org.springframework.boot.CommandLineRunner initData(UserService userService, AnnouncementService announcementService) {
        return args -> {
            try {
                // Создаем админа
                userService.registerUser("admin", "admin123", "admin@bntp.by", "БНТП", UserRole.ADMIN);

                // Создаем тестового резидента
                User resident = userService.registerUser("resident1", "password123", "resident@example.com", "ИТ Компания", UserRole.RESIDENT);
                userService.approveUser(resident.getId());

                // Создаем тестовое объявление
                Announcement announcement = new Announcement();
                announcement.setTitle("Добро пожаловать в БНТП!");
                announcement.setContent("Брестский научно-технологический парк приветствует новых резидентов. Мы предлагаем современную инфраструктуру и поддержку для развития инновационного бизнеса.");
                announcement.setType(AnnouncementType.NEWS);
                announcement.setAuthor(resident);
                Announcement created = announcementService.createAnnouncement(announcement);
                announcementService.publishAnnouncement(created.getId());

                System.out.println("✅ Тестовые данные созданы:");
                System.out.println("👤 Админ: admin / admin123");
                System.out.println("👤 Резидент: resident1 / password123");
                System.out.println("🌐 Сайт: http://localhost:8080");
                System.out.println("🗄️ H2 Console: http://localhost:8080/h2-console");

            } catch (Exception e) {
                System.out.println("⚠️ Ошибка инициализации: " + e.getMessage());
            }
        };
    }
}

// Entities
@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;
    private String email;
    private String company;
    private String phone;
    private String contactPerson;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean approved = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors, getters, setters
    public User() {}

    public User(String username, String password, String email, String company, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.company = company;
        this.role = role;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

enum UserRole {
    ADMIN, RESIDENT, USER
}

@Entity
@Table(name = "room_types")
class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private int area; // площадь в м2
    private int capacity; // количество мест
    private double hourlyRate; // стоимость за час

    // Constructors, getters, setters
    public RoomType() {}

    public RoomType(String name, String description, int area, int capacity, double hourlyRate) {
        this.name = name;
        this.description = description;
        this.area = area;
        this.capacity = capacity;
        this.hourlyRate = hourlyRate;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getArea() { return area; }
    public void setArea(int area) { this.area = area; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }
}

@Entity
@Table(name = "equipment")
class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double dailyRate; // стоимость за день
    private boolean available = true;

    // Constructors, getters, setters
    public Equipment() {}

    public Equipment(String name, String description, double dailyRate) {
        this.name = name;
        this.description = description;
        this.dailyRate = dailyRate;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getDailyRate() { return dailyRate; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}

@Entity
@Table(name = "bookings")
class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;

    // Контактные данные (для гостевых бронирований)
    private String guestName;
    private String guestPhone;
    private String guestEmail;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime submittedAt = LocalDateTime.now();

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<EquipmentType> selectedEquipment;

    // Constructors, getters, setters
    public Booking() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }
    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public Set<EquipmentType> getSelectedEquipment() { return selectedEquipment; }
    public void setSelectedEquipment(Set<EquipmentType> selectedEquipment) { this.selectedEquipment = selectedEquipment; }
}

enum BookingStatus {
    PENDING, APPROVED, REJECTED, CONFLICT
}

enum EquipmentType {
    INTERACTIVE_SCREEN("Интерактивный экран", 50.0),
    PROJECTOR("Проектор", 30.0),
    AUDIO_SYSTEM("Акустическая система/микрофоны", 40.0),
    CONFERENCE_CAMERA("Конференц-камера (Zoom)", 60.0),
    FLIPCHART("Флипчарт + маркеры", 15.0),
    THERMOPOT_4L("Термопот (4л)", 20.0),
    THERMOPOT_10L("Термопот (10л)", 35.0),
    VIDEO_CAMERA("Видеокамера Sony PXW-Z150", 100.0);

    private final String displayName;
    private final double dailyRate;

    EquipmentType(String displayName, double dailyRate) {
        this.displayName = displayName;
        this.dailyRate = dailyRate;
    }

    public String getDisplayName() { return displayName; }
    public double getDailyRate() { return dailyRate; }
}

@Entity
@Table(name = "announcements")
class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private AnnouncementType type;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    private boolean published = false;
    private boolean notificationSent = false;

    // Constructors, getters, setters
    public Announcement() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public AnnouncementType getType() { return type; }
    public void setType(AnnouncementType type) { this.type = type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public boolean isNotificationSent() { return notificationSent; }
    public void setNotificationSent(boolean notificationSent) { this.notificationSent = notificationSent; }
}

enum AnnouncementType {
    NEWS, VACANCY, EVENT
}

@Entity
@Table(name = "notifications")
class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String title;
    private String message;
    private boolean read = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors, getters, setters
    public Notification() {}

    public Notification(User user, String title, String message) {
        this.user = user;
        this.title = title;
        this.message = message;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

// Repositories
@Repository
interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByApprovedFalse();
    List<User> findByRole(UserRole role);
}

@Repository
interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByUserId(Long userId);
    List<Booking> findByRoomTypeAndStartTimeBetween(RoomType roomType, LocalDateTime start, LocalDateTime end);
    List<Booking> findByStartTimeBetweenAndStatus(LocalDateTime start, LocalDateTime end, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.roomType = :roomType AND " +
            "((b.startTime <= :endTime AND b.endTime >= :startTime)) AND " +
            "b.status IN ('PENDING', 'APPROVED')")
    List<Booking> findConflictingBookings(@Param("roomType") RoomType roomType,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);
}

@Repository
interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    Optional<RoomType> findByName(String name);
}

@Repository
interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByAvailableTrue();
}

@Repository
interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByPublishedTrueOrderByCreatedAtDesc();
    List<Announcement> findByPublishedFalseOrderByCreatedAtDesc();
    List<Announcement> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}

@Repository
interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}

// Services
@Service
class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String username, String password, String email, String company, UserRole role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User(username, passwordEncoder.encode(password), email, company, role);
        if (role == UserRole.ADMIN) {
            user.setApproved(true);
        }
        return userRepository.save(user);
    }

    public User createUser(String username, String password, String email, String company, String phone, String contactPerson, UserRole role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User(username, passwordEncoder.encode(password), email, company, role);
        user.setPhone(phone);
        user.setContactPerson(contactPerson);
        user.setApproved(true); // Создаваемые админом пользователи сразу активны
        return userRepository.save(user);
    }

    public User updateUserCredentials(Long userId, String newUsername, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow();

        // Проверяем, что новый username не занят (если он изменился)
        if (!user.getUsername().equals(newUsername)) {
            if (userRepository.findByUsername(newUsername).isPresent()) {
                throw new RuntimeException("Username already exists");
            }
            user.setUsername(newUsername);
        }

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getPendingUsers() {
        return userRepository.findByApprovedFalse();
    }

    public List<User> getResidents() {
        return userRepository.findByRole(UserRole.RESIDENT);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User approveUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setApproved(true);
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}

@Service
class BookingService {
    private final BookingRepository bookingRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final NotificationService notificationService;

    public BookingService(BookingRepository bookingRepository, RoomTypeRepository roomTypeRepository, NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.notificationService = notificationService;
    }

    public Booking createBooking(Booking booking) {
        // Проверяем конфликты
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                booking.getRoomType(), booking.getStartTime(), booking.getEndTime());

        if (!conflicts.isEmpty()) {
            booking.setStatus(BookingStatus.CONFLICT);
            // Уведомляем админов о конфликте
            notificationService.notifyAdminsAboutBookingConflict(booking, conflicts);
        }

        booking.setSubmittedAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    public List<Booking> getPendingBookings() {
        return bookingRepository.findByStatus(BookingStatus.PENDING);
    }

    public List<Booking> getConflictBookings() {
        return bookingRepository.findByStatus(BookingStatus.CONFLICT);
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public List<Booking> getRoomBookings(String roomTypeName, LocalDateTime start, LocalDateTime end) {
        RoomType roomType = roomTypeRepository.findByName(roomTypeName).orElse(null);
        if (roomType == null) return List.of();

        return bookingRepository.findByRoomTypeAndStartTimeBetween(roomType, start, end);
    }

    public void approveBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
    }

    public void rejectBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking);
    }
}

@Service
class AnnouncementService {
    private final AnnouncementRepository announcementRepository;
    private final NotificationService notificationService;

    public AnnouncementService(AnnouncementRepository announcementRepository, NotificationService notificationService) {
        this.announcementRepository = announcementRepository;
        this.notificationService = notificationService;
    }

    public Announcement createAnnouncement(Announcement announcement) {
        return announcementRepository.save(announcement);
    }

    public List<Announcement> getPublishedAnnouncements() {
        return announcementRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }

    public List<Announcement> getPendingAnnouncements() {
        return announcementRepository.findByPublishedFalseOrderByCreatedAtDesc();
    }

    public List<Announcement> getUserAnnouncements(Long userId) {
        return announcementRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
    }

    public void publishAnnouncement(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId).orElseThrow();
        announcement.setPublished(true);

        // Отправляем уведомления резидентам если еще не отправляли
        if (!announcement.isNotificationSent()) {
            notificationService.notifyResidentsAboutNewAnnouncement(announcement);
            announcement.setNotificationSent(true);
        }

        announcementRepository.save(announcement);
    }
}

@Service
class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void notifyResidentsAboutNewAnnouncement(Announcement announcement) {
        List<User> residents = userRepository.findByRole(UserRole.RESIDENT);
        for (User resident : residents) {
            if (resident.isApproved()) {
                Notification notification = new Notification(
                        resident,
                        "Новое объявление: " + announcement.getTitle(),
                        "Опубликовано новое объявление в категории " + getAnnouncementTypeDisplayName(announcement.getType())
                );
                notificationRepository.save(notification);
            }
        }
    }

    public void notifyAdminsAboutBookingConflict(Booking newBooking, List<Booking> conflictingBookings) {
        List<User> admins = userRepository.findByRole(UserRole.ADMIN);
        for (User admin : admins) {
            String message = String.format("Конфликт бронирования: новая заявка на %s с %s по %s пересекается с %d существующими бронированиями",
                    newBooking.getRoomType().getName(),
                    newBooking.getStartTime().toString(),
                    newBooking.getEndTime().toString(),
                    conflictingBookings.size());

            Notification notification = new Notification(admin, "Конфликт бронирования", message);
            notificationRepository.save(notification);
        }
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private String getAnnouncementTypeDisplayName(AnnouncementType type) {
        switch (type) {
            case NEWS: return "Новости";
            case VACANCY: return "Вакансии";
            case EVENT: return "События";
            default: return type.toString();
        }
    }
}

@Service
class RoomTypeService {
    private final RoomTypeRepository roomTypeRepository;

    public RoomTypeService(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    @Bean
    public org.springframework.boot.CommandLineRunner initRoomTypes(RoomTypeService roomTypeService) {
        return args -> {
            try {
                roomTypeService.createDefaultRoomTypes();
            } catch (Exception e) {
                System.out.println("Room types already exist or error: " + e.getMessage());
            }
        };
    }

    public void createDefaultRoomTypes() {
        if (roomTypeRepository.count() == 0) {
            roomTypeRepository.save(new RoomType("Переговорка", "Уютная переговорная комната для небольших встреч", 55, 15, 25.0));
            roomTypeRepository.save(new RoomType("Конференц-зал", "Просторный зал для проведения конференций и семинаров", 108, 80, 50.0));
            roomTypeRepository.save(new RoomType("Выставочный зал", "Большой зал для выставок и крупных мероприятий", 180, 120, 75.0));
        }
    }

    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }

    public Optional<RoomType> findByName(String name) {
        return roomTypeRepository.findByName(name);
    }
}

// Controllers
@Controller
class WebController {

    private final UserService userService;
    private final BookingService bookingService;
    private final AnnouncementService announcementService;
    private final RoomTypeService roomTypeService;
    private final NotificationService notificationService;

    public WebController(UserService userService, BookingService bookingService, AnnouncementService announcementService, RoomTypeService roomTypeService, NotificationService notificationService, UserRepository userRepository) {
        this.userService = userService;
        this.bookingService = bookingService;
        this.announcementService = announcementService;
        this.roomTypeService = roomTypeService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("announcements", announcementService.getPublishedAnnouncements());
        return "index";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        // Добавить ошибку аутентификации если есть
        String error = request.getParameter("error");
        if (error != null) {
            model.addAttribute("error", "Неверный логин или пароль");
        }
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);
            model.addAttribute("role", user.getRole().name());

            // Добавить уведомления
            model.addAttribute("notifications", notificationService.getUnreadNotifications(user.getId()));

            // Добавить данные в зависимости от роли
            switch (user.getRole()) {
                case ADMIN:
                    model.addAttribute("pendingUsers", userService.getPendingUsers());
                    model.addAttribute("pendingBookings", bookingService.getPendingBookings());
                    model.addAttribute("conflictBookings", bookingService.getConflictBookings());
                    model.addAttribute("pendingAnnouncements", announcementService.getPendingAnnouncements());
                    model.addAttribute("allUsers", userRepository.findByRole(UserRole.RESIDENT));
                    break;
                case RESIDENT:
                    model.addAttribute("userBookings", bookingService.getUserBookings(user.getId()));
                    model.addAttribute("userAnnouncements", announcementService.getUserAnnouncements(user.getId()));
                    model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
                    break;
            }
        }

        return "dashboard";
    }

    @GetMapping("/booking")
    public String booking(Model model) {
        model.addAttribute("roomTypes", roomTypeService.getAllRoomTypes());
        model.addAttribute("equipmentTypes", EquipmentType.values());
        return "booking";
    }
}

@RestController
@RequestMapping("/api")
class ApiController {

    private final UserService userService;
    private final BookingService bookingService;
    private final AnnouncementService announcementService;
    private final RoomTypeService roomTypeService;
    private final NotificationService notificationService;

    public ApiController(UserService userService, BookingService bookingService, AnnouncementService announcementService, RoomTypeService roomTypeService, NotificationService notificationService) {
        this.userService = userService;
        this.bookingService = bookingService;
        this.announcementService = announcementService;
        this.roomTypeService = roomTypeService;
        this.notificationService = notificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationRequest request) {
        try {
            UserRole role = UserRole.valueOf(request.getRole().toUpperCase());
            User user = userService.registerUser(request.getUsername(), request.getPassword(),
                    request.getEmail(), request.getCompany(), role);
            return ResponseEntity.ok().body("Registration successful. Awaiting approval.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/booking")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        try {
            Booking booking = new Booking();

            // Находим тип помещения
            RoomType roomType = roomTypeService.findByName(request.getRoomTypeName())
                    .orElseThrow(() -> new RuntimeException("Room type not found"));
            booking.setRoomType(roomType);

            booking.setStartTime(request.getStartTime());
            booking.setEndTime(request.getEndTime());
            booking.setPurpose(request.getPurpose());
            booking.setSelectedEquipment(request.getSelectedEquipment());

            // Если это гостевое бронирование
            if (request.getGuestName() != null) {
                booking.setGuestName(request.getGuestName());
                booking.setGuestPhone(request.getGuestPhone());
                booking.setGuestEmail(request.getGuestEmail());
            } else {
                // Получаем пользователя из контекста безопасности
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated()) {
                    userService.findByUsername(auth.getName()).ifPresent(booking::setUser);
                }
            }

            Booking created = bookingService.createBooking(booking);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Booking failed: " + e.getMessage());
        }
    }

    @PostMapping("/announcement")
    public ResponseEntity<?> createAnnouncement(@RequestBody Announcement announcement) {
        try {
            // Получаем автора из контекста безопасности
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                userService.findByUsername(auth.getName()).ifPresent(announcement::setAuthor);
            }

            Announcement created = announcementService.createAnnouncement(announcement);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Announcement creation failed: " + e.getMessage());
        }
    }

    @GetMapping("/announcements")
    public ResponseEntity<List<Announcement>> getAnnouncements() {
        return ResponseEntity.ok(announcementService.getPublishedAnnouncements());
    }

    @GetMapping("/room-types")
    public ResponseEntity<List<RoomType>> getRoomTypes() {
        return ResponseEntity.ok(roomTypeService.getAllRoomTypes());
    }

    @GetMapping("/equipment-types")
    public ResponseEntity<EquipmentType[]> getEquipmentTypes() {
        return ResponseEntity.ok(EquipmentType.values());
    }

    @GetMapping("/room-bookings/{roomTypeName}")
    public ResponseEntity<List<Booking>> getRoomBookings(
            @PathVariable String roomTypeName,
            @RequestParam String start,
            @RequestParam String end) {
        try {
            LocalDateTime startTime = LocalDateTime.parse(start);
            LocalDateTime endTime = LocalDateTime.parse(end);
            List<Booking> bookings = bookingService.getRoomBookings(roomTypeName, startTime, endTime);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Admin endpoints
    @PostMapping("/admin/create-user")
    public ResponseEntity<?> createUser(@RequestBody UserCreationRequest request) {
        try {
            UserRole role = UserRole.valueOf(request.getRole().toUpperCase());
            User user = userService.createUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail(),
                    request.getCompany(),
                    request.getPhone(),
                    request.getContactPerson(),
                    role
            );
            return ResponseEntity.ok("User created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("User creation failed: " + e.getMessage());
        }
    }

    @PostMapping("/admin/update-user-credentials/{userId}")
    public ResponseEntity<?> updateUserCredentials(
            @PathVariable Long userId,
            @RequestBody UserCredentialsUpdateRequest request) {
        try {
            userService.updateUserCredentials(userId, request.getUsername(), request.getPassword());
            return ResponseEntity.ok("Credentials updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Update failed: " + e.getMessage());
        }
    }

    @PostMapping("/admin/approve-user/{userId}")
    public ResponseEntity<?> approveUser(@PathVariable Long userId) {
        try {
            userService.approveUser(userId);
            return ResponseEntity.ok("User approved");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Approval failed: " + e.getMessage());
        }
    }

    @PostMapping("/admin/approve-booking/{bookingId}")
    public ResponseEntity<?> approveBooking(@PathVariable Long bookingId) {
        try {
            bookingService.approveBooking(bookingId);
            return ResponseEntity.ok("Booking approved");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Approval failed: " + e.getMessage());
        }
    }

    @PostMapping("/admin/publish-announcement/{announcementId}")
    public ResponseEntity<?> publishAnnouncement(@PathVariable Long announcementId) {
        try {
            announcementService.publishAnnouncement(announcementId);
            return ResponseEntity.ok("Announcement published");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Publishing failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/admin/delete-user/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("User deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Deletion failed: " + e.getMessage());
        }
    }

    // Notification endpoints
    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isPresent()) {
            List<Notification> notifications = notificationService.getUserNotifications(userOpt.get().getId());
            return ResponseEntity.ok(notifications);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/notifications/{notificationId}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok("Notification marked as read");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to mark as read: " + e.getMessage());
        }
    }
}

// DTOs
class UserRegistrationRequest {
    private String username;
    private String password;
    private String email;
    private String company;
    private String role;

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

class UserCreationRequest {
    private String username;
    private String password;
    private String email;
    private String company;
    private String phone;
    private String contactPerson;
    private String role;

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

class UserCredentialsUpdateRequest {
    private String username;
    private String password;

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

class BookingRequest {
    private String roomTypeName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
    private String guestName;
    private String guestPhone;
    private String guestEmail;
    private Set<EquipmentType> selectedEquipment;

    // Getters and setters
    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }
    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    public Set<EquipmentType> getSelectedEquipment() { return selectedEquipment; }
    public void setSelectedEquipment(Set<EquipmentType> selectedEquipment) { this.selectedEquipment = selectedEquipment; }
}

@Service
class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!user.isApproved()) {
            throw new DisabledException("User account is not approved");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }
}

// Security Configuration
@EnableWebSecurity
@Configuration
class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/api/register", "/api/announcements", "/api/room-types", "/api/equipment-types",
                                "/api/room-bookings/**", "/api/booking", "/register", "/booking",
                                "/css/**", "/js/**", "/h2-console/**").permitAll()
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // Для H2 консоли
        return http.build();
    }
}

// Template Controller for serving HTML
@Controller
class TemplateController {

    @GetMapping("/templates/{template}")
    public String getTemplate(@PathVariable String template) {
        return template;
    }
}