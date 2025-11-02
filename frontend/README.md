# Nexus Feed Frontend

A modern Reddit-like community news platform built with React, TypeScript, and Tailwind CSS.

## Tech Stack

- **React 19** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool
- **React Router** - Routing
- **TanStack Query** - Server state management
- **Zustand** - Client state management
- **Tailwind CSS v4** - Styling
- **Radix UI** - Accessible components
- **React Hook Form** - Form handling
- **Zod** - Schema validation
- **Axios** - HTTP client

## Project Structure

```
src/
├── components/
│   ├── layout/          # Layout components (Navbar, Layout)
│   ├── posts/           # Post-related components (PostCard, PostList)
│   └── ui/              # Reusable UI components (shadcn/ui)
├── hooks/               # Custom React hooks
│   ├── useAuth.ts       # Authentication hook
│   └── usePosts.ts      # Posts data fetching hook
├── lib/                 # Utility libraries
│   ├── api.ts           # Axios instance with interceptors
│   ├── api-client.ts    # API client functions
│   └── utils.ts         # Helper functions
├── pages/               # Page components
│   ├── Home.tsx         # Home feed page
│   ├── Login.tsx        # Login page
│   └── Register.tsx     # Registration page
├── stores/              # Zustand stores
│   └── authStore.ts     # Authentication state
├── types/               # TypeScript type definitions
│   └── index.ts         # Shared types
├── App.tsx              # Main app component with routing
├── main.tsx             # App entry point
└── index.css            # Global styles

```

## Features

### Implemented
- ✅ User authentication (login/register)
- ✅ JWT token management
- ✅ Home feed with posts
- ✅ Post voting (upvote/downvote)
- ✅ Responsive design
- ✅ Dark mode support
- ✅ Form validation
- ✅ Error handling
- ✅ Loading states
- ✅ Toast notifications

### API Integration
The frontend integrates with the following backend endpoints:

**Auth:**
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

**Posts:**
- `GET /api/posts` - Get all posts
- `GET /api/posts/:id` - Get single post
- `POST /api/posts` - Create post
- `DELETE /api/posts/:id` - Delete post
- `POST /api/posts/:id/vote` - Vote on post

**Comments:**
- `GET /api/posts/:postId/comments` - Get post comments
- `POST /api/comments` - Create comment
- `DELETE /api/comments/:id` - Delete comment
- `POST /api/comments/:id/vote` - Vote on comment

## Getting Started

1. Install dependencies:
```bash
pnpm install
```

2. Set up environment variables:
```bash
cp .env.example .env
```

3. Start the development server:
```bash
pnpm dev
```

The app will be available at `http://localhost:5173`

## Environment Variables

```env
VITE_API_URL=http://localhost:10008
```

## Development

### Adding New Features

1. **Create types** in `src/types/index.ts`
2. **Add API functions** in `src/lib/api-client.ts`
3. **Create custom hooks** in `src/hooks/`
4. **Build components** in `src/components/`
5. **Add pages** in `src/pages/`
6. **Update routing** in `src/App.tsx`

### Code Style

- Use functional components with hooks
- Prefer named exports over default exports (except for pages)
- Use TypeScript for type safety
- Follow the existing folder structure
- Use Tailwind CSS for styling
- Keep components small and focused

## Build

```bash
pnpm build
```

## Preview Production Build

```bash
pnpm preview
```
