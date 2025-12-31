📱 Nexoft Case Study: Contacts App

Merhabalarr! Bu proje, modern Android geliştirme pratikleri kullanılarak hazırlanmış, hem yerel veritabanı (Room) hem de uzak sunucu (Swagger API) ile tam senkronize çalışan gelişmiş bir klasik telefon rehberi uygulamasıdır.

Bu uygulamayı geliştirirken kodun okunabilir, sürdürülebilir ve esnek olmasına da odaklandım.

Öne Çıkan Özellikler

Tam Senkronizasyon (Offline-First)

API Entegrasyonu: Swagger API üzerinden tüm kişileri getirir, ekler, günceller ve siler.

Local Persistence: Yapılan her işlem anında hem sunucuda hem de yerel veritabanında (Room) güncellenir. İnternet olmasa dahi veriler görüntülenebilir.

Gelişmiş Görsel Deneyimi

Dinamik Glow Etkisi: Palette API kullanarak profil fotoğraflarındaki baskın rengi analiz eder ve fotoğrafın etrafına o renkte şık bir gölge (glow) efekti ekler.

Lottie Animasyonları: Başarılı işlemlerden sonra kullanıcıyı karşılayan akıcı ve etkileşimli animasyonlar.

Performanslı Resim Yükleme: Coil entegrasyonu ile uzak sunucudaki resimler asenkron ve bellek optimizasyonlu şekilde yüklenir.

Modern Kullanıcı Etkileşimi

Swipe Actions: Liste üzerinde sağdan sola kaydırarak hızlıca "Düzenle" veya "Sil" seçeneklerine erişim.

Akıllı Arama: Yazmaya başladığınız anda çalışan filtreleme, geçmiş aramaları hatırlama ve "Sonuç Bulunamadı" durum yönetimi.

Cihaz Entegrasyonu: Uygulama içindeki bir kişiyi, çalışma zamanı izinlerini (Runtime Permissions) yöneterek doğrudan telefonunuzun kendi rehberine kaydetme yeteneği.


Uygulama Akışı


Başlangıç: Uygulama açıldığında sunucudaki verilerle yerel veritabanı senkronize edilir.

Arama: Arama çubuğuna odaklandığınızda geçmiş aramalarınız listelenir ve gerçek zamanlı filtreleme yapılır.

Ekleme/Düzenleme: + butonuna basıldığında veya bir kişiye tıklandığında alttan açılan bir panel (BottomSheet) üzerinden işlemler yönetilir.

Resim Yükleme: Kamera veya galeriden seçilen resim önce sunucuya yüklenir, dönen URL ile kişi kaydedilir.

Rehbere Kaydet: Kişi detay sayfasında "Telefon Rehberine Kaydet" seçeneği ile sistem izinleri alınarak işlem tamamlanır.


Geliştirici: [Efe Ateş]
LinkedIn: linkedin.com/in/efeates
