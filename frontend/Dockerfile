# Author: yuqi.guo17@gmail.com

# Stage 1: build the React app
FROM node:18-alpine AS build
LABEL maintainer="yuqi.guo17@gmail.com"

# Set working directory
WORKDIR /app

# Copy dependency definitions
COPY package.json package-lock.json ./

# Install dependencies
RUN npm ci --silent

# Copy the rest of the source code
COPY . .

# Build the app for production
RUN npm run build

# Stage 2: serve the built app with Nginx
FROM nginx:stable-alpine
LABEL maintainer="yuqi.guo17@gmail.com"

# Copy static assets from build stage
COPY --from=build /app/build /usr/share/nginx/html

# Expose port 80 to the outside world
EXPOSE 80

# Start Nginx in the foreground
CMD ["nginx", "-g", "daemon off;"]
