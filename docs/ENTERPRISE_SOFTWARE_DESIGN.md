# Enterprise Banking System - Software Design Interview Case

Bu doküman, mevcut Transaction System projesini bir software design interview case'i gibi anlatmak için hazırlanmıştır. İlk kısım mevcut sistemi açıklar; ikinci kısım aynı problemi daha büyük ölçekli enterprise ortamda tasarlasaydım hangi mimari kararları alacağımı gösterir.

GitHub ve birçok Markdown viewer Mermaid diyagramlarını doğrudan render eder.

## 1. Problem Statement

Bir bankacılık sisteminde kullanıcılar hesap oluşturur, her kullanıcıya cüzdan açılır, kullanıcılar arası para transferi yapılır, transfer geçmişi ve admin raporları görüntülenir, şüpheli işlemler raporlanır ve alıcıya bildirim gönderilir.

Tasarımın hedefi:

- Para hareketlerinde tutarlılık ve audit izini korumak
- Kimlik doğrulama ve rol bazlı yetkilendirmeyi merkezi yönetmek
- Transfer akışını idempotent, izlenebilir ve hata durumlarına dayanıklı yapmak
- Raporlama ve bildirim gibi yan etkileri core transfer işleminden ayrıştırmak
- Monolitten gerektiğinde servis tabanlı mimariye evrilebilecek sınırlar kurmak

## 2. Assumptions

Interview sırasında netleştireceğim varsayımlar:

| Konu | Varsayım |
|---|---|
| Para birimi | İlk fazda TRY; target design çoklu para birimini destekler |
| Transfer türü | Kullanıcı cüzdanları arasında internal transfer |
| Tutarlılık | Debit-credit ve ledger kaydı güçlü tutarlı olmalı |
| Notification | Best effort; transfer başarısını geri almamalı |
| Fraud | İlk fazda raporlama; target design'da pre-check ve post-check olabilir |
| Auth | Mevcut sistemde JWT + role based auth; enterprise target'ta OIDC/IAM entegrasyonu |
| Ölçek | Tasarım yatay ölçeklenebilir olmalı; transaction write path dikkatli partition edilmeli |

## 3. Requirements

### Functional Requirements

- User registration
- Login, refresh token, logout
- Role based access control
- Wallet provisioning on user creation
- Wallet-to-wallet transfer
- Transaction history with pagination and filtering
- Fraud report
- Admin reports
- Idempotent notification recording
- Traceable audit data

### Non-Functional Requirements

| Requirement | Target |
|---|---|
| Correctness | Bir transferde debit ve credit birlikte başarılı olmalı veya birlikte rollback edilmeli |
| Security | JWT/OIDC, RBAC, ownership checks, mTLS, secrets management |
| Availability | Core transfer path notification hatalarından etkilenmemeli |
| Latency | Transfer p95 düşük tutulmalı; ağır fraud/report işlemleri async/read model tarafına taşınmalı |
| Auditability | Her para hareketi immutable ledger ve audit event ile izlenmeli |
| Observability | Trace ID, structured logs, metrics, alerts, audit trail |
| Scalability | Read-heavy raporlar CQRS/read replica ile ayrılmalı |
| Resilience | Timeout, retry, circuit breaker, DLQ, idempotency key |

## 4. Current Architecture

Mevcut proje iki uygulamadan oluşur:

- Enterprise App: User, Transaction, Common ve Bootstrap modüllerini içeren modüler monolit
- Notification App: Ayrı çalışan, kendi veritabanına sahip bildirim servisi

```mermaid
flowchart LR
    Client["Client / Postman / UI"] -->|HTTP + JWT| Enterprise["Enterprise App :8080"]
    Enterprise --> UserModule["User Module"]
    Enterprise --> TransactionModule["Transaction Module"]
    Enterprise --> CommonModule["Common Module"]
    UserModule -->|UserCreatedEvent| TransactionModule
    TransactionModule -->|HTTP Exchange + X-Trace-Id| Notification["Notification App :8081"]
    Enterprise --> EnterpriseDb[("H2 Enterprise DB")]
    Notification --> NotificationDb[("H2 Notification DB")]
```

### Current Component Diagram

