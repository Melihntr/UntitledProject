# Transaction System

Transaction System; kullanıcı, cüzdan, para transferi, raporlama ve bildirim süreçlerini örnekleyen Java 21 ve Spring Boot tabanlı bir bankacılık projesidir.

Proje iki uygulamadan oluşur:

- **Enterprise App:** Kullanıcı, cüzdan, işlem ve raporlama yeteneklerini barındıran modüler monolit.
- **Notification App:** Transfer bildirimlerini ayrı veri tabanında idempotent biçimde kaydeden bağımsız servis.

Kod tabanı; katmanlar arası bağımlılıkları portlar üzerinden sınırlandırmayı, iş kurallarını use case sınıflarında toplamayı, altyapı detaylarını adapter katmanında tutmayı ve servisler arası hata sözleşmesini açık hale getirmeyi amaçlar.

## İçindekiler

- [Temel Özellikler](#temel-özellikler)
- [Mimari Genel Bakış](#mimari-genel-bakış)
- [Modül Yapısı](#modül-yapısı)
- [Katmanlar ve Sorumluluklar](#katmanlar-ve-sorumluluklar)
- [Temel İş Akışları](#temel-iş-akışları)
- [API Referansı](#api-referansı)
- [Notification App ve Servisler Arası İletişim](#notification-app-ve-servisler-arası-iletişim)
- [Veri Modeli](#veri-modeli)
- [Güvenlik](#güvenlik)
- [Hata Yönetimi](#hata-yönetimi)
- [Loglama ve İzlenebilirlik](#loglama-ve-izlenebilirlik)
- [Test Stratejisi ve Coverage](#test-stratejisi-ve-coverage)
- [Projeyi Çalıştırma](#projeyi-çalıştırma)
- [Yapılandırma](#yapılandırma)
- [Geliştirme Rehberi](#geliştirme-rehberi)
- [Mevcut Sınırlar ve Production Yol Haritası](#mevcut-sınırlar-ve-production-yol-haritası)

## Temel Özellikler

- Kullanıcı oluşturma ve temel kullanıcı listeleme
- Kullanıcı oluşturulduğunda domain event üzerinden otomatik cüzdan oluşturma
- Cüzdanlar arası para transferi
- Kullanıcı bazlı, tarih filtreli ve sayfalı işlem geçmişi
- Aktif transfer, kullanıcı-cüzdan özeti ve sahipsiz cüzdan raporları
- Şüpheli transfer raporu
- Servisler arası Spring HTTP Exchange iletişimi
- Transfer bildirimlerinin ayrı Notification App içinde saklanması
- `eventId` tabanlı idempotent bildirim kaydı
- Servisler arasında `X-Trace-Id` aktarımı
- Tip güvenli başarı ve hata cevapları
- Bean Validation, merkezi exception handler ve SLF4J loglama
- Persistence tarafından üretilen UUID kimlikleri, entity ilişkileri ve optimistic locking
- Tüm modüllerde yüzde 100 line ve branch coverage eşiği

## Mimari Genel Bakış

```mermaid
flowchart LR
    Client["API İstemcisi"] --> Enterprise["Enterprise App :8080"]
    Enterprise --> User["User Modülü"]
    Enterprise --> Transaction["Transaction Modülü"]
    User -->|"UserCreatedEvent"| Transaction
    Transaction -->|"HTTP Exchange + X-Trace-Id"| Notification["Notification App :8081"]
    Enterprise --> EnterpriseDb[("Enterprise H2")]
    Notification --> NotificationDb[("Notification H2")]
```

Enterprise App, dağıtık sistem karmaşıklığı oluşturmadan domain sınırlarını belirgin tutmak için **modüler monolit** olarak tasarlanmıştır. Notification App ise bağımsız çalıştırılabilen ayrı bir servis olarak servisler arası iletişimi, hata sözleşmelerini ve idempotency yaklaşımını gösterir.

Her modülde ana bağımlılık yönü aşağıdaki gibidir:

```mermaid
flowchart LR
    Api["API / Controller"] --> UseCase["Use Case / Handler"]
    UseCase --> Domain["Domain Model ve Portlar"]
    Adapter["Infrastructure Adapter"] --> Domain
    Adapter --> External["JPA, Spring Event, HTTP veya Harici Servis"]
```

Bu yaklaşımın başlıca sonuçları:

- Controller yalnızca HTTP isteğini alır, doğrulanmış girdiyi use case'e aktarır ve cevabı döner.
- İş akışları handler sınıflarında yürütülür.
- Domain katmanı, JPA repository veya HTTP client gibi altyapı detaylarına doğrudan bağımlı değildir.
- Altyapı adapterları domain tarafından tanımlanan portları uygular.
- Entity kimlikleri `@GeneratedValue(strategy = GenerationType.UUID)` ile persistence tarafından üretilir; ID üretimi listener, controller veya use case katmanına sızmaz.
- Bildirim servisine erişim, işlem use case'inden `NotificationPort` aracılığıyla yapılır.

## Modül Yapısı

```text
enterprise-banking-system/
├── project-app/
│   ├── pom.xml
│   ├── project-common/
│   ├── project-user/
│   ├── project-transaction/
│   └── project-bootstrap/
└── project-notification/
    ├── pom.xml
    └── src/
```

### `project-common`

Modüller arasında paylaşılan teknik sözleşmeleri içerir:

- Ortak API cevap modeli
- Ortak hata cevap modeli
- Business exception yapısı
- Enterprise App merkezi exception handler'ı
- Trace ID üretimi ve MDC entegrasyonu

Ortak modül yalnızca gerçekten paylaşılan, domain bağımsız bileşenleri içerir. Kullanıcı veya işlem domainine özel davranışlar kendi modüllerinde kalır.

### `project-user`

Kullanıcı yönetimi ve kullanıcı odaklı raporların sahibidir:

- Kullanıcı oluşturma
- Temel kullanıcı listeleme
- Kullanıcı-cüzdan özet raporu
- Aktif transfer raporu
- Sahipsiz cüzdan kontrolü
- `UserCreatedEvent` yayınlama

Controller event yayınlamaz. Kullanıcı oluşturma use case'i, `UserEventPublisherPort` üzerinden eventi yayınlar; Spring event altyapısı adapter katmanında bulunur.

### `project-transaction`

Cüzdan ve para transferi domaininin sahibidir:

- Cüzdan oluşturma
- Transfer yürütme
- İşlem geçmişi
- Şüpheli transfer raporu
- Kullanıcı oluşturma eventini dinleme
- Notification App'e bildirim gönderme

`WalletEventListener`, kullanıcı oluşturma eventini dinler ve cüzdan oluşturma akışını başlatır. Cüzdan kimliği entity tarafından üretildiği için listener yalnızca iş akışı koordinasyonundan sorumludur.

### `project-bootstrap`

Enterprise App'in çalıştırılabilir modülüdür:

- Spring Boot uygulama başlangıç noktası
- Modüllerin birlikte ayağa kaldırılması
- HTTP Exchange client kurulumu
- Notification App URL ve timeout yapılandırması
- Enterprise App çalışma zamanı konfigürasyonu

### `project-notification`

Bağımsız Notification App'tir:

- Transfer bildirimlerini doğrular ve kaydeder
- Aynı `eventId` için mükerrer kayıt oluşturmaz
- Başarı ve hata cevaplarını tip güvenli sözleşmelerle döner
- Kendi merkezi exception handler'ına ve trace filtresine sahiptir
- Enterprise App'ten bağımsız veri tabanı kullanır

Notification App de Enterprise App ile aynı port-adapter sınırlarını izler:

```text
domain/
├── model/                 Altyapıdan bağımsız iş modelleri
├── port/                  Dış dünya ve persistence sözleşmeleri
├── handler/               Tek bir iş akışını yürüten handler implementasyonları
└── usecase/               Handler giriş sözleşmeleri ve input modelleri
infrastructure/
├── api/
│   ├── controller/        HTTP endpointleri
│   ├── dto/               Dış servis sözleşmeleri
│   └── mapper/            API DTO <-> domain dönüşümleri
├── adapter/               Domain portlarının teknik uygulamaları
├── entity/                Persistence entity'leri
├── mapper/                Domain <-> entity dönüşümleri
└── repository/            Spring Data repository'leri
```

`RecordNotificationHandler` yalnızca domain modelleri ve `NotificationPort` ile çalışır. Concurrent
unique-key yarışının teknik çözümü `NotificationPersistenceAdapter` içinde tutulur; controller ve
use case JPA entity veya repository detaylarını bilmez.

Paket sınırı tüm bounded contextlerde aynıdır: HTTP'ye bağlı `api` sınıfları `infrastructure.api` altında, iş akışını yürüten sınıflar `domain.handler` altında ve handler giriş sözleşmeleri `domain.usecase` altında tutulur. Böylece domain modeli taşıma protokolünden, handler implementasyonları da input tanımlarının fiziksel konumundan ayrılır.

## Katmanlar ve Sorumluluklar

### API Katmanı

API katmanının sorumlulukları:

- HTTP endpointlerini sunmak
- Request DTO doğrulamasını tetiklemek
- Header ve query parametrelerini almak
- Use case çağırmak
- Standart response dönmek

Controller içinde repository çağrısı, event yayınlama, UUID üretme veya iş kuralı çalıştırma yapılmaz.

### Use Case / Application Katmanı

Use case handlerları tek bir iş akışını yürütür. Örnekler:

- `CreateUserHandler`
- `ExecuteTransferHandler`
- `GetTransactionHistoryHandler`
- `CheckSuspiciousTransfersHandler`

Handlerlar domain portlarına bağımlıdır. Böylece persistence, event altyapısı ve HTTP iletişimi testlerde kolayca izole edilebilir.

### Domain Katmanı

Domain katmanı:

- Entity ve domain modellerini
- İş kurallarını
- Port arayüzlerini
- Domain eventlerini

içerir. Yeni entity kimlikleri uygulama kodunda atanmaz; persistence işlemi sırasında `@GeneratedValue(strategy = GenerationType.UUID)` ile üretilir.

Başlıca portlar:

| Modül | Port | Amaç |
|---|---|---|
| User | `UserPort` | Kullanıcı persistence işlemleri |
| User | `ReportPort` | Rapor sorguları |
| User | `UserEventPublisherPort` | Kullanıcı domain eventlerini yayınlama |
| Transaction | `TransactionPort` | Cüzdan ve işlem persistence işlemleri |
| Transaction | `NotificationPort` | Bildirim gönderme |

### Infrastructure / Adapter Katmanı

Adapterlar portların teknik uygulamasını sağlar:

- JPA persistence adapterları
- Spring Event publisher adapterı
- Spring Event listener
- HTTP Exchange notification adapterı
- Repository ve mapper bileşenleri

Bu sayede bir altyapı seçimi değiştiğinde use case ve domain kodunun etkilenmesi azaltılır.

## Temel İş Akışları

### Kullanıcı Oluşturma ve Cüzdan Açma

```mermaid
sequenceDiagram
    participant Client
    participant UserController
    participant CreateUserHandler
    participant UserPort
    participant EventPublisher
    participant WalletEventListener
    participant TransactionPort

    Client->>UserController: POST /api/v1/users
    UserController->>CreateUserHandler: create(request)
    CreateUserHandler->>UserPort: save(user)
    CreateUserHandler->>EventPublisher: publish(UserCreatedEvent)
    EventPublisher->>WalletEventListener: UserCreatedEvent
    WalletEventListener->>TransactionPort: save(wallet)
    CreateUserHandler-->>UserController: created user
    UserController-->>Client: success response
```

Akış özellikleri:

- Kullanıcı kaydı use case içinde oluşturulur.
- Event publisher controller yerine service/use case katmanında kullanılır.
- Event, aynı Enterprise App süreci içinde Spring Event ile senkron işlenir.
- Yeni cüzdan başlangıç bakiyesiyle oluşturulur.
- Kullanıcı ve cüzdan UUID değerleri persistence tarafından üretilir.

### Transfer ve Bildirim

```mermaid
sequenceDiagram
    participant Client
    participant RequestPipeline
    participant TransactionController
    participant TransferHandler
    participant TransactionPort
    participant NotificationAdapter
    participant NotificationApp
    participant NotificationDb

    Client->>RequestPipeline: Bearer access token + transfer request
    RequestPipeline->>TransactionController: traced request
    TransactionController->>TransferHandler: execute(request)
    TransferHandler->>TransactionPort: load wallets
    TransferHandler->>TransferHandler: validate and update balances
    TransferHandler->>TransactionPort: save transaction
    TransferHandler->>NotificationAdapter: notify receiver
    NotificationAdapter->>NotificationApp: POST notification + X-Trace-Id
    NotificationApp->>NotificationDb: idempotent save
    NotificationApp-->>NotificationAdapter: typed success/error response
    TransferHandler-->>TransactionController: completed transfer
    TransactionController-->>Client: success response
```

Transfer sırasında:

1. `Authorization: Bearer <accessToken>` header'ı `JwtAuthenticationFilter` tarafından doğrulanır.
2. Token içindeki kullanıcı kimliği ile gönderen kullanıcının aynı olması doğrulanır.
3. Gönderen ve alıcı cüzdanları yüklenir.
4. Bakiye ve transfer kuralları kontrol edilir.
5. Bakiyeler güncellenir ve işlem kaydı `COMPLETED` olarak saklanır.
6. Alıcı için Notification App'e bildirim gönderilir.
7. Bildirim hatası ayrıntılı biçimde loglanır; tamamlanmış transfer cevabı korunur.

## API Referansı

### Enterprise App

Varsayılan adres: `http://localhost:8080`

Ortak request header yaklaşımı:

```http
Accept: application/json
Content-Type: application/json
X-Trace-Id: optional-client-trace-id
Authorization: Bearer <accessToken>
```

`Authorization` header'ı public endpointlerde kullanılmaz. `X-Trace-Id` opsiyoneldir; verilmezse uygulama yeni trace ID üretir. User yetkili endpointler `ROLE_USER`, admin endpointler `ROLE_ADMIN` ister. Admin token'ı user endpointlerine, user token'ı admin endpointlerine giremez.

| Metot | Endpoint | Auth | Açıklama |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Public | Kullanıcı adı ve şifre ile access/refresh token üretir |
| `POST` | `/api/v1/auth/refresh` | Public | Geçerli refresh token ile yeni token çifti üretir |
| `POST` | `/api/v1/auth/logout` | Authenticated | Access token'ı blacklist'e alır, refresh token'ı revoke eder |
| `POST` | `/api/v1/users` | Public | Kullanıcı oluşturur |
| `GET` | `/api/v1/users/basic-list` | `ROLE_USER` | Temel kullanıcı listesini döner |
| `DELETE` | `/api/v1/users/{userId}` | `ROLE_USER` | Yalnızca oturumdaki kullanıcı kendi kaydını soft delete yapabilir |
| `GET` | `/api/v1/admin/reports/user-wallet-summary` | `ROLE_ADMIN` | Kullanıcı-cüzdan özetini döner |
| `GET` | `/api/v1/admin/reports/active-transfers` | `ROLE_ADMIN` | Aktif transfer raporunu döner |
| `GET` | `/api/v1/admin/reports/health/orphan-wallets` | `ROLE_ADMIN` | Sahipsiz cüzdanları döner |
| `POST` | `/api/v1/transactions/transfer` | `ROLE_USER` | Para transferi yapar |
| `GET` | `/api/v1/transactions/history` | `ROLE_USER` | Kullanıcının işlem geçmişini sayfalı döner |
| `GET` | `/api/v1/transactions/fraud-report` | `ROLE_ADMIN` | Şüpheli transfer raporunu döner |
| `DELETE` | `/api/v1/transactions/wallets/{walletId}` | `ROLE_USER` | Wallet ID ile yalnızca ilgili cüzdanı soft delete yapar |

Standart Enterprise App başarılı cevap formatı:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {}
}
```

Standart Enterprise App hata cevap formatı:

```json
{
  "success": false,
  "message": "Error message",
  "data": null
}
```

#### Login

```http
POST /api/v1/auth/login HTTP/1.1
Host: localhost:8080
Accept: application/json
Content-Type: application/json
X-Trace-Id: demo-login-trace

{
  "username": "test_sender",
  "password": "password123"
}
```

Örnek cevap:

```json
{
  "success": true,
  "message": "Login completed successfully.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresInSeconds": 900,
    "userId": "user-uuid",
    "role": "USER"
  }
}
```

Varsayılan admin seed bilgisi:

```text
username: admin
password: AdminPass123!
role: ADMIN
```

#### Refresh Token

```http
POST /api/v1/auth/refresh HTTP/1.1
Host: localhost:8080
Accept: application/json
Content-Type: application/json
X-Trace-Id: demo-refresh-trace

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Başarılı refresh işleminde eski refresh token revoke edilir ve yeni access/refresh token çifti döner.

#### Logout

```http
POST /api/v1/auth/logout HTTP/1.1
Host: localhost:8080
Accept: application/json
Content-Type: application/json
Authorization: Bearer <accessToken>
X-Trace-Id: demo-logout-trace

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

`refreshToken` body içinde opsiyoneldir. Access token varsa blacklist'e alınır; refresh token verilirse refresh session revoke edilir.

#### Kullanıcı Oluşturma

```http
POST /api/v1/users HTTP/1.1
Host: localhost:8080
Accept: application/json
Content-Type: application/json
X-Trace-Id: demo-create-user-trace

{
  "username": "demo-user",
  "email": "demo@example.com",
  "password": "strong-password"
}
```

Doğrulama kuralları:

- `username`: 3-50 karakter
- `email`: geçerli e-posta formatı
- `password`: en az 8 karakter

#### Temel Kullanıcı Listesi

```http
GET /api/v1/users/basic-list HTTP/1.1
Host: localhost:8080
Accept: application/json
Authorization: Bearer <userAccessToken>
X-Trace-Id: demo-basic-list-trace
```

Örnek `data`:

```json
[
  {
    "id": "user-uuid",
    "username": "test_sender"
  }
]
```

#### Kullanıcı Silme

```http
DELETE /api/v1/users/user-uuid HTTP/1.1
Host: localhost:8080
Accept: application/json
Authorization: Bearer <userAccessToken>
X-Trace-Id: demo-delete-user-trace
```

Token içindeki kullanıcı yalnızca kendi `userId` değerini silebilir. İşlem fiziksel delete değildir; `is_user_deleted=true` yapılır.

Başarılı cevap:

```json
{
  "success": true,
  "message": "User deleted successfully.",
  "data": null
}
```

#### Admin Kullanıcı-Cüzdan Özeti

```http
GET /api/v1/admin/reports/user-wallet-summary HTTP/1.1
Host: localhost:8080
Accept: application/json
Authorization: Bearer <adminAccessToken>
X-Trace-Id: demo-summary-trace
```

Örnek `data`:

```json
[
  {
    "username": "test_sender",
    "email": "sender@example.com",
    "balance": 1500.00
  }
]
```

#### Admin Aktif Transfer Raporu

```http
GET /api/v1/admin/reports/active-transfers HTTP/1.1
Host: localhost:8080
Accept: application/json
Authorization: Bearer <adminAccessToken>
X-Trace-Id: demo-active-transfer-trace
```

Örnek `data`:

```json
[
  {
    "username": "test_sender",
    "amount": 25.00,
    "createdAt": "2026-06-24T10:15:30"
  }
]
```

#### Admin Sahipsiz Cüzdan Sağlık Kontrolü

```http
GET /api/v1/admin/reports/health/orphan-wallets HTTP/1.1
Host: localhost:8080
Accept: application/json
Authorization: Bearer <adminAccessToken>
X-Trace-Id: demo-orphan-wallet-trace
```

Örnek `data`:

```json
[
  {
    "walletId": "wallet-uuid",
    "balance": 1500.00,
    "supposedUserId": "missing-user-id"
  }
]
```

#### Transfer

```http
POST /api/v1/transactions/transfer HTTP/1.1
Host: localhost:8080
Accept: application/json
Content-Type: application/json
Authorization: Bearer <userAccessToken>
X-Trace-Id: demo-transfer-trace

{
  "senderUserId": "sender-user-id",
  "receiverUserId": "receiver-user-id",
  "amount": 25.00
}
```

`amount` değeri en az `0.01` olmalıdır.

Token içindeki `userId`, `senderUserId` ile aynı olmalıdır. Aksi halde `403 Forbidden` döner.

Örnek cevap:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    "transactionId": "transaction-uuid",
    "status": "COMPLETED",
    "amount": 25.0,
    "transactionDate": "2026-06-24T10:15:30"
  }
}
```

#### İşlem Geçmişi

```http
GET /api/v1/transactions/history?userId=user-id&startDate=2026-01-01T00:00:00&endDate=2026-12-31T23:59:59&page=0&size=20 HTTP/1.1
Host: localhost:8080
Accept: application/json
Authorization: Bearer <userAccessToken>
X-Trace-Id: demo-history-trace
```

Token içindeki kullanıcı yalnızca kendi işlem geçmişini görüntüleyebilir.

İşlem geçmişi pagination sorgusu `TransactionRecordSpecifications.historyForUser(...)` ile JPA Criteria API üzerinden oluşturulur. Repository, `JpaSpecificationExecutor` kullanarak Criteria filtresi ile `Pageable` bilgisini birlikte uygular.

Örnek cevap `data` alanı Spring `Page` yapısıdır:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    "content": [
      {
        "transactionId": "transaction-uuid",
        "status": "COMPLETED",
        "amount": 25.0,
        "transactionDate": "2026-06-24T10:15:30"
      }
    ],
    "pageable": {},
    "totalElements": 1,
    "totalPages": 1,
    "last": true,
    "size": 20,
    "number": 0
  }
}
```

#### Admin Fraud Report

```http
GET /api/v1/transactions/fraud-report HTTP/1.1
Host: localhost:8080
Accept: application/json
Authorization: Bearer <adminAccessToken>
X-Trace-Id: demo-fraud-trace
```

Şüpheli kayıt yoksa veya persistence katmanı beklenmedik biçimde `null` döndürürse `data` alanı `null` değil `[]` olur.

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": []
}
```

#### Wallet Silme

```http
DELETE /api/v1/transactions/wallets/wallet-uuid HTTP/1.1
Host: localhost:8080
Accept: application/json
Authorization: Bearer <userAccessToken>
X-Trace-Id: demo-delete-wallet-trace
```

Path parametresi user ID değil wallet ID'dir. Wallet mevcut olsa bile token içindeki kullanıcı wallet sahibine ait değilse HTTP `403 Forbidden` ve başarısız `GenericResponse` döner. Sahiplik kontrolü controller yerine `DeleteWalletHandler` içinde uygulanır.

Her iki delete endpoint'i de soft delete uygular. Kayıtlar fiziksel olarak silinmez. Kullanıcı ancak `is_user_deleted=true` olduğunda silinmiş kabul edilir; alanın entity varsayılanı `false` değeridir. Cüzdan için varsayılanı `true` olan `is_active` alanı silmede `false` yapılır. Repository metotları Spring Data derived query isimleriyle silinmemiş kullanıcıları ve aktif cüzdanları döndürür.

İlgili user veya wallet bulunamadığında HTTP `404 Not Found` ve standart başarısız cevap döner:

```json
{
  "success": false,
  "message": "User not found with ID: requested-user-id",
  "data": null
}
```

### Notification App

Varsayılan adres: `http://localhost:8081`

| Metot | Endpoint | Auth | Açıklama |
|---|---|---|---|
| `POST` | `/api/v1/notifications` | Yok | Bildirimi idempotent biçimde kaydeder |

Örnek direct request:

```http
POST /api/v1/notifications HTTP/1.1
Host: localhost:8081
Accept: application/json
Content-Type: application/json
X-Trace-Id: demo-notification-trace

{
  "eventId": "transaction-uuid",
  "type": "TRANSFER_RECEIVED",
  "sourceService": "enterprise-app",
  "recipientId": "receiver-user-id",
  "title": "Transfer received",
  "message": "You received a transfer of 25.0 TRY.",
  "referenceId": "transaction-uuid",
  "amount": 25.0,
  "currency": "TRY"
}
```

Yeni kayıt için HTTP `201 Created`, daha önce işlenmiş aynı `eventId` için HTTP `200 OK` döner.

## Notification App ve Servisler Arası İletişim

Enterprise App ile Notification App arasındaki iletişim Spring **HTTP Exchange** kullanılarak yapılır. Client sözleşmesi annotation tabanlıdır ve altyapıda `RestClient` ile çalışır.

```java
@HttpExchange("/api/v1/notifications")
public interface NotificationHttpExchangeClient {

    @PostExchange(contentType = MediaType.APPLICATION_JSON_VALUE, accept = MediaType.APPLICATION_JSON_VALUE)
    NotificationResponse createNotification(
            @RequestHeader("X-Trace-Id") String traceId,
            @RequestBody TransferNotificationRequest request);
}
```

### Bildirim İsteği

```json
{
  "eventId": "transaction-uuid",
  "type": "TRANSFER_RECEIVED",
  "sourceService": "enterprise-app",
  "recipientId": "receiver-user-id",
  "title": "Transfer received",
  "message": "You received a transfer of 25.0 TRY.",
  "referenceId": "transaction-uuid",
  "amount": 25.0,
  "currency": "TRY"
}
```

Alanların anlamı:

| Alan | Açıklama |
|---|---|
| `eventId` | İdempotency anahtarı; transfer kimliğiyle ilişkilidir |
| `type` | Bildirim türü |
| `sourceService` | Bildirimi üreten servis |
| `recipientId` | Bildirimin hedef kullanıcısı |
| `title` | Kısa bildirim başlığı |
| `message` | Kullanıcıya gösterilecek açıklama |
| `referenceId` | Kaynak işleme ait referans |
| `amount` | Transfer tutarı |
| `currency` | Üç karakterli para birimi |

Doğrulama kuralları:

- `eventId`, `sourceService`, `recipientId` ve `referenceId` boş olamaz.
- `type` zorunludur.
- `title` en fazla 255 karakterdir.
- `message` en fazla 1000 karakterdir.
- `amount` pozitif olmalıdır.
- `currency` tam olarak 3 karakter olmalıdır.

### Başarılı Cevap

```json
{
  "notificationId": "notification-uuid",
  "eventId": "transaction-uuid",
  "status": "RECORDED",
  "duplicate": false,
  "createdAt": "2026-06-09T12:00:00"
}
```

### İdempotency

Notification App, `eventId` alanını unique olarak saklar:

- İlk istek yeni bildirim oluşturur ve `duplicate: false` döner.
- Aynı `eventId` ile sonraki istekler mevcut kaydı döner ve `duplicate: true` işaretlenir.
- Eş zamanlı isteklerde oluşabilecek unique constraint yarışı ayrıca ele alınır.

Bu davranış, istemci tekrar denemelerinde aynı iş olayı için birden fazla bildirim kaydı oluşmasını engeller.

### Timeout ve Hata Davranışı

Enterprise App varsayılan olarak:

- Bağlantı kurulması için `2s`
- Cevap okunması için `3s`

timeout kullanır.

Notification App'ten dönen tip güvenli hata cevabı `NotificationDeliveryException` içine dönüştürülür. Bu exception HTTP status, hata kodu ve trace ID bilgisini taşır. Notification App'e erişilememesi veya geçersiz cevap alınması da ayrı hata kodlarıyla loglanır.

Mevcut iş kuralına göre notification gönderimi **best effort** çalışır: bildirim hatası transferin başarılı sonucunu geri almaz.

## Veri Modeli

### Enterprise App Veri Tabanı

Varsayılan bağlantı: `jdbc:h2:mem:enterprisedb`

`WalletEntity`, sahibine `@OneToOne`; `TransactionRecordEntity` ise gönderen ve alıcı kullanıcılarına `@ManyToOne` ilişkileriyle bağlıdır. Bir kullanıcı yalnızca bir wallet sahibi olabildiği için `wallets.user_id` unique tutulur. Aynı kullanıcı çok sayıda işlemin göndereni veya alıcısı olabildiği için transaction ilişkileri bire bir değil, çoktan bire şeklindedir.

İlişkiler `WalletEntity.java` ve `TransactionRecordEntity.java` dosyalarında tanımlıdır ve veritabanında isimlendirilmiş foreign key constraint'leri üretir. Cascade delete kullanılmaz; user ve wallet silme operasyonları ilgili kaydı soft delete ile pasifleştirir.

Tüm entity ilişkileri açıkça `FetchType.LAZY` kullanır. İlişkili veri yalnızca transaction/persistence sınırı içinde ihtiyaç duyulduğunda yüklenir ve iki uygulamada da `spring.jpa.open-in-view=false` olduğu için web response oluşturulurken kontrolsüz lazy query çalıştırılmaz. Projede entity subtype kalıtımı bulunmadığından `@Inheritance(SINGLE_TABLE)` ve `@DiscriminatorColumn` kullanılmaz. Ortak audit alanları yeni tablo veya discriminator üretmeyen `@MappedSuperclass` üzerinden paylaşılır.

Repository katmanında elle JPQL veya native SQL yazılmaz. Basit filtreler Spring Data derived query metotlarına, dinamik ve sayfalı işlem geçmişi filtresi ise `TransactionRecordSpecifications.java` içindeki JPA Criteria/Specification tanımına devredilir.

Enterprise entityleri `project-common` içindeki `AuditableEntity`, bağımsız Notification App entitysi ise kendi `AuditableEntity` sınıfını genişletir. Her persistence create/update işleminde aşağıdaki alanlar otomatik tutulur:

- `created_at`
- `updated_at`
- `created_trace_id`
- `updated_trace_id`

#### `users`

| Alan | Açıklama |
|---|---|
| `id` | Persistence tarafından üretilen UUID |
| `username` | Unique kullanıcı adı |
| `email` | Unique e-posta |
| `is_user_deleted` | Soft delete durumu; entity varsayılanı `false`, yalnızca `true` ise kullanıcı silinmiştir |
| `created_at` | Oluşturulma zamanı |
| `updated_at` | Son güncellenme zamanı |
| `created_trace_id` | Kaydı oluşturan isteğin trace ID'si |
| `updated_trace_id` | Son güncelleme isteğinin trace ID'si |
| `version` | Optimistic locking sürümü |

#### `wallets`

| Alan | Açıklama |
|---|---|
| `id` | Persistence tarafından üretilen UUID |
| `user_id` | `WalletEntity.user` üzerinden kurulan unique kullanıcı ilişkisi |
| `balance` | Cüzdan bakiyesi |
| `is_active` | Cüzdan aktiflik durumu; entity varsayılanı `true` |
| `created_at`, `updated_at` | Audit zamanları |
| `created_trace_id`, `updated_trace_id` | Audit trace ID'leri |
| `version` | Optimistic locking sürümü |

#### `transaction_records`

| Alan | Açıklama |
|---|---|
| `id` | Persistence tarafından üretilen UUID |
| `sender_user_id` | `TransactionRecordEntity.sender` üzerinden gönderen kullanıcı ilişkisi |
| `receiver_user_id` | `TransactionRecordEntity.receiver` üzerinden alıcı kullanıcı ilişkisi |
| `amount` | Transfer tutarı |
| `transaction_date` | İşlem zamanı |
| `status` | İşlem durumu |
| `created_at`, `updated_at` | Audit zamanları |
| `created_trace_id`, `updated_trace_id` | Audit trace ID'leri |

### Notification App Veri Tabanı

Varsayılan bağlantı: `jdbc:h2:mem:notificationdb`

#### `notifications`

| Alan | Açıklama |
|---|---|
| `id` | Persistence tarafından üretilen UUID |
| `event_id` | Unique ve değiştirilemez idempotency anahtarı |
| `type` | Bildirim türü |
| `source_service` | Kaynak servis |
| `recipient_id` | Hedef kullanıcı |
| `title` | Bildirim başlığı |
| `message` | Bildirim açıklaması |
| `reference_id` | Kaynak işlem referansı |
| `amount` | `BigDecimal`, precision 19 ve scale 4 |
| `currency` | Üç karakterli para birimi |
| `status` | Bildirim durumu |
| `created_at` | Oluşturulma zamanı |
| `updated_at` | Son güncellenme zamanı |
| `created_trace_id` | Kaydı oluşturan uçtan uca trace ID |
| `updated_trace_id` | Son güncellemenin uçtan uca trace ID'si |

## Güvenlik

Enterprise App stateless Spring Security kullanır. Oturum bilgisi server session içinde tutulmaz; her korumalı istekte `Authorization: Bearer <accessToken>` header'ı beklenir.

Güvenlik bileşenleri:

- `SecurityConfiguration`: endpoint bazlı public, `ROLE_USER` ve `ROLE_ADMIN` kurallarını tanımlar.
- `JwtAuthenticationFilter`: `OncePerRequestFilter` olarak access token'ı doğrular, blacklist kontrolü yapar ve `SecurityContext` oluşturur.
- `JwtTokenService`: access ve refresh token üretir/doğrular.
- `RefreshTokenStoreService`: refresh token session bilgisini tutar ve refresh token reuse durumunu engellemek için eski token'ı revoke eder.
- `TokenBlacklistService`: logout edilen access token ID'lerini expiry zamanına kadar blacklist'te tutar.
- `JsonAuthenticationEntryPoint` ve `JsonAccessDeniedHandler`: auth hatalarını JSON `GenericResponse` formatıyla döner.
- `CurrentUserProvider`: business erişim kontrolleri için kullanıcı kimliğini yalnızca `SecurityContext` içindeki principal'dan okur.

Endpoint rol ayrımı:

| Pattern | Kural |
|---|---|
| `POST /api/v1/auth/login` | Public |
| `POST /api/v1/auth/refresh` | Public |
| `POST /api/v1/users` | Public |
| `/api/v1/admin/**` | `ROLE_ADMIN` |
| `/api/v1/transactions/fraud-report` | `ROLE_ADMIN` |
| `/api/v1/users/**` | `ROLE_USER` |
| `/api/v1/transactions/**` | `ROLE_USER` |
| Diğer endpointler | Authenticated |

Bu yapı endpointleri gizlemez; erişim kararı merkezi security filter chain üzerinden verilir. Controller ve servis katmanında rol bilgisi dolaştırılmaz. Servis tarafında yalnızca iş kuralı niteliğindeki sahiplik kontrolleri kalır:

- Transfer göndereninin token kullanıcısıyla aynı olması
- Kullanıcının yalnızca kendi işlem geçmişine erişmesi
- Kullanıcının yalnızca kendi user kaydını veya kendi wallet'ını soft delete yapabilmesi

MDC yalnızca log korelasyonu için kullanılır; güvenlik bilgisinin kaynağı değildir. Legacy `X-User-Id` interceptor sınıfı kodda dursa bile web configuration içinde kayıt edilmez.

## Hata Yönetimi

Enterprise App ve Notification App kendi merkezi exception handler'larına sahiptir. Handlerlar ayrı `handler` paketlerinde tutulur ve controller sorumluluklarından ayrılır.

### Enterprise App

| Hata | HTTP Status |
|---|---|
| Yetkisiz kaynak erişimi | `403 Forbidden` |
| Kaynak bulunamadı | `404 Not Found` |
| Business exception | `400 Bad Request` |
| Validation hatası | `400 Bad Request` |
| Data integrity ihlali | `409 Conflict` |
| Beklenmeyen hata | `500 Internal Server Error` |

### Notification App

Örnek hata cevabı:

```json
{
  "timestamp": "2026-06-09T12:00:00",
  "status": 400,
  "errorCode": "NOTIFICATION_REQUEST_INVALID",
  "message": "Notification request validation failed.",
  "traceId": "trace-id",
  "path": "/api/v1/notifications",
  "validationErrors": {
    "recipientId": "Recipient ID cannot be blank"
  }
}
```

Notification App hata kodları:

| Hata Kodu | HTTP Status | Açıklama |
|---|---:|---|
| `NOTIFICATION_REQUEST_INVALID` | 400 | Request validation başarısız |
| `NOTIFICATION_REQUEST_UNREADABLE` | 400 | Request body okunamadı |
| `NOTIFICATION_PERSISTENCE_UNAVAILABLE` | 503 | Bildirim kalıcı olarak saklanamadı |
| `NOTIFICATION_UNEXPECTED_ERROR` | 500 | Beklenmeyen servis hatası |

Enterprise App notification adapter hata kodları:

| Hata Kodu | Açıklama |
|---|---|
| `NOTIFICATION_HTTP_ERROR` | Notification App bir HTTP hata cevabı döndürdü |
| `NOTIFICATION_SERVICE_UNAVAILABLE` | Notification App'e erişilemedi veya geçerli cevap alınamadı |

## Loglama ve İzlenebilirlik

Her iki uygulama da SLF4J kullanır. Trace filtreleri:

1. İstekte gelen `X-Trace-Id` değerini kullanır.
2. Header yoksa yeni trace ID üretir.
3. Trace ID'yi MDC içine ekler.
4. `request.received`, `request.completed` veya `request.failed` olaylarını aynı ID ve süre bilgisiyle loglar.
5. `UserCreatedEvent` gibi uygulama içi eventlerde aynı trace ID'yi payload ile taşır.
6. Notification HTTP Exchange isteğinde aynı ID'yi `X-Trace-Id` header'ıyla diğer uygulamaya aktarır.
7. Persistence audit alanlarına create/update işleminin trace ID'sini yazar.
8. Cevap header'ında trace ID'yi döner ve request tamamlandığında MDC'yi temizler.

Notification akışında başarı, duplicate kayıt, validation hatası, persistence hatası, HTTP hatası ve erişim hatası ayrı log olaylarıyla kapsanır. Başlıca olay aileleri:

- `notification.record.*`
- `notification.client.*`
- `notification.dispatch.*`

Loglarda mümkün olduğunca `eventId`, `referenceId`, `recipientId`, hata kodu, HTTP status ve trace ID gibi operasyonel olarak anlamlı alanlar bulunur.

Controller sınıfları log üretmez. İş girdileri, uygulanan kararlar ve sonuçlar use case/service katmanında; event kaynaklı işlemler listener katmanında; teknik iletişim ayrıntıları ise ilgili adapter ve exception handler katmanında loglanır.

## Test Stratejisi ve Coverage

Projede JUnit 5, Mockito ve AssertJ kullanılır. Testler aşağıdaki davranışları kapsar:

- Controller request/response sözleşmeleri
- Validation ve exception handler davranışları
- Use case başarı ve hata senaryoları
- Persistence tabanlı UUID üretimi ve entity ilişkileri
- Persistence ve event adapterları
- Güvenlik header ve rol kontrolleri
- Notification idempotency ve eş zamanlı kayıt yarışı
- HTTP Exchange başarı, tip güvenli hata ve bağlantı hataları
- Trace ID üretimi ve aktarımı

JaCoCo her modülde yüzde 100 line ve branch coverage eşiğini zorunlu tutar. Doğrulanan mevcut durumda toplam **185 test** başarıyla çalışmaktadır.

Enterprise App ve Notification App birbirinden bağımsız Maven projeleridir. Repository kökünde parent
veya aggregator POM bulunmaz; her uygulama kendi dizinindeki Maven Wrapper ve POM ile doğrulanır.

Testleri ve coverage doğrulamasını çalıştırmak için:

```powershell
cd project-app
.\mvnw.cmd clean verify

cd ..\project-notification
.\mvnw.cmd clean verify
```

JaCoCo raporları:

- `project-app/project-common/target/site/jacoco/index.html`
- `project-app/project-user/target/site/jacoco/index.html`
- `project-app/project-transaction/target/site/jacoco/index.html`
- `project-app/project-bootstrap/target/site/jacoco/index.html`
- `project-notification/target/site/jacoco/index.html`

## Projeyi Çalıştırma

### Gereksinimler

- Java 21
- Maven 3.9+

Önce Notification App'i çalıştırın:

```powershell
cd project-notification
mvn spring-boot:run
```

Ardından ayrı terminalde Enterprise App'i çalıştırın:

```powershell
cd project-app
mvn -pl project-bootstrap -am spring-boot:run
```

Uygulamalar:

- Enterprise App: `http://localhost:8080`
- Notification App: `http://localhost:8081`
- Enterprise H2 Console: `http://localhost:8080/h2-console`
- Notification H2 Console: `http://localhost:8081/h2-console`

Alternatif olarak paket oluşturup jar dosyaları çalıştırılabilir:

```powershell
cd project-app
mvn clean package
java -jar project-bootstrap\target\project-bootstrap-0.0.1-SNAPSHOT.jar
```

```powershell
cd project-notification
mvn clean package
java -jar target\notification-0.0.1-SNAPSHOT.jar
```

## Yapılandırma

Enterprise App varsayılanları:

| Ayar | Varsayılan |
|---|---|
| Server port | `8080` |
| Veri tabanı | `jdbc:h2:mem:enterprisedb` |
| Notification base URL | `http://localhost:8081` |
| Notification connect timeout | `2s` |
| Notification read timeout | `3s` |

Notification App varsayılanları:

| Ayar | Varsayılan |
|---|---|
| Server port | `8081` |
| Veri tabanı | `jdbc:h2:mem:notificationdb` |

Her iki uygulama da geliştirme kolaylığı için H2 console sunar. H2 bellek veri tabanı kullanıldığı için uygulamalar kapatıldığında veriler kaybolur.

## Geliştirme Rehberi

Yeni bir özellik eklerken önerilen sıra:

1. İş kuralını ve domain modelini ilgili domain modülünde tanımlayın.
2. Harici bağımlılık gerekiyorsa domain katmanında küçük ve amaca özel bir port oluşturun.
3. Tek iş akışından sorumlu use case/handler sınıfını ekleyin.
4. JPA, HTTP veya event entegrasyonunu infrastructure adapter olarak uygulayın.
5. Controller'ı yalnızca HTTP sözleşmesi ve use case çağrısıyla sınırlı tutun.
6. Başarı, validation, business error ve altyapı hata senaryoları için test ekleyin.
7. `mvn clean verify` ile tüm modülleri ve coverage eşiklerini doğrulayın.

Kod tabanında korunması beklenen prensipler:

- Her sınıfın tek ve açık bir sorumluluğu olmalıdır.
- Controller içinde iş kuralı, event yayınlama veya persistence kodu bulunmamalıdır.
- Domain katmanı framework ve altyapı detaylarına bağımlı olmamalıdır.
- UUID gibi entity yaşam döngüsü detayları entity içinde kalmalıdır.
- Harici servis iletişimi port ve adapter üzerinden yürütülmelidir.
- Loglar hatayı yalnızca metin olarak değil, bağlamıyla birlikte açıklamalıdır.
- Yeni hata durumları merkezi handler sözleşmesine dahil edilmelidir.

## Mevcut Sınırlar ve Production Yol Haritası

Bu proje mimari yaklaşımı ve entegrasyon desenlerini göstermek için hazırlanmıştır. Production kullanımı öncesinde aşağıdaki alanlar geliştirilmelidir.

### Finansal Hassasiyet

Enterprise App cüzdan ve transaction tutarlarında şu anda `Double` kullanır. Finansal hesaplarda kayan nokta yuvarlama sorunlarını önlemek için tüm parasal alanlar `BigDecimal` ve veri tabanında uygun `DECIMAL` tipine taşınmalıdır.

### Kimlik Doğrulama ve Yetkilendirme

Mevcut uygulamada Spring Security, access/refresh JWT, role based endpoint ayrımı, logout blacklist ve refresh token revoke mantığı vardır. Production ortamında ek olarak:

- JWT secret/configuration değerleri environment secret yönetimine taşınmalı
- In-memory blacklist ve refresh token store kalıcı veya dağıtık storage'a alınmalı
- Token rotation ve reuse detection denetimleri genişletilmeli
- OAuth2 Resource Server veya merkezi identity provider entegrasyonu değerlendirilmeli
- Role modeli permission seviyesine indirgenebilecek şekilde genişletilmeli
- Servisler arası mTLS veya service credential kullanılmalı

### Şifre Yönetimi

Kullanıcı oluşturma isteğinde raw password doğrulanır ve `PasswordHasherPort` üzerinden BCrypt hash olarak saklanır. Production ortamında ek olarak:

- Password policy ve breached-password kontrolü eklenmeli
- Credential modeli kullanıcı profilinden ayrı yönetilmeli
- Password reset ve account lockout akışları eklenmeli
- Düz metin password hiçbir zaman loglanmamalı veya saklanmamalıdır

### Veri Tabanı

H2 ve `ddl-auto: update` geliştirme ortamı için uygundur. Production ortamında:

- PostgreSQL gibi kalıcı veri tabanı
- Flyway veya Liquibase migration
- Veri tabanı constraint ve index incelemesi
- Backup ve recovery planı

kullanılmalıdır.

### Bildirim Güvenilirliği

Bildirim çağrısı şu anda senkron ve best effort çalışır. Harici HTTP çağrısı transfer transaction'ı tamamlanmadan önce yapılabildiği için production tasarımında önerilen yaklaşım:

1. Transfer ile birlikte outbox kaydı oluşturmak.
2. Outbox kaydını commit sonrasında yayınlamak.
3. Kafka veya RabbitMQ üzerinden Notification App'e iletmek.
4. Retry, dead-letter queue ve operasyonel alarm eklemek.

Bu yaklaşım transfer ile bildirim olayı arasındaki güvenilirliği artırır ve harici servis gecikmesinin veri tabanı transaction süresini uzatmasını engeller.

### Dayanıklılık

HTTP iletişimi için timeout mevcuttur. Production için ek olarak:

- Resilience4j retry
- Circuit breaker
- Bulkhead
- Rate limiting
- Ölçüm ve alarm eşikleri

değerlendirilmelidir.

### Gözlemlenebilirlik

Trace ID ve yapılandırılmış log alanları temel izlenebilirliği sağlar. İleri seviye kullanım için:

- OpenTelemetry distributed tracing
- Micrometer metrics
- Prometheus ve Grafana
- Merkezi log toplama
- Hata oranı ve gecikme alarmları

eklenebilir.

### Yapılandırılabilir İş Kuralları

Yeni cüzdanın başlangıç bakiyesi şu anda sabit bir değerdir. Production ortamında bu ve benzeri iş kuralları doğrulanmış, versiyonlanabilir yapılandırmalar üzerinden yönetilmelidir.

## Önemli Kod Noktaları

- [Enterprise App başlangıç ve konfigürasyon modülü](project-app/project-bootstrap)
- [Ortak hata ve trace bileşenleri](project-app/project-common)
- [Kullanıcı domaini ve use case'leri](project-app/project-user)
- [Transaction domaini ve notification adapterı](project-app/project-transaction)
- [Bağımsız Notification App](project-notification)
