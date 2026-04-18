# Daily Task Manager

Aplikasi To-Do List personal berbasis Kotlin + Jetpack Compose.

## Fitur
- Tambah tugas dengan deadline tanggal & waktu (dropdown).
- Tandai tugas selesai (checkbox).
- Hapus tugas.
- Sorting berdasarkan deadline/status.
- UI Material Design.
- State tersimpan saat rotasi layar (Android).

## Tech Stack
- Kotlin
- Jetpack Compose (Android)
- Compose Desktop (JVM)
- Gradle

## Struktur Project
- `androidApp/` : aplikasi Android (emulator/perangkat)
- `app/` : aplikasi desktop (Compose Desktop)

## Cara Menjalankan

### Android
```bash
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug
```

APK output:
- `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

### Desktop
```bash
./gradlew :app:run
```

## TODO Sebelum Publish / Pengumpulan
- [ ] Isi nama anggota tim di bagian "Anggota".
- [ ] Tambahkan screenshot aplikasi di bagian "Screenshot".
- [ ] Isi link video demo.
- [ ] Isi link blog anggota 1.
- [ ] Isi link file presentasi PDF.
- [ ] Isi link download APK online (Drive/Release).
- [ ] Isi link repository publik (kalau README ini dipakai di template awal).

## Anggota
- [ ] Anggota 1 - `<NAMA> (<NIM>)`
- [ ] Anggota 2 - `<NAMA> (<NIM>)`
- [ ] Anggota 3 - `<NAMA> (<NIM>)`

## Screenshot
- [ ] Thumbnail/infografis: `<TAMBAHKAN_GAMBAR_DI_REPO>`
- [ ] UI aplikasi:
  - `<TAMBAHKAN_SCREENSHOT_1>`
  - `<TAMBAHKAN_SCREENSHOT_2>`

Contoh markdown gambar:
```md
![Thumbnail](assets/thumbnail-daily-task-manager.png)
![Home Screen](assets/screenshot-home.png)
```

## Link Penting
- Video presentasi: `<ISI_LINK_VIDEO_PRESENTASI>`
- Blog anggota 1: `<ISI_LINK_BLOG_ANGGOTA_1>`
- Slide/PDF presentasi: `<ISI_LINK_PDF_PRESENTASI>`
- Download APK: `<ISI_LINK_DOWNLOAD_APK>`
- Repository: `<ISI_LINK_REPOSITORY>`

## Catatan
- Jika build gagal karena cache Gradle lokal, pastikan `.gradle-user` tidak ikut di-commit.
- Untuk Android Studio, jalankan module `androidApp` (bukan `app`) jika target emulator.
