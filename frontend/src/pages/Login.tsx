import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useLocation } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Separator } from '@/components/ui/separator'
import { GoogleSignInButton } from '@/components/ui/google-sign-in-button'
import { UsernamePromptModal } from '@/components/ui/username-prompt-modal'
import { useAuth } from '@/hooks/useAuth'

const loginSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(1, 'Password is required'),
})

type LoginFormData = z.infer<typeof loginSchema>

export const Login = () => {
  const location = useLocation()
  const from = (location.state as { from?: string })?.from || '/'
  const {
    login,
    isLoading,
    googleLogin,
    completeGoogleRegistration,
    cancelGoogleRegistration,
    clearGoogleError,
    isGoogleLoading,
    pendingGoogleUser,
    googleRegistrationError,
  } = useAuth(from)

  const form = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  })

  const onSubmit = (data: LoginFormData) => {
    login(data)
  }

  return (
    <div className="flex items-center justify-center w-full min-h-[calc(100vh-8rem)]">
      <Card className="w-full max-w-md mx-4 bg-yellow-50">
        <CardHeader>
          <CardTitle>Welcome back</CardTitle>
          <CardDescription>Log in to your Nexus Feed account</CardDescription>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="email"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Email</FormLabel>
                    <FormControl>
                      <Input placeholder="you@example.com" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password</FormLabel>
                    <FormControl>
                      <Input type="password" placeholder="••••••••" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <Button 
                type="submit" 
                className="w-full bg-pink-400 text-black hover:bg-pink-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold disabled:opacity-50" 
                disabled={isLoading || isGoogleLoading}
              >
                {isLoading ? 'Logging in...' : 'Log in'}
              </Button>
            </form>
          </Form>

          <div className="relative my-4">
            <Separator />
            <span className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 bg-yellow-50 px-2 text-xs text-muted-foreground">
              or
            </span>
          </div>

          <GoogleSignInButton
            onSuccess={googleLogin}
            onError={() => {}}
          />

          <UsernamePromptModal
            open={!!pendingGoogleUser}
            onOpenChange={(open) => !open && cancelGoogleRegistration()}
            googleData={pendingGoogleUser}
            onSubmit={completeGoogleRegistration}
            onUsernameChange={clearGoogleError}
            isLoading={isGoogleLoading}
            error={googleRegistrationError}
          />
        </CardContent>
        <CardFooter className="flex justify-center">
          <p className="text-sm text-muted-foreground">
            Don't have an account?{' '}
            <Link to="/register" className="text-primary hover:underline">
              Sign up
            </Link>
          </p>
        </CardFooter>
      </Card>
    </div>
  )
}
