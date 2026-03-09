# Xpense Spring Boot Backend Setup

This guide walks you through creating a Spring Boot backend service for the Xpense app, replacing Firebase Firestore with a PostgreSQL-backed REST API. It covers project setup, data models (with Lombok), JPA repositories, service layer, REST controllers, JWT authentication, and database migrations.

> **Note:** This guide does not cover the GoCardless (bank integration) part. Only the core features — users, wallets, transactions, invitations, and statistics — are included.

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Project Initialization](#2-project-initialization)
3. [Project Structure](#3-project-structure)
4. [Configuration](#4-configuration)
5. [Database Schema](#5-database-schema)
6. [Models (JPA Entities)](#6-models-jpa-entities)
7. [Repositories](#7-repositories)
8. [Services](#8-services)
9. [REST Controllers (APIs)](#9-rest-controllers-apis)
10. [Security (JWT Authentication)](#10-security-jwt-authentication)
11. [Error Handling](#11-error-handling)
12. [Running the Application](#12-running-the-application)
13. [API Reference](#13-api-reference)

---

## 1. Prerequisites

- **Java 17** or later
- **Maven 3.8+** or **Gradle 8+**
- **PostgreSQL 14+** installed and running
- **An IDE** such as IntelliJ IDEA or VS Code

---

## 2. Project Initialization

Use [Spring Initializr](https://start.spring.io/) with the following settings:

| Setting       | Value                        |
|---------------|------------------------------|
| Project       | Maven                        |
| Language      | Java                         |
| Spring Boot   | 3.2.x (latest stable)        |
| Group         | com.rokudo                    |
| Artifact      | xpense-backend               |
| Package name  | com.rokudo.xpense             |
| Packaging     | Jar                          |
| Java          | 17                           |

**Dependencies to add:**

- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Spring Security
- Lombok
- Spring Boot Starter Validation

Or add the following to your `pom.xml` `<dependencies>` section:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.6</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.6</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.6</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 3. Project Structure

```
src/main/java/com/rokudo/xpense/
├── XpenseBackendApplication.java        # Main entry point
├── config/
│   └── SecurityConfig.java              # Spring Security + JWT config
├── controller/
│   ├── AuthController.java              # Login / Register
│   ├── UserController.java              # User profile CRUD
│   ├── WalletController.java            # Wallet CRUD
│   ├── TransactionController.java       # Transaction CRUD
│   ├── InvitationController.java        # Invitation CRUD
│   └── StatisticsController.java        # Statistics queries
├── dto/
│   ├── AuthRequest.java                 # Login request body
│   ├── AuthResponse.java               # Login response (token)
│   ├── RegisterRequest.java             # Register request body
│   └── StatisticsResponse.java          # Statistics response
├── model/
│   ├── User.java
│   ├── Wallet.java
│   ├── WalletUser.java
│   ├── Transaction.java
│   └── Invitation.java
├── repository/
│   ├── UserRepository.java
│   ├── WalletRepository.java
│   ├── TransactionRepository.java
│   └── InvitationRepository.java
├── security/
│   ├── JwtTokenProvider.java            # JWT creation & validation
│   └── JwtAuthenticationFilter.java     # Request filter
├── service/
│   ├── UserService.java
│   ├── WalletService.java
│   ├── TransactionService.java
│   ├── InvitationService.java
│   └── StatisticsService.java
└── exception/
    ├── GlobalExceptionHandler.java
    └── ResourceNotFoundException.java
```

---

## 4. Configuration

### `src/main/resources/application.properties`

```properties
# ── Server ──
server.port=8080

# ── PostgreSQL ──
spring.datasource.url=jdbc:postgresql://localhost:5432/xpense
spring.datasource.username=postgres
spring.datasource.password=your_password

# ── JPA / Hibernate ──
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# ── JWT ──
app.jwt.secret=your-256-bit-secret-key-change-this-in-production
app.jwt.expiration-ms=86400000
```

> **Tip:** Set `spring.jpa.hibernate.ddl-auto=update` during initial development to auto-create tables, then switch to `validate` + migration scripts for production.

---

## 5. Database Schema

Create the PostgreSQL database and tables. Run these SQL statements in order:

```sql
-- Create the database
CREATE DATABASE xpense;

-- Connect to xpense database, then run:

-- 1. Users
CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) UNIQUE NOT NULL,
    picture_url  VARCHAR(500),
    password     VARCHAR(255) NOT NULL,
    fcm_token    VARCHAR(500),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Wallets
CREATE TABLE wallets (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title         VARCHAR(255) NOT NULL,
    creator_id    UUID NOT NULL REFERENCES users(id),
    amount        DOUBLE PRECISION DEFAULT 0.0,
    currency      VARCHAR(10) NOT NULL DEFAULT 'RON',
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Wallet-User join table (shared wallets)
CREATE TABLE wallet_users (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    user_id   UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_name VARCHAR(255),
    user_pic  VARCHAR(500),
    UNIQUE(wallet_id, user_id)
);

-- 4. Transactions
CREATE TABLE transactions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id         UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    type              VARCHAR(20) NOT NULL CHECK (type IN ('Income', 'Expense', 'Transfer')),
    category          VARCHAR(100),
    user_id           UUID NOT NULL REFERENCES users(id),
    user_name         VARCHAR(255),
    title             VARCHAR(255),
    amount            DOUBLE PRECISION NOT NULL,
    currency          VARCHAR(10) NOT NULL DEFAULT 'RON',
    date              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pic_url           VARCHAR(500),
    is_cash_transaction BOOLEAN DEFAULT FALSE,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Invitations
CREATE TABLE invitations (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id                   UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    creator_id                  UUID NOT NULL REFERENCES users(id),
    creator_name                VARCHAR(255),
    wallet_title                VARCHAR(255),
    invited_person_phone_number VARCHAR(50) NOT NULL,
    creator_pic_url             VARCHAR(500),
    date                        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status                      VARCHAR(20) NOT NULL DEFAULT 'Sent'
                                CHECK (status IN ('Sent', 'Accepted', 'Declined'))
);

-- Indexes for common queries
CREATE INDEX idx_transactions_wallet_id ON transactions(wallet_id);
CREATE INDEX idx_transactions_date ON transactions(date);
CREATE INDEX idx_transactions_wallet_date ON transactions(wallet_id, date DESC);
CREATE INDEX idx_wallet_users_wallet_id ON wallet_users(wallet_id);
CREATE INDEX idx_wallet_users_user_id ON wallet_users(user_id);
CREATE INDEX idx_invitations_phone ON invitations(invited_person_phone_number);
```

---

## 6. Models (JPA Entities)

### `model/User.java`

```java
package com.rokudo.xpense.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "picture_url")
    private String pictureUrl;

    @Column(nullable = false)
    private String password;

    @Column(name = "fcm_token")
    private String fcmToken;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### `model/Wallet.java`

```java
package com.rokudo.xpense.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false)
    @Builder.Default
    private Double amount = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private String currency = "RON";

    @CreationTimestamp
    @Column(name = "creation_date", updatable = false)
    private LocalDateTime creationDate;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WalletUser> walletUsers = new ArrayList<>();

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();
}
```

### `model/WalletUser.java`

```java
package com.rokudo.xpense.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "wallet_users",
       uniqueConstraints = @UniqueConstraint(columnNames = {"wallet_id", "user_id"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_pic")
    private String userPic;
}
```

### `model/Transaction.java`

```java
package com.rokudo.xpense.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    public static final String INCOME_TYPE = "Income";
    public static final String EXPENSE_TYPE = "Expense";
    public static final String TRANSFER_TYPE = "Transfer";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false)
    private String type;

    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_name")
    private String userName;

    private String title;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    @Builder.Default
    private String currency = "RON";

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(name = "pic_url")
    private String picUrl;

    @Column(name = "is_cash_transaction")
    @Builder.Default
    private Boolean isCashTransaction = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### `model/Invitation.java`

```java
package com.rokudo.xpense.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invitations")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitation {

    public static final String STATUS_SENT = "Sent";
    public static final String STATUS_ACCEPTED = "Accepted";
    public static final String STATUS_DECLINED = "Declined";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "creator_name")
    private String creatorName;

    @Column(name = "wallet_title")
    private String walletTitle;

    @Column(name = "invited_person_phone_number", nullable = false)
    private String invitedPersonPhoneNumber;

    @Column(name = "creator_pic_url")
    private String creatorPicUrl;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime date = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private String status = STATUS_SENT;
}
```

---

## 7. Repositories

### `repository/UserRepository.java`

```java
package com.rokudo.xpense.repository;

import com.rokudo.xpense.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);
}
```

### `repository/WalletRepository.java`

```java
package com.rokudo.xpense.repository;

import com.rokudo.xpense.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Query("SELECT w FROM Wallet w JOIN w.walletUsers wu WHERE wu.user.id = :userId ORDER BY w.creationDate DESC")
    List<Wallet> findAllByUserId(@Param("userId") UUID userId);

    @Query("SELECT w FROM Wallet w JOIN w.walletUsers wu WHERE wu.user.id = :userId ORDER BY w.creationDate DESC LIMIT 1")
    Wallet findLatestByUserId(@Param("userId") UUID userId);
}
```

### `repository/TransactionRepository.java`

```java
package com.rokudo.xpense.repository;

import com.rokudo.xpense.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByWalletIdAndDateBetweenOrderByDateDesc(
            UUID walletId, LocalDateTime start, LocalDateTime end);

    List<Transaction> findByWalletIdOrderByDateDesc(UUID walletId);

    @Query("SELECT t FROM Transaction t WHERE t.wallet.id = :walletId ORDER BY t.date DESC LIMIT 1")
    Transaction findLatestByWalletId(@Param("walletId") UUID walletId);

    @Query("SELECT t FROM Transaction t WHERE t.wallet.id = :walletId AND t.date >= :start AND t.date <= :end AND t.type = :type ORDER BY t.date DESC")
    List<Transaction> findByWalletIdAndDateBetweenAndType(
            @Param("walletId") UUID walletId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("type") String type);

    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t WHERE t.wallet.id = :walletId AND t.date >= :start AND t.date <= :end AND t.type = 'Expense' GROUP BY t.category")
    List<Object[]> getAmountByCategory(
            @Param("walletId") UUID walletId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.wallet.id = :walletId AND t.date >= :start AND t.date <= :end AND t.type = 'Expense'")
    Double getTotalExpenseAmount(
            @Param("walletId") UUID walletId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
```

### `repository/InvitationRepository.java`

```java
package com.rokudo.xpense.repository;

import com.rokudo.xpense.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    List<Invitation> findByInvitedPersonPhoneNumberOrderByDateDesc(String phoneNumber);

    List<Invitation> findByCreatorIdOrderByDateDesc(UUID creatorId);
}
```

---

## 8. Services

### `service/UserService.java`

```java
package com.rokudo.xpense.service;

import com.rokudo.xpense.exception.ResourceNotFoundException;
import com.rokudo.xpense.model.User;
import com.rokudo.xpense.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone: " + phoneNumber));
    }

    public User updateUser(UUID id, User updatedUser) {
        User user = getUserById(id);
        if (updatedUser.getName() != null) user.setName(updatedUser.getName());
        if (updatedUser.getPictureUrl() != null) user.setPictureUrl(updatedUser.getPictureUrl());
        if (updatedUser.getFcmToken() != null) user.setFcmToken(updatedUser.getFcmToken());
        return userRepository.save(user);
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
```

### `service/WalletService.java`

```java
package com.rokudo.xpense.service;

import com.rokudo.xpense.exception.ResourceNotFoundException;
import com.rokudo.xpense.model.Wallet;
import com.rokudo.xpense.model.WalletUser;
import com.rokudo.xpense.model.User;
import com.rokudo.xpense.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserService userService;

    public Wallet getWalletById(UUID id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));
    }

    public List<Wallet> getWalletsByUserId(UUID userId) {
        return walletRepository.findAllByUserId(userId);
    }

    @Transactional
    public Wallet createWallet(Wallet wallet, UUID creatorId) {
        User creator = userService.getUserById(creatorId);
        wallet.setCreator(creator);

        // Add creator as a wallet user
        WalletUser walletUser = WalletUser.builder()
                .wallet(wallet)
                .user(creator)
                .userName(creator.getName())
                .userPic(creator.getPictureUrl())
                .build();
        wallet.getWalletUsers().add(walletUser);

        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet updateWallet(UUID id, Wallet updated) {
        Wallet wallet = getWalletById(id);
        if (updated.getTitle() != null) wallet.setTitle(updated.getTitle());
        if (updated.getCurrency() != null) wallet.setCurrency(updated.getCurrency());
        return walletRepository.save(wallet);
    }

    @Transactional
    public void updateWalletAmount(UUID walletId, double delta) {
        Wallet wallet = getWalletById(walletId);
        wallet.setAmount(wallet.getAmount() + delta);
        walletRepository.save(wallet);
    }

    @Transactional
    public void deleteWallet(UUID id) {
        if (!walletRepository.existsById(id)) {
            throw new ResourceNotFoundException("Wallet not found with id: " + id);
        }
        walletRepository.deleteById(id);
    }
}
```

### `service/TransactionService.java`

```java
package com.rokudo.xpense.service;

import com.rokudo.xpense.exception.ResourceNotFoundException;
import com.rokudo.xpense.model.Transaction;
import com.rokudo.xpense.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletService walletService;

    public Transaction getTransactionById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    public List<Transaction> getTransactionsByWalletAndDateRange(UUID walletId, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByWalletIdAndDateBetweenOrderByDateDesc(walletId, start, end);
    }

    public List<Transaction> getAllTransactionsByWallet(UUID walletId) {
        return transactionRepository.findByWalletIdOrderByDateDesc(walletId);
    }

    public Transaction getLatestTransaction(UUID walletId) {
        return transactionRepository.findLatestByWalletId(walletId);
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        Transaction saved = transactionRepository.save(transaction);

        // Update wallet balance (mirrors the Firebase Cloud Function logic)
        if (!Boolean.TRUE.equals(transaction.getIsCashTransaction())) {
            double delta = Transaction.INCOME_TYPE.equals(transaction.getType())
                    ? transaction.getAmount()
                    : -transaction.getAmount();
            walletService.updateWalletAmount(transaction.getWallet().getId(), delta);
        }

        return saved;
    }

    @Transactional
    public Transaction updateTransaction(UUID id, Transaction updated) {
        Transaction existing = getTransactionById(id);

        // Reverse old wallet balance effect
        if (!Boolean.TRUE.equals(existing.getIsCashTransaction())) {
            double oldDelta = Transaction.INCOME_TYPE.equals(existing.getType())
                    ? -existing.getAmount()
                    : existing.getAmount();
            walletService.updateWalletAmount(existing.getWallet().getId(), oldDelta);
        }

        // Apply updates
        if (updated.getType() != null) existing.setType(updated.getType());
        if (updated.getCategory() != null) existing.setCategory(updated.getCategory());
        if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
        if (updated.getAmount() != null) existing.setAmount(updated.getAmount());
        if (updated.getDate() != null) existing.setDate(updated.getDate());
        if (updated.getIsCashTransaction() != null) existing.setIsCashTransaction(updated.getIsCashTransaction());

        Transaction saved = transactionRepository.save(existing);

        // Apply new wallet balance effect
        if (!Boolean.TRUE.equals(saved.getIsCashTransaction())) {
            double newDelta = Transaction.INCOME_TYPE.equals(saved.getType())
                    ? saved.getAmount()
                    : -saved.getAmount();
            walletService.updateWalletAmount(saved.getWallet().getId(), newDelta);
        }

        return saved;
    }

    @Transactional
    public void deleteTransaction(UUID id) {
        Transaction transaction = getTransactionById(id);

        // Reverse wallet balance effect
        if (!Boolean.TRUE.equals(transaction.getIsCashTransaction())) {
            double delta = Transaction.INCOME_TYPE.equals(transaction.getType())
                    ? -transaction.getAmount()
                    : transaction.getAmount();
            walletService.updateWalletAmount(transaction.getWallet().getId(), delta);
        }

        transactionRepository.deleteById(id);
    }
}
```

### `service/InvitationService.java`

```java
package com.rokudo.xpense.service;

import com.rokudo.xpense.exception.ResourceNotFoundException;
import com.rokudo.xpense.model.*;
import com.rokudo.xpense.repository.InvitationRepository;
import com.rokudo.xpense.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final WalletRepository walletRepository;
    private final UserService userService;

    public Invitation getInvitationById(UUID id) {
        return invitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found with id: " + id));
    }

    public List<Invitation> getInvitationsForUser(String phoneNumber) {
        return invitationRepository.findByInvitedPersonPhoneNumberOrderByDateDesc(phoneNumber);
    }

    public Invitation createInvitation(Invitation invitation) {
        return invitationRepository.save(invitation);
    }

    @Transactional
    public Invitation updateStatus(UUID id, String status) {
        Invitation invitation = getInvitationById(id);
        invitation.setStatus(status);
        Invitation saved = invitationRepository.save(invitation);

        // If accepted, add the invited user to the wallet
        // (mirrors the Firebase Cloud Function invitesListener logic)
        if (Invitation.STATUS_ACCEPTED.equals(status)) {
            User invitedUser = userService.getUserByPhoneNumber(invitation.getInvitedPersonPhoneNumber());
            Wallet wallet = walletRepository.findById(invitation.getWallet().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

            WalletUser walletUser = WalletUser.builder()
                    .wallet(wallet)
                    .user(invitedUser)
                    .userName(invitedUser.getName())
                    .userPic(invitedUser.getPictureUrl())
                    .build();
            wallet.getWalletUsers().add(walletUser);
            walletRepository.save(wallet);
        }

        return saved;
    }
}
```

### `service/StatisticsService.java`

```java
package com.rokudo.xpense.service;

import com.rokudo.xpense.dto.StatisticsResponse;
import com.rokudo.xpense.model.Transaction;
import com.rokudo.xpense.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final TransactionRepository transactionRepository;

    /**
     * Get monthly statistics for a wallet.
     * Replaces the Firebase Cloud Function that computed statistics on transaction write.
     * Here, statistics are computed on-the-fly from the transactions table.
     */
    public StatisticsResponse getMonthlyStatistics(UUID walletId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        Double totalExpense = transactionRepository.getTotalExpenseAmount(walletId, start, end);

        List<Object[]> categoryAmounts = transactionRepository.getAmountByCategory(walletId, start, end);
        Map<String, Double> amountByCategory = new LinkedHashMap<>();
        for (Object[] row : categoryAmounts) {
            amountByCategory.put((String) row[0], (Double) row[1]);
        }

        List<Transaction> transactions = transactionRepository
                .findByWalletIdAndDateBetweenOrderByDateDesc(walletId, start, end);

        // Group transactions by day
        Map<Integer, List<Transaction>> transactionsByDay = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getDate().getDayOfMonth()));

        return StatisticsResponse.builder()
                .totalAmountSpent(totalExpense)
                .amountByCategory(amountByCategory)
                .transactionsByDay(transactionsByDay)
                .transactions(transactions)
                .build();
    }

    /**
     * Get yearly statistics for a wallet (aggregated from all months).
     */
    public StatisticsResponse getYearlyStatistics(UUID walletId, int year) {
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        Double totalExpense = transactionRepository.getTotalExpenseAmount(walletId, start, end);

        List<Object[]> categoryAmounts = transactionRepository.getAmountByCategory(walletId, start, end);
        Map<String, Double> amountByCategory = new LinkedHashMap<>();
        for (Object[] row : categoryAmounts) {
            amountByCategory.put((String) row[0], (Double) row[1]);
        }

        List<Transaction> transactions = transactionRepository
                .findByWalletIdAndDateBetweenOrderByDateDesc(walletId, start, end);

        Map<Integer, List<Transaction>> transactionsByDay = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getDate().getDayOfMonth()));

        return StatisticsResponse.builder()
                .totalAmountSpent(totalExpense)
                .amountByCategory(amountByCategory)
                .transactionsByDay(transactionsByDay)
                .transactions(transactions)
                .build();
    }
}
```

---

## 9. REST Controllers (APIs)

### DTOs

#### `dto/AuthRequest.java`

```java
package com.rokudo.xpense.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private String password;
}
```

#### `dto/AuthResponse.java`

```java
package com.rokudo.xpense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UUID userId;
    private String name;
}
```

#### `dto/RegisterRequest.java`

```java
package com.rokudo.xpense.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private String password;
    private String pictureUrl;
}
```

#### `dto/StatisticsResponse.java`

```java
package com.rokudo.xpense.dto;

import com.rokudo.xpense.model.Transaction;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class StatisticsResponse {
    private Double totalAmountSpent;
    private Map<String, Double> amountByCategory;
    private Map<Integer, List<Transaction>> transactionsByDay;
    private List<Transaction> transactions;
}
```

### Controllers

#### `controller/AuthController.java`

```java
package com.rokudo.xpense.controller;

import com.rokudo.xpense.dto.AuthRequest;
import com.rokudo.xpense.dto.AuthResponse;
import com.rokudo.xpense.dto.RegisterRequest;
import com.rokudo.xpense.model.User;
import com.rokudo.xpense.repository.UserRepository;
import com.rokudo.xpense.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = User.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .pictureUrl(request.getPictureUrl())
                .build();
        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId().toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, user.getId(), user.getName()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtTokenProvider.generateToken(user.getId().toString());
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getName()));
    }
}
```

#### `controller/UserController.java`

```java
package com.rokudo.xpense.controller;

import com.rokudo.xpense.model.User;
import com.rokudo.xpense.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

#### `controller/WalletController.java`

```java
package com.rokudo.xpense.controller;

import com.rokudo.xpense.model.Wallet;
import com.rokudo.xpense.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestBody Wallet wallet,
                                                @RequestParam UUID creatorId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletService.createWallet(wallet, creatorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wallet> getWallet(@PathVariable UUID id) {
        return ResponseEntity.ok(walletService.getWalletById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Wallet>> getWalletsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(walletService.getWalletsByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Wallet> updateWallet(@PathVariable UUID id, @RequestBody Wallet wallet) {
        return ResponseEntity.ok(walletService.updateWallet(id, wallet));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWallet(@PathVariable UUID id) {
        walletService.deleteWallet(id);
        return ResponseEntity.noContent().build();
    }
}
```

#### `controller/TransactionController.java`

```java
package com.rokudo.xpense.controller;

import com.rokudo.xpense.model.Transaction;
import com.rokudo.xpense.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets/{walletId}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@PathVariable UUID walletId,
                                                          @RequestBody Transaction transaction) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(transaction));
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactions(
            @PathVariable UUID walletId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(
                    transactionService.getTransactionsByWalletAndDateRange(walletId, startDate, endDate));
        }
        return ResponseEntity.ok(transactionService.getAllTransactionsByWallet(walletId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable UUID walletId,
                                                       @PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @GetMapping("/latest")
    public ResponseEntity<Transaction> getLatestTransaction(@PathVariable UUID walletId) {
        return ResponseEntity.ok(transactionService.getLatestTransaction(walletId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable UUID walletId,
                                                          @PathVariable UUID id,
                                                          @RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, transaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID walletId,
                                                   @PathVariable UUID id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
```

#### `controller/InvitationController.java`

```java
package com.rokudo.xpense.controller;

import com.rokudo.xpense.model.Invitation;
import com.rokudo.xpense.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping
    public ResponseEntity<Invitation> createInvitation(@RequestBody Invitation invitation) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationService.createInvitation(invitation));
    }

    @GetMapping("/user/{phoneNumber}")
    public ResponseEntity<List<Invitation>> getInvitationsForUser(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(invitationService.getInvitationsForUser(phoneNumber));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Invitation> updateStatus(@PathVariable UUID id,
                                                    @RequestParam String status) {
        return ResponseEntity.ok(invitationService.updateStatus(id, status));
    }
}
```

#### `controller/StatisticsController.java`

```java
package com.rokudo.xpense.controller;

import com.rokudo.xpense.dto.StatisticsResponse;
import com.rokudo.xpense.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/wallets/{walletId}/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/monthly")
    public ResponseEntity<StatisticsResponse> getMonthlyStatistics(
            @PathVariable UUID walletId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(statisticsService.getMonthlyStatistics(walletId, year, month));
    }

    @GetMapping("/yearly")
    public ResponseEntity<StatisticsResponse> getYearlyStatistics(
            @PathVariable UUID walletId,
            @RequestParam int year) {
        return ResponseEntity.ok(statisticsService.getYearlyStatistics(walletId, year));
    }
}
```

---

## 10. Security (JWT Authentication)

### `security/JwtTokenProvider.java`

```java
package com.rokudo.xpense.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

### `security/JwtAuthenticationFilter.java`

```java
package com.rokudo.xpense.security;

import com.rokudo.xpense.model.User;
import com.rokudo.xpense.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtTokenProvider.validateToken(token)) {
                String userId = jwtTokenProvider.getUserIdFromToken(token);
                User user = userRepository.findById(UUID.fromString(userId)).orElse(null);

                if (user != null) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

### `config/SecurityConfig.java`

```java
package com.rokudo.xpense.config;

import com.rokudo.xpense.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## 11. Error Handling

### `exception/ResourceNotFoundException.java`

```java
package com.rokudo.xpense.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

### `exception/GlobalExceptionHandler.java`

```java
package com.rokudo.xpense.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 404,
                "error", "Not Found",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 400,
                "error", "Bad Request",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 500,
                "error", "Internal Server Error",
                "message", ex.getMessage()
        ));
    }
}
```

---

## 12. Running the Application

### 1. Set up PostgreSQL

```bash
# Create the database
psql -U postgres -c "CREATE DATABASE xpense;"

# Run the schema (save the SQL from Section 5 to a file)
psql -U postgres -d xpense -f schema.sql
```

### 2. Configure credentials

Edit `src/main/resources/application.properties` and set your PostgreSQL username, password, and JWT secret.

### 3. Build and run

```bash
# Using Maven
./mvnw clean install
./mvnw spring-boot:run

# Or using the JAR directly
java -jar target/xpense-backend-0.0.1-SNAPSHOT.jar
```

The server starts at `http://localhost:8080`.

### 4. Test with cURL

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John","phoneNumber":"+40712345678","password":"secret123"}'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"+40712345678","password":"secret123"}'
```

**Create a wallet** (use the token from login):
```bash
curl -X POST "http://localhost:8080/api/wallets?creatorId=<user-uuid>" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"My Wallet","currency":"RON"}'
```

**Add a transaction:**
```bash
curl -X POST http://localhost:8080/api/wallets/<wallet-uuid>/transactions \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "Expense",
    "category": "Groceries",
    "amount": 150.50,
    "currency": "RON",
    "date": "2025-03-09T10:30:00",
    "title": "Weekly groceries"
  }'
```

**Get monthly statistics:**
```bash
curl "http://localhost:8080/api/wallets/<wallet-uuid>/statistics/monthly?year=2025&month=3" \
  -H "Authorization: Bearer <token>"
```

---

## 13. API Reference

### Auth

| Method | Endpoint             | Description        | Auth Required |
|--------|---------------------|--------------------|---------------|
| POST   | `/api/auth/register` | Register new user  | No            |
| POST   | `/api/auth/login`    | Login, get JWT     | No            |

### Users

| Method | Endpoint          | Description        | Auth Required |
|--------|------------------|--------------------|---------------|
| GET    | `/api/users/{id}` | Get user by ID     | Yes           |
| PUT    | `/api/users/{id}` | Update user        | Yes           |
| DELETE | `/api/users/{id}` | Delete user        | Yes           |

### Wallets

| Method | Endpoint                    | Description                 | Auth Required |
|--------|----------------------------|-----------------------------|---------------|
| POST   | `/api/wallets`             | Create wallet               | Yes           |
| GET    | `/api/wallets/{id}`        | Get wallet by ID            | Yes           |
| GET    | `/api/wallets/user/{userId}` | Get all wallets for user  | Yes           |
| PUT    | `/api/wallets/{id}`        | Update wallet               | Yes           |
| DELETE | `/api/wallets/{id}`        | Delete wallet               | Yes           |

### Transactions

| Method | Endpoint                                              | Description                     | Auth Required |
|--------|------------------------------------------------------|---------------------------------|---------------|
| POST   | `/api/wallets/{walletId}/transactions`               | Create transaction              | Yes           |
| GET    | `/api/wallets/{walletId}/transactions`               | List transactions (with optional date range) | Yes |
| GET    | `/api/wallets/{walletId}/transactions/{id}`          | Get transaction by ID           | Yes           |
| GET    | `/api/wallets/{walletId}/transactions/latest`        | Get latest transaction          | Yes           |
| PUT    | `/api/wallets/{walletId}/transactions/{id}`          | Update transaction              | Yes           |
| DELETE | `/api/wallets/{walletId}/transactions/{id}`          | Delete transaction              | Yes           |

### Invitations

| Method | Endpoint                               | Description                    | Auth Required |
|--------|---------------------------------------|--------------------------------|---------------|
| POST   | `/api/invitations`                    | Create invitation              | Yes           |
| GET    | `/api/invitations/user/{phoneNumber}` | Get invitations for a user     | Yes           |
| PATCH  | `/api/invitations/{id}/status`        | Accept/Decline invitation      | Yes           |

### Statistics

| Method | Endpoint                                           | Description              | Auth Required |
|--------|---------------------------------------------------|--------------------------|---------------|
| GET    | `/api/wallets/{walletId}/statistics/monthly`      | Monthly stats (year, month params) | Yes    |
| GET    | `/api/wallets/{walletId}/statistics/yearly`       | Yearly stats (year param) | Yes           |

### Transaction Categories

The following categories are supported (matching the Android app):

| Category       |
|---------------|
| Groceries      |
| Restaurant     |
| Drinks         |
| Transport      |
| Fuel           |
| Bills          |
| Gifts          |
| Medical        |
| Others         |
| Housing        |
| Clothing       |
| Entertainment  |
| Income         |
