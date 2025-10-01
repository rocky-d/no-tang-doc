
import { Link } from 'react-router-dom';
export default function NotFoundPage() {
    return (
        <div style={{ padding: 24 }}>
            <h2 style={{ marginTop: 0 }}>404 Not Found</h2>
            <p>页面不存在。</p>
            <Link to="/">返回主页</Link>
        </div>
    );
}