```mermaid
flowchart TB
    subgraph EnterpriseApp["Enterprise App"]
        AuthController["AuthController"]
        UserController["UserController"]
        AdminReportController["AdminReportController"]
        TransactionController["TransactionController"]
        Security["JwtAuthenticationFilter + SecurityConfiguration"]
        CurrentUser["CurrentUserProvider"]

        subgraph UserDomain["User Domain"]
            CreateUser["CreateUserHandler"]
            DeleteUser["DeleteUserHandler"]
            Reports["Admin Report Handlers"]
            UserPort["UserPort / ReportPort"]
        end

        subgraph TransactionDomain["Transaction Domain"]
            ExecuteTransfer["ExecuteTransferHandler"]
            History["GetTransactionHistoryHandler"]
            Fraud["CheckSuspiciousTransfersHandler"]
            DeleteWallet["DeleteWalletHandler"]
            TransactionPort["TransactionPort"]
            NotificationPort["NotificationPort"]
        end

        JpaAdapters["Persistence Adapters + Spring Data Repositories"]
        NotificationAdapter["NotificationHttpExchangeAdapter"]
    end

    Client["Client"] --> Security
    Security --> AuthController
    Security --> UserController
    Security --> TransactionController
    Security --> AdminReportController

    UserController --> CreateUser
    UserController --> DeleteUser
    AdminReportController --> Reports
    TransactionController --> CurrentUser
    TransactionController --> ExecuteTransfer
    TransactionController --> History
    TransactionController --> Fraud
    TransactionController --> DeleteWallet

    CreateUser --> UserPort
    DeleteUser --> UserPort
    Reports --> UserPort
    ExecuteTransfer --> TransactionPort
    ExecuteTransfer --> NotificationPort
    History --> TransactionPort
    Fraud --> TransactionPort
    DeleteWallet --> TransactionPort

    UserPort --> JpaAdapters
    TransactionPort --> JpaAdapters
    NotificationPort --> NotificationAdapter
    NotificationAdapter --> NotificationApp["Notification App"]
```

### Current Layering

```mermaid
flowchart BT
    Infra["Infrastructure: Controller, Adapter, Entity, Repository, HTTP Exchange"]
    Domain["Domain: Model, Port, Handler, Usecase Input"]
    Common["Common: GenericResponse, Security, Trace, Handler, Audit"]

    Infra --> Domain
    Infra --> Common
    Domain --> Common
```

Kural: Domain portları altyapı detaylarını bilmez. Controller doğrudan repository, event publisher veya UUID generation yapmaz.

## 5. Target Enterprise Architecture

Enterprise ölçekte bu sistemi event-driven, ledger merkezli ve servis sınırları net olacak şekilde tasarlardım. İlk deploy yine modüler monolit olabilir; fakat bounded context sınırları servisleşmeye hazır tutulur.

```mermaid
flowchart LR
    Client["Web / Mobile / Partner API"] --> WAF["WAF / Rate Limit"]
    WAF --> Gateway["API Gateway"]
    Gateway --> IAM["IAM / OIDC Provider"]
    Gateway --> AccountSvc["Account Service"]
    Gateway --> TransferSvc["Transfer Service"]
    Gateway --> ReportingApi["Reporting API"]

    TransferSvc --> FraudSvc["Fraud Service"]
    TransferSvc --> WalletSvc["Wallet Service"]
    WalletSvc --> LedgerSvc["Ledger Service"]
    LedgerSvc --> LedgerDb[("Ledger DB")]
    WalletSvc --> WalletDb[("Wallet DB")]
    AccountSvc --> AccountDb[("Account DB")]

    TransferSvc --> Outbox[("Outbox Table")]
    Outbox --> Relay["Outbox Relay"]
    Relay --> Bus["Event Bus / Kafka"]

    Bus --> NotificationSvc["Notification Service"]
    Bus --> AuditSvc["Audit Service"]
    Bus --> ReportingProjector["Reporting Projector"]
    NotificationSvc --> NotificationDb[("Notification DB")]
    AuditSvc --> AuditStore[("WORM / Audit Store")]
    ReportingProjector --> ReportingDb[("Reporting Read DB")]
    ReportingApi --> ReportingDb

    Gateway --> Redis["Redis: Token Blacklist / Rate Limit / Idempotency"]
```

### Why This Split

