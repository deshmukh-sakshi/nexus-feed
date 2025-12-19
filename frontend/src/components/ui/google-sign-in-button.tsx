import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google'

interface GoogleSignInButtonProps {
  onSuccess: (idToken: string) => void
  onError?: () => void
}

export function GoogleSignInButton({ onSuccess, onError }: GoogleSignInButtonProps) {
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || ''

  if (!clientId) {
    return null
  }

  return (
    <GoogleOAuthProvider clientId={clientId}>
      <div className="w-full flex justify-center">
        <GoogleLogin
          onSuccess={(response) => {
            if (response.credential) {
              onSuccess(response.credential)
            }
          }}
          onError={onError}
          useOneTap={false}
          theme="outline"
          size="large"
          width="100%"
          text="continue_with"
          shape="rectangular"
        />
      </div>
    </GoogleOAuthProvider>
  )
}
