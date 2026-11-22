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

async function registerAccount(
  username: string,
  password: string,
): Promise<CurrentUser> {
  const headers = await getCsrfHeaders()

  const response = await fetch('/api/auth/register', {
    method: 'POST',
    headers: {
      ...headers,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ username, password }),
  })

  if (response.status === 201) {
    return response.json()
  }

  if (response.status === 400 || response.status === 409) {
    const problem = await response.json()
    throw new Error(problem.detail ?? 'Please check your registration details.')
  }

  throw new Error('Unable to create your account. Please try again.')
}

export default function App() {
  const [session, setSession] = useState<SessionState>({ status: 'loading' })
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [formError, setFormError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [logoutError, setLogoutError] = useState('')
  const [isRegistering, setIsRegistering] = useState(false)
  const [accountNotice, setAccountNotice] = useState('')

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

  function switchForm() {
    setIsRegistering(!isRegistering)
    setPassword('')
    setFormError('')
    setAccountNotice('')
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (isSubmitting) return

    setIsSubmitting(true)
    setFormError('')
    setAccountNotice('')

    try {
      if (isRegistering) {
        const account = await registerAccount(username, password)
        setUsername(account.username)
        setPassword('')
        setIsRegistering(false)
        setAccountNotice('Account created. Sign in with your password.')
        return
      }

      await login(username, password)
      setPassword('')

      const currentSession = await loadSession()
      setSession(currentSession)

      if (currentSession.status === 'guest') {
        setFormError('Your session is inactive. Please sign in again.')
      }
    } catch (error) {
      if (error instanceof TypeError) {
        setFormError('Unable to connect to CastleOps.')
      } else if (error instanceof Error) {
        setFormError(error.message)
      } else {
        setFormError('Something went wrong. Please try again.')
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
    setFormError('')
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
          <h2>{isRegistering ? 'Create account' : 'Sign in'}</h2>

          {accountNotice && <p role="status">{accountNotice}</p>}

          <form onSubmit={handleSubmit}>
            <fieldset disabled={isSubmitting}>
              <legend>Account details</legend>

              <div>
                <label htmlFor="username">Username</label>
                <input
                  id="username"
                  name="username"
                  autoComplete="username"
                  value={username}
                  onChange={(event) => setUsername(event.target.value)}
                  aria-describedby={isRegistering ? 'username-help' : undefined}
                  required
                />
                {isRegistering && (
                  <p id="username-help">
                    Use 3–32 letters, digits, or underscores.
                  </p>
                )}
              </div>

              <div>
                <label htmlFor="password">Password</label>
                <input
                  id="password"
                  name="password"
                  type="password"
                  autoComplete={isRegistering ? 'new-password' : 'current-password'}
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  aria-describedby={isRegistering ? 'password-help' : undefined}
                  required
                />
                {isRegistering && (
                  <p id="password-help">
                    Use at least 12 characters and at most 72 UTF-8 bytes.
                  </p>
                )}
              </div>

              {formError && <p role="alert">{formError}</p>}

              <button type="submit">
                {isRegistering
                  ? (isSubmitting ? 'Creating account…' : 'Create account')
                  : (isSubmitting ? 'Signing in…' : 'Sign in')}
              </button>
            </fieldset>
          </form>

          <button type="button" onClick={switchForm} disabled={isSubmitting}>
            {isRegistering ? 'Back to sign in' : 'Create an account'}
          </button>
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