| Bounded Context | Responsibility | Scaling Characteristic |
|---|---|---|
| Account Service | User profile, KYC, lifecycle | Read-heavy |
| Auth/IAM | Identity, roles, sessions | Security-critical |
| Wallet Service | Balance view, wallet ownership | Hot write path |
| Ledger Service | Immutable double-entry movements | Correctness-critical |
| Transfer Service | Orchestration, idempotency, fraud decision | Latency-sensitive |
| Notification Service | Idempotent customer notification | Async, retry-heavy |
| Reporting Service | Admin reports, fraud reports, history projections | Read-heavy |
| Audit Service | Immutable compliance events | Append-only |

## 6. C4-Style Diagrams

### System Context

```mermaid
flowchart LR
    Customer["Customer"] -->|Transfer money, view history| BankingSystem["Enterprise Banking System"]
    Admin["Admin / Risk Ops"] -->|Reports, fraud review| BankingSystem
    BankingSystem --> IAM["Identity Provider"]
    BankingSystem --> NotificationProvider["Email/SMS/Push Provider"]
    BankingSystem --> Monitoring["Monitoring / SIEM"]
    BankingSystem --> CoreBanking["External Core Banking (future)"]
```

### Container View

```mermaid
flowchart TB
    subgraph Edge["Edge Layer"]
        Gateway["API Gateway"]
        Auth["Auth / OIDC"]
    end

    subgraph Application["Application Layer"]
        UserApi["User API"]
        TransferApi["Transfer API"]
        AdminApi["Admin Report API"]
        NotificationApi["Notification API"]
    end

    subgraph Domain["Domain Services"]
        UserDomain["User Domain"]
        WalletDomain["Wallet Domain"]
        LedgerDomain["Ledger Domain"]
        FraudDomain["Fraud Domain"]
        NotificationDomain["Notification Domain"]
    end

    subgraph Data["Data Layer"]
        UserDb[("User DB")]
        WalletDb[("Wallet DB")]
        LedgerDb[("Ledger DB")]
        NotificationDb[("Notification DB")]
        ReportingDb[("Reporting DB")]
        EventBus["Event Bus"]
    end

    Gateway --> Auth
    Gateway --> UserApi
    Gateway --> TransferApi
    Gateway --> AdminApi
    TransferApi --> WalletDomain
    TransferApi --> LedgerDomain
    TransferApi --> FraudDomain
    UserApi --> UserDomain
    NotificationApi --> NotificationDomain
    UserDomain --> UserDb
    WalletDomain --> WalletDb
    LedgerDomain --> LedgerDb
    NotificationDomain --> NotificationDb
    LedgerDomain --> EventBus
    EventBus --> ReportingDb
```

## 7. Data Model

### Current Logical ERD

```mermaid
erDiagram
    USERS ||--o| WALLETS : owns
    USERS ||--o{ TRANSACTION_RECORDS : sends
    USERS ||--o{ TRANSACTION_RECORDS : receives

    USERS {
        string id PK
        string username UK
        string email UK
        string password_hash
        string role
        boolean is_user_deleted
        datetime created_at
        datetime updated_at
        string created_trace_id
        string updated_trace_id
        long version
    }

    WALLETS {
        string id PK
        string user_id FK
        decimal balance
        boolean is_active
        datetime created_at
        datetime updated_at
        string created_trace_id
        string updated_trace_id
    }

    TRANSACTION_RECORDS {
        string id PK
        string sender_user_id FK
        string receiver_user_id FK
        double amount
        string status
        datetime transaction_date
        datetime created_at
        datetime updated_at
    }

    NOTIFICATIONS {
        string id PK
        string event_id UK
        string type
        string source_service
        string recipient_id
        string reference_id
        decimal amount
        string currency
        string status
        datetime created_at
    }
```

### Target Enterprise Ledger Model

Mevcut model transfer kaydı ve wallet balance için yeterli. Enterprise bankacılıkta asıl kaynak immutable double-entry ledger olmalı.

