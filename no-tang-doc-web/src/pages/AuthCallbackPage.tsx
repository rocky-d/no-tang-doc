import { useEffect, useState } from 'react';
import { exchangeAuthorizationCode } from '../utils/authApi';
import { readTemp, clearTemp } from '../utils/pkce';
import { useAuth } from '../components/AuthContext';

// OIDC 授权码回调：校验 state / PKCE，调用后端交换为 access_token & refresh_token，写入前端内存与 localStorage
export function AuthCallbackPage() {
  const [message, setMessage] = useState('Processing authorization...');
  const { completeLogin } = useAuth();

  useEffect(() => {
    (async () => {
      const url = new URL(window.location.href);
      const code = url.searchParams.get('code');
      const returnedState = url.searchParams.get('state');
      const error = url.searchParams.get('error');
      const errorDesc = url.searchParams.get('error_description');

      if (error) {
        setMessage(`Authorization failed: ${error}${errorDesc ? ' - ' + errorDesc : ''}`);
        return;
      }
      if (!code) {
        setMessage('Missing authorization code.');
        return;
      }

      const storedState = readTemp('oidc_state');
      const nonce = readTemp('oidc_nonce') || '';
      const codeVerifier = readTemp('pkce_verifier') || '';
      if (!storedState || storedState !== returnedState) {
        setMessage('State mismatch. Potential CSRF detected.');
        clearTemp(['pkce_verifier','oidc_state','oidc_nonce']);
        return;
      }
      if (!codeVerifier) {
        setMessage('Missing PKCE verifier; cannot complete authorization.');
        return;
      }

      setMessage('Exchanging authorization code...');
      const redirectUri = window.location.origin + '/auth/callback';
      const result = await exchangeAuthorizationCode({ code, codeVerifier, redirectUri, nonce });
      if (!result.success || !result.tokens) {
        setMessage('Code exchange failed: ' + (result.error || 'unknown_error'));
        clearTemp(['pkce_verifier','oidc_state','oidc_nonce']);
        return;
      }

      // 清除一次性数据
      clearTemp(['pkce_verifier','oidc_state','oidc_nonce']);

      setMessage('Finalizing session...');
      console.log('OIDC tokens:', result.tokens); // 打印获取到的token
      completeLogin(result.tokens);

      const redirect = readTemp('post_login_redirect') || '/dashboard';
      clearTemp(['post_login_redirect']);
      window.location.replace(redirect);
    })();
  }, [completeLogin]);

  return <div style={{ padding: '2rem', textAlign: 'center' }}>{message}</div>;
}
