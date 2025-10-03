import { useEffect, useCallback } from 'react';
import { useAuth } from '../components/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';
import { Button } from '../components/ui/button';

export function LoginPage() {
    const { isAuthenticated, login, isLoading } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const SUPPRESSION_MS = 4000; // 刚登出后抑制自动重新登录的时间窗口

    const getRedirect = useCallback(() => {
        const sp = new URLSearchParams(location.search);
        return sp.get('redirect') || '/dashboard';
    }, [location.search]);

    // 判断是否刚刚登出
    const recentLogoutTs = Number(sessionStorage.getItem('recent_logout') || '0');
    const age = recentLogoutTs ? Date.now() - recentLogoutTs : 0;
    const justLoggedOut = !!recentLogoutTs && age < SUPPRESSION_MS;

    // 初始自动逻辑：如果已登录 -> 重定向；未登录且不是刚登出 -> 触发 OIDC 登录
    useEffect(() => {
        if (isLoading) return;
        const redirect = getRedirect();
        if (isAuthenticated) {
            navigate(redirect, { replace: true });
            return;
        }
        if (justLoggedOut) return; // 抑制期内不自动登录
        login(redirect);
    }, [isAuthenticated, isLoading, navigate, login, getRedirect, justLoggedOut]);

    // 在抑制窗口结束后自动尝试一次登录
    useEffect(() => {
        if (isLoading) return;
        if (!justLoggedOut) return;
        const remaining = SUPPRESSION_MS - age;
        const t = window.setTimeout(() => {
            // 再次确认还未登录 & 标志仍存在
            if (!isAuthenticated && sessionStorage.getItem('recent_logout')) {
                sessionStorage.removeItem('recent_logout');
                login(getRedirect());
            }
        }, remaining > 0 ? remaining : 0);
        return () => window.clearTimeout(t);
    }, [justLoggedOut, age, isAuthenticated, isLoading, login, getRedirect]);

    const handleLoginNow = () => {
        sessionStorage.removeItem('recent_logout');
        login(getRedirect());
    };

    if (isLoading) {
        return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading...</div>;
    }

    if (isAuthenticated) {
        return <div style={{ padding: '2rem', textAlign: 'center' }}>Redirecting...</div>;
    }

    return (
        <div style={{ padding: '2rem', textAlign: 'center' }}>
            {justLoggedOut ? (
                <div>
                    <p style={{ marginBottom: '1rem' }}>You have logged out.</p>
                    <Button onClick={handleLoginNow} size="sm">Sign In again</Button>
                </div>
            ) : (
                <p>Redirecting to authorization server (login)...</p>
            )}
        </div>
    );
}