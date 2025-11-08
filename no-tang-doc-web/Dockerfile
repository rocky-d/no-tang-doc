# ---------- Build stage ----------
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# ---------- Runtime stage ----------
FROM nginx:alpine
COPY deploy/nginx.conf /etc/nginx/conf.d/default.conf
# 这里改 dist -> build
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