```mermaid
erDiagram
    ACCOUNT ||--o{ WALLET : owns
    WALLET ||--o{ LEDGER_ACCOUNT : maps_to
    TRANSFER ||--o{ LEDGER_ENTRY : produces
    LEDGER_ACCOUNT ||--o{ LEDGER_ENTRY : has
    TRANSFER ||--o{ TRANSFER_EVENT : emits

    ACCOUNT {
        uuid id PK
        string customer_number UK
        string status
        string kyc_status
    }

    WALLET {
        uuid id PK
        uuid account_id FK
        string currency
        string status
        decimal available_balance
        decimal pending_balance
        long version
    }

    TRANSFER {
        uuid id PK
        string idempotency_key UK
        uuid sender_wallet_id
        uuid receiver_wallet_id
        decimal amount
        string currency
        string status
        datetime created_at
    }

    LEDGER_ACCOUNT {
        uuid id PK
        uuid wallet_id FK
        string account_type
        string currency
    }

    LEDGER_ENTRY {
        uuid id PK
        uuid transfer_id FK
        uuid ledger_account_id FK
        string direction
        decimal amount
        string currency
        datetime posted_at
    }

    TRANSFER_EVENT {
        uuid id PK
        uuid transfer_id FK
        string event_type
        string payload
        datetime created_at
    }
```

Ledger invariant:

```text
sum(debit entries) == sum(credit entries)
```

Bu invariant sağlanmadan transfer `COMPLETED` olamaz.

## 8. Core Flows

### Login Flow

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant AuthService
    participant UserRepository
    participant JwtTokenService
    participant RefreshStore

    Client->>AuthController: POST /auth/login username/password
    AuthController->>AuthService: login(username, password)
    AuthService->>UserRepository: find active user by username
    UserRepository-->>AuthService: UserEntity
    AuthService->>AuthService: BCrypt password check
    AuthService->>JwtTokenService: create access token
    AuthService->>JwtTokenService: create refresh token
    AuthService->>RefreshStore: store refresh token session
    AuthService-->>AuthController: AuthResponse
    AuthController-->>Client: GenericResponse<AuthResponse>
```

### Current Transfer Flow

```mermaid
sequenceDiagram
    participant Client
    participant JwtFilter
    participant TransactionController
    participant AccessValidator
    participant ExecuteTransferHandler
    participant TransactionPort
    participant NotificationAdapter
    participant NotificationApp

    Client->>JwtFilter: POST /transactions/transfer + Bearer token
    JwtFilter->>JwtFilter: validate token and role USER
    JwtFilter->>TransactionController: authenticated request
    TransactionController->>AccessValidator: validate token user == senderUserId
    TransactionController->>ExecuteTransferHandler: handle(input)
    ExecuteTransferHandler->>TransactionPort: load sender and receiver wallets
    TransactionPort-->>ExecuteTransferHandler: wallets
    ExecuteTransferHandler->>ExecuteTransferHandler: validate balance and update balances
    ExecuteTransferHandler->>TransactionPort: persist transfer and wallets
    ExecuteTransferHandler->>NotificationAdapter: send received-transfer notification
    NotificationAdapter->>NotificationApp: HTTP Exchange + X-Trace-Id
    NotificationApp-->>NotificationAdapter: created or duplicate
    ExecuteTransferHandler-->>TransactionController: transaction model
    TransactionController-->>Client: GenericResponse<TransferResponse>
```

### Target Transfer Flow With Outbox

```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant TransferSvc
    participant FraudSvc
    participant WalletSvc
    participant LedgerSvc
    participant TransferDb
    participant Outbox
    participant EventBus
    participant NotificationSvc
    participant ReportingSvc

    Client->>Gateway: POST /transfers + Idempotency-Key + JWT
    Gateway->>TransferSvc: authenticated command
    TransferSvc->>TransferDb: check idempotency key
    TransferSvc->>FraudSvc: synchronous risk pre-check
    FraudSvc-->>TransferSvc: allow / review / deny
    TransferSvc->>WalletSvc: reserve or validate funds
    WalletSvc-->>TransferSvc: funds reserved
    TransferSvc->>LedgerSvc: post double-entry ledger transaction
    LedgerSvc-->>TransferSvc: ledger posted
    TransferSvc->>TransferDb: mark transfer COMPLETED
    TransferSvc->>Outbox: insert TransferCompleted event in same transaction
    Outbox-->>EventBus: relay event
    EventBus-->>NotificationSvc: TransferCompleted
    EventBus-->>ReportingSvc: TransferCompleted
    TransferSvc-->>Client: transfer completed
