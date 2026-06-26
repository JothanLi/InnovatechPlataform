FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

ARG VITE_GATEWAY_API_URL=http://localhost:8090/api/v1
ENV VITE_GATEWAY_API_URL=${VITE_GATEWAY_API_URL}

RUN npm run build


FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]