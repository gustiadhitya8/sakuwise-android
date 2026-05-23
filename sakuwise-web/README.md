# sakuwise-web

Landing page + privacy policy untuk aplikasi [Sakuwise](https://play.google.com/store/apps/details?id=com.gustiadhitya.sakuwise) (Android).

🌐 Live: https://gustiadhitya.github.io/sakuwise-web/

## Struktur

```
sakuwise-web/
├── index.html                # Landing page
├── privacy/
│   └── index.html            # Privacy policy (Indonesia + English, language toggle)
├── .nojekyll                 # Skip Jekyll processing (we use plain HTML)
└── README.md
```

## URLs

| Halaman | URL |
|---|---|
| Landing | https://gustiadhitya.github.io/sakuwise-web/ |
| Kebijakan Privasi (ID) | https://gustiadhitya.github.io/sakuwise-web/privacy/ |
| Privacy Policy (EN) | https://gustiadhitya.github.io/sakuwise-web/privacy/#en |

## Update Privacy Policy

Bila perlu update:

1. Edit `privacy/index.html` (mengandung ID + EN dalam satu file)
2. Update tanggal "Berlaku sejak" / "Effective" di kedua bahasa
3. Update versi
4. Commit + push
5. GitHub Pages akan auto-deploy dalam ~1-2 menit

## License

© 2026 Gusti Adhitya. All rights reserved.

Privacy policy content licensed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/) untuk penggunaan ulang sebagai template oleh developer lain.
