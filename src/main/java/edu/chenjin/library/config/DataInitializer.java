package edu.chenjin.library.config;

import edu.chenjin.library.entity.Book;
import edu.chenjin.library.entity.Loan;
import edu.chenjin.library.entity.Notification;
import edu.chenjin.library.entity.Reservation;
import edu.chenjin.library.entity.User;
import edu.chenjin.library.repository.BookRepository;
import edu.chenjin.library.repository.LoanRepository;
import edu.chenjin.library.repository.NotificationRepository;
import edu.chenjin.library.repository.ReservationRepository;
import edu.chenjin.library.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           BookRepository bookRepository,
                           LoanRepository loanRepository,
                           ReservationRepository reservationRepository,
                           NotificationRepository notificationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.reservationRepository = reservationRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedUsers();
            log.info("已初始化演示用户");
        }
        if (bookRepository.count() == 0) {
            seedBooks();
            log.info("已初始化演示图书");
        }
        if (loanRepository.count() == 0 && reservationRepository.count() == 0) {
            seedLoansAndReservations();
            log.info("已初始化借阅、预约与通知数据");
        }
    }

    private void seedUsers() {
        createUser("admin", "admin123", "ADMIN", "13800000001", "系统管理员", user -> {
            user.setPermissionLevel(9);
            user.setEntryTime(LocalDate.of(2020, 9, 1));
            user.setDuty("系统管理");
            user.setEmail("admin@library.edu.cn");
        });
        createUser("operator", "operator123", "OPERATOR", "13800000002", "图书馆学", user -> {
            user.setPermissionLevel(5);
            user.setEntryTime(LocalDate.of(2021, 3, 1));
            user.setDuty("借阅服务");
            user.setArea("一楼借阅区");
        });
        createUser("student1", "student123", "STUDENT", "13800000003", "计算机科学与技术", user -> {
            user.setGrade("2024级");
            user.setCollege("信息工程学院");
            user.setEmail("student1@library.edu.cn");
        });
        createUser("teacher", "teacher123", "TEACHER", "13800000004", "软件工程", user -> {
            user.setTitle("副教授");
            user.setCollege("信息工程学院");
        });
        createUser("student2", "student123", "STUDENT", "13800000005", "软件工程", user -> {
            user.setGrade("2023级");
            user.setCollege("信息工程学院");
        });
        createUser("student3", "student123", "STUDENT", "13800000006", "汉语言文学", user -> {
            user.setGrade("2024级");
            user.setCollege("文学院");
        });
        createUser("student4", "student123", "STUDENT", "13800000007", "会计学", user -> {
            user.setGrade("2022级");
            user.setCollege("经济管理学院");
        });
        createUser("student5", "student123", "STUDENT", "13800000008", "人工智能", user -> {
            user.setGrade("2023级");
            user.setCollege("信息工程学院");
        });
        createUser("student6", "student123", "STUDENT", "13800000009", "视觉传达设计", user -> {
            user.setGrade("2024级");
            user.setCollege("艺术设计学院");
        });
        createUser("student7", "student123", "STUDENT", "13800000010", "历史学", user -> {
            user.setGrade("2022级");
            user.setCollege("人文学院");
        });
        createUser("student8", "student123", "STUDENT", "13800000011", "数据科学与大数据技术", user -> {
            user.setGrade("2023级");
            user.setCollege("信息工程学院");
        });
        createUser("student9", "student123", "STUDENT", "13800000012", "英语", user -> {
            user.setGrade("2024级");
            user.setCollege("外国语学院");
        });
        createUser("student10", "student123", "STUDENT", "13800000013", "工商管理", user -> {
            user.setGrade("2022级");
            user.setCollege("经济管理学院");
        });
        createUser("teacher2", "teacher123", "TEACHER", "13800000014", "中国古代文学", user -> {
            user.setTitle("讲师");
            user.setCollege("文学院");
        });
        createUser("teacher3", "teacher123", "TEACHER", "13800000015", "计算机应用技术", user -> {
            user.setTitle("教授");
            user.setCollege("信息工程学院");
        });
        createUser("operator2", "operator123", "OPERATOR", "13800000016", "信息管理与信息系统", user -> {
            user.setPermissionLevel(4);
            user.setDuty("图书采编");
            user.setArea("二楼书库");
        });
    }

    private User createUser(String name, String password, String role, String phone,
                            String major, java.util.function.Consumer<User> customizer) {
        User user = new User();
        user.setName(name);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setPhone(phone);
        user.setMajor(major);
        user.setStatus("ACTIVE");
        user.setLoginTime(LocalDateTime.now().minusDays(1));
        customizer.accept(user);
        return userRepository.save(user);
    }

    private void seedBooks() {
        Object[][] data = {
                {"三体", "刘慈欣", "文学", "重庆出版社", 1, 32.00, "A-01", "地球文明与三体文明的信息交流、生死搏杀及两个文明在宇宙中的兴衰历程。"},
                {"活着", "余华", "文学", "作家出版社", 6, 28.00, "A-02", "讲述一个人历尽世间沧桑和磨难的一生，亦将中国大半个世纪的社会变迁凝缩其间。"},
                {"百年孤独", "加西亚·马尔克斯", "文学", "南海出版公司", 1, 39.50, "A-03", "布恩迪亚家族七代人的传奇故事，以及加勒比海沿岸小镇马孔多的百年兴衰。"},
                {"红楼梦", "曹雪芹", "文学", "人民文学出版社", 8, 59.70, "A-04", "中国古典四大名著之一，以贾、史、王、薛四大家族的兴衰为背景。"},
                {"平凡的世界", "路遥", "文学", "北京十月文艺出版社", 6, 49.00, "A-05", "全景式地表现中国当代城乡社会生活的长篇小说。"},
                {"围城", "钱钟书", "文学", "人民文学出版社", 4, 36.00, "A-06", "以幽默而辛辣的笔触描绘抗战初期知识分子的群像。"},
                {"白夜行", "东野圭吾", "文学", "南海出版公司", 5, 39.50, "A-07", "一桩十九年前的悬案，牵出两个人长达十九年的命运纠葛。"},
                {"解忧杂货店", "东野圭吾", "文学", "南海出版公司", 5, 39.50, "A-08", "一家能够咨询烦恼的杂货店，连接起跨越时空的温暖故事。"},
                {"深入理解Java虚拟机", "周志明", "计算机", "机械工业出版社", 6, 129.00, "B-01", "系统讲解 Java 虚拟机的内存模型、垃圾收集、类加载与性能调优。"},
                {"Spring实战", "克雷格·沃斯", "计算机", "人民邮电出版社", 5, 99.00, "B-02", "全面介绍 Spring Boot、Spring MVC、Spring Data 等核心技术的实践指南。"},
                {"算法导论", "托马斯·科尔曼", "计算机", "机械工业出版社", 4, 128.00, "B-03", "计算机算法领域的经典教材，覆盖排序、图算法、动态规划等主题。"},
                {"计算机网络：自顶向下方法", "詹姆斯·库罗斯", "计算机", "机械工业出版社", 5, 89.00, "B-04", "从应用层出发，自顶向下讲解计算机网络的基本原理与协议。"},
                {"数据库系统概论", "王珊", "计算机", "高等教育出版社", 7, 42.00, "B-05", "系统介绍数据库系统的基本概念、关系数据库、SQL 与事务处理。"},
                {"代码整洁之道", "罗伯特·马丁", "计算机", "人民邮电出版社", 5, 59.00, "B-06", "通过大量实例讲解如何编写可读、可维护的高质量代码。"},
                {"重构：改善既有代码的设计", "马丁·福勒", "计算机", "人民邮电出版社", 4, 99.00, "B-07", "系统整理重构手法，帮助开发者在保持行为不变的前提下改善代码结构。"},
                {"Java编程思想", "布鲁斯·埃克尔", "计算机", "机械工业出版社", 4, 108.00, "B-08", "深入讲解 Java 语言特性与面向对象设计思想的经典著作。"},
                {"万历十五年", "黄仁宇", "历史", "中华书局", 6, 32.00, "C-01", "以万历十五年为中心，剖析明代政治、经济与社会的深层结构。"},
                {"人类简史", "尤瓦尔·赫拉利", "历史", "中信出版社", 7, 68.00, "C-02", "从认知革命、农业革命到科学革命，重新审视人类文明的演进。"},
                {"明朝那些事儿", "当年明月", "历史", "北京联合出版公司", 8, 358.00, "C-03", "以轻松幽默的笔法讲述明朝三百年历史。"},
                {"枪炮、病菌与钢铁", "贾雷德·戴蒙德", "历史", "中信出版社", 4, 69.00, "C-04", "从地理与环境角度解释不同大陆人类社会发展差异。"},
                {"经济学原理", "曼昆", "经济", "北京大学出版社", 5, 88.00, "D-01", "以十大经济学原理为主线，介绍微观与宏观经济学基础。"},
                {"国富论", "亚当·斯密", "经济", "商务印书馆", 3, 58.00, "D-02", "古典经济学奠基之作，系统论述分工、市场与国民财富。"},
                {"原则", "瑞·达利欧", "经济", "中信出版社", 4, 98.00, "D-03", "桥水基金创始人总结的生活与工作原则。"},
                {"时间简史", "史蒂芬·霍金", "科学", "湖南科学技术出版社", 5, 45.00, "E-01", "从宇宙大爆炸到黑洞，带领读者探索宇宙的起源与命运。"},
                {"物种起源", "查尔斯·达尔文", "科学", "商务印书馆", 3, 48.00, "E-02", "生物进化论奠基之作，阐述自然选择与物种演化。"},
                {"艺术的故事", "贡布里希", "艺术", "广西美术出版社", 4, 280.00, "F-01", "以通俗流畅的语言讲述从史前到现代的艺术发展史。"},
                {"美的历程", "李泽厚", "艺术", "生活·读书·新知三联书店", 4, 42.00, "F-02", "从远古图腾到明清文艺，梳理中国美学的发展脉络。"},
                {"苏菲的世界", "乔斯坦·贾德", "哲学", "作家出版社", 5, 42.00, "G-01", "以小说形式讲述西方哲学史，是哲学入门的经典读物。"},
                {"教育心理学", "陈琦", "教育", "高等教育出版社", 4, 52.00, "H-01", "介绍学习理论、学生心理发展与教学设计的心理学基础。"},
                {"月亮与六便士", "毛姆", "文学", "上海译文出版社", 5, 39.00, "A-09", "以画家高更为原型，探讨理想与现实、艺术与生活的冲突。"}
        };
        for (int i = 0; i < data.length; i++) {
            Object[] row = data[i];
            Book book = new Book();
            book.setTitle((String) row[0]);
            book.setAuthor((String) row[1]);
            book.setType((String) row[2]);
            book.setPublisher((String) row[3]);
            book.setIsbn(String.format("9787115%06d", i + 1));
            int total = (Integer) row[4];
            book.setTotalCopies(total);
            book.setAvailableCopies(total);
            book.setPrice(BigDecimal.valueOf((Double) row[5]));
            book.setShelfNo((String) row[6]);
            book.setIntroduction((String) row[7]);
            book.setDays(30);
            book.setStatus("ACTIVE");
            bookRepository.save(book);
        }
    }

    private void seedLoansAndReservations() {
        Map<String, User> users = new HashMap<>();
        userRepository.findAll().forEach(user -> users.put(user.getName(), user));
        List<Book> books = new ArrayList<>(bookRepository.findAll());
        if (books.size() < 12 || !users.containsKey("student1")) {
            return;
        }

        Book threeBody = books.get(0);
        Book solitude = books.get(2);
        createBorrow(users.get("student2"), threeBody, 18, null, "BORROWED");
        createBorrow(users.get("teacher"), solitude, 46, null, "BORROWED");

        createBorrow(users.get("student1"), books.get(1), 8, null, "BORROWED");
        createBorrow(users.get("student1"), books.get(8), 42, null, "BORROWED");
        createBorrow(users.get("student4"), books.get(3), 12, null, "BORROWED");
        createBorrow(users.get("student5"), books.get(4), 20, 10, "RETURNED");
        createBorrow(users.get("student6"), books.get(16), 25, null, "BORROWED");
        createBorrow(users.get("teacher2"), books.get(17), 16, null, "BORROWED");
        createBorrow(users.get("student7"), books.get(5), 35, 5, "RETURNED");
        createBorrow(users.get("student8"), books.get(11), 9, null, "BORROWED");
        createBorrow(users.get("student9"), books.get(23), 22, null, "BORROWED");
        createBorrow(users.get("student10"), books.get(24), 30, 2, "RETURNED");
        createBorrow(users.get("student3"), books.get(6), 14, null, "BORROWED");

        Reservation reservation = new Reservation();
        reservation.setUserId(users.get("student3").getId());
        reservation.setBookId(solitude.getId());
        reservation.setStatus("WAITING");
        reservationRepository.save(reservation);

        Notification welcome = new Notification();
        welcome.setUserId(users.get("student1").getId());
        welcome.setTitle("欢迎使用智慧图书馆");
        welcome.setContent("欢迎回来！您可以在这里查询馆藏、借阅图书、预约缺藏书目，并实时接收到书通知。");
        welcome.setType("SYSTEM");
        welcome.setLink("/books");
        welcome.setReadFlag(false);
        notificationRepository.save(welcome);

        Notification tip = new Notification();
        tip.setUserId(users.get("student1").getId());
        tip.setTitle("预约到书提醒");
        tip.setContent("当预约的图书出现可借副本时，系统会通过右上角消息中心实时提醒您，请留意查收。");
        tip.setType("SYSTEM");
        tip.setLink("/my-reservations");
        tip.setReadFlag(false);
        notificationRepository.save(tip);
    }

    private void createBorrow(User user, Book book, int daysAgo, Integer returnedDaysAgo, String status) {
        if (user == null || book == null) {
            return;
        }
        LocalDateTime borrowDate = LocalDateTime.now().minusDays(daysAgo);
        Loan loan = new Loan();
        loan.setUserId(user.getId());
        loan.setBookId(book.getId());
        loan.setBorrowDate(borrowDate);
        loan.setDueDate(borrowDate.plusDays(book.getDays() == null ? 30 : book.getDays()));
        loan.setStatus(status);
        loan.setOperatorId(1L);
        if ("RETURNED".equals(status)) {
            loan.setReturnDate(LocalDateTime.now().minusDays(returnedDaysAgo == null ? 1 : returnedDaysAgo));
        } else {
            if (book.getAvailableCopies() <= 0) {
                return;
            }
            book.setAvailableCopies(book.getAvailableCopies() - 1);
            bookRepository.save(book);
        }
        loanRepository.save(loan);
    }
}
