📱 Nexoft Case Study: Contacts App
Merhaba! Bu proje, modern Android geliştirme pratikleri kullanılarak hazırlanmış, hem yerel veritabanı (Room) hem de uzak sunucu (Swagger API) ile tam senkronize çalışan gelişmiş bir rehber yönetim uygulamasıdır.
Bu uygulamayı geliştirirken sadece "çalışmasına" değil, kodun okunabilir, sürdürülebilir ve esnek olmasına (SOLID, DRY, KISS) odaklandım.
✨ Öne Çıkan Özellikler
•
Tam Senkronizasyon: Swagger API üzerinden tüm kişileri getirir, ekler, günceller ve siler. Yapılan her işlem anında hem sunucuda hem de yerel veritabanında güncellenir.
•
Gelişmiş Görsel Deneyimi:
◦
Dinamik Glow Etkisi: Palette API kullanarak profil fotoğraflarındaki baskın rengi analiz eder ve fotoğrafın etrafına o renkte şık bir gölge (glow) efekti ekler.
◦
Lottie Animasyonları: Başarılı işlemlerden sonra kullanıcıyı karşılayan akıcı animasyonlar.
◦
Coil Entegrasyonu: Uzak sunucudaki resimleri asenkron ve performanslı şekilde yükler.
•
Modern Kullanıcı Etkileşimi:
◦
Swipe Actions: Liste üzerinde sağdan sola kaydırarak hızlıca "Düzenle" veya "Sil" seçeneklerine ulaşım.
◦
Akıllı Arama: Yazmaya başladığınız anda çalışan filtreleme, geçmiş aramaları hatırlama ve "Sonuç Bulunamadı" durumları.
•
Cihaz ile Entegrasyon: Uygulama içindeki bir kişiyi, gerekli izinleri yöneterek doğrudan telefonunuzun kendi rehberine kaydetme yeteneği.
🛠️ Teknik Yığın (Tech Stack)
Uygulamanın mimarisi, Android'in en güncel kütüphaneleri üzerine inşa edilmiştir:
•
Dil: Kotlin
•
UI: Jetpack Compose (Modern ve deklaratif arayüz)
•
Mimari: Clean Architecture prensipleriyle desteklenmiş UI Katmanı.
•
Yerel Veritabanı: Room Database (Offline-first yaklaşımı için).
•
Networking: Retrofit + OkHttp + GSON (API iletişimi ve hata yönetimi).
•
Görüntü İşleme: Coil (Resim yükleme) & Palette API (Renk analizi).
•
Animasyon: Lottie Compose.
•
Diğer: Compose SwipeBox (Liste etkileşimleri).

🚀 Kurulum ve Çalıştırma
1.
Projeyi klonlayın:
Shell Script
git clone https://github.com/kullaniciadi/CaseStudy-Contacts.git
2.
Android Studio (Ladybug veya üstü önerilir) ile projeyi açın.
3.
Gradle Sync işleminin tamamlanmasını bekleyin.
4.
Cihazınızda veya emülatörde çalıştırın.
◦
Not: API bağlantısı için cihazın internete erişimi olduğundan emin olun.
📖 Uygulama Akışı
1.
Başlangıç: Uygulama açıldığında sunucudaki verilerle yerel veritabanı senkronize edilir.
2.
Arama: Arama çubuğuna odaklandığınızda geçmiş aramalarınız listelenir.
3.
Ekleme/Düzenleme: + butonuna basıldığında veya bir kişiye tıklandığında alttan açılan bir panel (BottomSheet) üzerinden işlemler yapılır.
4.
Resim Yükleme: Kamera veya galeriden seçilen resim önce sunucuya yüklenir, dönen URL ile kişi kaydedilir.
5.
Rehbere Kaydet: Kişi detay sayfasında "Telefon Rehberine Kaydet" seçeneği ile cihaz izinleri alınarak işlem tamamlanır.
