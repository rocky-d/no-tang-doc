import { useEffect, useState, useRef } from 'react';
import { exchangeAuthorizationCode } from '../utils/authApi';
import { readTemp, clearTemp } from '../utils/pkce';
import { useAuth } from '../components/AuthContext';

// OIDC 授权码回调：校验 state / PKCE，调用后端交换为 access_token & refresh_token，写入前端内存与 localStorage
export function AuthCallbackPage() {
  const [message, setMessage] = useState('Processing authorization...');
  const { completeLogin } = useAuth();
  const ranRef = useRef(false); // 防止在 React.StrictMode 下重复执行

  useEffect(() => {
    if (ranRef.current) return; // 严格模式下第二次触发直接退出
    ranRef.current = true;
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

      // 如果已经基于同一个 code 完成过交换，直接跳过（应对浏览器/重复挂载）
      if (sessionStorage.getItem('code_exchanged') === code) {
        console.log('[auth][callback] code already exchanged, skipping duplicate run');
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

      // 标记该 code 已使用，防止 StrictMode 再次调用
      sessionStorage.setItem('code_exchanged', code);

      clearTemp(['pkce_verifier','oidc_state','oidc_nonce']);
      setMessage('Finalizing session...');
      console.log('OIDC tokens:', result.tokens);
      completeLogin(result.tokens);

      const redirect = readTemp('post_login_redirect') || '/dashboard';
      clearTemp(['post_login_redirect']);
      window.location.replace(redirect);
    })();
  }, [completeLogin]);

  return <div style={{ padding: '2rem', textAlign: 'center' }}>{message}</div>;
}
