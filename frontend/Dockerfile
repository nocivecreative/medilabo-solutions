# --- Build stage : compile la SPA Angular en bundle statique ---
FROM node:24-alpine AS build
WORKDIR /app

# Copie package*.json seuls d'abord : la couche npm ci reste en cache tant que
# les dépendances ne changent pas (build incrémental plus rapide / sobre).
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
# Build de production (tree-shaking, minification, hashing) — configuration par défaut.
RUN npm run build

# --- Runtime stage : nginx sert le bundle + proxifie l'API ---
FROM nginx:alpine
# Bundle produit par le builder @angular/build:application : dist/<projet>/browser.
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
