# === STAGE 1: Build (Construirea aplicației React) ===

# Folosim o imagine Node.js (alege versiunea 18 sau 20)
FROM node:20-alpine AS build

# Setăm directorul de lucru
WORKDIR /app

# Copiem package.json și package-lock.json
COPY package*.json ./

# Instalăm dependențele
RUN npm install

# Copiem restul codului sursă
COPY . .

# Rulăm comanda de build (care creează folderul 'dist' sau 'build')
RUN npm run build

# === STAGE 2: Serve (Servirea fișierelor statice cu Nginx) ===

# Folosim o imagine Nginx stabilă și foarte mică
FROM nginx:stable-alpine

# Copiem fișierele compilate din stagiul 'build'
# NOTĂ: Dacă 'npm run build' creează un folder 'build' (în loc de 'dist'),
# schimbă linia de mai jos din /app/dist în /app/build
COPY --from=build /app/build /usr/share/nginx/html
# Expunem portul 80, pe care Nginx ascultă implicit
EXPOSE 80

# Nginx pornește automat.