```

Burada notification ve reporting async tüketici olur. Transfer response bu tüketicilere bağlı kalmaz.

### Notification Idempotency

```mermaid
sequenceDiagram
    participant Producer
    participant NotificationApi
    participant Handler
    participant NotificationDb

    Producer->>NotificationApi: POST /notifications eventId=tx-1
    NotificationApi->>Handler: recordNotification(input)
    Handler->>NotificationDb: findByEventId(tx-1)
    alt not found
        Handler->>NotificationDb: insert eventId=tx-1
        NotificationDb-->>Handler: saved
        Handler-->>Producer: 201 duplicate=false
    else found
        NotificationDb-->>Handler: existing notification
        Handler-->>Producer: 200 duplicate=true
    end
```

## 9. State Machines

### Transfer State

```mermaid
stateDiagram-v2
    [*] --> REQUESTED
    REQUESTED --> FRAUD_CHECKED: fraud allow
    REQUESTED --> REJECTED: fraud deny
    FRAUD_CHECKED --> FUNDS_RESERVED
    FUNDS_RESERVED --> LEDGER_POSTED
    LEDGER_POSTED --> COMPLETED
    FUNDS_RESERVED --> FAILED: reservation timeout
    LEDGER_POSTED --> RECONCILIATION_REQUIRED: publish failure
    COMPLETED --> [*]
    REJECTED --> [*]
    FAILED --> [*]
```

### Token State

```mermaid
stateDiagram-v2
    [*] --> ISSUED
    ISSUED --> REFRESHED: refresh token accepted
    ISSUED --> BLACKLISTED: logout access token
    ISSUED --> EXPIRED: ttl elapsed
    REFRESHED --> REVOKED: old refresh token revoked
    BLACKLISTED --> [*]
    EXPIRED --> [*]
    REVOKED --> [*]
```

## 10. Consistency Strategy

### Current System

- Enterprise App transfer update'leri tek process ve tek database transaction sınırında tutulur.
- Wallet balance update ve transaction record aynı use case içinde yapılır.
- Notification HTTP call best effort kabul edilir.
- Notification App `eventId` ile idempotency sağlar.
- Audit fields ve trace ID kayıt üstünde tutulur.

### Enterprise Target

| Concern | Strategy |
|---|---|
| Debit-credit correctness | Single ledger transaction with double-entry invariant |
| Cross-service side effects | Transactional outbox |
| Async delivery | At-least-once event delivery + idempotent consumers |
| Duplicate client requests | Idempotency-Key per transfer command |
| Reporting consistency | Eventually consistent read model |
| Reconciliation | Periodic ledger balance verification jobs |

Important interview point:

```text
Exactly-once delivery is not assumed. The practical design is at-least-once delivery plus idempotent writes.
```

## 11. Security Design

```mermaid
flowchart LR
    Client --> Gateway["API Gateway"]
    Gateway --> JwtFilter["JWT Validation Filter"]
    JwtFilter --> SecurityContext["SecurityContext"]
    SecurityContext --> Controller["Controller"]
    Controller --> Ownership["Business Ownership Checks"]
    Ownership --> Handler["Use Case Handler"]
```

Mevcut sistemde:

- `JwtAuthenticationFilter` access token doğrular.
- `SecurityConfiguration` endpoint pattern bazlı rol kontrolü yapar.
- `CurrentUserProvider` kullanıcı kimliğini `SecurityContext` üzerinden okur.
- `TokenBlacklistService` logout edilmiş access token'ları tutar.
- `RefreshTokenStoreService` refresh session revoke mantığını yönetir.

Enterprise hardening:

- OIDC provider entegrasyonu
- KMS/HSM ile token secret ve encryption key yönetimi
- Redis veya distributed cache üzerinde blacklist ve idempotency store
- mTLS for service-to-service calls
- API Gateway rate limiting
- Fine-grained permission model
- Sensitive field masking in logs
- SIEM integration for suspicious auth events

## 12. Scalability

### Read vs Write Split

```mermaid
flowchart LR
    TransferWrites["Transfer Write Path"] --> PrimaryDb[("Primary DB")]
    PrimaryDb --> Cdc["CDC / Outbox"]
    Cdc --> EventBus["Event Bus"]
    EventBus --> ReadProjector["Read Model Projector"]
    ReadProjector --> ReadDb[("Reporting / History Read DB")]
    AdminReports["Admin Reports"] --> ReadDb
    UserHistory["User History"] --> ReadDb
