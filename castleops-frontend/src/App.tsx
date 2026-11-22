import { useEffect, useState, type FormEvent } from 'react'

type CurrentUser = {
  id: string
  username: string
}

type SessionState =
  | { status: 'loading' }
  | { status: 'guest' }
  | { status: 'authenticated'; user: CurrentUser }
  | { status: 'error' }

async function loadSession(signal?: AbortSignal): Promise<SessionState> {
  const response = await fetch('/api/auth/me', { signal })

  if (response.status === 401) {
    return { status: 'guest' }
  }

  if (!response.ok) {
    throw new Error('Unable to check your session. Please try again.')
  }

  const user: CurrentUser = await response.json()
  return { status: 'authenticated', user }
}

async function getCsrfHeaders(): Promise<Record<string, string>> {
  const response = await fetch('/api/auth/csrf')

  if (!response.ok) {
    throw new Error('Unable to verify your request. Please try again.')
  }

  const { headerName, token } = await response.json()
  return { [headerName]: token }
}

async function login(username: string, password: string): Promise<void> {
 const headers = await getCsrfHeaders()

 const response = await fetch('/api/auth/login', {
   method: 'POST',
   headers,
   body: new URLSearchParams({ username, password }),
 })

  if (response.status === 401) {
    throw new Error('Incorrect username or password.')
  }

  if (response.status !== 204) {
    throw new Error('Unable to sign in. Please try again.')
  }
}

export default function App() {
  const [session, setSession] = useState<SessionState>({ status: 'loading' })
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [loginError, setLoginError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [logoutError, setLogoutError] = useState('')

  useEffect(() => {
    const controller = new AbortController()

    async function checkSession() {
      try {
        const currentSession = await loadSession(controller.signal)

        if (!controller.signal.aborted) {
          setSession(currentSession)
        }
      } catch {
        if (!controller.signal.aborted) {
          setSession({ status: 'error' })
        }
      }
    }

    void checkSession()
    return () => controller.abort()
  }, [])

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (isSubmitting) return

    setLoginError('')
    setIsSubmitting(true)

    try {
      await login(username, password)
      setPassword('')

      const currentSession = await loadSession()
      setSession(currentSession)

      if (currentSession.status === 'guest') {
        setLoginError('Your session is inactive. Please sign in again.')
      }
    } catch (error) {
      if (error instanceof TypeError) {
        setLoginError('Unable to connect to CastleOps.')
      } else if (error instanceof Error) {
        setLoginError(error.message)
      } else {
        setLoginError('Unable to sign in. Please try again.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

async function handleLogout() {
  if (isSubmitting) return

  setIsSubmitting(true)
  setLogoutError('')

  try {
    const headers = await getCsrfHeaders()
    const response = await fetch('/api/auth/logout', {
      method: 'POST',
      headers,
    })

    if (response.status !== 204) {
      throw new Error('Sign-out failed')
    }

    setUsername('')
    setPassword('')
    setLoginError('')
    setSession({ status: 'guest' })
  } catch {
    setLogoutError('Unable to sign out. Please try again.')
  } finally {
    setIsSubmitting(false)
  }
}

  return (
    <main>
      <h1>CastleOps</h1>

      {session.status === 'loading' && <p>Checking your session…</p>}

      {session.status === 'guest' && (
        <>
          <p>Browsing as a guest.</p>

          <form onSubmit={handleLogin}>
            <div>
              <label htmlFor="username">Username</label>
              <input
                id="username"
                name="username"
                autoComplete="username"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
                required
              />
            </div>

            <div>
              <label htmlFor="password">Password</label>
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                required
              />
            </div>

            {loginError && <p role="alert">{loginError}</p>}

            <button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Signing in…' : 'Sign in'}
            </button>
          </form>
        </>
      )}

      {session.status === 'authenticated' && (
        <>
          <p>Signed in as {session.user.username}.</p>

          <button type="button" onClick={handleLogout} disabled={isSubmitting}>
            {isSubmitting ? 'Signing out…' : 'Sign out'}
          </button>

          {logoutError && <p role="alert">{logoutError}</p>}
        </>
      )}

      {session.status === 'error' && <p>Unable to connect to CastleOps.</p>}
    </main>
  )
}