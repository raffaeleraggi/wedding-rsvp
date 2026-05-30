FROM node:22-alpine AS frontend-build

WORKDIR /frontend

COPY frontend/package.json frontend/package-lock.json* ./

RUN npm install

COPY frontend/src ./src
COPY frontend/public ./public
COPY frontend/index.html ./
COPY frontend/vite.config.js* ./

RUN chmod +x ./node_modules/.bin/vite
RUN npm run build