```

Scaling decisions:

| Area | Approach |
|---|---|
| Login/token blacklist | Redis cluster |
| Transaction history | Partition by user ID or wallet ID, read model for queries |
| Ledger writes | Partition by ledger account or wallet ID; avoid cross-partition hot transactions |
| Reports | Async projections; do not run heavy joins on OLTP DB |
| Notifications | Queue consumers with retry and DLQ |
| Fraud | Fast synchronous rules + async ML/risk enrichment |

### Capacity Thinking Example

| Metric | Example Target | Design Impact |
|---|---:|---|
| Registered users | 1M+ | User DB indexed by username/email |
| Transfers per day | 10M | Ledger partitioning, idempotency store, queue |
| Transfer p95 | < 300 ms | Keep fraud pre-check lightweight |
| Notification delivery | Eventually consistent | Async queue and retry |
| Transaction history p95 | < 500 ms | Read model and pagination |

## 13. Resilience and Failure Handling

```mermaid
flowchart TB
    TransferSvc["Transfer Service"] -->|timeout 2s| FraudSvc["Fraud Service"]
    TransferSvc -->|ACID call| LedgerSvc["Ledger Service"]
    TransferSvc -->|outbox event| EventBus["Event Bus"]
    EventBus --> NotificationSvc["Notification Service"]
    NotificationSvc -->|retry with backoff| Provider["Email/SMS Provider"]
    NotificationSvc --> DLQ["Dead Letter Queue"]

    FraudSvc -->|unavailable| Fallback["Risk fallback: deny or manual review"]
    Provider -->|permanent failure| DLQ
```

Failure policy examples:

| Failure | Policy |
|---|---|
| JWT invalid | 401 JSON error |
| Role mismatch | 403 JSON error |
| Insufficient balance | Business error, no ledger entry |
| Notification service down | Log + retry async; transfer remains completed |
| Event consumer duplicate | Idempotent eventId/idempotency key |
| Ledger invariant violation | Rollback and alert |
| Outbox relay down | Events remain in outbox table until relay recovers |

## 14. Observability

```mermaid
flowchart LR
    Request["Incoming Request"] --> TraceFilter["TraceIdFilter"]
    TraceFilter --> MDC["MDC traceId"]
    MDC --> Logs["Structured Logs"]
    TraceFilter --> Metrics["HTTP Metrics"]
    Transfer["Transfer Use Case"] --> Audit["Audit Event"]
    Transfer --> BusinessMetrics["Business Metrics"]
    Logs --> APM["APM / Log Platform"]
    Metrics --> Prometheus["Prometheus"]
    Prometheus --> Grafana["Grafana"]
    Audit --> AuditStore["Immutable Audit Store"]
```

Minimum metrics:

- `http.server.requests`
- `auth.login.success/failure`
- `transfer.completed`
- `transfer.rejected`
- `transfer.failed`
- `notification.created`
- `notification.duplicate`
- `notification.delivery.failure`
- `fraud.report.query.duration`
- DB connection pool metrics

Minimum alerts:

- Transfer failure rate above threshold
- Ledger invariant mismatch
- Notification DLQ growth
- Auth failure spike
- High p95 latency on transfer
- Outbox backlog age above threshold

## 15. Deployment View

```mermaid
flowchart TB
    subgraph Internet
        User["User Device"]
        Admin["Admin Device"]
    end

    subgraph Cloud["Cloud / Data Center"]
        LB["Load Balancer"]
        Gateway["API Gateway"]

        subgraph AppCluster["Kubernetes / App Cluster"]
            EnterprisePods["Enterprise App Pods"]
            NotificationPods["Notification App Pods"]
            OutboxRelayPods["Outbox Relay Pods"]
        end

        subgraph DataStores["Managed Data Stores"]
            EnterpriseDb[("Enterprise PostgreSQL")]
            NotificationDb[("Notification PostgreSQL")]
            Redis[("Redis Cluster")]
            Kafka[("Kafka Cluster")]
        end

        Observability["Logs / Metrics / Traces"]
    end

    User --> LB
    Admin --> LB
    LB --> Gateway
    Gateway --> EnterprisePods
    EnterprisePods --> EnterpriseDb
    EnterprisePods --> Redis
    EnterprisePods --> Kafka
    Kafka --> NotificationPods
    NotificationPods --> NotificationDb
    OutboxRelayPods --> Kafka
    EnterprisePods --> Observability
    NotificationPods --> Observability
