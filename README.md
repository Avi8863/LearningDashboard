# Learning Dashboard

A modern native Android Learning Dashboard application built with **Kotlin**, **Jetpack Compose**, **Coroutines / StateFlow**, and **Room Database**.

---

## Demo Credentials for Testing
- **Email:** `test@example.com`
- **Password:** `password123`

---

## Architecture & Design Decisions

### 1. Why this Architecture?
The project strictly follows **Clean Architecture** combined with the **MVVM (Model-View-ViewModel)** pattern:

- **Presentation Layer (`presentation`)**: Built with Jetpack Compose and ViewModels. ViewModels expose unidirectional `StateFlow` (`LoginUiState`, `CourseUiState`) to ensure reactive, lifecycle-aware UI state updates without memory leaks.
- **Domain Layer (`domain`)**: Holds pure Kotlin domain models (`Course`, `Lesson`) and repository interfaces (`CourseRepository`), keeping business logic decoupled from frameworks.
- **Data Layer (`data`)**: Contains `CourseRepositoryImpl`, Room DAO (`CourseDao`), Room Database (`AppDatabase`), and `MockCourseApi`. Encapsulates data fetching, local persistence, progress calculations, and cache merging.

**Why MVVM + Clean Architecture?**
- **Testability**: Pure domain logic and ViewModels can be thoroughly unit-tested using fast JVM test dispatchers without Android framework dependencies.
- **Maintainability & Separation of Concerns**: Switching API clients (e.g. Retrofit/Ktor) or local persistence (Room) requires zero changes to the UI or ViewModel.

---

## Offline Support

### 2. How Offline Data is Stored & Loaded
- **Local Cache**: Courses and lesson progress are persisted locally using **Room Database** (`CourseEntity`).
- **Offline Fallback**: When fetching courses (`getCourses()`), if network connectivity fails or an exception occurs, the repository seamlessly returns previously cached courses from Room.
- **State Preservation on Sync**: When fresh data is retrieved from the remote API, the repository merges remote data with locally completed lesson states so user progress is never lost.
- **Instant Updates**: Marking a lesson as completed immediately updates local Room storage and recalculates overall course progress, maintaining responsiveness offline.

---

## Security

### 3. Storing Authentication Tokens in Production
In a production application, access tokens and refresh tokens should **never** be stored in plain `SharedPreferences` or hardcoded in source code:
1. **EncryptedSharedPreferences / Encrypted DataStore**: Use `EncryptedSharedPreferences` (backed by **Android Keystore System**) to encrypt sensitive tokens at rest using AES-256 GCM encryption.
2. **Biometric / Master Key Invalidation**: Bind encryption keys to hardware security modules (HSM) or TEE (Trusted Execution Environment).
3. **Token Lifecycle**: Automatically invalidate and clear stored tokens on 401 Unauthorized, session expiration, or logout. Use OkHttp `Authenticator` for seamless refresh token rotations.
4. **Network Security**: Enforce HTTPS and **SSL Pinning** to prevent Man-In-The-Middle (MITM) attacks.

---

## Scale (1M+ Users & Hundreds of Courses)

### 4. System & App Improvements at Scale
1. **Pagination & Jetpack Paging 3**: Replace full course list loading with incremental cursor-based API pagination and `PagingDataAdapter` / `LazyColumn` paging to optimize memory and network payload.
2. **Normalized Database Schema**: Separate static course metadata (`CourseEntity`) from dynamic user progress (`UserProgressEntity` with `userId`, `courseId`, `lessonId`, `completedAt`).
3. **Background Sync with WorkManager**: Use `WorkManager` with network constraints to synchronize offline lesson completions with the backend reliably in the background.
4. **Server-side Caching & CDN**: Distribute course content via CloudFront/Cloudflare CDN and cache course lists in Redis to offload main database instances.
5. **Real-time Synchronization / WebSockets**: Push instant progress updates across devices using WebSockets or Server-Sent Events (SSE).

---

## Second Platform (iOS / macOS Implementation)

### 5. Implementing on iOS / macOS
If building this application natively for iOS/macOS using **Swift**:
- **UI Framework**: **SwiftUI** using `NavigationStack`, `@State`, and `List` views.
- **Architecture**: **MVVM with Clean Architecture**, leveraging Swift's `@Observable` macro (iOS 17+) or `ObservableObject` with `Published` properties.
- **Concurrency**: Swift Async/Await (`async/await`, `Task`, `AsyncStream`).
- **Networking**: `URLSession` wrapped in an API Client protocol.
- **Local Storage**: **SwiftData** or **CoreData** for offline course entity persistence.
- **Security**: **Keychain Services** (`SecItem`) for hardware-backed secure authentication token storage.

---

## Testing & Verification

14 Unit Tests written and verified:
- `CourseViewModelTest`: Verifies UI state transitions (`Loading`, `Success`, `Empty`, `Error`) and lesson completion progress recalculation.
- `LoginViewModelTest`: Verifies email/password validation rules, format checking, and authentication success/failure states.
- `CourseRepositoryImplTest`: Verifies online API fetching, Room caching, offline fallback execution, and entity persistence.

All tests run via: `./gradlew test`
