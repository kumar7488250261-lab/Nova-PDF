#!/bin/bash
set -e

BASE="app/src/main/res"

cat << 'EOF' > /tmp/pdfnova_square.svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="512" height="512">
  <defs>
    <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#2563EB"/>
      <stop offset="40%" stop-color="#3B82F6"/>
      <stop offset="75%" stop-color="#4F46E5"/>
      <stop offset="100%" stop-color="#1E1B4B"/>
    </linearGradient>
    <linearGradient id="n_left" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="#0072FF"/>
      <stop offset="40%" stop-color="#0284C7"/>
      <stop offset="100%" stop-color="#2563EB"/>
    </linearGradient>
    <linearGradient id="n_diag" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#0062FF"/>
      <stop offset="45%" stop-color="#2563EB"/>
      <stop offset="85%" stop-color="#6366F1"/>
      <stop offset="100%" stop-color="#7C3AED"/>
    </linearGradient>
    <linearGradient id="n_right" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="#7C3AED"/>
      <stop offset="60%" stop-color="#6366F1"/>
      <stop offset="100%" stop-color="#4F46E5"/>
    </linearGradient>
    <linearGradient id="fold" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#E0E7FF"/>
      <stop offset="100%" stop-color="#C7D2FE"/>
    </linearGradient>
    <filter id="shadow" x="-10%" y="-10%" width="120%" height="120%">
      <feDropShadow dx="0" dy="16" stdDeviation="20" flood-color="#0F172A" flood-opacity="0.35"/>
    </filter>
  </defs>

  <rect x="0" y="0" width="512" height="512" rx="115" fill="url(#bg)"/>

  <g filter="url(#shadow)">
    <path d="M 145 110 L 290 110 L 375 195 L 375 400 C 375 418 360 432 342 432 L 145 432 C 127 432 112 418 112 400 L 112 142 C 112 124 127 110 145 110 Z" fill="#FFFFFF"/>
    <path d="M 290 110 L 375 195 L 305 195 C 296 195 290 188 290 180 Z" fill="url(#fold)"/>
  </g>

  <path d="M 160 215 C 160 205 168 198 178 198 L 210 198 L 210 370 L 178 370 C 168 370 160 362 160 352 Z" fill="url(#n_left)"/>
  <path d="M 165 198 L 212 198 L 322 370 L 275 370 Z" fill="url(#n_diag)"/>
  <path d="M 272 198 L 305 198 C 315 198 324 206 324 216 L 324 352 C 324 362 315 370 305 370 L 272 370 Z" fill="url(#n_right)"/>
</svg>
EOF

cat << 'EOF' > /tmp/pdfnova_circle.svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="512" height="512">
  <defs>
    <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#2563EB"/>
      <stop offset="40%" stop-color="#3B82F6"/>
      <stop offset="75%" stop-color="#4F46E5"/>
      <stop offset="100%" stop-color="#1E1B4B"/>
    </linearGradient>
    <linearGradient id="n_left" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="#0072FF"/>
      <stop offset="40%" stop-color="#0284C7"/>
      <stop offset="100%" stop-color="#2563EB"/>
    </linearGradient>
    <linearGradient id="n_diag" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#0062FF"/>
      <stop offset="45%" stop-color="#2563EB"/>
      <stop offset="85%" stop-color="#6366F1"/>
      <stop offset="100%" stop-color="#7C3AED"/>
    </linearGradient>
    <linearGradient id="n_right" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="#7C3AED"/>
      <stop offset="60%" stop-color="#6366F1"/>
      <stop offset="100%" stop-color="#4F46E5"/>
    </linearGradient>
    <linearGradient id="fold" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#E0E7FF"/>
      <stop offset="100%" stop-color="#C7D2FE"/>
    </linearGradient>
    <filter id="shadow" x="-10%" y="-10%" width="120%" height="120%">
      <feDropShadow dx="0" dy="16" stdDeviation="20" flood-color="#0F172A" flood-opacity="0.35"/>
    </filter>
  </defs>

  <circle cx="256" cy="256" r="256" fill="url(#bg)"/>

  <g filter="url(#shadow)">
    <path d="M 145 110 L 290 110 L 375 195 L 375 400 C 375 418 360 432 342 432 L 145 432 C 127 432 112 418 112 400 L 112 142 C 112 124 127 110 145 110 Z" fill="#FFFFFF"/>
    <path d="M 290 110 L 375 195 L 305 195 C 296 195 290 188 290 180 Z" fill="url(#fold)"/>
  </g>

  <path d="M 160 215 C 160 205 168 198 178 198 L 210 198 L 210 370 L 178 370 C 168 370 160 362 160 352 Z" fill="url(#n_left)"/>
  <path d="M 165 198 L 212 198 L 322 370 L 275 370 Z" fill="url(#n_diag)"/>
  <path d="M 272 198 L 305 198 C 315 198 324 206 324 216 L 324 352 C 324 362 315 370 305 370 L 272 370 Z" fill="url(#n_right)"/>
</svg>
EOF

for spec in mdpi:48 hdpi:72 xhdpi:96 xxhdpi:144 xxxhdpi:192; do
  dir="$BASE/mipmap-${spec%:*}"
  size="${spec#*:}"
  echo "Generating $dir at ${size}x${size}..."
  rm -f "$dir/ic_launcher.webp" "$dir/ic_launcher_round.webp"
  convert -background none /tmp/pdfnova_square.svg -resize "${size}x${size}!" "PNG32:$dir/ic_launcher.png"
  convert -background none /tmp/pdfnova_circle.svg -resize "${size}x${size}!" "PNG32:$dir/ic_launcher_round.png"
done

echo "Raster icons generated successfully."