```

## 16. API Design Notes

Current APIs:

| API | Role |
|---|---|
| `POST /api/v1/auth/login` | Public |
| `POST /api/v1/auth/refresh` | Public |
| `POST /api/v1/auth/logout` | Authenticated |
| `POST /api/v1/users` | Public |
| `GET /api/v1/users/basic-list` | USER |
| `DELETE /api/v1/users/{userId}` | USER owner |
| `POST /api/v1/transactions/transfer` | USER sender |
| `GET /api/v1/transactions/history` | USER owner |
| `GET /api/v1/transactions/fraud-report` | ADMIN |
| `DELETE /api/v1/transactions/wallets/{walletId}` | USER owner |
| `POST /api/v1/notifications` | Internal service call |

Enterprise API improvements:

- Add `Idempotency-Key` header to transfer command.
- Return `202 Accepted` for async flows that enter manual fraud review.
- Use opaque external IDs instead of exposing internal IDs where needed.
- Add cursor-based pagination for large histories.
- Add versioned APIs: `/api/v2/transfers`.
- Add problem-details compatible error model if external consumers require it.

## 17. Trade-Offs

| Decision | Benefit | Cost |
|---|---|---|
| Modular monolith first | Simpler deployment, strong local consistency | Scaling per bounded context is limited |
| Split Notification App | Demonstrates service communication and idempotency | More operational surface |
| JWT stateless auth | Horizontally scalable APIs | Logout needs blacklist store |
| Outbox pattern | Reliable event publishing | Extra relay and storage complexity |
| Double-entry ledger | Strong financial auditability | More complex data model |
| CQRS for reporting | Fast reports without harming OLTP | Eventual consistency |
| Async notification | Transfer path remains stable | Notification may be delayed |

## 18. Interview Presentation Script

Kısa sunum akışı:

1. "Önce core requirement'i ayırırım: transfer para hareketidir, notification yan etkidir."
2. "Mevcut sistem modüler monolit; domain sınırları port/adapter ile ayrılmış."
3. "Transfer path güçlü tutarlı olmalı; notification best effort ve idempotent olmalı."
4. "Enterprise versiyonda transferi ledger merkezli tasarlarım; double-entry invariant ana güvenlik hattıdır."
5. "Servisler arası event için exactly-once varsaymam; outbox + at-least-once + idempotent consumer kullanırım."
6. "Auth tarafında Security Filter Chain, JWT, refresh token revoke, blacklist ve RBAC merkezi kalır."
7. "Raporlama OLTP DB'den ayrılır; event projection/read model ile büyütülür."
8. "Observability ve audit para sisteminde feature değil zorunluluktur."

## 19. What I Would Build Next

Öncelik sırası:

1. Transfer endpoint'e `Idempotency-Key` eklemek
2. Wallet balance için `Double` yerine `BigDecimal` kullanmak
3. Double-entry ledger modeli eklemek
4. Transactional outbox eklemek
5. Notification delivery'i queue consumer modeline almak
6. Refresh token ve blacklist store'u Redis'e taşımak
7. Reporting read model oluşturmak
8. Fraud kararını rule engine + async enrichment şeklinde büyütmek
9. Audit event store'u immutable hale getirmek
10. Deployment için Docker Compose veya Kubernetes manifestleri eklemek

## 20. Review Checklist

Interview sonunda kendime soracağım kontrol listesi:

- Para hareketi transaction boundary içinde mi?
- Duplicate request transferi iki kez çalıştırabilir mi?
- Notification başarısızlığı transferi geri alıyor mu?
- Raporlama OLTP write path'i yavaşlatıyor mu?
- Token logout ve refresh revoke davranışı net mi?
- Admin ve user endpoint erişimleri merkezi mi?
- Trace ID servisler arası taşınıyor mu?
- Audit bilgisi sonradan değiştirilemez şekilde saklanabilir mi?
- Manual fraud review için durum modeli hazır mı?
- Sistem hangi noktadan yatay scale edilecek?
