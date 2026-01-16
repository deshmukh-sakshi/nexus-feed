# Nexus Feed

A modern social media platform built with Spring Boot and React, featuring user-generated content, voting systems, and community interaction capabilities.

## 🚀 Features

- **Posts & Content**: Create posts with titles, text, images, and tags
- **Voting System**: Upvote/downvote posts and comments with karma tracking
- **Comments**: Threaded commenting system
- **User Authentication**: Traditional registration and Google OAuth support
- **Tagging System**: Categorize and discover content through tags
- **User Profiles**: Badges, karma scores, and achievement system
- **Admin Panel**: Content moderation and user management tools
- **Reporting System**: Community-driven content moderation
- **Email Notifications**: Automated user communications
- **Trending Content**: Dynamic sidebar with trending tags and top users

## 🛠️ Tech Stack

### Backend
- **Spring Boot 3.5.6** with Java 17
- **PostgreSQL** (production) / **H2** (testing)
- **Spring Security** with JWT authentication
- **Spring Data JPA** with Hibernate
- **Maven** build system
- **jqwik** for property-based testing

### Frontend
- **React 19** with TypeScript
- **Vite** build tool (Rolldown)
- **Tailwind CSS 4.x** for styling
- **Zustand** for state management
- **TanStack Query** for server state
- **Radix UI** component primitives
- **pnpm** package manager

## 📁 Project Structure

```
nexus-feed/
├── backend/          # Spring Boot API server
│   ├── src/main/java/com/nexus/feed/backend/
│   │   ├── Auth/     # Authentication & authorization
│   │   ├── Admin/    # Admin functionality
│   │   ├── Controller/ # REST endpoints
│   │   ├── Service/  # Business logic
│   │   ├── Repository/ # Data access
│   │   └── Entity/   # JPA entities
│   └── pom.xml
├── frontend/         # React TypeScript client
│   ├── src/
│   │   ├── components/ # UI components
│   │   ├── pages/    # Route components
│   │   ├── hooks/    # Custom React hooks
│   │   ├── stores/   # State management
│   │   └── types/    # TypeScript definitions
│   └── package.json
└── .kiro/           # Kiro configuration and specs
```

## 🚦 Getting Started

### Prerequisites

- **Java 17** or higher
- **Node.js 18** or higher
- **pnpm** package manager
- **PostgreSQL** database (for production)

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Set up environment variables:
   ```bash
   # Database configuration
   export DB_URL=jdbc:postgresql://localhost:5432/nexus_feed
   export DB_USERNAME=your_username
   export DB_PASSWORD=your_password
   
   # JWT configuration
   export JWT_SECRET=your_jwt_secret_key
   
   # Google OAuth (optional)
   export GOOGLE_CLIENT_ID=your_google_client_id
   
   # Email configuration (optional)
   export MAIL_USERNAME=your_email@gmail.com
   export MAIL_PASSWORD=your_app_password
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The backend will start on `http://localhost:10008`

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   pnpm install
   ```

3. Create environment file:
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

4. Start the development server:
   ```bash
   pnpm dev
   ```

The frontend will start on `http://localhost:5173`

## 🧪 Testing

### Backend Tests
```bash
cd backend
./mvnw test                    # Run all tests
./mvnw test -Dtest=ClassName   # Run specific test class
```

### Frontend Tests
```bash
cd frontend
pnpm type-check               # TypeScript type checking
pnpm lint                     # ESLint checking
```

## 🏗️ Building for Production

### Backend
```bash
cd backend
./mvnw clean package
```

### Frontend
```bash
cd frontend
pnpm build
pnpm preview                  # Preview production build
```

## 🎨 Design Philosophy

Nexus Feed features a distinctive "brutalist" design aesthetic with:
- Bold black borders and shadows
- Bright, vibrant colors
- Sharp, geometric shapes
- Retro-modern UI elements

## 🔧 Development

### Code Style
- **Backend**: Lombok annotations, camelCase methods, PascalCase classes
- **Frontend**: PascalCase components, camelCase variables, Tailwind utilities
- **Database**: snake_case naming convention

### Architecture Patterns
- **Backend**: Layered architecture (Controller → Service → Repository)
- **Frontend**: Component composition with custom hooks
- **State**: Zustand for global state, React Query for server state

## 📝 API Documentation

The backend provides RESTful APIs for:
- Authentication (`/api/auth/*`)
- Posts (`/api/posts/*`)
- Comments (`/api/comments/*`)
- Users (`/api/users/*`)
- Admin (`/api/admin/*`)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

If you encounter any issues or have questions:
1. Check existing [Issues](../../issues)
2. Create a new issue with detailed information
3. Include steps to reproduce any bugs

---

Built with ❤️ using Spring Boot and React